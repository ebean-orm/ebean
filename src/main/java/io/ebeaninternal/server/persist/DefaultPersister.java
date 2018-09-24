package io.ebeaninternal.server.persist;

import io.ebean.CallableSql;
import io.ebean.MergeOptions;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.Update;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.BeanPersistController;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiUpdate;
import io.ebeaninternal.server.core.Message;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequest.Type;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.core.PersistRequestCallableSql;
import io.ebeaninternal.server.core.PersistRequestOrmUpdate;
import io.ebeaninternal.server.core.PersistRequestUpdateSql;
import io.ebeaninternal.server.core.Persister;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanManager;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.IntersectionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persister implementation using DML.
 * <p>
 * This object uses DmlPersistExecute to perform the actual persist execution.
 * </p>
 * <p>
 * This object:
 * <ul>
 * <li>Determines insert or update for saved beans</li>
 * <li>Determines the concurrency mode</li>
 * <li>Handles cascading of save and delete</li>
 * <li>Handles the batching and queueing</li>
 * </p>
 */
public final class DefaultPersister implements Persister {

  private static final Logger PUB = LoggerFactory.getLogger("io.ebean.PUB");

  private static final Logger logger = LoggerFactory.getLogger(DefaultPersister.class);

  /**
   * Actually does the persisting work.
   */
  private final PersistExecute persistExecute;

  private final SpiEbeanServer server;

  private final BeanDescriptorManager beanDescriptorManager;

  private final boolean updatesDeleteMissingChildren;

  public DefaultPersister(SpiEbeanServer server, Binder binder, BeanDescriptorManager descMgr) {
    this.server = server;
    this.updatesDeleteMissingChildren = server.getServerConfig().isUpdatesDeleteMissingChildren();
    this.beanDescriptorManager = descMgr;
    this.persistExecute = new DefaultPersistExecute(binder, server.getServerConfig().getPersistBatchSize());
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
    persistExecute.visitMetrics(visitor);
  }

  /**
   * Execute the CallableSql.
   */
  @Override
  public int executeCallable(CallableSql callSql, Transaction t) {

    return executeOrQueue(new PersistRequestCallableSql(server, callSql, (SpiTransaction) t, persistExecute));
  }

  /**
   * Execute the orm update.
   */
  @Override
  public int executeOrmUpdate(Update<?> update, Transaction t) {

    SpiUpdate<?> ormUpdate = (SpiUpdate<?>) update;

    BeanManager<?> mgr = beanDescriptorManager.getBeanManager(ormUpdate.getBeanType());

    if (mgr == null) {
      String msg = "No BeanManager found for type [" + ormUpdate.getBeanType() + "]. Is it an entity?";
      throw new PersistenceException(msg);
    }

    return executeOrQueue(new PersistRequestOrmUpdate(server, mgr, ormUpdate, (SpiTransaction) t, persistExecute));
  }

  private int executeOrQueue(PersistRequest request) {
    try {
      request.initTransIfRequired();
      int rc = request.executeOrQueue();
      request.commitTransIfRequired();
      return rc;

    } catch (RuntimeException e) {
      request.rollbackTransIfRequired();
      throw e;
    }
  }

