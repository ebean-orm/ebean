package io.ebeaninternal.server.core;

import io.ebean.ValuePair;
import io.ebean.annotation.DocStoreMode;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PreGetterCallback;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistListener;
import io.ebean.event.BeanPersistRequest;
import io.ebean.event.changelog.BeanChange;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanManager;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.persist.BatchedSqlException;
import io.ebeaninternal.server.persist.DeleteMode;
import io.ebeaninternal.server.persist.Flags;
import io.ebeaninternal.server.persist.PersistExecute;
import io.ebeaninternal.server.transaction.BeanPersistIdMap;
import io.ebeanservice.docstore.api.DocStoreUpdate;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.DocStoreUpdates;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PersistRequest for insert update or delete of a bean.
 */
public final class PersistRequestBean<T> extends PersistRequest implements BeanPersistRequest<T>, DocStoreUpdate, PreGetterCallback, SpiProfileTransactionEvent {

  private final BeanManager<T> beanManager;

  private final BeanDescriptor<T> beanDescriptor;

  private final BeanPersistListener beanPersistListener;

  /**
   * For per post insert update delete control.
   */
  private final BeanPersistController controller;

  /**
   * The bean being persisted.
   */
  private final T bean;

  private final EntityBean entityBean;

  /**
   * The associated intercept.
   */
  private final EntityBeanIntercept intercept;

  /**
   * The parent bean for unidirectional save.
   */
  private final Object parentBean;

  private final boolean dirty;

  private final boolean publish;

  private int flags;

  private DocStoreMode docStoreMode;

  private ConcurrencyMode concurrencyMode;

  /**
   * The unique id used for logging summary.
   */
  private Object idValue;

  /**
   * Hash value used to handle cascade delete both ways in a relationship.
   */
  private Integer beanHash;

  /**
   * Flag set if this is a stateless update.
   */
  private boolean statelessUpdate;

  private boolean notifyCache;

  private boolean deleteMissingChildren;

  /**
   * Flag used to detect when only many properties where updated via a cascade. Used to ensure
   * appropriate caches are updated in that case.
   */
  private boolean updatedManysOnly;

  /**
   * Element collection change as part of bean cache.
   */
  private Map<String, Object> collectionChanges;

  /**
   * Many properties that were cascade saved (and hence might need caches updated later).
   */
  private List<BeanPropertyAssocMany<?>> updatedManys;

  /**
   * Need to get and store the updated properties because the persist listener is notified
   * later on a different thread and the bean has been reset at that point.
   */
  private Set<String> updatedProperties;

  /**
   * Flags indicating the dirty properties on the bean.
   */
  private boolean[] dirtyProperties;

  /**
   * Flag set when request is added to JDBC batch.
   */
  private boolean batched;

  /**
   * Flag set when batchOnCascade to avoid using batch on the top bean.
   */
  private boolean skipBatchForTopLevel;

  /**
   * Flag set when batch mode is turned on for a persist cascade.
   */
  private boolean batchOnCascadeSet;

  /**
   * Set for updates to determine if all loaded properties are included in the update.
   */
  private boolean requestUpdateAllLoadedProps;

  private long version;

  private long now;

  private long profileOffset;

  /**
   * Flag set when request is added to JDBC batch registered as a "getter callback" to automatically flush batch.
   */
  private boolean getterCallback;

  /**
   * postUpdate notifications. Used to combine bean and element update updates into single postUpdate event.
   */
  private int pendingPostUpdateNotify;

  public PersistRequestBean(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr, SpiTransaction t,
                            PersistExecute persistExecute, PersistRequest.Type type, int flags) {

    super(server, t, persistExecute);
    this.entityBean = (EntityBean) bean;
    this.intercept = entityBean._ebean_getIntercept();
    this.beanManager = mgr;
    this.beanDescriptor = mgr.getBeanDescriptor();
    this.beanPersistListener = beanDescriptor.getPersistListener();
    this.bean = bean;
    this.parentBean = parentBean;
    this.controller = beanDescriptor.getPersistController();
    this.type = type;
    this.docStoreMode = calcDocStoreMode(transaction, type);
    this.flags = flags;
    if (Flags.isRecurse(flags)) {
      this.persistCascade = t.isPersistCascade();
    }

    if (this.type == Type.UPDATE) {
      if (intercept.isNew()) {
        // 'stateless update' - set loaded properties as dirty
        intercept.setNewBeanForUpdate();
        statelessUpdate = true;
      }
      // Mark Mutable scalar properties (like Hstore) as dirty where necessary
      beanDescriptor.checkMutableProperties(intercept);
    }
    this.concurrencyMode = beanDescriptor.getConcurrencyMode(intercept);
    this.publish = Flags.isPublish(flags);
    if (isMarkDraftDirty(publish)) {
      beanDescriptor.setDraftDirty(entityBean, true);
    }
    this.dirty = intercept.isDirty();
    initGeneratedProperties();
  }

