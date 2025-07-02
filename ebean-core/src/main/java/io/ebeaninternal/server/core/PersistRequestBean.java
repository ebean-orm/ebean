package io.ebeaninternal.server.core;

import io.ebean.InsertOptions;
import io.ebean.ValuePair;
import io.ebean.annotation.DocStoreMode;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PreGetterCallback;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistListener;
import io.ebean.event.BeanPersistRequest;
import io.ebean.event.changelog.BeanChange;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.persist.*;
import io.ebeaninternal.server.transaction.BeanPersistIdMap;
import io.ebeanservice.docstore.api.DocStoreUpdate;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.DocStoreUpdates;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.sql.Statement;
import java.util.*;

/**
 * PersistRequest for insert update or delete of a bean.
 */
public final class PersistRequestBean<T> extends PersistRequest implements BeanPersistRequest<T>, DocStoreUpdate, PreGetterCallback, SpiProfileTransactionEvent {

  private final BeanManager<T> beanManager;
  private final BeanDescriptor<T> beanDescriptor;
  private final BeanPersistListener beanPersistListener;
  private final BeanPersistController controller;
  private final T bean;
  private final EntityBean entityBean;
  private final EntityBeanIntercept intercept;
  /**
   * The parent bean for unidirectional save.
   */
  private final Object parentBean;
  private final boolean dirty;
  private final boolean publish;
  private int flags;
  private boolean saveRecurse;
  private DocStoreMode docStoreMode;
  private final ConcurrencyMode concurrencyMode;
  /**
   * The unique id used for logging summary.
   */
  private Object idValue;
  /**
   * Hash value used to handle cascade delete both ways in a relationship.
   */
  private Integer beanHash;
  private boolean statelessUpdate;
  private boolean notifyCache;
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
   * Set true when the request includes cascade save to a many.
   */
  private boolean updatedMany;
  /**
   * Many properties that were cascade saved (and hence might need caches updated later).
   */
  private List<BeanPropertyAssocMany<?>> updatedManys;
  /**
   * Store the updated properties to notify persist listener.
   */
  private Set<String> updatedProperties;
  /**
   * Flags indicating the dirty properties on the bean.
   */
  private boolean[] dirtyProperties;
  /**
   * Imported OneToOne orphan that needs to be deleted.
   */
  private EntityBean orphanBean;
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
  private boolean pendingPostUpdateNotify;
  /**
   * Set to true when post execute has occurred (so includes batch flush).
   */
  private boolean postExecute;
  /**
   * Set to true after many properties have been persisted (so includes element collections).
   */
  private boolean complete;
  /**
   * Many-to-many intersection table changes that are held for later batch processing.
   */
  private List<SaveMany> saveMany;
  private InsertOptions insertOptions;
  private BeanProperty[] dirtyGenerated;