  @Override
  public void addBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction) {
    new PersistRequestUpdateSql(server, sqlUpdate, transaction, persistExecute).addBatch();
  }

  @Override
  public int[] executeBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction) {
    BatchControl batchControl = transaction.getBatchControl();
    try {
      return batchControl.execute(sqlUpdate.getSql(), sqlUpdate.isGetGeneratedKeys());
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  /**
   * Execute the updateSql.
   */
  @Override
  public int executeSqlUpdate(SqlUpdate updSql, Transaction t) {

    return executeOrQueue(new PersistRequestUpdateSql(server, (SpiSqlUpdate) updSql, (SpiTransaction) t, persistExecute));
  }

  /**
   * Restore draft beans to match live beans given the query.
   */
  @Override
  public <T> List<T> draftRestore(Query<T> query, Transaction transaction) {

    Class<T> beanType = query.getBeanType();
    BeanDescriptor<T> desc = server.getBeanDescriptor(beanType);

    DraftHandler<T> draftHandler = new DraftHandler<>(desc, transaction);

    List<T> liveBeans = draftHandler.fetchSourceBeans(query, false);
    PUB.debug("draftRestore [{}] count[{}]", desc.getName(), liveBeans.size());
    if (liveBeans.isEmpty()) {
      return Collections.emptyList();
    }

    draftHandler.fetchDestinationBeans(liveBeans, true);

    BeanManager<T> mgr = beanDescriptorManager.getBeanManager(beanType);

    for (T liveBean : liveBeans) {
      T draftBean = draftHandler.publishToDestinationBean(liveBean);
      // reset @DraftDirty and @DraftReset properties
      draftHandler.resetDraft(draftBean);

      PUB.trace("draftRestore bean [{}] id[{}]", desc.getName(), draftHandler.getId());
      update(createRequest(draftBean, transaction, null, mgr, Type.UPDATE, Flags.RECURSE));
    }

    PUB.debug("draftRestore - complete for [{}]", desc.getName());
    return draftHandler.getDrafts();
  }

  /**
   * Helper method to return the list of Id values for the list of beans.
   */
  private <T> List<Object> getBeanIds(BeanDescriptor<T> desc, List<T> beans) {
    List<Object> idList = new ArrayList<>(beans.size());
    for (T liveBean : beans) {
      idList.add(desc.getBeanId(liveBean));
    }
    return idList;
  }

  /**
   * Publish from draft to live given the query.
   */
  @Override
  public <T> List<T> publish(Query<T> query, Transaction transaction) {

    Class<T> beanType = query.getBeanType();
    BeanDescriptor<T> desc = server.getBeanDescriptor(beanType);

    DraftHandler<T> draftHandler = new DraftHandler<>(desc, transaction);

    List<T> draftBeans = draftHandler.fetchSourceBeans(query, true);
    PUB.debug("publish [{}] count[{}]", desc.getName(), draftBeans.size());
    if (draftBeans.isEmpty()) {
      return Collections.emptyList();
    }

    draftHandler.fetchDestinationBeans(draftBeans, false);

    BeanManager<T> mgr = beanDescriptorManager.getBeanManager(beanType);

    List<T> livePublish = new ArrayList<>(draftBeans.size());
    for (T draftBean : draftBeans) {
      T liveBean = draftHandler.publishToDestinationBean(draftBean);
      livePublish.add(liveBean);

      // reset @DraftDirty and @DraftReset properties
      draftHandler.resetDraft(draftBean);

      Type persistType = draftHandler.isInsert() ? Type.INSERT : Type.UPDATE;
      PUB.trace("publish bean [{}] id[{}] type[{}]", desc.getName(), draftHandler.getId(), persistType);

      PersistRequestBean<T> request = createRequest(liveBean, transaction, null, mgr, persistType, Flags.PUBLISH_RECURSE);
      if (persistType == Type.INSERT) {
        insert(request);
      } else {
        update(request);
      }
    }

    draftHandler.updateDrafts(transaction, mgr);

    PUB.debug("publish - complete for [{}]", desc.getName());
    return livePublish;
  }

  /**
   * Helper to handle draft beans (properties reset etc).
   */
  class DraftHandler<T> {

    final BeanDescriptor<T> desc;
    final Transaction transaction;
    final BeanProperty draftDirty;
    final List<T> draftUpdates = new ArrayList<>();

    /**
     * Id value of the last published bean.
     */
    Object id;

    /**
     * True if the last published bean is new/insert.
     */
    boolean insert;

    /**
     * The destination beans to publish/restore to mapped by id.
     */
    Map<?, T> destBeans;

    DraftHandler(BeanDescriptor<T> desc, Transaction transaction) {
      this.desc = desc;
      this.transaction = transaction;
      this.draftDirty = desc.getDraftDirty();
    }

    /**
     * Return the list of draft beans with changes (to be persisted).
     */
    List<T> getDrafts() {
      return draftUpdates;
    }

    /**
     * Set the draft dirty state to false and reset any dirtyReset properties.
     */
    void resetDraft(T draftBean) {
      if (desc.draftReset(draftBean)) {
        // draft bean is dirty so collect it for persisting later
        draftUpdates.add(draftBean);
      }
    }

    /**
     * Save all the draft beans (with various properties reset etc).
     */
    void updateDrafts(Transaction transaction, BeanManager<T> mgr) {
      if (!draftUpdates.isEmpty()) {
        // update the dirty status on the drafts that have been published
        PUB.debug("publish - update dirty status on [{}] drafts", draftUpdates.size());
        for (T draftUpdate : draftUpdates) {
          update(createRequest(draftUpdate, transaction, null, mgr, Type.UPDATE, Flags.ZERO));
        }
      }
    }

    /**
     * Fetch the source beans based on the query.
     */
    List<T> fetchSourceBeans(Query<T> query, boolean asDraft) {
      desc.draftQueryOptimise(query);
      if (asDraft) {
        query.asDraft();
      }
      return server.findList(query, transaction);
    }

    /**
     * Fetch the destination beans that will be published to.
     */
    void fetchDestinationBeans(List<T> sourceBeans, boolean asDraft) {

      List<Object> ids = getBeanIds(desc, sourceBeans);

      Query<T> destQuery = server.find(desc.getBeanType()).where().idIn(ids).query();
      if (asDraft) {
        destQuery.asDraft();
      }
      desc.draftQueryOptimise(destQuery);
      this.destBeans = server.findMap(destQuery, transaction);
    }

    /**
     * Publish/restore the values from the sourceBean to the matching destination bean.
     */
    T publishToDestinationBean(T sourceBean) {
      id = desc.getBeanId(sourceBean);
      T destBean = destBeans.get(id);
      insert = (destBean == null);
      // apply changes from liveBean to draftBean
      return desc.publish(sourceBean, destBean);
    }

    /**
     * Return true if the last publish resulted in an new bean to insert.
     */
    boolean isInsert() {
      return insert;
    }

    /**
     * Return the Id value of the last published/restored bean.
     */
    Object getId() {
      return id;
    }
  }

  /**
   * Recursively delete the bean. This calls back to the EbeanServer.
   */
  private int deleteRecurse(EntityBean detailBean, Transaction t, DeleteMode deleteMode) {
    return deleteRequest(createRequest(detailBean, t, deleteMode.persistType()));
  }

  @Override
  public int merge(BeanDescriptor<?> desc, EntityBean bean, MergeOptions options, SpiTransaction transaction) {

    MergeHandler merge = new MergeHandler(server, desc, bean, options, transaction);
    List<EntityBean> deleteBeans = merge.merge();
    if (!deleteBeans.isEmpty()) {
      // all detected deletes for the merge paths
      for (EntityBean deleteBean : deleteBeans) {
        delete(deleteBean, transaction, options.isDeletePermanent());
      }
    }

    // cascade save as normal with forceUpdate flags set
    PersistRequestBean<?> request = createRequestRecurse(bean, transaction, null, Flags.MERGE);
    request.checkBatchEscalationOnCascade();
    saveRecurse(request);
    request.flushBatchOnCascade();

    // lambda expects a return
    return 0;
  }

  /**
   * Update the bean.
   */
  @Override
  public void update(EntityBean entityBean, Transaction t) {
    update(entityBean, t, updatesDeleteMissingChildren);
  }

  /**
   * Update the bean specifying deleteMissingChildren.
   */
  @Override
  public void update(EntityBean entityBean, Transaction t, boolean deleteMissingChildren) {

    PersistRequestBean<?> req = createRequest(entityBean, t, PersistRequest.Type.UPDATE);
    req.setDeleteMissingChildren(deleteMissingChildren);
    req.checkDraft();
    try {
      req.initTransIfRequiredWithBatchCascade();
      if (req.isReference()) {
        // its a reference so see if there are manys to save...
        if (req.isPersistCascade()) {
          saveAssocMany(req);
        }
        req.checkUpdatedManysOnly();
      } else {
        update(req);
      }

      req.commitTransIfRequired();
      req.flushBatchOnCascade();

    } catch (RuntimeException ex) {
      req.rollbackTransIfRequired();
      throw ex;
    }
  }

  /**
   * Insert or update the bean.
   */
  @Override
  public void save(EntityBean bean, Transaction t) {
    if (bean._ebean_getIntercept().isUpdate()) {
      // deleteMissingChildren is false when using 'save' on 'loaded' beans
      update(bean, t, false);
    } else {
      insert(bean, t);
    }
  }

  /**
   * Insert this bean.
   */
  @Override
  public void insert(EntityBean bean, Transaction t) {

    PersistRequestBean<?> req = createRequest(bean, t, PersistRequest.Type.INSERT);
    try {
      req.initTransIfRequiredWithBatchCascade();
      insert(req);
      req.commitTransIfRequired();
      req.flushBatchOnCascade();

    } catch (RuntimeException ex) {
      req.rollbackTransIfRequired();
      throw ex;
    }
  }

  void saveRecurse(EntityBean bean, Transaction t, Object parentBean, int flags) {

    // determine insert or update taking into account stateless updates
    saveRecurse(createRequestRecurse(bean, t, parentBean, flags));
  }

  private void saveRecurse(PersistRequestBean<?> request) {
    if (request.isReference()) {
      // its a reference...
      if (request.isPersistCascade()) {
        // save any associated List held beans
        request.flagUpdate();
        saveAssocMany(request);
      }
      request.checkUpdatedManysOnly();
    } else {
      if (request.isInsert()) {
        insert(request);
      } else {
        update(request);
      }
    }
  }

  /**
   * Insert the bean.
   */
  private void insert(PersistRequestBean<?> request) {

    if (request.isRegisteredBean()) {
      // skip as already inserted/updated in this request (recursive cascading)
      return;
    }
    request.flagInsert();
    try {
      if (request.isPersistCascade()) {
        // save associated One beans recursively first
        saveAssocOne(request);
      }

      // set the IDGenerated value if required
      setIdGenValue(request);
      request.executeOrQueue();

      if (request.isPersistCascade()) {
        // save any associated List held beans
        saveAssocMany(request);
      }
    } finally {
      request.unRegisterBean();
    }
  }

  /**
   * Update the bean.
   */
  private void update(PersistRequestBean<?> request) {

    if (request.isRegisteredBean()) {
      // skip as already inserted/updated in this request (recursive cascading)
      return;
    }
    request.flagUpdate();
    try {
      if (request.isPersistCascade()) {
        // save associated One beans recursively first
        saveAssocOne(request);
      }

      if (request.isDirty()) {
        request.executeOrQueue();

      } else if (logger.isDebugEnabled()) {
        logger.debug(Message.msg("persist.update.skipped", request.getBean()));
      }

      if (request.isPersistCascade()) {
        // save all the beans in assocMany's after
        saveAssocMany(request);
      }

      request.checkUpdatedManysOnly();

    } finally {
      request.unRegisterBean();
    }
  }

  /**
   * Delete the bean with the explicit transaction.
   * Return false if the delete is executed without OCC and 0 rows were deleted.
   */
  @Override
  public int delete(EntityBean bean, Transaction t, boolean permanent) {

    Type deleteType = permanent ? Type.DELETE_PERMANENT : Type.DELETE;
    PersistRequestBean<EntityBean> originalRequest = createRequest(bean, t, deleteType);

    if (originalRequest.isHardDeleteDraft()) {
      // a hard delete of a draftable bean so first we need to  delete the associated 'live' bean
      // due to FK constraint and then after that execute the original delete of the draft bean
      return deleteRequest(createPublishRequest(originalRequest.createReference(), t, Type.DELETE_PERMANENT, Flags.PUBLISH), originalRequest);

    } else {
      // normal delete or soft delete
      return deleteRequest(originalRequest);
    }
  }

  /**
   * Execute the delete request returning true if a delete occurred.
   */
  int deleteRequest(PersistRequestBean<?> req) {
    return deleteRequest(req, null);
  }

  /**
   * Execute the delete request support a second delete request for live and draft permanent delete.
   * A common transaction is used across both requests.
   */
  private int deleteRequest(PersistRequestBean<?> req, PersistRequestBean<?> draftReq) {

    if (req.isRegisteredForDeleteBean()) {
      // skip deleting bean. Used where cascade is on
      // both sides of a relationship
      if (logger.isDebugEnabled()) {
        logger.debug("skipping delete on alreadyRegistered " + req.getBean());
      }
      return 0;
    }

    try {
      req.initTransIfRequiredWithBatchCascade();
      int rows = delete(req);
      if (draftReq != null) {
        // delete the 'draft' bean ('live' bean deleted first)
        draftReq.setTrans(req.getTransaction());
        rows = delete(draftReq);
      }
      req.commitTransIfRequired();
      req.flushBatchOnCascade();

      return rows;

    } catch (RuntimeException ex) {
      req.rollbackTransIfRequired();
      throw ex;
    }
  }

  private void deleteList(List<?> beanList, SpiTransaction t, DeleteMode deleteMode, boolean children) {
    if (children) {
      t.depth(-1);
      t.checkBatchEscalationOnCollection();
    }
    for (Object aBeanList : beanList) {
      deleteRecurse((EntityBean) aBeanList, t, deleteMode);
    }
    if (children) {
      t.flushBatchOnCollection();
      t.depth(+1);
    }
  }

  /**
   * Delete by a List of Id's.
   */
  @Override
  public int deleteMany(Class<?> beanType, Collection<?> ids, Transaction transaction, boolean permanent) {

    if (ids == null || ids.isEmpty()) {
      return 0;
    }

    BeanDescriptor<?> descriptor = beanDescriptorManager.getBeanDescriptor(beanType);
    DeleteMode deleteMode = (permanent || !descriptor.isSoftDelete()) ? DeleteMode.HARD : DeleteMode.SOFT;
    if (descriptor.isMultiTenant()) {
      return deleteAsBeans(ids, transaction, deleteMode, descriptor);
    }

    ArrayList<Object> idList = new ArrayList<>(ids.size());
    for (Object id : ids) {
      // convert to appropriate type if required
      idList.add(descriptor.convertId(id));
    }

    return delete(descriptor, null, idList, transaction, deleteMode);
  }

  /**
   * Convert delete by Ids into delete many beans.
   */
  private int deleteAsBeans(Collection<?> ids, Transaction transaction, DeleteMode deleteMode, BeanDescriptor<?> descriptor) {
    // convert to a delete by bean
    int total = 0;
    for (Object id : ids) {
      EntityBean bean = descriptor.createEntityBean();
      descriptor.convertSetId(id, bean);
      int rowCount = deleteRecurse(bean, transaction, deleteMode);
      if (rowCount == -1) {
        total = -1;
      } else if (total != -1) {
        total += rowCount;
      }
    }
    return total;
  }

  /**
   * Delete by Id.
   */
  @Override
  public int delete(Class<?> beanType, Object id, Transaction transaction, boolean permanent) {
    BeanDescriptor<?> descriptor = beanDescriptorManager.getBeanDescriptor(beanType);
    if (descriptor.isMultiTenant()) {
      // convert to a delete by bean
      EntityBean bean = descriptor.createEntityBean();
      descriptor.convertSetId(id, bean);
      return delete(bean, transaction, permanent);
    }

    id = descriptor.convertId(id);
    DeleteMode deleteMode = (permanent || !descriptor.isSoftDelete()) ? DeleteMode.HARD : DeleteMode.SOFT;
    return delete(descriptor, id, null, transaction, deleteMode);
  }

  @Override
  public int deleteByIds(BeanDescriptor<?> descriptor, List<Object> idList, Transaction transaction, boolean permanent) {
    DeleteMode deleteMode = (permanent || !descriptor.isSoftDelete()) ? DeleteMode.HARD : DeleteMode.SOFT;
    return delete(descriptor, null, idList, transaction, deleteMode);
  }

  /**
   * Delete by Id or a List of Id's.
   */
  private int delete(BeanDescriptor<?> descriptor, Object id, List<Object> idList, Transaction transaction, DeleteMode deleteMode) {

    SpiTransaction t = (SpiTransaction) transaction;
    if (t.isPersistCascade()) {
      BeanPropertyAssocOne<?>[] propImportDelete = descriptor.propertiesOneImportedDelete();
      if (propImportDelete.length > 0) {
        // We actually need to execute a query to get the foreign key values
        // as they are required for the delete cascade. Query back just the
        // Id and the appropriate foreign key values
        Query<?> q = deleteRequiresQuery(descriptor, propImportDelete, deleteMode);
        if (idList != null) {
          q.where().idIn(idList);
          if (t.isLogSummary()) {
            t.logSummary("-- DeleteById of " + descriptor.getName() + " ids[" + idList + "] requires fetch of foreign key values");
          }
          List<?> beanList = server.findList(q, t);
          deleteList(beanList, t, deleteMode, false);
          return beanList.size();

        } else {
          q.where().idEq(id);
          if (t.isLogSummary()) {
            t.logSummary("-- DeleteById of " + descriptor.getName() + " id[" + id + "] requires fetch of foreign key values");
          }
          EntityBean bean = (EntityBean) server.findOne(q, t);
          if (bean == null) {
            return 0;
          } else {
            return deleteRecurse(bean, t, deleteMode);
          }
        }
      }
    }

    if (t.isPersistCascade()) {
      // OneToOne exported side with delete cascade
      BeanPropertyAssocOne<?>[] expOnes = descriptor.propertiesOneExportedDelete();
      for (BeanPropertyAssocOne<?> expOne : expOnes) {
        BeanDescriptor<?> targetDesc = expOne.getTargetDescriptor();
        // only cascade soft deletes when supported by target
        if (deleteMode.isHard() || targetDesc.isSoftDelete()) {
          if (deleteMode.isHard() && targetDesc.isDeleteByStatement()) {
            SqlUpdate sqlDelete = expOne.deleteByParentId(id, idList);
            executeSqlUpdate(sqlDelete, t);
          } else {
            List<Object> childIds = expOne.findIdsByParentId(id, idList, t);
            if (childIds != null && !childIds.isEmpty()) {
              deleteChildrenById(t, targetDesc, childIds, deleteMode);
            }
          }
        }
      }

      // OneToMany's with delete cascade
      BeanPropertyAssocMany<?>[] manys = descriptor.propertiesManyDelete();
      for (BeanPropertyAssocMany<?> many : manys) {
        if (!many.isManyToMany()) {
          BeanDescriptor<?> targetDesc = many.getTargetDescriptor();
          // only cascade soft deletes when supported by target
          if (deleteMode.isHard() || targetDesc.isSoftDelete()) {
            if (deleteMode.isHard() && targetDesc.isDeleteByStatement()) {
              // we can just delete children with a single statement
              SqlUpdate sqlDelete = many.deleteByParentId(id, idList);
              executeSqlUpdate(sqlDelete, t);
            } else {
              // we need to fetch the Id's to delete (recurse or notify L2 cache)
              List<Object> childIds = many.findIdsByParentId(id, idList, t, null);
              if (!childIds.isEmpty()) {
                delete(targetDesc, null, childIds, t, deleteMode);
              }
            }
          }
        }
      }
    }

    if (deleteMode.isHard()) {
      // ManyToMany's ... delete from intersection table
      BeanPropertyAssocMany<?>[] manys = descriptor.propertiesManyToMany();
      for (BeanPropertyAssocMany<?> many : manys) {
        SqlUpdate sqlDelete = many.deleteByParentId(id, idList);
        if (t.isLogSummary()) {
          t.logSummary("-- Deleting intersection table entries: " + many.getFullBeanName());
        }
        executeSqlUpdate(sqlDelete, t);
      }
    }

    // delete the bean(s)
    SqlUpdate deleteById = descriptor.deleteById(id, idList, deleteMode);
    if (t.isLogSummary()) {
      if (idList != null) {
        t.logSummary("-- Deleting " + descriptor.getName() + " Ids: " + idList);
      } else {
        t.logSummary("-- Deleting " + descriptor.getName() + " Id: " + id);
      }
    }

    // use Id's to update L2 cache rather than Bulk table event
    notifyDeleteById(descriptor, id, idList, transaction);
    deleteById.setAutoTableMod(false);
    if (idList != null) {
      t.getEvent().addDeleteByIdList(descriptor, idList);
    } else {
      t.getEvent().addDeleteById(descriptor, id);
    }
    int rows = executeSqlUpdate(deleteById, t);

    // Delete from the persistence context so that it can't be fetched again later
    PersistenceContext persistenceContext = t.getPersistenceContext();
    if (idList != null) {
      for (Object idValue : idList) {
        descriptor.contextDeleted(persistenceContext, idValue);
      }
    } else {
      descriptor.contextDeleted(persistenceContext, id);
    }
    return rows;
  }

  private void notifyDeleteById(BeanDescriptor<?> descriptor, Object id, List<Object> idList, Transaction transaction) {

    BeanPersistController controller = descriptor.getPersistController();
    if (controller != null) {
      DeleteIdRequest request = new DeleteIdRequest(server, transaction, id);
      if (idList == null) {
        controller.preDelete(request);
      } else {
        for (Object idValue : idList) {
          request.setId(idValue);
          controller.preDelete(request);
        }
      }
    }
  }

  /**
   * We need to create and execute a query to get the foreign key values as
   * the delete cascades to them (foreign keys).
   */
  private Query<?> deleteRequiresQuery(BeanDescriptor<?> desc, BeanPropertyAssocOne<?>[] propImportDelete, DeleteMode deleteMode) {

    Query<?> q = server.createQuery(desc.getBeanType());
    StringBuilder sb = new StringBuilder(30);
    for (BeanPropertyAssocOne<?> aPropImportDelete : propImportDelete) {
      sb.append(aPropImportDelete.getName()).append(",");
    }
    q.setAutoTune(false);
    q.select(sb.toString());
    if (deleteMode.isHard() && desc.isSoftDelete()) {
      // hard delete so we want this query to include logically deleted rows (if any)
      q.setIncludeSoftDeletes();
    }
    return q;
  }

  /**
   * Delete the bean.
   * <p>
   * Note that preDelete fires before the deletion of children.
   * </p>
   */
  private int delete(PersistRequestBean<?> request) {

    DeleteUnloadedForeignKeys unloadedForeignKeys = null;

    if (request.isPersistCascade()) {
      // delete children first ... register the
      // bean to handle bi-directional cascading
      request.registerDeleteBean();
      deleteAssocMany(request);
      request.unregisterDeleteBean();

      unloadedForeignKeys = getDeleteUnloadedForeignKeys(request);
      if (unloadedForeignKeys != null) {
        // there are foreign keys that we don't have on this partially
        // populated bean so we actually need to query them (to cascade delete)
        unloadedForeignKeys.queryForeignKeys();
      }
    }

    int count = request.executeOrQueue();

    if (request.isPersistCascade()) {
      deleteAssocOne(request);

      if (unloadedForeignKeys != null) {
        unloadedForeignKeys.deleteCascade();
      }
    }

    // return true if using JDBC batch (as we can't tell until the batch is flushed)
    return count;
  }

  /**
   * Save the associated child beans contained in a List.
   * <p>
   * This will automatically copy over any join properties from the parent
   * bean to the child beans.
   * </p>
   */
  private void saveAssocMany(PersistRequestBean<?> request) {

    EntityBean parentBean = request.getEntityBean();
    BeanDescriptor<?> desc = request.getBeanDescriptor();
    SpiTransaction t = request.getTransaction();

    // exported ones with cascade save
    for (BeanPropertyAssocOne<?> prop : desc.propertiesOneExportedSave()) {
      // check for partial beans
      if (request.isLoadedProperty(prop)) {
        EntityBean detailBean = prop.getValueAsEntityBean(parentBean);
        if (detailBean != null) {
          if (!prop.isSaveRecurseSkippable(detailBean)) {
            t.depth(+1);
            prop.setParentBeanToChild(parentBean, detailBean);
            saveRecurse(detailBean, t, parentBean, request.getFlags());
            t.depth(-1);
          }
        }
      }
    }

    // many's with cascade save
    boolean insertedParent = request.isInsertedParent();
    for (BeanPropertyAssocMany<?> many : desc.propertiesManySave()) {
      // check that property is loaded and collection should be cascaded to
      if (request.isLoadedProperty(many) && !many.isSkipSaveBeanCollection(parentBean, insertedParent)) {
        saveMany(insertedParent, many, parentBean, request);
        if (!insertedParent) {
          request.addUpdatedManyProperty(many);
        }
      }
    }
  }

  private void saveMany(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    saveManyRequest(insertedParent, many, parentBean, request).save();
  }

  private SaveManyBase saveManyRequest(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    if (!many.isElementCollection()) {
      return new SaveManyBeans(insertedParent, many, parentBean, request, this);

    } else if (many.getManyType().isMap()) {
      return new SaveManyElementCollectionMap(insertedParent, many, parentBean, request);

    } else {
      return new SaveManyElementCollection(insertedParent, many, parentBean, request);
    }
  }

  void deleteAssocManyIntersection(EntityBean bean, BeanPropertyAssocMany<?> many, Transaction t, boolean publish) {

    // delete all intersection rows for this bean
    IntersectionRow intRow = many.buildManyToManyDeleteChildren(bean, publish);
    SqlUpdate sqlDelete = intRow.createDeleteChildren(server);
    executeSqlUpdate(sqlDelete, t);
  }

  /**
   * Delete beans in any associated many.
   * <p>
   * This is called prior to deleting the parent bean.
   * </p>
   */
  private void deleteAssocMany(PersistRequestBean<?> request) {

    SpiTransaction t = request.getTransaction();
    t.depth(-1);

    BeanDescriptor<?> desc = request.getBeanDescriptor();
    EntityBean parentBean = request.getEntityBean();
    DeleteMode deleteMode = request.deleteMode();

    BeanPropertyAssocOne<?>[] expOnes = desc.propertiesOneExportedDelete();
    if (expOnes.length > 0) {

      DeleteUnloadedForeignKeys unloaded = null;
      for (BeanPropertyAssocOne<?> prop : expOnes) {
        // for soft delete check cascade type also supports soft delete
        if (deleteMode.isHard() || prop.isTargetSoftDelete()) {
          if (request.isLoadedProperty(prop)) {
            Object detailBean = prop.getValue(parentBean);
            if (detailBean != null) {
              deleteRecurse((EntityBean) detailBean, t, deleteMode);
            }
          } else {
            if (unloaded == null) {
              unloaded = new DeleteUnloadedForeignKeys(server, request);
            }
            unloaded.add(prop);
          }
        }
      }
      if (unloaded != null) {
        unloaded.queryForeignKeys();
        unloaded.deleteCascade();
      }
    }

    // Many's with delete cascade
    for (BeanPropertyAssocMany<?> many : desc.propertiesManyDelete()) {
      if (many.hasJoinTable()) {
        if (deleteMode.isHard()) {
          // delete associated rows from intersection table (but not during soft delete)
          deleteAssocManyIntersection(parentBean, many, t, request.isPublish());
        }
      } else {

        if (ModifyListenMode.REMOVALS == many.getModifyListenMode()) {
          // PrivateOwned ...
          // if soft delete then check target also supports soft delete
          if (deleteMode.isHard() || many.isTargetSoftDelete()) {
            Object details = many.getValue(parentBean);
            if (details instanceof BeanCollection<?>) {
              Set<?> modifyRemovals = ((BeanCollection<?>) details).getModifyRemovals();
              if (modifyRemovals != null && !modifyRemovals.isEmpty()) {

                // delete the orphans that have been removed from the collection
                for (Object detail : modifyRemovals) {
                  EntityBean detailBean = (EntityBean) detail;
                  if (many.hasId(detailBean)) {
                    deleteRecurse(detailBean, t, deleteMode);
                  }
                }
              }
            }
          }
        }

        deleteManyDetails(t, desc, parentBean, many, null, deleteMode);
      }
    }

    // restore the depth
    t.depth(+1);
  }

  /**
   * Delete the 'many' detail beans for a given parent bean.
   * <p>
   * For stateless updates this deletes details beans that are no longer in
   * the many - the excludeDetailIds holds the detail beans that are in the
   * collection (and should not be deleted).
   * </p>
   */
  void deleteManyDetails(SpiTransaction t, BeanDescriptor<?> desc, EntityBean parentBean,
                         BeanPropertyAssocMany<?> many, List<Object> excludeDetailIds, DeleteMode deleteMode) {

    if (many.getCascadeInfo().isDelete()) {
      // cascade delete the beans in the collection
      BeanDescriptor<?> targetDesc = many.getTargetDescriptor();
      if (deleteMode.isHard() || targetDesc.isSoftDelete()) {
        if (targetDesc.isDeleteByStatement()) {
          // Just delete all the children with one statement
          IntersectionRow intRow = many.buildManyDeleteChildren(parentBean, excludeDetailIds);
          SqlUpdate sqlDelete = intRow.createDelete(server, deleteMode);
          executeSqlUpdate(sqlDelete, t);

        } else {
          // Delete recurse using the Id values of the children
          Object parentId = desc.getId(parentBean);
          List<Object> idsByParentId = many.findIdsByParentId(parentId, null, t, excludeDetailIds);
          if (!idsByParentId.isEmpty()) {
            deleteChildrenById(t, targetDesc, idsByParentId, deleteMode);
          }
        }
      }
    }
  }

  /**
   * Cascade delete child entities by Id.
   * <p>
   * Will use delete by object if the child entity has manyToMany relationships.
   */
  private void deleteChildrenById(SpiTransaction t, BeanDescriptor<?> targetDesc, List<Object> childIds, DeleteMode deleteMode) {

    if (!targetDesc.isDeleteByBulk()) {
      // convert into a list of reference objects and perform delete by object
      List<Object> refList = new ArrayList<>(childIds.size());
      for (Object id : childIds) {
        refList.add(targetDesc.createReference(id, null));
      }
      deleteList(refList, t, deleteMode, true);

    } else {
      // perform delete by statement if possible
      delete(targetDesc, null, childIds, t, deleteMode);
    }
  }

  /**
   * Save any associated one beans.
   */
  private void saveAssocOne(PersistRequestBean<?> request) {

    BeanDescriptor<?> desc = request.getBeanDescriptor();

    // imported ones with save cascade
    for (BeanPropertyAssocOne<?> prop : desc.propertiesOneImportedSave()) {
      // check for partial objects
      if (request.isLoadedProperty(prop)) {
        EntityBean detailBean = prop.getValueAsEntityBean(request.getEntityBean());
        if (detailBean != null
          && !prop.isSaveRecurseSkippable(detailBean)
          && !prop.isReference(detailBean)
          && !request.isParent(detailBean)) {
          SpiTransaction t = request.getTransaction();
          t.depth(-1);
          saveRecurse(detailBean, t, null, request.getFlags());
          t.depth(+1);
        }
      }
    }

    for (BeanPropertyAssocOne<?> prop : desc.propertiesOneExportedSave()) {
      if (prop.isOrphanRemoval() && request.isDirtyProperty(prop)) {
        Object origValue = request.getOrigValue(prop);
        if (origValue instanceof EntityBean) {
          delete((EntityBean) origValue, request.getTransaction(), true);
        }
      }
    }
  }

  /**
   * Support for loading any Imported Associated One properties that are not
   * loaded but required for Delete cascade.
   */
  private DeleteUnloadedForeignKeys getDeleteUnloadedForeignKeys(PersistRequestBean<?> request) {

    DeleteUnloadedForeignKeys fkeys = null;

    for (BeanPropertyAssocOne<?> one : request.getBeanDescriptor().propertiesOneImportedDelete()) {
      if (!request.isLoadedProperty(one)) {
        // we have cascade Delete on a partially populated bean and
        // this property was not loaded (so we are going to have to fetch it)
        if (fkeys == null) {
          fkeys = new DeleteUnloadedForeignKeys(server, request);
        }
        fkeys.add(one);
      }
    }

    return fkeys;
  }

  /**
   * Delete any associated one beans.
   */
  private void deleteAssocOne(PersistRequestBean<?> request) {

    DeleteMode deleteMode = request.deleteMode();

    for (BeanPropertyAssocOne<?> prop : request.getBeanDescriptor().propertiesOneImportedDelete()) {
      if (deleteMode.isHard() || prop.isTargetSoftDelete()) {
        if (request.isLoadedProperty(prop)) {
          Object detailBean = prop.getValue(request.getEntityBean());
          if (detailBean != null) {
            EntityBean detail = (EntityBean) detailBean;
            if (prop.hasId(detail)) {
              deleteRecurse(detail, request.getTransaction(), deleteMode);
            }
          }
        }
      }
    }
  }

  /**
   * Set Id Generated value for insert.
   */
  private void setIdGenValue(PersistRequestBean<?> request) {

    BeanDescriptor<?> desc = request.getBeanDescriptor();
    if (!desc.isUseIdGenerator()) {
      return;
    }

    BeanProperty idProp = desc.getIdProperty();
    if (idProp == null || idProp.isEmbedded()) {
      // not supporting IdGeneration for concatenated or Embedded
      return;
    }

    EntityBean bean = request.getEntityBean();
    Object uid = idProp.getValue(bean);

    if (DmlUtil.isNullOrZero(uid)) {

      // generate the nextId and set it to the property
      Object nextId = desc.nextId(request.getTransaction());

      // cast the data type if required and set it
      desc.convertSetId(nextId, bean);
    }
  }

  /**
   * Create the Persist Request Object that wraps all the objects used to
   * perform an insert, update or delete.
   */
  private <T> PersistRequestBean<T> createRequest(T bean, Transaction t, PersistRequest.Type type) {
    return createRequestInternal(bean, t, type, Flags.ZERO);
  }

  /**
   * Create the Persist Request Object additionally specifying the publish status.
   */
  <T> PersistRequestBean<T> createPublishRequest(T bean, Transaction t, PersistRequest.Type type, int flags) {
    return createRequestInternal(bean, t, type, Flags.unsetRecuse(flags));
  }

  /**
   * Create the Persist Request Object additionally specifying the publish status.
   */
  private <T> PersistRequestBean<T> createRequestInternal(T bean, Transaction t, PersistRequest.Type type, int flags) {
    BeanManager<T> mgr = getBeanManager(bean);
    if (mgr == null) {
      throw new PersistenceException(errNotRegistered(bean.getClass()));
    }
    return createRequest(bean, t, null, mgr, type, flags);
  }

  /**
   * Create an Insert or Update PersistRequestBean when cascading.
   * <p>
   * This call determines the PersistRequest.Type based on bean state and the insert flag (root persist type).
   */
  private <T> PersistRequestBean<T> createRequestRecurse(T bean, Transaction t, Object parentBean, int flags) {
    BeanManager<T> mgr = getBeanManager(bean);
    if (mgr == null) {
      throw new PersistenceException(errNotRegistered(bean.getClass()));
    }
    BeanDescriptor<T> desc = mgr.getBeanDescriptor();
    EntityBean entityBean = (EntityBean) bean;
    PersistRequest.Type type;
    if (Flags.isPublishMergeOrNormal(flags)) {
      // just use bean state to determine insert or update
      type = entityBean._ebean_getIntercept().isUpdate() ? Type.UPDATE : Type.INSERT;
    } else {
      // determine Insert or Update based on bean state and insert flag
      boolean insertMode = Flags.isInsert(flags);
      type = desc.isInsertMode(entityBean._ebean_getIntercept(), insertMode) ? Type.INSERT : Type.UPDATE;
    }
    return createRequest(bean, t, parentBean, mgr, type, Flags.setRecurse(flags));
  }

  /**
   * Create the Persist Request Object that wraps all the objects used to
   * perform an insert, update or delete.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private <T> PersistRequestBean<T> createRequest(T bean, Transaction t, Object parentBean, BeanManager<?> mgr,
                                                  PersistRequest.Type type, int flags) {

    if (type == Type.DELETE_PERMANENT) {
      type = Type.DELETE;
    } else if (type == Type.DELETE && mgr.getBeanDescriptor().isSoftDelete()) {
      // automatically convert to soft delete for types that support it
      type = Type.DELETE_SOFT;
    }

    return new PersistRequestBean(server, bean, parentBean, mgr, (SpiTransaction) t, persistExecute, type, flags);
  }

  private String errNotRegistered(Class<?> beanClass) {
    String msg = "The type [" + beanClass + "] is not a registered entity?";
    msg += " If you don't explicitly list the entity classes to use Ebean will search for them in the classpath.";
    msg += " If the entity is in a Jar check the ebean.search.jars property in ebean.properties file or check ServerConfig.addJar().";
    return msg;
  }

  /**
   * Return the BeanDescriptor for a bean that is being persisted.
   * <p>
   * Note that this checks to see if the bean is a MapBean with a tableName.
   * If so it will return the table based BeanDescriptor.
   * </p>
   */
  @SuppressWarnings("unchecked")
  private <T> BeanManager<T> getBeanManager(T bean) {

    return (BeanManager<T>) beanDescriptorManager.getBeanManager(bean.getClass());
  }
}