  /**
   * Add to profile as batched bean insert, update or delete.
   */
  @Override
  public void profile(long offset, int flushCount) {
    profileBase(type.profileEventId, offset, beanDescriptor.getProfileId(), flushCount);
  }

  /**
   * Return the document store event that should be used for this request.
   * <p>
   * Used to check if the Transaction has set the mode to IGNORE when doing large batch inserts that we
   * don't want to send to the doc store.
   */
  private DocStoreMode calcDocStoreMode(SpiTransaction txn, Type type) {
    DocStoreMode txnMode = (txn == null) ? null : txn.getDocStoreMode();
    return beanDescriptor.getDocStoreMode(type, txnMode);
  }

  /**
   * Return true if the draftDirty property should be set to true for this request.
   */
  private boolean isMarkDraftDirty(boolean publish) {
    return !publish && type != Type.DELETE && beanDescriptor.isDraftable();
  }

  /**
   * Set the transaction from prior persist request.
   * Only used when hard deleting draft & associated live beans.
   */
  public void setTrans(SpiTransaction transaction) {
    this.transaction = transaction;
    this.createdTransaction = false;
    this.persistCascade = transaction.isPersistCascade();
  }

  /**
   * Init the transaction and also check for batch on cascade escalation.
   */
  public void initTransIfRequiredWithBatchCascade() {

    if (createImplicitTransIfRequired()) {
      docStoreMode = calcDocStoreMode(transaction, type);
    }
    checkBatchEscalationOnCascade();
  }

  /**
   * Check for batch escalation on cascade.
   */
  public void checkBatchEscalationOnCascade() {
    if (transaction.checkBatchEscalationOnCascade(this)) {
      // we escalated to use batch mode so flush when done
      // but if createdTransaction then commit will flush it
      batchOnCascadeSet = !createdTransaction;
    }
    persistCascade = transaction.isPersistCascade();
  }

  private void initGeneratedProperties() {
    switch (type) {
      case INSERT:
        onInsertGeneratedProperties();
        break;
      case UPDATE:
        if (!beanDescriptor.isReference(intercept) && (dirty || statelessUpdate)) {
          onUpdateGeneratedProperties();
        }
        break;
      case DELETE_SOFT:
        onUpdateGeneratedProperties();
        break;
    }
  }

  private void onUpdateGeneratedProperties() {

    for (BeanProperty prop : beanDescriptor.propertiesGenUpdate()) {

      GeneratedProperty generatedProperty = prop.getGeneratedProperty();
      if (prop.isVersion()) {
        if (isLoadedProperty(prop)) {
          // @Version property must be loaded to be involved
          Object value = generatedProperty.getUpdateValue(prop, entityBean, now());
          Object oldVal = prop.getValue(entityBean);
          setVersionValue(value);
          intercept.setOldValue(prop.getPropertyIndex(), oldVal);
        }
      } else {
        // @WhenModified set without invoking interception
        Object oldVal = prop.getValue(entityBean);
        Object value = generatedProperty.getUpdateValue(prop, entityBean, now());
        prop.setValueChanged(entityBean, value);
        intercept.setOldValue(prop.getPropertyIndex(), oldVal);
      }
    }
  }

  private void onInsertGeneratedProperties() {
    for (BeanProperty prop : beanDescriptor.propertiesGenInsert()) {
      Object value = prop.getGeneratedProperty().getInsertValue(prop, entityBean, now());
      prop.setValueChanged(entityBean, value);
    }
  }

  /**
   * If using batch on cascade flush if required.
   */
  public void flushBatchOnCascade() {
    if (batchOnCascadeSet) {
      // we escalated to batch mode for request so flush
      transaction.flushBatchOnCascade();
      batchOnCascadeSet = false;
    }
  }

  @Override
  public void rollbackTransIfRequired() {
    if (batchOnCascadeSet) {
      transaction.flushBatchOnRollback();
      batchOnCascadeSet = false;
    }
    super.rollbackTransIfRequired();
  }

  /**
   * Return true is this request was added to the JDBC batch.
   */
  public boolean isBatched() {
    return batched;
  }

