package com.avaje.ebeaninternal.server.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OptimisticLockException;

import com.avaje.ebean.ValuePair;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanPersistRequest;
import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEvent;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanManager;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.persist.BatchControl;
import com.avaje.ebeaninternal.server.persist.PersistExecute;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.avaje.ebeaninternal.server.transaction.BeanDelta;
import com.avaje.ebeaninternal.server.transaction.BeanPersistIdMap;

/**
 * PersistRequest for insert update or delete of a bean.
 */
public final class PersistRequestBean<T> extends PersistRequest implements BeanPersistRequest<T> {

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

  public PersistRequestBean(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr, SpiTransaction t,
      PersistExecute persistExecute, PersistRequest.Type type, boolean saveRecurse) {

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
    this.dirty = intercept.isDirty();
  }

  /**
   * Init the transaction and also check for batch on cascade escalation.
   */
  public void initTransIfRequiredWithBatchCascade() {
    createImplicitTransIfRequired(false);
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

  @Override
  public Map<String, ValuePair> getUpdatedValues() {
    return intercept.getDirtyValues();
  }

  public boolean isNotify() {
    this.notifyCache = beanDescriptor.isCacheNotify();
    return notifyCache || isNotifyPersistListener();
  }

  public boolean isNotifyPersistListener() {
    return beanPersistListener != null;
  }

  /**
   * Notify/Update the local L2 cache after the transaction has successfully committed.
   */
  public void notifyCache() {
    if (notifyCache) {
      switch (type) {
      case INSERT:
        beanDescriptor.cacheHandleInsert(idValue, this);
        break;
      case UPDATE:
        beanDescriptor.cacheHandleUpdate(idValue, this);
        break;
      case DELETE:
        // Bean deleted from cache early via postDelete()
        break;
      default:
        throw new IllegalStateException("Invalid type " + type);
      }
    }
  }

  public void addToPersistMap(BeanPersistIdMap beanPersistMap) {

    beanPersistMap.add(beanDescriptor, type, idValue);
  }

  public boolean notifyLocalPersistListener() {
    if (beanPersistListener == null) {
      return false;

    } else {
      switch (type) {
      case INSERT:
        return beanPersistListener.inserted(bean);

      case UPDATE:
        return beanPersistListener.updated(bean, updatedProperties);

      case DELETE:
        return beanPersistListener.deleted(bean);

      default:
        return false;
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
      beanHash = new Integer(hc);
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

  public BeanDelta createDeltaBean() {
    return new BeanDelta(beanDescriptor, getBeanId());
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

    case DELETE:
      persistExecute.executeDeleteBean(this);
      return -1;

    default:
      throw new RuntimeException("Invalid type " + type);
    }
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
  public final void checkRowCount(int rowCount) throws SQLException {
    if (rowCount != 1) {
      String m = Message.msg("persist.conc2", "" + rowCount);
      throw new OptimisticLockException(m, null, bean);
    }
    switch (type) {
      case DELETE: postDelete(); break;
      case UPDATE: postUpdate(); break;
      default: // do nothing
    }
  }

  /**
   * Clear the bean from the PersistenceContext (L1 cache) for stateless updates.
   */
  private void postUpdate() {
    if (statelessUpdate) {
      transaction.getPersistenceContext().clear(beanDescriptor.getBeanType(), idValue);
    }
  }

  /**
   * Aggressive L1 and L2 cache cleanup for deletes.
   */
  private void postDelete() {
    // Delete the bean from the PersistenceContent
    transaction.getPersistenceContext().clear(beanDescriptor.getBeanType(), idValue);
    // Delete from cache early even if transaction fails
    beanDescriptor.cacheHandleDelete(idValue, this);
  }

  /**
   * Post processing.
   */
  public void postExecute() throws SQLException {

    if (controller != null) {
      controllerPost();
    }

    // if bean persisted again then should result in an update
    intercept.setLoaded();

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
      controller.postDelete(this);
      break;
    default:
      break;
    }
  }

  private void logSummary() {

    String name = beanDescriptor.getName();
    switch (type) {
    case INSERT:
      transaction.logSummary("Inserted [" + name + "] [" + idValue + "]");
      break;
    case UPDATE:
      transaction.logSummary("Updated [" + name + "] [" + idValue + "]");
      break;
    case DELETE:
      transaction.logSummary("Deleted [" + name + "] [" + idValue + "]");
      break;
    default:
      break;
    }
  }

  /**
   * Add the bean to the TransactionEvent. This will be used by TransactionManager to synch Cache,
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
   * Create a GenerateDmlRequest used to generate the DML.
   * <p>
   * Will used changed properties or loaded properties depending on the
   * BeanDescriptor.isUpdateChangesOnly() value.
   * </p>
   */
  public GenerateDmlRequest createGenerateDmlRequest(boolean emptyStringAsNull) {
    return new GenerateDmlRequest(emptyStringAsNull, intercept, beanDescriptor.isUpdateChangesOnly());
  }

  /**
   * Return true if the property should be included in the update.
   */
  public boolean isAddToUpdate(BeanProperty prop) {
    if (transaction.isUpdateAllLoadedProperties()) {
      return intercept.isLoadedProperty(prop.getPropertyIndex());
    } else {
      return intercept.isDirtyProperty(prop.getPropertyIndex());
    }
  }

  public List<DerivedRelationshipData> getDerivedRelationships() {
    return transaction.getDerivedRelationship(bean);
  }

  public void postInsert() {
    // mark all properties as loaded after an insert to support immediate update
    int len = intercept.getPropertyLength();
    for (int i = 0; i < len; i++) {
      intercept.setLoadedProperty(i);
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
    // if (notifyCache) {
    if (updatedManys == null) {
      updatedManys = new ArrayList<BeanPropertyAssocMany<?>>(5);
    }
    updatedManys.add(updatedAssocMany);
    // }
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
      addEvent();
    }
  }

  /**
   * Return true if only many properties where updated.
   */
  public boolean isUpdatedManysOnly() {
    return updatedManysOnly;
  }

}
