package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.ValuePair;
import com.avaje.ebean.bean.PreGetterCallback;
import com.avaje.ebeaninternal.api.ConcurrencyMode;
import com.avaje.ebean.annotation.DocStoreMode;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanPersistRequest;
import com.avaje.ebean.event.changelog.BeanChange;
import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.server.cache.CacheChangeSet;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanManager;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.persist.BatchControl;
import com.avaje.ebeaninternal.server.persist.PersistExecute;
import com.avaje.ebeaninternal.server.transaction.BeanPersistIdMap;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateContext;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdates;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdate;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PersistRequest for insert update or delete of a bean.
 */
public final class PersistRequestBean<T> extends PersistRequest implements BeanPersistRequest<T>, DocStoreUpdate, PreGetterCallback {

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

  /**
   * Flag set when request is added to JDBC batch registered as a "getter callback" to automatically flush batch.
   */
  private boolean getterCallback;

  public PersistRequestBean(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr, SpiTransaction t,
                            PersistExecute persistExecute, PersistRequest.Type type, boolean saveRecurse, boolean publish) {

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
    if (saveRecurse) {
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
    this.publish = publish;
    if (isMarkDraftDirty(publish)) {
      beanDescriptor.setDraftDirty(entityBean, true);
    }
    this.dirty = intercept.isDirty();
  }

  /**
   * Return the document store event that should be used for this request.
   *
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
    if (transaction.checkBatchEscalationOnCascade(this)) {
      // we escalated to use batch mode so flush when done
      // but if createdTransaction then commit will flush it
      batchOnCascadeSet = !createdTransaction;
    }
    persistCascade = transaction.isPersistCascade();
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
  public void preGetterTrigger() {
    transaction.flushBatch();
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

    for (int i = 0; i < propertyPositions.length; i++) {
      if (dirtyProperties[propertyPositions[i]]) {
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
          beanDescriptor.cacheHandleInsert(this, changeSet);
          break;
        case UPDATE:
          beanDescriptor.cacheHandleUpdate(idValue, this, changeSet);
          break;
        case DELETE:
        case SOFT_DELETE:
          beanDescriptor.cacheHandleDelete(idValue, this, changeSet);
          break;
        default:
          throw new IllegalStateException("Invalid type " + type);
      }
    }
  }

  /**
   * Process the persist request updating the document store.
   */
  public void docStoreUpdate(DocStoreUpdateContext txn) throws IOException {

    switch (type) {
      case INSERT:
        beanDescriptor.docStoreInsert(idValue, this, txn);
        break;
      case UPDATE:
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
  public void addToQueue(DocStoreUpdates docStoreUpdates) {
    switch (type) {
      case INSERT:
        docStoreUpdates.queueIndex(beanDescriptor.getDocStoreQueueId(), idValue);
        break;
      case UPDATE:
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
      int hc = 31 * bean.getClass().getName().hashCode();
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

  public BeanManager<T> getBeanManager() {
    return beanManager;
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
  public void postControllerPrepareUpdate() {
    if (intercept.isNew() && controller != null) {
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
    return beanDescriptor.createReference(Boolean.FALSE, false, getBeanId(), null);
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

  @Override
  public int executeNow() {
    if (getterCallback) {
      intercept.clearGetterCallback();
    }
    switch (type) {
      case INSERT:
        persistExecute.executeInsertBean(this);
        return -1;

      case UPDATE:
        if (beanPersistListener != null) {
          // store the updated properties for sending later
          updatedProperties = getUpdatedProperties();
        }
        persistExecute.executeUpdateBean(this);
        return -1;

      case SOFT_DELETE:
        prepareForSoftDelete();
        persistExecute.executeUpdateBean(this);
        return -1;

      case DELETE:
        return persistExecute.executeDeleteBean(this);

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

    BatchControl control = transaction.getBatchControl();
    if (control != null) {
      return control.executeOrQueue(this, batch);
    }
    if (batch) {
      control = persistExecute.createBatchControl(transaction);
      return control.executeOrQueue(this, true);

    } else {
      return executeNow();
    }
  }

  /**
   * Set the generated key back to the bean. Only used for inserts with getGeneratedKeys.
   */
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
  public final void checkRowCount(int rowCount) {
    if (ConcurrencyMode.VERSION == concurrencyMode && rowCount != 1) {
      String m = Message.msg("persist.conc2", "" + rowCount);
      throw new OptimisticLockException(m, null, bean);
    }
    switch (type) {
      case DELETE:
      case SOFT_DELETE:
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
  public void postExecute() {

    changeLog();

    if (controller != null) {
      controllerPost();
    }
    setNotifyCache();

    if (type == Type.UPDATE && (notifyCache || docStoreMode == DocStoreMode.UPDATE)) {
      // get the dirty properties for update notification to the doc store
      dirtyProperties = intercept.getDirtyProperties();
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

  private void controllerPost() {
    switch (type) {
      case INSERT:
        controller.postInsert(this);
        break;
      case UPDATE:
        controller.postUpdate(this);
        break;
      case DELETE:
      case SOFT_DELETE:
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
      case SOFT_DELETE:
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
   * Determine the concurrency mode depending on fully/partially populated bean.
   * <p>
   * Specifically with version concurrency we want to check that the version property was one of the
   * loaded properties.
   * </p>
   */
  public ConcurrencyMode determineConcurrencyMode() {

    // 'partial bean' update/delete...
    if (concurrencyMode.equals(ConcurrencyMode.VERSION)) {
      // check the version property was loaded
      BeanProperty prop = beanDescriptor.getVersionProperty();
      if (prop == null || !intercept.isLoadedProperty(prop.getPropertyIndex())) {
        concurrencyMode = ConcurrencyMode.NONE;
      }
    }

    return concurrencyMode;
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

  public List<DerivedRelationshipData> getDerivedRelationships() {
    return transaction.getDerivedRelationship(bean);
  }

  private void postInsert() {
    // mark all properties as loaded after an insert to support immediate update
    int len = intercept.getPropertyLength();
    for (int i = 0; i < len; i++) {
      intercept.setLoadedProperty(i);
    }
    beanDescriptor.setEmbeddedOwner(entityBean);
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
      updatedManys = new ArrayList<BeanPropertyAssocMany<?>>(5);
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
  }

  /**
   * Return true if only many properties where updated.
   */
  public boolean isUpdatedManysOnly() {
    return updatedManysOnly;
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
   * Return true if this request is a 'publish' action.
   */
  public boolean isPublish() {
    return publish;
  }

  /**
   * Return the key for an update persist request.
   */
  public int getUpdatePlanHash() {

    int hash;
    if (determineUpdateAllLoadedProperties()) {
      hash = intercept.getLoadedPropertyHash();
    } else {
      hash = intercept.getDirtyPropertyHash();
    }

    BeanProperty versionProperty = beanDescriptor.getVersionProperty();
    if (versionProperty != null) {
      if (intercept.isLoadedProperty(versionProperty.getPropertyIndex())) {
        hash = hash * 31 + 7;
      }
    }

    if (publish) {
      hash = hash * 31;
    }

    return hash;
  }

  /**
   * Return the table to update depending if the request is a 'publish' one or normal.
   */
  public String getUpdateTable() {
    return publish ? beanDescriptor.getBaseTable() : beanDescriptor.getDraftTable();
  }

  /**
   * Return true if this is a soft delete request.
   */
  public boolean isSoftDelete() {
    return Type.SOFT_DELETE == type;
  }

  /**
   * Set the value of the Version property on the bean.
   */
  public void setVersionValue(Object versionValue) {
    version = beanDescriptor.setVersion(entityBean, versionValue);
  }

  /**
   * Return the version in long form (if set).
   */
  public long getVersion() {
    return version;
  }
}