  /**
   * Set when request is added to the JDBC batch.
   */
  public void setBatched() {
    batched = true;
    if (type == Type.INSERT || type == Type.UPDATE) {
      // used to trigger automatic jdbc batch flush
      intercept.registerGetterCallback(this);
      getterCallback = true;
    }
  }

  @Override
  public void preGetterTrigger(int propertyIndex) {
    if (flushBatchOnGetter(propertyIndex)) {
      transaction.flushBatch();
    }
  }

  private boolean flushBatchOnGetter(int propertyIndex) {
    // propertyIndex of -1 the Id property, no flush for get Id on UPDATE
    if (propertyIndex == -1) {
      if (beanDescriptor.isIdLoaded(intercept)) {
        return false;
      } else {
        return type == Type.INSERT;
      }
    } else {
      return beanDescriptor.isGeneratedProperty(propertyIndex);
    }
  }

  public void setSkipBatchForTopLevel() {
    skipBatchForTopLevel = true;
  }

  @Override
  public boolean isBatchThisRequest() {
    return !skipBatchForTopLevel && super.isBatchThisRequest();
  }

  /**
   * Return true if this is an insert request.
   */
  public boolean isInsert() {
    return Type.INSERT == type;
  }

  @Override
  public Set<String> getLoadedProperties() {
    return intercept.getLoadedPropertyNames();
  }

  @Override
  public Set<String> getUpdatedProperties() {
    return intercept.getDirtyPropertyNames();
  }

  /**
   * Return the dirty properties on this request.
   */
  public boolean[] getDirtyProperties() {
    return dirtyProperties;
  }

  /**
   * Return true if any of the given property names are dirty.
   */
  @Override
  public boolean hasDirtyProperty(Set<String> propertyNames) {
    return intercept.hasDirtyProperty(propertyNames);
  }