  public PersistRequestBean(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr, SpiTransaction t,
                            PersistExecute persistExecute, PersistRequest.Type type, int flags) {
    super(server, t, persistExecute);
    this.entityBean = (EntityBean) bean;
    this.intercept = entityBean._ebean_getIntercept();
    this.beanManager = mgr;
    this.beanDescriptor = mgr.getBeanDescriptor();
    this.beanPersistListener = beanDescriptor.persistListener();
    this.bean = bean;
    this.parentBean = parentBean;
    this.controller = beanDescriptor.persistController();
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
      } else if (!intercept.isDirty()) {
        // check if any mutable scalar properties are dirty
        beanDescriptor.checkAnyMutableProperties(intercept);
      }
    }
    this.concurrencyMode = beanDescriptor.concurrencyMode(intercept);
    this.publish = Flags.isPublish(flags);
    if (isMarkDraftDirty(publish)) {
      beanDescriptor.setDraftDirty(entityBean, true);
    }
    this.dirty = intercept.isDirty();
  }

  /**
   * Init generated properties for soft delete (as it's an update).
   */
  public void initForSoftDelete() {
    initGeneratedProperties();
  }

  @Override
  public void addTimingBatch(long startNanos, int batch) {
    beanDescriptor.metricPersistBatch(type, startNanos, batch);
  }

  @Override
  public void addTimingNoBatch(long startNanos) {
    beanDescriptor.metricPersistNoBatch(type, startNanos);
  }

  /**
   * Add to profile as batched bean insert, update or delete.
   */
  @Override
  public void profile(long offset, int flushCount) {
    profileBase(type.profileEventId, offset, beanDescriptor.name(), flushCount);
  }

  /**
   * Return the document store event that should be used for this request.
   * <p>
   * Used to check if the Transaction has set the mode to IGNORE when doing large batch inserts that we
   * don't want to send to the doc store.
   */
  private DocStoreMode calcDocStoreMode(SpiTransaction txn, Type type) {
    DocStoreMode txnMode = (txn == null) ? null : txn.docStoreMode();
    return beanDescriptor.docStoreMode(type, txnMode);
  }

  @Override
  public boolean isCascade() {
    return Flags.isRecurse(flags);
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
    transaction.markNotQueryOnly();
  }

  /**
   * Check for batch escalation on cascade.
   */
  public void checkBatchEscalationOnCascade() {
    if (type == Type.UPDATE || beanDescriptor.isBatchEscalateOnCascade(type)) {
      if (transaction.checkBatchEscalationOnCascade(this)) {
        // we escalated to use batch mode so flush when done
        // but if createdTransaction then commit will flush it
        batchOnCascadeSet = !createdTransaction;
      }
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
    dirtyGenerated = beanDescriptor.propertiesGenUpdate();
    for (BeanProperty prop : beanDescriptor.propertiesGenUpdate()) {
      GeneratedProperty generatedProperty = prop.generatedProperty();
      if (prop.isVersion()) {
        if (isLoadedProperty(prop)) {
          // @Version property must be loaded to be involved
          Object value = generatedProperty.getUpdateValue(prop, entityBean, now());
          Object oldVal = prop.getValue(entityBean);
          setVersionValue(value);
          intercept.setOldValue(prop.propertyIndex(), oldVal);
        }
      } else {
        // @WhenModified set without invoking interception
        Object oldVal = prop.getValue(entityBean);
        Object value = generatedProperty.getUpdateValue(prop, entityBean, now());
        prop.setValueChanged(entityBean, value);
        intercept.setOldValue(prop.propertyIndex(), oldVal);
      }
    }
  }

  private void onInsertGeneratedProperties() {
    dirtyGenerated = beanDescriptor.propertiesGenInsert();
    for (BeanProperty prop : beanDescriptor.propertiesGenInsert()) {
      Object value = prop.generatedProperty().getInsertValue(prop, entityBean, now());
      prop.setValueChanged(entityBean, value);
    }
  }

  /**
   * Undos the update of generated properties.
   */
  @Override
  public void undo() {
    if (dirtyGenerated != null) {
      // Do an undo once, and undo only modified properties.
      for (BeanProperty prop : dirtyGenerated) {
        if (!prop.isVersion() || isLoadedProperty(prop)) {
          Object oldVal = intercept.origValue(prop.propertyIndex());
          prop.setValue(entityBean, oldVal);
        }
      }
      dirtyGenerated = null;
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
      if (beanDescriptor.hasSingleIdProperty()) {
        // used to trigger automatic jdbc batch flush
        intercept.registerGetterCallback(this);
        getterCallback = true;
      }
    }
  }

  @Override
  public void preGetterTrigger(int propertyIndex) {
    if (flushBatchOnGetter(propertyIndex)) {
      transaction.flush();
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
  public Set<String> loadedProperties() {
    return intercept.loadedPropertyNames();
  }

  @Override
  public Set<String> updatedProperties() {
    return intercept.dirtyPropertyNames();
  }

  public boolean isChangedProperty(int propertyIndex) {
    if (dirtyProperties == null) {
      return intercept.isChangedProperty(propertyIndex);
    } else {
      return dirtyProperties[propertyIndex];
    }
  }

  /**
   * Return the dirty properties on this request.
   */
  @Override
  public boolean[] dirtyProperties() {
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
  public Map<String, ValuePair> updatedValues() {
    return intercept.dirtyValues();
  }

  /**
   * Set the cache notify status.
   */
  private void setNotifyCache() {
    this.notifyCache = beanDescriptor.isCacheNotify(type, publish);
  }

  /**
   * Return true if this change should notify persist listener or doc store (and keep the request).
   */
  private boolean isNotifyListeners() {
    return isNotifyPersistListener() || isDocStoreNotify();
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
  private void notifyCache(CacheChangeSet changeSet) {
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
      case UPDATE:
      case DELETE_SOFT:
        docStoreUpdates.queueIndex(beanDescriptor.docStoreQueueId(), idValue);
        break;
      case DELETE:
        docStoreUpdates.queueDelete(beanDescriptor.docStoreQueueId(), idValue);
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
   * Return true if this bean has been already been persisted (inserted or updated) in this transaction.
   */
  public boolean isRegisteredBean() {
    return transaction.isRegisteredBean(bean);
  }

  public void unRegisterBean() {
    if (!saveRecurse) {
      // only clear all persisted beans when persisting at the top level
      transaction.unregisterBeans();
    }
  }

  /**
   * The hash used to register the bean with the transaction.
   * <p>
   * Takes into account the class type and id value.
   */
  private Integer beanHash() {
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
    Integer hash = beanHash();
    transaction.registerDeleteBean(hash);
  }

  public boolean isRegisteredForDeleteBean() {
    if (transaction == null) {
      return false;
    } else {
      Integer hash = beanHash();
      return transaction.isRegisteredDeleteBean(hash);
    }
  }

  /**
   * Return the BeanDescriptor for the associated bean.
   */
  public BeanDescriptor<T> descriptor() {
    return beanDescriptor;
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
  public ConcurrencyMode concurrencyMode() {
    return concurrencyMode;
  }

  /**
   * Returns a description of the request. This is typically the bean class name or the base table
   * for MapBeans.
   * <p>
   * Used to determine common persist requests for queueing and statement batching.
   * </p>
   */
  public String fullName() {
    return beanDescriptor.fullName();
  }

  /**
   * Return the bean associated with this request.
   */
  @Override
  public T bean() {
    return bean;
  }

  public EntityBean entityBean() {
    return entityBean;
  }

  /**
   * Return the Id value for the bean.
   */
  public Object beanId() {
    return beanDescriptor.getId(entityBean);
  }

  /**
   * Create and return a new reference bean matching this beans Id value.
   */
  public T createReference() {
    return beanDescriptor.createRef(beanId(), null);
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
  public Object parentBean() {
    return parentBean;
  }

  /**
   * Return the intercept if there is one.
   */
  public EntityBeanIntercept intercept() {
    return intercept;
  }

  /**
   * Return true if this property is loaded (full bean or included in partial bean).
   */
  public boolean isLoadedProperty(BeanProperty prop) {
    return intercept.isLoadedProperty(prop.propertyIndex());
  }

  /**
   * Return true if the property is dirty.
   */
  public boolean isDirtyProperty(BeanProperty prop) {
    return intercept.isDirtyProperty(prop.propertyIndex());
  }

  /**
   * Return the original / old value for the given property.
   */
  public Object origValue(BeanProperty prop) {
    return intercept.origValue(prop.propertyIndex());
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
    beanDescriptor.softDeleteValue(entityBean);
  }

  @Override
  public int executeOrQueue() {
    boolean batch = isBatchThisRequest();
    try {
      BatchControl control = transaction.batchControl();
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
  public void checkRowCount(int rowCount) {
    if (rowCount != 1 && rowCount != Statement.SUCCESS_NO_INFO) {
      if (ConcurrencyMode.VERSION == concurrencyMode) {
        throw new OptimisticLockException("Data has changed. updated row count " + rowCount, null, bean);
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
      beanDescriptor.contextClear(transaction.persistenceContext(), idValue);
    }
  }

  private void postUpdateNotify() {
    if (pendingPostUpdateNotify) {
      controller.postUpdate(this);
    }
  }

  /**
   * Remove deleted beans from the persistence context early.
   */
  public void removeFromPersistenceContext() {
    idValue = beanDescriptor.getId(entityBean);
    beanDescriptor.contextDeleted(transaction.persistenceContext(), idValue);
  }

  /**
   * Aggressive L1 and L2 cache cleanup for deletes.
   */
  private void postDelete() {
    beanDescriptor.contextClear(transaction.persistenceContext(), idValue);
  }

  private void changeLog() {
    BeanChange changeLogBean = beanDescriptor.changeLogBean(this);
    if (changeLogBean != null) {
      transaction.addBeanChange(changeLogBean);
    }
  }

  /**
   * Post processing.
   */
  @Override
  public void postExecute() {
    saveQueuedMany();
    postExecute = true;
    if (controller != null) {
      controllerPost();
    }
    setNotifyCache();
    boolean isChangeLog = beanDescriptor.isChangeLog();
    if (type == Type.UPDATE) {
      // get the dirty properties for notify cache & orphanRemoval of vanilla collection detection
      dirtyProperties = intercept.dirtyProperties();
    }
    if (isChangeLog) {
      changeLog();
    }
    // if bean persisted again then should result in an update
    intercept.setLoaded();
    if (isInsert()) {
      postInsert();
    }
    addPostCommitListeners();
    notifyCacheOnPostExecute();
    if (logSummary()) {
      logSummaryMessage();
    }
  }

  private void saveQueuedMany() {
    if (saveMany != null) {
      saveMany.forEach(SaveMany::saveBatch);
    }
  }

  /**
   * Ensure the preUpdate event fires (for case where only element collection has changed).
   */
  public void preElementCollectionUpdate() {
    if (controller != null && !dirty) {
      // fire preUpdate notification when only element collection updated
      controller.preUpdate(this);
      pendingPostUpdateNotify = true;
    }
    if (!dirty) {
      setNotifyCache();
    }
  }

  public boolean isNotifyCache() {
    return notifyCache;
  }

  private void controllerPost() {
    boolean old = transaction.isFlushOnQuery();
    transaction.setFlushOnQuery(false);
    try {
      switch (type) {
        case INSERT:
          controller.postInsert(this);
          break;
        case UPDATE:
          controller.postUpdate(this);
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
    } finally {
      transaction.setFlushOnQuery(old);
    }
  }

  private void logSummaryMessage() {
    String draft = (beanDescriptor.isDraftable() && !publish) ? " draft[true]" : "";
    String name = beanDescriptor.name();
    switch (type) {
      case INSERT:
        transaction.logSummary("Inserted [{0}] [{1}]{2}", name, (idValue == null ? "" : idValue), draft);
        break;
      case UPDATE:
        transaction.logSummary("Updated [{0}] [{1}]{2}", name, idValue , draft);
        break;
      case DELETE:
        transaction.logSummary("Deleted [{0}] [{1}]{2}", name, idValue , draft);
        break;
      case DELETE_SOFT:
        transaction.logSummary("SoftDelete [{0}] [{1}]{2}", name, idValue , draft);
        break;
      default:
        break;
    }
  }

  /**
   * Add the request to TransactionEvent if there are post commit listeners.
   */
  private void addPostCommitListeners() {
    TransactionEvent event = transaction.event();
    if (event != null && isNotifyListeners()) {
      event.addListenerNotify(this);
    }
  }

  /**
   * Return true if the property should be included in the update.
   */
  public boolean isAddToUpdate(BeanProperty prop) {
    if (requestUpdateAllLoadedProps) {
      return intercept.isLoadedProperty(prop.propertyIndex());
    } else {
      return intercept.isDirtyProperty(prop.propertyIndex());
    }
  }

  /**
   * Register the derived relationships to get executed later (on JDBC batch flush or commit).
   */
  public void deferredRelationship(EntityBean assocBean, ImportedId importedId, EntityBean bean) {
    transaction.registerDeferred(new PersistDeferredRelationship(server, beanDescriptor, assocBean, importedId, bean));
  }

  private void postInsert() {
    // mark all properties as loaded after an insert to support immediate update
    beanDescriptor.setAllLoaded(entityBean);
    if (!publish) {
      beanDescriptor.setDraft(entityBean);
    }
    if (transaction.isAutoPersistUpdates() && idValue != null) {
      // with getGeneratedKeys off we will not have a idValue
      beanDescriptor.contextPut(transaction.persistenceContext(), idValue, entityBean);
    }
  }

  /**
   * Return if persist can be skipped on the reference only bean.
   */
  public boolean isSkipReference() {
    return intercept.isReference() || (Flags.isRecurse(flags) && beanDescriptor.referenceIdPropertyOnly(intercept));
  }

  public boolean isReference() {
    return beanDescriptor.isReference(intercept);
  }

  public void setUpdatedMany() {
    this.updatedMany = true;
  }

  /**
   * This many property has potential L2 cache update for many Ids.
   */
  public void addUpdatedManyForL2Cache(BeanPropertyAssocMany<?> many) {
    if (updatedManys == null) {
      updatedManys = new ArrayList<>(5);
    }
    updatedManys.add(many);
  }

  /**
   * Return the list of updated many properties for L2 cache update (can be null).
   */
  public List<BeanPropertyAssocMany<?>> updatedManyForL2Cache() {
    return updatedManys;
  }

  /**
   * Completed insert or delete request. Do cache notify in non-batched case.
   */
  public void complete() {
    notifyCacheOnComplete();
  }

  /**
   * Completed update request handling cases for element collection and where ONLY
   * many properties were updated.
   */
  public void completeUpdate() {
    if (!dirty && updatedMany) {
      // set the flag and register for post commit processing if there
      // is caching or registered listeners
      if (idValue == null) {
        this.idValue = beanDescriptor.getId(entityBean);
      }
      // not dirty so postExecute() will never be called
      // set true to trigger cache notify if needed
      postExecute = true;
      updatedManysOnly = true;
      setNotifyCache();
      addPostCommitListeners();
      saveQueuedMany();
    }
    notifyCacheOnComplete();
    postUpdateNotify();
  }

  /**
   * Notify cache when using batched persist.
   */
  private void notifyCacheOnPostExecute() {
    postExecute = true;
    if (notifyCache && complete) {
      // add cache notification (on batch persist)
      TransactionEvent event = transaction.event();
      if (event != null) {
        notifyCache(event.obtainCacheChangeSet());
      }
    }
  }

  /**
   * Notify cache when not use batched persist.
   */
  private void notifyCacheOnComplete() {
    complete = true;
    if (notifyCache && postExecute) {
      // add cache notification (on non-batch persist)
      TransactionEvent event = transaction.event();
      if (event != null) {
        notifyCache(event.obtainCacheChangeSet());
      }
    }
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
          docStoreUpdates.queueDelete(beanDescriptor.docStoreQueueId(), idValue);
        } else {
          docStoreUpdates.queueIndex(beanDescriptor.docStoreQueueId(), idValue);
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
      requestUpdateAllLoadedProps = isBatchThisRequest() && server.isUpdateAllPropertiesInBatch();
    }
    return requestUpdateAllLoadedProps;
  }

  /**
   * Return the flags set on this persist request.
   */
  public int flags() {
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
  public String updatePlanHash() {
    StringBuilder key;
    if (determineUpdateAllLoadedProperties()) {
      key = intercept.loadedPropertyKey();
    } else {
      key = intercept.dirtyPropertyKey();
    }
    BeanProperty versionProperty = beanDescriptor.versionProperty();
    if (versionProperty != null) {
      if (intercept.isLoadedProperty(versionProperty.propertyIndex())) {
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
  public String updateTable() {
    return publish ? beanDescriptor.baseTable() : beanDescriptor.draftTable();
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
  public long version() {
    return version;
  }

  private void setTenantId() {
    Object tenantId = transaction.tenantId();
    if (tenantId != null) {
      beanDescriptor.setTenantId(entityBean, tenantId);
    }
  }

  private void executeInsert() {
    setGeneratedId();
    setTenantId();
    if (controller == null || controller.preInsert(this)) {
      beanManager.getBeanPersister().insert(this);
    }
  }

  private void executeUpdate() {
    setTenantId();
    if (controller == null || controller.preUpdate(this)) {
      // check dirty state for all mutable scalar properties (like DbJson, Hstore)
      beanDescriptor.checkAllMutableProperties(intercept);
      if (beanPersistListener != null) {
        updatedProperties = updatedProperties();
      }
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
    if (type == Type.UPDATE) {
      dirtyProperties = intercept.dirtyProperties();
    }
    // processing now so set IGNORE (unlike DB + DocStore processing with post-commit)
    docStoreMode = DocStoreMode.IGNORE;
    try {
      docStoreUpdate(transaction.docStoreTransaction().obtain());
      postExecute();
      if (type == Type.UPDATE
        && beanDescriptor.isDocStoreEmbeddedInvalidation()
        && transaction.isPersistCascade()) {
        // queue embedded/nested updates for later processing
        beanDescriptor.docStoreUpdateEmbedded(this, transaction.docStoreTransaction().queue());
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
      now = server.clockNow();
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
    profileBase(type.profileEventId, profileOffset, beanDescriptor.name(), 1);
  }

  /**
   * Set the request flags indicating this is an insert.
   */
  public void flagInsert() {
    initGeneratedProperties();
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
    initGeneratedProperties();
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

      String key = beanDescriptor.cacheKey(idValue);
      Map<String, Object> changes = new LinkedHashMap<>();
      EntityBean bean = entityBean();
      boolean[] dirtyProperties = dirtyProperties();
      if (dirtyProperties != null) {
        for (int i = 0; i < dirtyProperties.length; i++) {
          if (dirtyProperties[i]) {
            BeanProperty property = beanDescriptor.propertyByIndex(i);
            if (property.isCacheDataInclude()) {
              Object val = property.getCacheDataValue(bean);
              changes.put(property.name(), val);
              if (property.isNaturalKey()) {
                updateNaturalKey = true;
                String valStr = (val == null) ? null : val.toString();
                changeSet.addNaturalKeyPut(beanDescriptor, key, valStr);
              }
            }
          }
        }
      }
      if (collectionChanges != null) {
        // add element collection update
        changes.putAll(collectionChanges);
      }
      changeSet.addBeanUpdate(beanDescriptor, key, changes, updateNaturalKey, version());
    }
  }

  /**
   * Set an orphan bean that needs to be deleted AFTER the request has persisted.
   */
  public void setImportedOrphanForRemoval(BeanPropertyAssocOne<?> prop) {
    Object orphan = origValue(prop);
    if (orphan instanceof EntityBean) {
      orphanBean = (EntityBean) orphan;
    }
  }

  public EntityBean importedOrphanForRemoval() {
    return orphanBean;
  }

  /**
   * Return the SQL used to fetch the last inserted id value.
   */
  public String selectLastInsertedId() {
    return beanDescriptor.selectLastInsertedId(publish);
  }

  /**
   * Return true if the intersection table updates or element collection updates should be queued.
   */
  public boolean isQueueSaveMany() {
    return !postExecute;
  }

  /**
   * The intersection table updates or element collection to the batch executed later on postExecute.
   */
  public void addSaveMany(SaveMany saveManyRequest) {
    if (this.saveMany == null) {
      this.saveMany = new ArrayList<>();
    }
    this.saveMany.add(saveManyRequest);
  }

  public boolean isForcedUpdate() {
    return Flags.isUpdateForce(flags);
  }

  /**
   * Set when this request is from cascading persist.
   */
  public void setSaveRecurse() {
    saveRecurse = true;
  }

  private void setGeneratedId() {
    beanDescriptor.setGeneratedId(entityBean, transaction);
  }

  public void setInsertOptions(InsertOptions insertOptions) {
    this.insertOptions  = insertOptions;
  }

  public InsertOptions insertOptions() {
    return insertOptions;
  }
}
