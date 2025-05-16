package io.ebeaninternal.server.persist;

import io.avaje.applog.AppLog;
import io.ebean.*;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.BeanPersistController;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.core.*;
import io.ebeaninternal.server.core.PersistRequest.Type;
import io.ebeaninternal.server.deploy.*;

import java.sql.SQLException;
import java.util.*;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

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

  private static final System.Logger PUB = AppLog.getLogger("io.ebean.PUB");
  private static final System.Logger log = CoreLog.internal;

  /**
   * Actually does the persisting work.
   */
  private final PersistExecute persistExecute;
  private final SpiEbeanServer server;
  private final BeanDescriptorManager beanDescriptorManager;
  private final int maxDeleteBatch;

  public DefaultPersister(SpiEbeanServer server, Binder binder, BeanDescriptorManager descMgr) {
    this.server = server;
    this.beanDescriptorManager = descMgr;
    this.persistExecute = new DefaultPersistExecute(binder, server.config().getPersistBatchSize());
    this.maxDeleteBatch = initMaxDeleteBatch(server.databasePlatform().maxInBinding());
  }

  /**
   * When delete batching is required limit the size.
   * e.g. SqlServer has a 2100 parameter limit, so delete max 2000 for it.
   */
  private int initMaxDeleteBatch(int maxInBinding) {
    return maxInBinding == 0 ? 1000 : Math.min(1000, maxInBinding);
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
    BeanManager<?> mgr = beanManager(ormUpdate.beanType());
    return executeOrQueue(new PersistRequestOrmUpdate(server, mgr, ormUpdate, (SpiTransaction) t, persistExecute));
  }

  private int executeOrQueue(PersistRequest request) {
    try {
      request.initTransIfRequired();
      int rc = request.executeOrQueue();
      request.commitTransIfRequired();
      return rc;
    } catch (Throwable e) {
      request.rollbackTransIfRequired();
      throw e;
    } finally {
      request.clearTransIfRequired();
    }
  }

  @Override
  public void addBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction) {
    new PersistRequestUpdateSql(server, sqlUpdate, transaction, persistExecute).addBatch();
  }

  @Override
  public int[] executeBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction) {
    BatchControl batchControl = transaction.batchControl();
    try {
      return batchControl.execute(sqlUpdate.getGeneratedSql(), sqlUpdate.isGetGeneratedKeys());
    } catch (SQLException e) {
      throw transaction.translate(e.getMessage(), e);
    }
  }

  @Override
  public void executeOrQueue(SpiSqlUpdate update, SpiTransaction t, boolean queue, int queuePosition) {
    if (queue) {
      addToFlushQueue(update, t, queuePosition);
    } else {
      executeSqlUpdate(update, t);
    }
  }

  /**
   * Add to the flush queue in position 0, 1 or 2.
   */
  @Override
  public void addToFlushQueue(SpiSqlUpdate update, SpiTransaction t, int pos) {
    new PersistRequestUpdateSql(server, update, t, persistExecute).addToFlushQueue(pos);
  }

  /**
   * Execute the updateSql.
   */
  @Override
  public int executeSqlUpdate(SqlUpdate updSql, Transaction t) {
    return executeOrQueue(new PersistRequestUpdateSql(server, (SpiSqlUpdate) updSql, (SpiTransaction) t, persistExecute));
  }

  @Override
  public int executeSqlUpdateNow(SpiSqlUpdate updSql, Transaction t) {
    return executeOrQueue(new PersistRequestUpdateSql(server, updSql, (SpiTransaction) t, persistExecute, true));
  }

  /**
   * Restore draft beans to match live beans given the query.
   */
  @Override
  public <T> List<T> draftRestore(Query<T> query, Transaction transaction) {
    Class<T> beanType = query.getBeanType();
    BeanDescriptor<T> desc = server.descriptor(beanType);
    DraftHandler<T> draftHandler = new DraftHandler<>(desc, transaction);

    query.usingTransaction(transaction);
    List<T> liveBeans = draftHandler.fetchSourceBeans((SpiQuery<T>) query, false);
    PUB.log(DEBUG, "draftRestore [{0}] count[{1}]", desc.name(), liveBeans.size());
    if (liveBeans.isEmpty()) {
      return Collections.emptyList();
    }

    draftHandler.fetchDestinationBeans(liveBeans, true);
    BeanManager<T> mgr = beanDescriptorManager.beanManager(beanType);

    for (T liveBean : liveBeans) {
      T draftBean = draftHandler.publishToDestinationBean(liveBean);
      // reset @DraftDirty and @DraftReset properties
      draftHandler.resetDraft(draftBean);

      PUB.log(TRACE, "draftRestore bean [{0}] id[{1}]", desc.name(), draftHandler.getId());
      update(createRequest(draftBean, transaction, null, mgr, Type.UPDATE, Flags.RECURSE));
    }

    PUB.log(DEBUG, "draftRestore - complete for [{0}]", desc.name());
    return draftHandler.getDrafts();
  }

  /**
   * Helper method to return the list of Id values for the list of beans.
   */
  private <T> List<Object> getBeanIds(BeanDescriptor<T> desc, List<T> beans) {
    List<Object> idList = new ArrayList<>(beans.size());
    for (T liveBean : beans) {
      idList.add(desc.id(liveBean));
    }
    return idList;
  }

  /**
   * Publish from draft to live given the query.
   */
  @Override
  public <T> List<T> publish(Query<T> query, Transaction transaction) {
    Class<T> beanType = query.getBeanType();
    BeanDescriptor<T> desc = server.descriptor(beanType);
    DraftHandler<T> draftHandler = new DraftHandler<>(desc, transaction);

    query.usingTransaction(transaction);
    List<T> draftBeans = draftHandler.fetchSourceBeans((SpiQuery<T>) query, true);
    PUB.log(DEBUG, "publish [{0}] count[{1}]", desc.name(), draftBeans.size());
    if (draftBeans.isEmpty()) {
      return Collections.emptyList();
    }

    draftHandler.fetchDestinationBeans(draftBeans, false);
    BeanManager<T> mgr = beanDescriptorManager.beanManager(beanType);

    List<T> livePublish = new ArrayList<>(draftBeans.size());
    for (T draftBean : draftBeans) {
      T liveBean = draftHandler.publishToDestinationBean(draftBean);
      livePublish.add(liveBean);

      // reset @DraftDirty and @DraftReset properties
      draftHandler.resetDraft(draftBean);

      Type persistType = draftHandler.isInsert() ? Type.INSERT : Type.UPDATE;
      PUB.log(TRACE, "publish bean [{0}] id[{1}] type[{2}]", desc.name(), draftHandler.getId(), persistType);

      PersistRequestBean<T> request = createRequest(liveBean, transaction, null, mgr, persistType, Flags.PUBLISH_RECURSE);
      if (persistType == Type.INSERT) {
        insert(request);
      } else {
        update(request);
      }
      request.resetDepth();
    }

    draftHandler.updateDrafts(transaction, mgr);
    PUB.log(DEBUG, "publish - complete for [{0}]", desc.name());
    return livePublish;
  }

  /**
   * Helper to handle draft beans (properties reset etc).
   */
  class DraftHandler<T> {

    final BeanDescriptor<T> desc;
    final Transaction transaction;
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
        PUB.log(DEBUG, "publish - update dirty status on [{0}] drafts", draftUpdates.size());
        for (T draftUpdate : draftUpdates) {
          update(createRequest(draftUpdate, transaction, null, mgr, Type.UPDATE, Flags.ZERO));
        }
      }
    }

    /**
     * Fetch the source beans based on the query.
     */
    List<T> fetchSourceBeans(SpiQuery<T> query, boolean asDraft) {
      desc.draftQueryOptimise(query);
      if (asDraft) {
        query.asDraft();
      }
      return server.findList(query);
    }

    /**
     * Fetch the destination beans that will be published to.
     */
    void fetchDestinationBeans(List<T> sourceBeans, boolean asDraft) {
      List<Object> ids = getBeanIds(desc, sourceBeans);
      SpiQuery<T> destQuery = server.createQuery(desc.type());
      destQuery.usingTransaction(transaction);
      destQuery.where().idIn(ids);
      if (asDraft) {
        destQuery.asDraft();
      }
      desc.draftQueryOptimise(destQuery);
      this.destBeans = server.findMap(destQuery);
    }

    /**
     * Publish/restore the values from the sourceBean to the matching destination bean.
     */
    T publishToDestinationBean(T sourceBean) {
      id = desc.id(sourceBean);
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
    return deleteRequest(createDeleteCascade(detailBean, t, deleteMode.persistType()));
  }

  /**
   * Delete without being a cascade.
   */
  private int delete(EntityBean detailBean, Transaction t, DeleteMode deleteMode) {
    return deleteRequest(createDeleteRequest(detailBean, t, deleteMode.persistType()));
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
    PersistRequestBean<?> req = createRequest(entityBean, t, PersistRequest.Type.UPDATE);
    req.checkDraft();
    try {
      req.initTransIfRequiredWithBatchCascade();
      if (req.isReference()) {
        // its a reference so see if there are manys to save...
        if (req.isPersistCascade()) {
          saveAssocMany(req);
        }
        req.completeUpdate();
      } else {
        update(req);
      }
      req.resetDepth();
      req.commitTransIfRequired();
      req.flushBatchOnCascade();
    } catch (Throwable ex) {
      req.rollbackTransIfRequired();
      throw ex;
    } finally {
      req.clearTransIfRequired();
    }
  }

  /**
   * Insert or update the bean.
   */
  @Override
  public void save(EntityBean bean, Transaction t) {
    if (bean._ebean_getIntercept().isUpdate()) {
      update(bean, t);
    } else {
      insert(bean, null, t);
    }
  }

  /**
   * Insert this bean.
   */
  @Override
  public void insert(EntityBean bean, InsertOptions insertOptions, Transaction t) {
    PersistRequestBean<?> req = createRequest(bean, t, PersistRequest.Type.INSERT);
    if (req.isSkipReference()) {
      // skip insert on reference bean
      return;
    }
    if (insertOptions != null) {
      req.setInsertOptions(insertOptions);
    }
    try {
      req.initTransIfRequiredWithBatchCascade();
      insert(req);
      req.resetDepth();
      req.commitTransIfRequired();
      req.flushBatchOnCascade();
    } catch (Throwable ex) {
      req.rollbackTransIfRequired();
      throw ex;
    } finally {
      req.clearTransIfRequired();
    }
  }

  void saveRecurse(EntityBean bean, Transaction t, Object parentBean, int flags) {
    // determine insert or update taking into account stateless updates
    saveRecurse(createRequestRecurse(bean, t, parentBean, flags));
  }

  private void saveRecurse(PersistRequestBean<?> request) {
    request.setSaveRecurse();
    if (request.isReference()) {
      // its a reference...
      if (request.isPersistCascade()) {
        // save any associated List held beans
        request.flagUpdate();
        saveAssocMany(request);
      }
      request.completeUpdate();
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
      request.executeOrQueue();
      if (request.isPersistCascade()) {
        // save any associated List held beans
        saveAssocMany(request);
      }
      request.complete();

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
      } else if (log.isLoggable(DEBUG)) {
        log.log(DEBUG, "Update skipped as bean is unchanged: {0}", request.bean());
      }
      if (request.isPersistCascade()) {
        // save all the beans in assocMany's after
        saveAssocMany(request);
      }
      request.completeUpdate();
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
    PersistRequestBean<EntityBean> originalRequest = createDeleteRequest(bean, t, deleteType);
    if (originalRequest.isHardDeleteDraft()) {
      // a hard delete of a draftable bean so first we need to  delete the associated 'live' bean
      // due to FK constraint and then after that execute the original delete of the draft bean
      return deleteRequest(createDeleteRequest(originalRequest.createReference(), t, Type.DELETE_PERMANENT, Flags.PUBLISH), originalRequest);

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
      if (log.isLoggable(DEBUG)) {
        log.log(DEBUG, "skipping delete on alreadyRegistered {0}", req.bean());
      }
      return 0;
    }
    try {
      req.initTransIfRequiredWithBatchCascade();
      int rows = delete(req);
      if (draftReq != null) {
        // delete the 'draft' bean ('live' bean deleted first)
        draftReq.setTrans(req.transaction());
        rows = delete(draftReq);
      }
      req.commitTransIfRequired();
      req.flushBatchOnCascade();
      return rows;

    } catch (Throwable ex) {
      req.rollbackTransIfRequired();
      throw ex;
    } finally {
      req.clearTransIfRequired();
    }
  }

  private void deleteCascade(List<?> beanList, SpiTransaction t, DeleteMode deleteMode, boolean children) {
    if (children) {
      t.depth(-1);
      t.checkBatchEscalationOnCollection();
    }
    for (Object bean : beanList) {
      deleteRecurse((EntityBean) bean, t, deleteMode);
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
    BeanDescriptor<?> descriptor = beanDescriptorManager.descriptor(beanType);
    DeleteMode deleteMode = (permanent || !descriptor.isSoftDelete()) ? DeleteMode.HARD : DeleteMode.SOFT;
    if (descriptor.isMultiTenant()) {
      return deleteAsBeans(ids, transaction, deleteMode, descriptor);
    }
    ArrayList<Object> idList = new ArrayList<>(ids.size());
    for (Object id : ids) {
      // convert to appropriate type if required
      idList.add(descriptor.convertId(id));
    }
    return delete(descriptor, idList, transaction, deleteMode);
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
      int rowCount = delete(bean, transaction, deleteMode);
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
    BeanDescriptor<?> descriptor = beanDescriptorManager.descriptor(beanType);
    if (descriptor.isMultiTenant()) {
      // convert to a delete by bean
      EntityBean bean = descriptor.createEntityBean();
      descriptor.convertSetId(id, bean);
      return delete(bean, transaction, permanent);
    }

    id = descriptor.convertId(id);
    DeleteMode deleteMode = (permanent || !descriptor.isSoftDelete()) ? DeleteMode.HARD : DeleteMode.SOFT;
    return delete(descriptor, id, transaction, deleteMode);
  }

  @Override
  public int deleteByIds(BeanDescriptor<?> descriptor, List<Object> idList, Transaction transaction, boolean permanent) {
    DeleteMode deleteMode = (permanent || !descriptor.isSoftDelete()) ? DeleteMode.HARD : DeleteMode.SOFT;
    return delete(descriptor, idList, transaction, deleteMode);
  }

  private int delete(BeanDescriptor<?> descriptor, List<Object> idList, Transaction transaction, DeleteMode deleteMode) {
    final int batch = maxDeleteBatch / descriptor.idBinder().size();
    int rows = 0;
    for (List<Object> batchOfIds : Lists.partition(idList, batch)) {
      rows += new DeleteBatchHelpMultiple(descriptor, batchOfIds, transaction, deleteMode).deleteBatch();
    }
    return rows;
  }

  /**
   * Delete by Id or a List of Id's.
   */
  private int delete(BeanDescriptor<?> descriptor, Object id, Transaction transaction, DeleteMode deleteMode) {
    return new DeleteBatchHelpSingle(descriptor, id, transaction, deleteMode).deleteBatch();
  }

  private abstract class DeleteBatchHelp {
    final BeanDescriptor<?> descriptor;
    final SpiTransaction transaction;
    final DeleteMode deleteMode;

    public DeleteBatchHelp(BeanDescriptor<?> descriptor, Transaction transaction, DeleteMode deleteMode) {
      this.descriptor = descriptor;
      this.transaction = (SpiTransaction) transaction;
      this.deleteMode = deleteMode;
    }

    int deleteBatch() {

      if (transaction.isPersistCascade()) {
        BeanPropertyAssocOne<?>[] propImportDelete = descriptor.propertiesOneImportedDelete();
        if (propImportDelete.length > 0) {
          return deleteImported(propImportDelete);
        }

        // OneToOne exported side with delete cascade
        BeanPropertyAssocOne<?>[] expOnes = descriptor.propertiesOneExportedDelete();
        for (BeanPropertyAssocOne<?> expOne : expOnes) {
          BeanDescriptor<?> targetDesc = expOne.targetDescriptor();
          // only cascade soft deletes when supported by target
          if (deleteMode.isHard() || targetDesc.isSoftDelete()) {
            if (deleteMode.isHard() && targetDesc.isDeleteByStatement()) {
              executeSqlUpdate(sqlDeleteChildren(expOne), transaction);
            } else {
              List<Object> childIds = findChildIds(expOne, deleteMode.isHard());
              if (childIds != null && !childIds.isEmpty()) {
                deleteChildrenById(transaction, targetDesc, childIds, deleteMode);
              }
            }
          }
        }

        // OneToMany's with delete cascade
        BeanPropertyAssocMany<?>[] manys = descriptor.propertiesManyDelete();
        for (BeanPropertyAssocMany<?> many : manys) {
          if (!many.isManyToMany()) {
            BeanDescriptor<?> targetDesc = many.targetDescriptor();
            // only cascade soft deletes when supported by target
            if (deleteMode.isHard() || targetDesc.isSoftDelete()) {
              if (deleteMode.isHard() && targetDesc.isDeleteByStatement()) {
                // we can just delete children with a single statement
                executeSqlUpdate(sqlDeleteChildren(many), transaction);
              } else {
                // we need to fetch the Id's to delete (recurse or notify L2 cache)
                List<Object> childIds = findChildIds(many, deleteMode.isHard());
                if (!childIds.isEmpty()) {
                  delete(targetDesc, childIds, transaction, deleteMode);
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
          if (transaction.isLogSummary()) {
            transaction.logSummary("-- Deleting intersection table entries: {0}", many.fullName());
          }
          executeSqlUpdate(sqlDeleteChildren(many), transaction);
        }
      }

      // delete the bean(s)
      return deleteBeans();
    }

    abstract int deleteImported(BeanPropertyAssocOne<?>[] propImportDelete);

    abstract SqlUpdate sqlDeleteChildren(BeanPropertyAssoc<?> prop);

    abstract List<Object> findChildIds(BeanPropertyAssoc<?> prop, boolean includeSoftDeletes);

    abstract int deleteBeans();
  }

  private class DeleteBatchHelpSingle extends DeleteBatchHelp {
    private final Object id;

    public DeleteBatchHelpSingle(BeanDescriptor<?> descriptor, Object id, Transaction transaction, DeleteMode deleteMode) {
      super(descriptor, transaction, deleteMode);
      this.id = id;
    }

    int deleteImported(BeanPropertyAssocOne<?>[] propImportDelete) {
      // We actually need to execute a query to get the foreign key values
      // as they are required for the delete cascade. Query back just the
      // Id and the appropriate foreign key values
      SpiQuery<?> q = deleteRequiresQuery(descriptor, propImportDelete, deleteMode);
      q.usingTransaction(transaction);
      q.where().idEq(id);
      if (transaction.isLogSummary()) {
        transaction.logSummary("-- DeleteById of {0} id[{1}] requires fetch of foreign key values", descriptor.name(), id);
      }
      EntityBean bean = (EntityBean) server.findOne(q);
      if (bean == null) {
        return 0;
      } else {
        return deleteRecurse(bean, transaction, deleteMode);
      }
    }

    @Override
    SqlUpdate sqlDeleteChildren(BeanPropertyAssoc<?> prop) {
      return prop.deleteByParentId(id);
    }

    @Override
    List<Object> findChildIds(BeanPropertyAssoc<?> prop, boolean includeSoftDeletes) {
      return prop.findIdsByParentId(id, transaction, includeSoftDeletes);
    }

    int deleteBeans() {
      SqlUpdate deleteById = descriptor.deleteById(id, null, deleteMode);
      if (transaction.isLogSummary()) {
        transaction.logSummary("-- Deleting {0} Id: {1}", descriptor.name(), id);
      }

      // use Id's to update L2 cache rather than Bulk table event
      notifyDeleteById(descriptor, id, null, transaction);
      deleteById.setAutoTableMod(false);
      transaction.event().addDeleteById(descriptor, id);

      int rows = executeSqlUpdate(deleteById, transaction);

      // Delete from the persistence context so that it can't be fetched again later
      PersistenceContext persistenceContext = transaction.persistenceContext();
      descriptor.contextDeleted(persistenceContext, id);
      return rows;
    }
  }

  private class DeleteBatchHelpMultiple extends DeleteBatchHelp {
    private final List<Object> idList;

    public DeleteBatchHelpMultiple(BeanDescriptor<?> descriptor, List<Object> idList, Transaction transaction, DeleteMode deleteMode) {
      super(descriptor, transaction, deleteMode);
      this.idList = idList;
    }

    int deleteImported(BeanPropertyAssocOne<?>[] propImportDelete) {
      // We actually need to execute a query to get the foreign key values
      // as they are required for the delete cascade. Query back just the
      // Id and the appropriate foreign key values
      SpiQuery<?> q = deleteRequiresQuery(descriptor, propImportDelete, deleteMode);
      q.usingTransaction(transaction);
      q.where().idIn(idList);
      if (transaction.isLogSummary()) {
        transaction.logSummary("-- DeleteById of {0} ids[{1}] requires fetch of foreign key values", descriptor.name(), idList);
      }
      List<?> beanList = server.findList(q);
      deleteCascade(beanList, transaction, deleteMode, false);
      return beanList.size();
    }

    SqlUpdate sqlDeleteChildren(BeanPropertyAssoc<?> prop) {
      return prop.deleteByParentIdList(idList);
    }

    @Override
    List<Object> findChildIds(BeanPropertyAssoc<?> prop, boolean includeSoftDeletes) {
      return prop.findIdsByParentIdList(idList, transaction, includeSoftDeletes);
    }

    int deleteBeans() {
      SqlUpdate deleteById = descriptor.deleteById(null, idList, deleteMode);
      if (transaction.isLogSummary()) {
        transaction.logSummary("-- Deleting {0} Ids: {1}", descriptor.name(), idList);
      }

      // use Id's to update L2 cache rather than Bulk table event
      notifyDeleteById(descriptor, null, idList, transaction);
      deleteById.setAutoTableMod(false);
      transaction.event().addDeleteByIdList(descriptor, idList);

      int rows = executeSqlUpdate(deleteById, transaction);

      // Delete from the persistence context so that it can't be fetched again later
      PersistenceContext persistenceContext = transaction.persistenceContext();
      for (Object idValue : idList) {
        descriptor.contextDeleted(persistenceContext, idValue);
      }
      return rows;
    }
  }

  private void notifyDeleteById(BeanDescriptor<?> descriptor, Object id, List<Object> idList, Transaction transaction) {
    BeanPersistController controller = descriptor.persistController();
    if (controller != null) {
      DeleteIdsRequest request;
      if (idList == null) {
        request = new DeleteIdsRequest(server, transaction, descriptor.type(), List.of(id));
      } else {
        request = new DeleteIdsRequest(server, transaction, descriptor.type(), Collections.unmodifiableList(idList));
      }
      controller.preDelete(request);
    }
  }

  /**
   * We need to create and execute a query to get the foreign key values as
   * the delete cascades to them (foreign keys).
   */
  private SpiQuery<?> deleteRequiresQuery(BeanDescriptor<?> desc, BeanPropertyAssocOne<?>[] propImportDelete, DeleteMode deleteMode) {
    SpiQuery<?> q = server.createQuery(desc.type());
    StringBuilder sb = new StringBuilder(30);
    for (BeanPropertyAssocOne<?> aPropImportDelete : propImportDelete) {
      sb.append(aPropImportDelete.name()).append(',');
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

      unloadedForeignKeys = getDeleteUnloadedForeignKeys(request);
      if (unloadedForeignKeys != null) {
        // there are foreign keys that we don't have on this partially
        // populated bean so we actually need to query them (to cascade delete)
        unloadedForeignKeys.queryForeignKeys();
      }
    }

    int count = request.executeOrQueue();
    request.removeFromPersistenceContext();
    if (request.isPersistCascade()) {
      deleteAssocOne(request);
      if (unloadedForeignKeys != null) {
        unloadedForeignKeys.deleteCascade();
      }
    }
    request.complete();
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
    EntityBean parentBean = request.entityBean();
    BeanDescriptor<?> desc = request.descriptor();
    SpiTransaction t = request.transaction();
    EntityBean orphanForRemoval = request.importedOrphanForRemoval();
    if (orphanForRemoval != null) {
      delete(orphanForRemoval, request.transaction(), false);
    }

    // exported ones with cascade save
    for (BeanPropertyAssocOne<?> prop : desc.propertiesOneExportedSave()) {
      // check for partial beans
      if (request.isLoadedProperty(prop)) {
        EntityBean detailBean = prop.valueAsEntityBean(parentBean);
        if (detailBean != null) {
          if (!prop.isSaveRecurseSkippable(detailBean)) {
            t.depth(+1);
            prop.setParentBeanToChild(parentBean, detailBean);
            saveRecurse(detailBean, t, parentBean, request.flags());
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
      }
    }
  }

  private void saveMany(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    saveManyRequest(insertedParent, many, parentBean, request).save();
  }

  private SaveManyBase saveManyRequest(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    if (!many.isElementCollection()) {
      return new SaveManyBeans(this, insertedParent, many, parentBean, request);
    } else if (many.manyType().isMap()) {
      return new SaveManyElementCollectionMap(this, insertedParent, many, parentBean, request);
    } else {
      return new SaveManyElementCollection(this, insertedParent, many, parentBean, request);
    }
  }

  void deleteManyIntersection(EntityBean bean, BeanPropertyAssocMany<?> many, SpiTransaction t, boolean publish, boolean queue) {
    SpiSqlUpdate sqlDelete = deleteAllIntersection(bean, many, publish);
    if (queue) {
      addToFlushQueue(sqlDelete, t, BatchControl.DELETE_QUEUE);
    } else {
      executeSqlUpdate(sqlDelete, t);
    }
  }

  private SpiSqlUpdate deleteAllIntersection(EntityBean bean, BeanPropertyAssocMany<?> many, boolean publish) {
    IntersectionRow intRow = many.buildManyToManyDeleteChildren(bean, publish);
    return intRow.createDeleteChildren(server, many.extraWhere());
  }

  /**
   * Delete beans in any associated many.
   * <p>
   * This is called prior to deleting the parent bean.
   * </p>
   */
  private void deleteAssocMany(PersistRequestBean<?> request) {
    SpiTransaction t = request.transaction();
    t.depth(-1);

    BeanDescriptor<?> desc = request.descriptor();
    EntityBean parentBean = request.entityBean();
    DeleteMode deleteMode = request.deleteMode();

    BeanPropertyAssocOne<?>[] expOnes = desc.propertiesOneExportedDelete();
    if (expOnes.length > 0) {

      DeleteUnloadedForeignKeys unloaded = null;
      for (BeanPropertyAssocOne<?> prop : expOnes) {
        // for soft delete check cascade type also supports soft delete
        if (deleteMode.isHard() || prop.isTargetSoftDelete()) {
          if (prop.isPrimaryKeyExport()) {
            // we can delete by id, neither if property loaded or not
            delete(prop.targetDescriptor(), prop.descriptor().id(parentBean), t, deleteMode);
          } else if (request.isLoadedProperty(prop)) {
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
          deleteManyIntersection(parentBean, many, t, request.isPublish(), false);
        }
      } else {
        if (ModifyListenMode.REMOVALS == many.modifyListenMode()) {
          // PrivateOwned ...
          // if soft delete then check target also supports soft delete
          if (deleteMode.isHard() || many.isTargetSoftDelete()) {
            Object details = many.getValue(parentBean);
            if (details instanceof BeanCollection<?>) {
              Set<?> modifyRemovals = ((BeanCollection<?>) details).modifyRemovals();
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
   */
  void deleteManyDetails(SpiTransaction t, BeanDescriptor<?> desc, EntityBean parentBean,
                         BeanPropertyAssocMany<?> many, Set<Object> excludeDetailIds, DeleteMode deleteMode) {
    if (many.cascadeInfo().isDelete()) {
      // cascade delete the beans in the collection
      final BeanDescriptor<?> targetDesc = many.targetDescriptor();
      final int batch = maxDeleteBatch / targetDesc.idBinder().size();
      if (deleteMode.isHard() || targetDesc.isSoftDelete()) {
        if (targetDesc.isDeleteByStatement()
          && (excludeDetailIds == null || excludeDetailIds.size() <= batch)) {
          // Just delete all the children with one statement
          IntersectionRow intRow = many.buildManyDeleteChildren(parentBean, excludeDetailIds);
          SqlUpdate sqlDelete = intRow.createDelete(server, deleteMode, many.extraWhere());
          executeSqlUpdate(sqlDelete, t);

        } else {
          // Delete recurse using the Id values of the children
          Object parentId = desc.getId(parentBean);
          List<Object> idsByParentId;
          if (excludeDetailIds == null || excludeDetailIds.size() <= batch) {
            idsByParentId = many.findIdsByParentId(parentId, t, deleteMode.isHard(), excludeDetailIds);
          } else {
            // if we hit the parameter limit, we must filter that on the java side
            // as there is no easy way to batch "not in" queries.
            idsByParentId = many.findIdsByParentId(parentId, t, deleteMode.isHard(), null);
            idsByParentId.removeIf(excludeDetailIds::contains);
          }
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
      deleteCascade(refList, t, deleteMode, true);
    } else {
      // perform delete by statement if possible
      delete(targetDesc, childIds, t, deleteMode);
    }
  }

  /**
   * Save any associated one beans.
   */
  private void saveAssocOne(PersistRequestBean<?> request) {
    BeanDescriptor<?> desc = request.descriptor();
    // imported ones with save cascade
    for (BeanPropertyAssocOne<?> prop : desc.propertiesOneImportedSave()) {
      // check for partial objects
      if (prop.isOrphanRemoval() && request.isDirtyProperty(prop)) {
        request.setImportedOrphanForRemoval(prop);
      }
      if (request.isLoadedProperty(prop)) {
        EntityBean detailBean = prop.valueAsEntityBean(request.entityBean());
        if (detailBean != null
          && !prop.isSaveRecurseSkippable(detailBean)
          && !prop.isReference(detailBean)
          && !request.isParent(detailBean)) {
          SpiTransaction t = request.transaction();
          t.depthDecrement();
          saveRecurse(detailBean, t, null, request.flags());
          t.depth(+1);
        }
      }
    }
    for (BeanPropertyAssocOne<?> prop : desc.propertiesOneExportedSave()) {
      if (prop.isOrphanRemoval() && request.isDirtyProperty(prop)) {
        deleteOrphan(request, prop);
      }
    }
  }

  private void deleteOrphan(PersistRequestBean<?> request, BeanPropertyAssocOne<?> prop) {
    Object origValue = request.origValue(prop);
    if (origValue instanceof EntityBean) {
      delete((EntityBean) origValue, request.transaction(), false);
    }
  }

  /**
   * Support for loading any Imported Associated One properties that are not
   * loaded but required for Delete cascade.
   */
  private DeleteUnloadedForeignKeys getDeleteUnloadedForeignKeys(PersistRequestBean<?> request) {
    DeleteUnloadedForeignKeys fkeys = null;
    for (BeanPropertyAssocOne<?> one : request.descriptor().propertiesOneImportedDelete()) {
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
    for (BeanPropertyAssocOne<?> prop : request.descriptor().propertiesOneImportedDelete()) {
      if (deleteMode.isHard() || prop.isTargetSoftDelete()) {
        if (request.isLoadedProperty(prop)) {
          Object detailBean = prop.getValue(request.entityBean());
          if (detailBean != null) {
            EntityBean detail = (EntityBean) detailBean;
            if (prop.hasId(detail)) {
              deleteRecurse(detail, request.transaction(), deleteMode);
            }
          }
        }
      }
    }
  }

  /**
   * Create the Persist Request Object that wraps all the objects used to
   * perform an insert, update or delete.
   */
  private <T> PersistRequestBean<T> createRequest(T bean, Transaction t, PersistRequest.Type type) {
    return createRequestInternal(bean, t, type);
  }

  /**
   * Create the Persist Request Object additionally specifying the publish status.
   */
  private <T> PersistRequestBean<T> createRequestInternal(T bean, Transaction t, PersistRequest.Type type) {
    BeanManager<T> mgr = beanManager(bean.getClass());
    return createRequest(bean, t, null, mgr, type, Flags.ZERO);
  }

  /**
   * Create an Insert or Update PersistRequestBean when cascading.
   * <p>
   * This call determines the PersistRequest.Type based on bean state and the insert flag (root persist type).
   */
  private <T> PersistRequestBean<T> createRequestRecurse(T bean, Transaction t, Object parentBean, int flags) {
    BeanManager<T> mgr = beanManager(bean.getClass());
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
    // no delete requests come here
    return new PersistRequestBean(server, bean, parentBean, mgr, (SpiTransaction) t, persistExecute, type, flags);
  }

  <T> PersistRequestBean<T> createDeleteRemoved(T bean, Transaction t, int flags) {
    return createDeleteRequest(bean, t, PersistRequest.Type.DELETE, Flags.unsetRecurse(flags));
  }

  private <T> PersistRequestBean<T> createDeleteRequest(EntityBean bean, Transaction t, Type type) {
    return createDeleteRequest(bean, t, type, Flags.ZERO);
  }

  private <T> PersistRequestBean<T> createDeleteCascade(EntityBean bean, Transaction t, Type type) {
    return createDeleteRequest(bean, t, type, Flags.RECURSE);
  }

  @SuppressWarnings({"unchecked"})
  private <T> PersistRequestBean<T> createDeleteRequest(Object bean, Transaction t, PersistRequest.Type type, int flags) {
    BeanManager<T> mgr = beanManager(bean.getClass());
    if (type == Type.DELETE_PERMANENT) {
      type = Type.DELETE;
    } else if (type == Type.DELETE && mgr.getBeanDescriptor().isSoftDelete()) {
      // automatically convert to soft delete for types that support it
      type = Type.DELETE_SOFT;
    }

    PersistRequestBean<T> request = new PersistRequestBean<>(server, (T) bean, null, mgr, (SpiTransaction) t, persistExecute, type, flags);
    request.initForSoftDelete();
    return request;
  }

  /**
   * Return the BeanDescriptor for a bean that is being persisted.
   * <p>
   * Note that this checks to see if the bean is a MapBean with a tableName.
   * If so it will return the table based BeanDescriptor.
   * </p>
   */
  @SuppressWarnings("unchecked")
  private <T> BeanManager<T> beanManager(Class<?> cls) {
    return (BeanManager<T>) beanDescriptorManager.beanManager(cls);
  }
}