  /**
   * Return true if any of the given properties are dirty.
   */
  public boolean hasDirtyProperty(int[] propertyPositions) {

    for (int propertyPosition : propertyPositions) {
      if (dirtyProperties[propertyPosition]) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Map<String, ValuePair> getUpdatedValues() {
    return intercept.getDirtyValues();
  }

  /**
   * Set the cache notify status.
   */
  private void setNotifyCache() {
    this.notifyCache = beanDescriptor.isCacheNotify(type, publish);
  }

  /**
   * Return true if this change should notify cache, listener or doc store.
   */
  public boolean isNotify() {
    return notifyCache || isNotifyPersistListener() || isDocStoreNotify();
  }

  /**
   * Return true if this request should update the document store.
   */
  private boolean isDocStoreNotify() {
    return docStoreMode != DocStoreMode.IGNORE;
  }

  private boolean isNotifyPersistListener() {
    return beanPersistListener != null;
  }

  /**
   * Collect L2 cache changes to be applied after the transaction has successfully committed.
   */
  public void notifyCache(CacheChangeSet changeSet) {
    if (notifyCache) {
      switch (type) {
        case INSERT:
          beanDescriptor.cachePersistInsert(this, changeSet);
          break;
        case UPDATE:
          beanDescriptor.cachePersistUpdate(idValue, this, changeSet);
          break;
        case DELETE:
        case DELETE_SOFT:
          beanDescriptor.cachePersistDelete(idValue, this, changeSet);
          break;
        default:
          throw new IllegalStateException("Invalid type " + type);
      }
    }
  }

  /**
   * Process the persist request updating the document store.
   */
  @Override
  public void docStoreUpdate(DocStoreUpdateContext txn) throws IOException {

    switch (type) {
      case INSERT:
        beanDescriptor.docStoreInsert(idValue, this, txn);
        break;
      case UPDATE:
      case DELETE_SOFT:
        beanDescriptor.docStoreUpdate(idValue, this, txn);
        break;
      case DELETE:
        beanDescriptor.docStoreDeleteById(idValue, txn);
        break;
      default:
        throw new IllegalStateException("Invalid type " + type);
    }
  }

  /**
   * Add this event to the queue entries in IndexUpdates.
   */
  @Override
  public void addToQueue(DocStoreUpdates docStoreUpdates) {
    switch (type) {
      case INSERT:
        docStoreUpdates.queueIndex(beanDescriptor.getDocStoreQueueId(), idValue);
        break;
      case UPDATE:
      case DELETE_SOFT:
        docStoreUpdates.queueIndex(beanDescriptor.getDocStoreQueueId(), idValue);
        break;
      case DELETE:
        docStoreUpdates.queueDelete(beanDescriptor.getDocStoreQueueId(), idValue);
        break;
      default:
        throw new IllegalStateException("Invalid type " + type);
    }
  }

  public void addToPersistMap(BeanPersistIdMap beanPersistMap) {

    beanPersistMap.add(beanDescriptor, type, idValue);
  }

  public void notifyLocalPersistListener() {
    if (beanPersistListener != null) {
      switch (type) {
        case INSERT:
          beanPersistListener.inserted(bean);
          break;

        case UPDATE:
          beanPersistListener.updated(bean, updatedProperties);
          break;

        case DELETE:
          beanPersistListener.deleted(bean);
          break;

        case DELETE_SOFT:
          beanPersistListener.softDeleted(bean);
          break;

        default:
      }
    }
  }

  public boolean isParent(Object o) {
    return o == parentBean;
  }

  /**
   * Return true if this bean has been already been persisted (inserted or updated) in this
   * transaction.
   */
  public boolean isRegisteredBean() {
    return transaction.isRegisteredBean(bean);
  }

  public void unRegisterBean() {
    transaction.unregisterBean(bean);
  }

  /**
   * The hash used to register the bean with the transaction.
   * <p>
   * Takes into account the class type and id value.
   * </p>
   */
  private Integer getBeanHash() {
    if (beanHash == null) {
      Object id = beanDescriptor.getId(entityBean);
      int hc = 92821 * bean.getClass().getName().hashCode();
      if (id != null) {
        hc += id.hashCode();
      }
      beanHash = hc;
    }
    return beanHash;
  }

  public void registerDeleteBean() {
    Integer hash = getBeanHash();
    transaction.registerDeleteBean(hash);
  }

  public void unregisterDeleteBean() {
    Integer hash = getBeanHash();
    transaction.unregisterDeleteBean(hash);
  }

  public boolean isRegisteredForDeleteBean() {
    if (transaction == null) {
      return false;
    } else {
      Integer hash = getBeanHash();
      return transaction.isRegisteredDeleteBean(hash);
    }
  }

  /**
   * Return the BeanDescriptor for the associated bean.
   */
  public BeanDescriptor<T> getBeanDescriptor() {
    return beanDescriptor;
  }

  /**
   * Return true if a stateless update should also delete any missing details beans.
   */
  public boolean isDeleteMissingChildren() {
    return deleteMissingChildren;
  }

  /**
   * Set if deleteMissingChildren occurs on cascade save to OneToMany or ManyToMany.
   */
  public void setDeleteMissingChildren(boolean deleteMissingChildren) {
    this.deleteMissingChildren = deleteMissingChildren;
  }

  /**
   * Prepare the update after potential modifications in a BeanPersistController.
   */
  private void postControllerPrepareUpdate() {
    if (statelessUpdate && controller != null) {
      // 'stateless update' - set dirty properties modified in controller preUpdate
      intercept.setNewBeanForUpdate();
    }
  }

  /**
   * Used to skip updates if we know the bean is not dirty. This is the case for EntityBeans that
   * have not been modified.
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * Return the concurrency mode used for this persist.
   */
  public ConcurrencyMode getConcurrencyMode() {
    return concurrencyMode;
  }

  /**
   * Returns a description of the request. This is typically the bean class name or the base table
   * for MapBeans.
   * <p>
   * Used to determine common persist requests for queueing and statement batching.
   * </p>
   */
  public String getFullName() {
    return beanDescriptor.getFullName();
  }

  /**
   * Return the bean associated with this request.
   */
  @Override
  public T getBean() {
    return bean;
  }

  public EntityBean getEntityBean() {
    return entityBean;
  }

  /**
   * Return the Id value for the bean.
   */
  public Object getBeanId() {
    return beanDescriptor.getId(entityBean);
  }

  /**
   * Create and return a new reference bean matching this beans Id value.
   */
  public T createReference() {
    return beanDescriptor.createReference(getBeanId(), null);
  }

  /**
   * Return true if the bean type is a Draftable.
   */
  public boolean isDraftable() {
    return beanDescriptor.isDraftable();
  }

  /**
   * Return true if this request is a hard delete of a draftable bean.
   * If this is true Ebean is expected to auto-publish and delete the associated live bean.
   */
  public boolean isHardDeleteDraft() {
    if (type == Type.DELETE && beanDescriptor.isDraftable() && !beanDescriptor.isDraftableElement()) {
      // deleting a top level draftable bean
      if (beanDescriptor.isLiveInstance(entityBean)) {
        throw new PersistenceException("Explicit Delete is not allowed on a 'live' bean - only draft beans");
      }
      return true;
    }
    return false;
  }

  /**
   * Return true if this was a hard/permanent delete request (and should cascade as such).
   */
  public boolean isHardDeleteCascade() {
    return (type == Type.DELETE && beanDescriptor.isSoftDelete());
  }

  /**
   * Checks for @Draftable entity beans with @Draft property that the bean is a 'draft'.
   * Save or Update is not allowed to execute using 'live' beans - must use publish().
   */
  public void checkDraft() {
    if (beanDescriptor.isDraftable() && beanDescriptor.isLiveInstance(entityBean)) {
      throw new PersistenceException("Save or update is not allowed on a 'live' bean - only draft beans");
    }
  }

  /**
   * Return the parent bean for cascading save with unidirectional relationship.
   */
  public Object getParentBean() {
    return parentBean;
  }

  /**
   * Return the controller if there is one associated with this type of bean. This returns null if
   * there is no controller associated.
   */
  public BeanPersistController getBeanController() {
    return controller;
  }

  /**
   * Return the intercept if there is one.
   */
  public EntityBeanIntercept getEntityBeanIntercept() {
    return intercept;
  }

  /**
   * Return true if this property is loaded (full bean or included in partial bean).
   */
  public boolean isLoadedProperty(BeanProperty prop) {
    return intercept.isLoadedProperty(prop.getPropertyIndex());
  }

  /**
   * Return true if the property is dirty.
   */
  public boolean isDirtyProperty(BeanProperty prop) {
    return intercept.isDirtyProperty(prop.getPropertyIndex());
  }

  /**
   * Return the original / old value for the given property.
   */
  public Object getOrigValue(BeanProperty prop) {
    return intercept.getOrigValue(prop.getPropertyIndex());
  }

  @Override
  public int executeNow() {
    if (getterCallback) {
      intercept.clearGetterCallback();
    }
    switch (type) {
      case INSERT:
        executeInsert();
        return -1;

      case UPDATE:
        if (beanPersistListener != null) {
          // store the updated properties for sending later
          updatedProperties = getUpdatedProperties();
        }
        executeUpdate();
        return -1;

      case DELETE_SOFT:
        prepareForSoftDelete();
        executeSoftDelete();
        return -1;

      case DELETE:
        return executeDelete();

      default:
        throw new RuntimeException("Invalid type " + type);
    }
  }

  /**
   * Soft delete is executed as update so we want to set deleted=true property.
   */
  private void prepareForSoftDelete() {

    beanDescriptor.setSoftDeleteValue(entityBean);
  }

  @Override
  public int executeOrQueue() {

    boolean batch = isBatchThisRequest();
    try {
      BatchControl control = transaction.getBatchControl();
      if (control != null) {
        return control.executeOrQueue(this, batch);
      }
      if (batch) {
        control = persistExecute.createBatchControl(transaction);
        return control.executeOrQueue(this, true);

      } else {
        return executeNoBatch();
      }
    } catch (BatchedSqlException e) {
      throw transaction.translate(e.getMessage(), e.getCause());
    }
  }

  private int executeNoBatch() {
    profileOffset = transaction.profileOffset();
    int result = executeNow();
    transaction.profileEvent(this);
    return result;
  }

  /**
   * Set the generated key back to the bean. Only used for inserts with getGeneratedKeys.
   */
  @Override
  public void setGeneratedKey(Object idValue) {
    if (idValue != null) {
      // remember it for logging summary
      this.idValue = beanDescriptor.convertSetId(idValue, entityBean);
    }
  }

  /**
   * Set the Id value that was bound. Used for the purposes of logging summary information on this
   * request.
   */
  public void setBoundId(Object idValue) {
    this.idValue = idValue;
  }

  /**
   * Check for optimistic concurrency exception.
   */
  @Override
  public final void checkRowCount(int rowCount) {
    if (rowCount != 1 && rowCount != Statement.SUCCESS_NO_INFO) {
      if (ConcurrencyMode.VERSION == concurrencyMode) {
        throw new OptimisticLockException(Message.msg("persist.conc2", String.valueOf(rowCount)), null, bean);
      } else if (rowCount == 0 && type == Type.UPDATE) {
        throw new EntityNotFoundException("No rows updated");
      }
    }
    switch (type) {
      case DELETE:
      case DELETE_SOFT:
        postDelete();
        break;
      case UPDATE:
        postUpdate();
        break;
      default: // do nothing
    }
  }

  /**
   * Clear the bean from the PersistenceContext (L1 cache) for stateless updates.
   */
  private void postUpdate() {
    if (statelessUpdate) {
      beanDescriptor.contextClear(transaction.getPersistenceContext(), idValue);
    }
  }

  private void postUpdateNotify() {
    if (pendingPostUpdateNotify > 0) {
      // invoke the delayed postUpdate notification (combined with element collection update)
      controller.postUpdate(this);
    } else {
      // batched update with no element collection, send postUpdate notification once it executes
      pendingPostUpdateNotify = -1;
    }
  }

  /**
   * Aggressive L1 and L2 cache cleanup for deletes.
   */
  private void postDelete() {
    beanDescriptor.contextClear(transaction.getPersistenceContext(), idValue);
  }

  private void changeLog() {
    BeanChange changeLogBean = beanDescriptor.getChangeLogBean(this);
    if (changeLogBean != null) {
      transaction.addBeanChange(changeLogBean);
    }
  }

  /**
   * Post processing.
   */
  @Override
  public void postExecute() {

    if (controller != null) {
      controllerPost();
    }
    setNotifyCache();

    boolean isChangeLog = beanDescriptor.isChangeLog();
    if (type == Type.UPDATE && (isChangeLog || notifyCache || docStoreMode == DocStoreMode.UPDATE)) {
      // get the dirty properties for update notification to the doc store
      dirtyProperties = intercept.getDirtyProperties();
    }
    if (isChangeLog) {
      changeLog();
    }

    // if bean persisted again then should result in an update
    intercept.setLoaded();
    if (isInsert()) {
      postInsert();
    }

    addEvent();

    if (isLogSummary()) {
      logSummary();
    }
  }

  /**
   * Ensure the preUpdate event fires (for case where only element collection has changed).
   */
  public void preElementCollectionUpdate() {
    if (controller != null && !dirty) {
      // fire preUpdate notification when only element collection updated
      controller.preUpdate(this);
    }
  }

  /**
   * Combine with the beans postUpdate event notification.
   */
  public boolean postElementCollectionUpdate() {
    if (controller != null) {
      pendingPostUpdateNotify += 2;
    }
    if (!dirty) {
      setNotifyCache();
    }
    return notifyCache;
  }

  private void controllerPost() {
    switch (type) {
      case INSERT:
        controller.postInsert(this);
        break;
      case UPDATE:
        if (pendingPostUpdateNotify == -1) {
          // notify now - batched bean update with no element collection
          controller.postUpdate(this);
        } else {
          // delay notify to combine with element collection update
          pendingPostUpdateNotify++;
        }
        break;
      case DELETE_SOFT:
        controller.postSoftDelete(this);
        break;
      case DELETE:
        controller.postDelete(this);
        break;
      default:
        break;
    }
  }

  private void logSummary() {

    String draft = (beanDescriptor.isDraftable() && !publish) ? " draft[true]" : "";
    String name = beanDescriptor.getName();
    switch (type) {
      case INSERT:
        transaction.logSummary("Inserted [" + name + "] [" + idValue + "]" + draft);
        break;
      case UPDATE:
        transaction.logSummary("Updated [" + name + "] [" + idValue + "]" + draft);
        break;
      case DELETE:
        transaction.logSummary("Deleted [" + name + "] [" + idValue + "]" + draft);
        break;
      case DELETE_SOFT:
        transaction.logSummary("SoftDelete [" + name + "] [" + idValue + "]" + draft);
        break;
      default:
        break;
    }
  }

  /**
   * Add the bean to the TransactionEvent. This will be used by TransactionManager to sync Cache,
   * Cluster and text indexes.
   */
  private void addEvent() {

    TransactionEvent event = transaction.getEvent();
    if (event != null) {
      event.add(this);
    }
  }

  /**
   * Return true if the update DML/SQL must be dynamically generated.
   * <p>
   * This is the case for updates/deletes of partially populated beans.
   * </p>
   */
  public boolean isDynamicUpdateSql() {
    return beanDescriptor.isUpdateChangesOnly() || !intercept.isFullyLoadedBean();
  }

  /**
   * Return true if the property should be included in the update.
   */
  public boolean isAddToUpdate(BeanProperty prop) {
    if (requestUpdateAllLoadedProps) {
      return intercept.isLoadedProperty(prop.getPropertyIndex());
    } else {
      return intercept.isDirtyProperty(prop.getPropertyIndex());
    }
  }

  /**
   * Register the derived relationships to get executed later (on JDBC batch flush or commit).
   */
  public void deferredRelationship(EntityBean assocBean, ImportedId importedId, EntityBean bean) {
    transaction.registerDeferred(new PersistDeferredRelationship(ebeanServer, beanDescriptor, assocBean, importedId, bean));
  }

  private void postInsert() {
    // mark all properties as loaded after an insert to support immediate update
    beanDescriptor.setAllLoaded(entityBean);
    if (!publish) {
      beanDescriptor.setDraft(entityBean);
    }
  }

  public boolean isReference() {
    return beanDescriptor.isReference(intercept);
  }

  /**
   * This many property has been cascade saved. Keep note of this and update the 'many property'
   * cache on post commit.
   */
  public void addUpdatedManyProperty(BeanPropertyAssocMany<?> updatedAssocMany) {
    if (updatedManys == null) {
      updatedManys = new ArrayList<>(5);
    }
    updatedManys.add(updatedAssocMany);
  }

  /**
   * Return the list of cascade updated many properties (can be null).
   */
  public List<BeanPropertyAssocMany<?>> getUpdatedManyCollections() {
    return updatedManys;
  }

  /**
   * Check if any of its many properties where cascade saved and hence we need to update related
   * many property caches.
   */
  public void checkUpdatedManysOnly() {
    if (!dirty && updatedManys != null) {
      // set the flag and register for post commit processing if there
      // is caching or registered listeners
      if (idValue == null) {
        this.idValue = beanDescriptor.getId(entityBean);
      }
      updatedManysOnly = true;
      setNotifyCache();
      addEvent();
    }
    postUpdateNotify();
  }

  /**
   * For requests that update document store add this event to either the list
   * of queue events or list of update events.
   */
  public void addDocStoreUpdates(DocStoreUpdates docStoreUpdates) {

    if (type == Type.UPDATE) {
      beanDescriptor.docStoreUpdateEmbedded(this, docStoreUpdates);
    }
    switch (docStoreMode) {
      case UPDATE: {
        docStoreUpdates.addPersist(this);
        return;
      }
      case QUEUE: {
        if (type == Type.DELETE) {
          docStoreUpdates.queueDelete(beanDescriptor.getDocStoreQueueId(), idValue);
        } else {
          docStoreUpdates.queueIndex(beanDescriptor.getDocStoreQueueId(), idValue);
        }
      }
      break;
      default:
        break;
    }
  }

  /**
   * Determine if all loaded properties should be used for an update.
   * <p>
   * Takes into account transaction setting and JDBC batch.
   * </p>
   */
  private boolean determineUpdateAllLoadedProperties() {

    Boolean txnUpdateAll = transaction.isUpdateAllLoadedProperties();
    if (txnUpdateAll != null) {
      // use the setting explicitly set on the transaction
      requestUpdateAllLoadedProps = txnUpdateAll;
    } else {
      // if using batch use the server default setting
      requestUpdateAllLoadedProps = isBatchThisRequest() && ebeanServer.isUpdateAllPropertiesInBatch();
    }

    return requestUpdateAllLoadedProps;
  }

  /**
   * Return the flags set on this persist request.
   */
  public int getFlags() {
    return flags;
  }

  /**
   * Return true if this request is a 'publish' action.
   */
  public boolean isPublish() {
    return publish;
  }

  /**
   * Return the key for an update persist request.
   */
  public String getUpdatePlanHash() {

    StringBuilder key;
    if (determineUpdateAllLoadedProperties()) {
      key = intercept.getLoadedPropertyKey();
    } else {
      key = intercept.getDirtyPropertyKey();
    }

    BeanProperty versionProperty = beanDescriptor.getVersionProperty();
    if (versionProperty != null) {
      if (intercept.isLoadedProperty(versionProperty.getPropertyIndex())) {
        key.append('v');
      }
    }

    if (publish) {
      key.append('p');
    }

    return key.toString();
  }

  /**
   * Return the table to update depending if the request is a 'publish' one or normal.
   */
  public String getUpdateTable() {
    return publish ? beanDescriptor.getBaseTable() : beanDescriptor.getDraftTable();
  }

  /**
   * Return the delete mode - Soft or Hard.
   */
  public DeleteMode deleteMode() {
    return Type.DELETE_SOFT == type ? DeleteMode.SOFT : DeleteMode.HARD;
  }

  /**
   * Set the value of the Version property on the bean.
   */
  private void setVersionValue(Object versionValue) {
    version = beanDescriptor.setVersion(entityBean, versionValue);
  }

  /**
   * Return the version in long form (if set).
   */
  public long getVersion() {
    return version;
  }

  private void setTenantId() {
    Object tenantId = transaction.getTenantId();
    if (tenantId != null) {
      beanDescriptor.setTenantId(entityBean, tenantId);
    }
  }

  private void executeInsert() {
    setTenantId();
    if (controller == null || controller.preInsert(this)) {
      beanManager.getBeanPersister().insert(this);
    }
  }

  private void executeUpdate() {
    setTenantId();
    if (controller == null || controller.preUpdate(this)) {
      postControllerPrepareUpdate();
      beanManager.getBeanPersister().update(this);
    }
  }

  private void executeSoftDelete() {
    setTenantId();
    if (controller == null || controller.preSoftDelete(this)) {
      postControllerPrepareUpdate();
      beanManager.getBeanPersister().update(this);
    }
  }

  private int executeDelete() {
    setTenantId();
    if (controller == null || controller.preDelete(this)) {
      return beanManager.getBeanPersister().delete(this);
    }
    // delete handled by the BeanController so return 0
    return 0;
  }

  /**
   * Persist to the document store now (via buffer, not post commit).
   */
  public void docStorePersist() {
    idValue = beanDescriptor.getId(entityBean);
    switch (type) {
      case UPDATE:
        dirtyProperties = intercept.getDirtyProperties();
        break;
    }
    // processing now so set IGNORE (unlike DB + DocStore processing with post-commit)
    docStoreMode = DocStoreMode.IGNORE;
    try {
      docStoreUpdate(transaction.getDocStoreTransaction().obtain());
      postExecute();
      if (type == Type.UPDATE
        && beanDescriptor.isDocStoreEmbeddedInvalidation()
        && transaction.isPersistCascade()) {
        // queue embedded/nested updates for later processing
        beanDescriptor.docStoreUpdateEmbedded(this, transaction.getDocStoreTransaction().queue());
      }
    } catch (IOException e) {
      throw new PersistenceException("Error persisting doc store bean", e);
    }
  }

  /**
   * Use a common 'now' value across both when created and when updated etc.
   */
  public long now() {
    if (now == 0) {
      now = ebeanServer.clockNow();
    }
    return now;
  }

  /**
   * Return true if this is a stateless update request (in which case it doesn't really have 'old values').
   */
  public boolean isStatelessUpdate() {
    return statelessUpdate;
  }

  /**
   * Add to profile as single bean insert, update or delete (not batched).
   */
  @Override
  public void profile() {
    profileBase(type.profileEventId, profileOffset, beanDescriptor.getProfileId(), 1);
  }

  /**
   * Set the request flags indicating this is an insert.
   */
  public void flagInsert() {
    if (intercept.isNew()) {
      flags = Flags.setInsertNormal(flags);
    } else {
      flags = Flags.setInsert(flags);
    }
  }

  /**
   * Unset the request insert flag indicating this is an update.
   */
  public void flagUpdate() {
    if (intercept.isLoaded()) {
      flags = Flags.setUpdateNormal(flags);
    } else {
      flags = Flags.setUpdate(flags);
    }
  }

  /**
   * Return true if this request is an insert.
   */
  public boolean isInsertedParent() {
    return Flags.isInsert(flags);
  }

  /**
   * Add an element collection change to L2 bean cache update.
   */
  public void addCollectionChange(String name, Object value) {
    if (collectionChanges == null) {
      collectionChanges = new LinkedHashMap<>();
    }
    collectionChanges.put(name, value);
  }

  /**
   * Build the bean update for the L2 cache.
   */
  public void addBeanUpdate(CacheChangeSet changeSet) {

    if (!updatedManysOnly || collectionChanges != null) {

      boolean updateNaturalKey = false;

      Map<String, Object> changes = new LinkedHashMap<>();
      EntityBean bean = getEntityBean();
      boolean[] dirtyProperties = getDirtyProperties();
      if (dirtyProperties != null) {
        for (int i = 0; i < dirtyProperties.length; i++) {
          if (dirtyProperties[i]) {
            BeanProperty property = beanDescriptor.propertyByIndex(i);
            if (property.isCacheDataInclude()) {
              Object val = property.getCacheDataValue(bean);
              changes.put(property.getName(), val);
              if (property.isNaturalKey()) {
                updateNaturalKey = true;
                changeSet.addNaturalKeyPut(beanDescriptor, idValue, val);
              }
            }
          }
        }
      }
      if (collectionChanges != null) {
        // add element collection update
        changes.putAll(collectionChanges);
      }

      changeSet.addBeanUpdate(beanDescriptor, idValue, changes, updateNaturalKey, getVersion());
    }
  }
}
