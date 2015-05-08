package com.avaje.ebeaninternal.server.persist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.CallableSql;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.Update;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollection.ModifyListenMode;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdate;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestUpdateSql;
import com.avaje.ebeaninternal.server.core.Persister;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.core.PersistRequest.Type;
import com.avaje.ebeaninternal.server.deploy.BeanCollectionUtil;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.deploy.BeanManager;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.IntersectionRow;
import com.avaje.ebeaninternal.server.deploy.ManyType;

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
 * 
 * @see com.avaje.ebeaninternal.server.persist.DefaultPersistExecute
 */
public final class DefaultPersister implements Persister {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPersister.class);

	/**
	 * Actually does the persisting work.
	 */
	private final PersistExecute persistExecute;

	private final SpiEbeanServer server;

	private final BeanDescriptorManager beanDescriptorManager;

  private final boolean updatesDeleteMissingChildren;

	public DefaultPersister(SpiEbeanServer server, Binder binder, BeanDescriptorManager descMgr, PstmtBatch pstmtBatch) {

		this.server = server;
		this.updatesDeleteMissingChildren = server.getServerConfig().isUpdatesDeleteMissingChildren();
		this.beanDescriptorManager = descMgr;
		this.persistExecute = new DefaultPersistExecute(binder, pstmtBatch, server.getServerConfig().getPersistBatchSize());
	}

	/**
	 * Execute the CallableSql.
	 */
	public int executeCallable(CallableSql callSql, Transaction t) {

		PersistRequestCallableSql request = new PersistRequestCallableSql(server, callSql, (SpiTransaction) t, persistExecute);
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

	/**
	 * Execute the orm update.
	 */
	public int executeOrmUpdate(Update<?> update, Transaction t) {

		SpiUpdate<?> ormUpdate = (SpiUpdate<?>) update;

		BeanManager<?> mgr = beanDescriptorManager.getBeanManager(ormUpdate.getBeanType());

		if (mgr == null) {
			String msg = "No BeanManager found for type [" + ormUpdate.getBeanType() + "]. Is it an entity?";
			throw new PersistenceException(msg);
		}

		PersistRequestOrmUpdate request = new PersistRequestOrmUpdate(server, mgr, ormUpdate, (SpiTransaction) t, persistExecute);
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

	/**
	 * Execute the updateSql.
	 */
	public int executeSqlUpdate(SqlUpdate updSql, Transaction t) {

		PersistRequestUpdateSql request = new PersistRequestUpdateSql(server, updSql, (SpiTransaction) t, persistExecute);
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

	/**
	 * Recursively delete the bean. This calls back to the EbeanServer.
	 */
	private void deleteRecurse(Object detailBean, Transaction t) {
		// NB: a new PersistRequest is made
		server.delete(detailBean, t);
	}

  /**
   * Update the bean.
   */
  public void update(EntityBean entityBean, Transaction t) {
    update(entityBean, t, updatesDeleteMissingChildren);
  }
  
  /**
   * Update the bean specifying deleteMissingChildren.
   */
  public void update(EntityBean entityBean, Transaction t, boolean deleteMissingChildren) {

    PersistRequestBean<?> req = createRequest(entityBean, t, null, PersistRequest.Type.UPDATE);
    req.setDeleteMissingChildren(deleteMissingChildren);
    try {
      req.initTransIfRequiredWithBatchCascade();
      if (req.isReference()) {
        // its a reference so see if there are manys to save...
        if (req.isPersistCascade()) {
          saveAssocMany(false, req, false);
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
	public void save(EntityBean bean, Transaction t) {
	  if (bean._ebean_getIntercept().isLoaded()) {
	    // deleteMissingChildren is false when using 'save' on 'loaded' beans
      update(bean, t, false);
	  } else {
      insert(bean, t);
	  }
	}

	/**
	 * Insert this bean.
	 */
	public void insert(EntityBean bean, Transaction t) {

		PersistRequestBean<?> req = createRequest(bean, t, null, PersistRequest.Type.INSERT);
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

	private void saveRecurse(EntityBean bean, Transaction t, Object parentBean, boolean insertMode) {

	  // determine insert or update taking into account stateless updates
		PersistRequestBean<?> request = createRequest(bean, t, parentBean, insertMode);
		
		if (request.isReference()) {
			// its a reference...
			if (request.isPersistCascade()) {
				// save any associated List held beans
				saveAssocMany(false, request, insertMode);
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

		if (request.isRegisteredBean()){
			// skip as already inserted/updated in this request (recursive cascading)
			return;
		}
		
		try {
			if (request.isPersistCascade()) {
				// save associated One beans recursively first
				saveAssocOne(request, true);
			}
	
			// set the IDGenerated value if required
			setIdGenValue(request);
			request.executeOrQueue();
			
			if (request.isPersistCascade()) {
				// save any associated List held beans
				saveAssocMany(true, request, true);
			}
		} finally {
			request.unRegisterBean();
		}
	}

	/**
	 * Update the bean.
	 */
	private void update(PersistRequestBean<?> request) {

		if (request.isRegisteredBean()){
			// skip as already inserted/updated in this request (recursive cascading)
			return;
		}
		
		try {
			if (request.isPersistCascade()) {
				// save associated One beans recursively first
				saveAssocOne(request, false);
			}
	
			if (request.isDirty()) {
				request.executeOrQueue();
	
			} else {
				// skip validation on unchanged bean
				if (logger.isDebugEnabled()) {
					logger.debug(Message.msg("persist.update.skipped", request.getBean()));
				}
			}
	
			if (request.isPersistCascade()) {
				// save all the beans in assocMany's after
				saveAssocMany(false, request, false);
			}
			
			request.checkUpdatedManysOnly();
			
		} finally {
			request.unRegisterBean();
		}
	}

	/**
	 * Delete the bean with the explicit transaction.
	 */
	public void delete(EntityBean bean, Transaction t) {

		PersistRequestBean<?> req = createRequest(bean, t, null, PersistRequest.Type.DELETE);
		if (req.isRegisteredForDeleteBean()) {
			// skip deleting bean. Used where cascade is on
			// both sides of a relationship
			if (logger.isDebugEnabled()) {
				logger.debug("skipping delete on alreadyRegistered " + bean);
			}
			return;
		}

		try {
			req.initTransIfRequiredWithBatchCascade();
			delete(req);
			req.commitTransIfRequired();
      req.flushBatchOnCascade();

		} catch (RuntimeException ex) {
			req.rollbackTransIfRequired();
			throw ex;
		}
	}

	private void deleteList(List<?> beanList, Transaction t) {
		for (int i = 0; i < beanList.size(); i++) {
			EntityBean bean = (EntityBean)beanList.get(i);
			delete(bean, t);
		}
	}

	/**
	 * Delete by a List of Id's.
	 */
	public void deleteMany(Class<?> beanType, Collection<?> ids, Transaction transaction) {

		if (ids == null || ids.size() == 0) {
			return;
		}

		BeanDescriptor<?> descriptor = beanDescriptorManager.getBeanDescriptor(beanType);

		ArrayList<Object> idList = new ArrayList<Object>(ids.size());
		for (Object id : ids) {
			// convert to appropriate type if required
			idList.add(descriptor.convertId(id));
		}

		delete(descriptor, null, idList, transaction);
	}

	/**
	 * Delete by Id.
	 */
	public int delete(Class<?> beanType, Object id, Transaction transaction) {

		BeanDescriptor<?> descriptor = beanDescriptorManager.getBeanDescriptor(beanType);

		// convert to appropriate type if required
		id = descriptor.convertId(id);
		return delete(descriptor, id, null, transaction);
	}

	/**
	 * Delete by Id or a List of Id's.
	 */
	private int delete(BeanDescriptor<?> descriptor, Object id, List<Object> idList, Transaction transaction) {

		SpiTransaction t = (SpiTransaction) transaction;
		if (t.isPersistCascade()) {
			BeanPropertyAssocOne<?>[] propImportDelete = descriptor.propertiesOneImportedDelete();
			if (propImportDelete.length > 0) {
				// We actually need to execute a query to get the foreign key values
				// as they are required for the delete cascade. Query back just the
				// Id and the appropriate foreign key values
				Query<?> q = deleteRequiresQuery(descriptor, propImportDelete);
				if (idList != null) {
					q.where().idIn(idList);
					if (t.isLogSummary()) {
						t.logSummary("-- DeleteById of " + descriptor.getName() + " ids[" + idList + "] requires fetch of foreign key values");
					}
					List<?> beanList = server.findList(q, t);
					deleteList(beanList, t);
					return beanList.size();

				} else {
					q.where().idEq(id);
					if (t.isLogSummary()) {
						t.logSummary("-- DeleteById of " + descriptor.getName() + " id[" + id + "] requires fetch of foreign key values");
					}
					EntityBean bean = (EntityBean)server.findUnique(q, t);
					if (bean == null) {
						return 0;
					} else {
						delete(bean, t);
						return 1;
					}
				}
			}
		}

		if (t.isPersistCascade()) {
			// OneToOne exported side with delete cascade
			BeanPropertyAssocOne<?>[] expOnes = descriptor.propertiesOneExportedDelete();
			for (int i = 0; i < expOnes.length; i++) {			  
				BeanDescriptor<?> targetDesc = expOnes[i].getTargetDescriptor();
				if (targetDesc.isDeleteRecurseSkippable() && !targetDesc.isBeanCaching()) {
					SqlUpdate sqlDelete = expOnes[i].deleteByParentId(id, idList);
					executeSqlUpdate(sqlDelete, t);
				} else {
					List<Object> childIds = expOnes[i].findIdsByParentId(id, idList, t);
				  deleteChildrenById(t, targetDesc, childIds);
				}
			}

			// OneToMany's with delete cascade
			BeanPropertyAssocMany<?>[] manys = descriptor.propertiesManyDelete();
			for (int i = 0; i < manys.length; i++) {
				BeanDescriptor<?> targetDesc = manys[i].getTargetDescriptor();
				if (targetDesc.isDeleteRecurseSkippable() && !targetDesc.isBeanCaching()) {
					// we can just delete children with a single statement
					SqlUpdate sqlDelete = manys[i].deleteByParentId(id, idList);
					executeSqlUpdate(sqlDelete, t);
				} else {
					// we need to fetch the Id's to delete (recurse or notify L2 cache)
					List<Object> childIds = manys[i].findIdsByParentId(id, idList, t, null);
					if (!childIds.isEmpty()) {
						delete(targetDesc, null, childIds, t);
					}
				}
			}
		}

		// ManyToMany's ... delete from intersection table
		BeanPropertyAssocMany<?>[] manys = descriptor.propertiesManyToMany();
		for (int i = 0; i < manys.length; i++) {
			SqlUpdate sqlDelete = manys[i].deleteByParentId(id, idList);
			if (t.isLogSummary()) {
				t.logSummary("-- Deleting intersection table entries: " + manys[i].getFullBeanName());
			}
			executeSqlUpdate(sqlDelete, t);
		}

		// delete the bean(s)
		SqlUpdate deleteById = descriptor.deleteById(id, idList);
		if (t.isLogSummary()) {
      if (idList != null) {
        t.logSummary("-- Deleting " + descriptor.getName() + " Ids: " + idList);
      } else {
        t.logSummary("-- Deleting " + descriptor.getName() + " Id: " + id);
      }
		}

		// use Id's to update L2 cache rather than Bulk table event
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
		  for (Object  idValue : idList) {
        persistenceContext.deleted(descriptor.getBeanType(), idValue);
      }
		} else {
		  persistenceContext.deleted(descriptor.getBeanType(), id);
		}
		return rows;
	}

	/**
	 * We need to create and execute a query to get the foreign key values as
	 * the delete cascades to them (foreign keys).
	 */
	private Query<?> deleteRequiresQuery(BeanDescriptor<?> desc, BeanPropertyAssocOne<?>[] propImportDelete) {

		Query<?> q = server.createQuery(desc.getBeanType());
		StringBuilder sb = new StringBuilder(30);
		for (int i = 0; i < propImportDelete.length; i++) {
			sb.append(propImportDelete[i].getName()).append(",");
		}
		q.setAutofetch(false);
		q.select(sb.toString());
		return q;
	}

	/**
	 * Delete the bean.
	 * <p>
	 * Note that preDelete fires before the deletion of children.
	 * </p>
	 */
	private void delete(PersistRequestBean<?> request) {

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

		request.executeOrQueue();

		if (request.isPersistCascade()) {
			deleteAssocOne(request);

			if (unloadedForeignKeys != null) {
				unloadedForeignKeys.deleteCascade();
			}
		}

	}

	/**
	 * Save the associated child beans contained in a List.
	 * <p>
	 * This will automatically copy over any join properties from the parent
	 * bean to the child beans.
	 * </p>
	 */
	private void saveAssocMany(boolean insertedParent, PersistRequestBean<?> request, boolean insertMode) {

		EntityBean parentBean = request.getEntityBean();
		BeanDescriptor<?> desc = request.getBeanDescriptor();
		SpiTransaction t = request.getTransaction();

		// exported ones with cascade save
		BeanPropertyAssocOne<?>[] expOnes = desc.propertiesOneExportedSave();
		for (int i = 0; i < expOnes.length; i++) {
			BeanPropertyAssocOne<?> prop = expOnes[i];

			// check for partial beans
			if (request.isLoadedProperty(prop)) {
			  EntityBean detailBean = prop.getValueAsEntityBean(parentBean);
				if (detailBean != null) {
					if (!prop.isSaveRecurseSkippable(detailBean)) {
						t.depth(+1);
						prop.setParentBeanToChild(parentBean, detailBean);
						saveRecurse(detailBean, t, parentBean, insertMode);
						t.depth(-1);
					}
				}
			}
		}

		// many's with cascade save
		BeanPropertyAssocMany<?>[] manys = desc.propertiesManySave();
		for (int i = 0; i < manys.length; i++) {
		  // check that property is loaded and not empty uninitialised collection
      if (request.isLoadedProperty(manys[i]) && !manys[i].isEmptyBeanCollection(parentBean)) {
        saveMany(new SaveManyPropRequest(insertedParent, manys[i], parentBean, request), insertMode);
        if (!insertedParent) {
          request.addUpdatedManyProperty(manys[i]);
        }
      }
		}
	}

	/**
	 * Helper to wrap the details when saving a OneToMany or ManyToMany
	 * relationship.
	 */
	private static class SaveManyPropRequest {
		private final boolean insertedParent;
		private final BeanPropertyAssocMany<?> many;
		private final EntityBean parentBean;
		private final SpiTransaction transaction;
		private final boolean cascade;
		private final boolean deleteMissingChildren;

		private SaveManyPropRequest(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
			this.insertedParent = insertedParent;
			this.many = many;
			this.cascade = many.getCascadeInfo().isSave();
			this.parentBean = parentBean;
			this.transaction = request.getTransaction();
			this.deleteMissingChildren = request.isDeleteMissingChildren();
		}

		private SaveManyPropRequest(BeanPropertyAssocMany<?> many, EntityBean parentBean, SpiTransaction t) {
			this.insertedParent = false;
			this.many = many;
			this.parentBean = parentBean;
			this.transaction = t;
			this.cascade = true;
			this.deleteMissingChildren = false;
		}
		
		public boolean isSaveIntersection() {
		  return transaction.isSaveAssocManyIntersection(many.getIntersectionTableJoin().getTable(), many.getBeanDescriptor().getName());
		}
		
		private Object getValue() {
			return many.getValue(parentBean);
		}

		private boolean isModifyListenMode() {
			return ModifyListenMode.REMOVALS.equals(many.getModifyListenMode());
		}

		private boolean isDeleteMissingChildren() {
			return deleteMissingChildren;
		}

		private boolean isInsertedParent() {
			return insertedParent;
		}

		private BeanPropertyAssocMany<?> getMany() {
			return many;
		}

		private EntityBean getParentBean() {
			return parentBean;
		}

		private SpiTransaction getTransaction() {
			return transaction;
		}

		private boolean isCascade() {
			return cascade;
		}
	}

	private void saveMany(SaveManyPropRequest saveMany, boolean insertMode) {

		if (saveMany.getMany().isManyToMany()) {
			
		  // check if we can save the m2m intersection in this direction
		  boolean saveIntersectionFromThisDirection = saveMany.isSaveIntersection();
			if (saveMany.isCascade()) {
				// Need explicit Cascade to save the beans on other side
				saveAssocManyDetails(saveMany, false, insertMode);
			}
			// for ManyToMany save the 'relationship' via inserts/deletes
			// into/from the intersection table
			if (saveIntersectionFromThisDirection) {
			  // only allowed on one direction of a m2m based on beanName
			  saveAssocManyIntersection(saveMany, saveMany.isDeleteMissingChildren());
			}
		} else {
      if (saveMany.isModifyListenMode()) {
        // delete any removed beans via private owned. Needs to occur before 
        // a 'deleteMissingChildren' statement occurs
        removeAssocManyPrivateOwned(saveMany);
      }
		  if (saveMany.isCascade()) {
		    // potentially deletes 'missing children' for 'stateless update'
				saveAssocManyDetails(saveMany, saveMany.isDeleteMissingChildren(), insertMode);
			}
		}
	}

	private void removeAssocManyPrivateOwned(SaveManyPropRequest saveMany) {

		Object details = saveMany.getValue();

		// check that the list is not null and if it is a BeanCollection
		// check that is has been populated (don't trigger lazy loading)
		if (details instanceof BeanCollection<?>) {

			BeanCollection<?> c = (BeanCollection<?>) details;
			Set<?> modifyRemovals = c.getModifyRemovals();
			if (modifyRemovals != null && !modifyRemovals.isEmpty()) {

				SpiTransaction t = saveMany.getTransaction();
				// increase depth for batching order
				t.depth(+1);
				for (Object removedBean : modifyRemovals) {
				  if (removedBean instanceof EntityBean) {
				    EntityBean eb = (EntityBean)removedBean;
				    if (eb._ebean_getIntercept().isLoaded()) {
				      // only delete if the bean was loaded meaning that
				      // it is know to exist in the DB
				      deleteRecurse(removedBean, t);
				    }
				  }
				}
				t.depth(-1);
			}
		}
	}

	/**
	 * Save the details from a OneToMany collection.
	 */
	private void saveAssocManyDetails(SaveManyPropRequest saveMany, boolean deleteMissingChildren, boolean insertMode) {

		BeanPropertyAssocMany<?> prop = saveMany.getMany();

		Object details = saveMany.getValue();

		// check that the list is not null and if it is a BeanCollection
		// check that is has been populated (don't trigger lazy loading)
		// For a Map this is a collection of Map.Entry objects and not beans
		Collection<?> collection = BeanCollectionUtil.getActualEntries(details);

		if (collection == null) {
			// nothing to do here
			return;
		}

    BeanDescriptor<?> targetDescriptor = prop.getTargetDescriptor();
		if (saveMany.isInsertedParent()) {
			// performance optimisation for large collections
		  targetDescriptor.preAllocateIds(collection.size());
		}

		ArrayList<Object> detailIds = null;
		if (deleteMissingChildren) {
			// collect the Id's (to exclude from deleteManyDetails)
			detailIds = new ArrayList<Object>();
		}

		// increase depth for batching order
		SpiTransaction t = saveMany.getTransaction();
		t.depth(+1);

		// if a map, then we get the key value and
		// set it to the appropriate property on the
		// detail bean before we save it
		boolean isMap = ManyType.JAVA_MAP.equals(prop.getManyType());
		EntityBean parentBean = saveMany.getParentBean();
		Object mapKeyValue = null;

		boolean saveSkippable = prop.isSaveRecurseSkippable();
		boolean skipSavingThisBean;

		for (Object detailBean : collection) {
			if (isMap) {
				// its a map so need the key and value
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) detailBean;
				mapKeyValue = entry.getKey();
				detailBean = entry.getValue();
			}

			if (detailBean instanceof EntityBean) {
			  EntityBean detail = (EntityBean)detailBean;
        EntityBeanIntercept ebi = detail._ebean_getIntercept();
			  if (prop.isManyToMany()) {
	        skipSavingThisBean = targetDescriptor.isReference(ebi);
	      } else {
          if (targetDescriptor.isReference(ebi)) {
            // we can skip this one
            skipSavingThisBean = true;

          } else if (ebi.isNewOrDirty()) {
            skipSavingThisBean = false;
            // set the parent bean to detailBean
            prop.setJoinValuesToChild(parentBean, detail, mapKeyValue);            

          } else {
            // unmodified so skip depending on prop.isSaveRecurseSkippable();
            skipSavingThisBean = saveSkippable;
          }
	      }

        if (!skipSavingThisBean) {
          saveRecurse(detail, t, parentBean, insertMode);
        }
        if (detailIds != null) {
          // remember the Id (other details not in the collection) will be removed
          Object id = targetDescriptor.getId(detail);
          if (!DmlUtil.isNullOrZero(id)) {
            detailIds.add(id);
          }
        }
      }
		}

		if (detailIds != null) {
		  // deleteMissingChildren is true so deleting children that were not just processed
			deleteManyDetails(t, prop.getBeanDescriptor(), parentBean, prop, detailIds);
		}

		t.depth(-1);
	}

	public int deleteManyToManyAssociations(EntityBean ownerBean, String propertyName, Transaction t) {

		BeanDescriptor<?> descriptor = beanDescriptorManager.getBeanDescriptor(ownerBean.getClass());
		BeanPropertyAssocMany<?> prop = (BeanPropertyAssocMany<?>) descriptor.getBeanProperty(propertyName);
		return deleteAssocManyIntersection(ownerBean, prop, t);
	}

	public void saveManyToManyAssociations(EntityBean ownerBean, String propertyName, Transaction t) {

		BeanDescriptor<?> descriptor = beanDescriptorManager.getBeanDescriptor(ownerBean.getClass());
		BeanPropertyAssocMany<?> prop = (BeanPropertyAssocMany<?>) descriptor.getBeanProperty(propertyName);

		saveAssocManyIntersection(new SaveManyPropRequest(prop, ownerBean, (SpiTransaction) t), false);
	}

	public void saveAssociation(EntityBean parentBean, String propertyName, Transaction t) {

		BeanDescriptor<?> descriptor = beanDescriptorManager.getBeanDescriptor(parentBean.getClass());
		SpiTransaction trans = (SpiTransaction) t;

		BeanProperty prop = descriptor.getBeanProperty(propertyName);
		if (prop == null) {
			String msg = "Could not find property [" + propertyName + "] on bean " + parentBean.getClass();
			throw new PersistenceException(msg);
		}

		if (prop instanceof BeanPropertyAssocMany<?>) {
			BeanPropertyAssocMany<?> manyProp = (BeanPropertyAssocMany<?>) prop;
			saveMany(new SaveManyPropRequest(manyProp, parentBean, (SpiTransaction) t), true);

		} else if (prop instanceof BeanPropertyAssocOne<?>) {
			BeanPropertyAssocOne<?> oneProp = (BeanPropertyAssocOne<?>) prop;
			EntityBean assocBean = oneProp.getValueAsEntityBean(parentBean);

			int depth = oneProp.isOneToOneExported() ? 1 : -1;
			int revertDepth = -1 * depth;

			trans.depth(depth);
			saveRecurse(assocBean, t, parentBean, true);
			trans.depth(revertDepth);

		} else {
			String msg = "Expecting [" + prop.getFullBeanName() + "] to be a OneToMany, OneToOne, ManyToOne or ManyToMany property?";
			throw new PersistenceException(msg);
		}

	}

	/**
	 * Save the additions and removals from a ManyToMany collection as inserts
	 * and deletes from the intersection table.
	 * <p>
	 * This is done via MapBeans.
	 * </p>
	 */
	private void saveAssocManyIntersection(SaveManyPropRequest saveManyPropRequest, boolean deleteMissingChildren) {

		BeanPropertyAssocMany<?> prop = saveManyPropRequest.getMany();
		Object value = prop.getValue(saveManyPropRequest.getParentBean());
		if (value == null) {
			return;
		}

		SpiTransaction t = saveManyPropRequest.getTransaction();
		boolean vanillaCollection = !(value instanceof BeanCollection<?>);

		if (vanillaCollection || deleteMissingChildren) {
			// delete all intersection rows and then treat all
			// beans in the collection as additions
			deleteAssocManyIntersection(saveManyPropRequest.getParentBean(), prop, t);
		}

    Collection<?> deletions = null;
    Collection<?> additions;

		if (saveManyPropRequest.isInsertedParent() || vanillaCollection || deleteMissingChildren) {
			// treat everything in the list/set/map as an intersection addition
			if (value instanceof Map<?, ?>) {
				additions = ((Map<?, ?>) value).values();
			} else if (value instanceof Collection<?>) {
				additions = (Collection<?>) value;
			} else {
				String msg = "Unhandled ManyToMany type " + value.getClass().getName() + " for " + prop.getFullBeanName();
				throw new PersistenceException(msg);
			}
			if (!vanillaCollection) {
				((BeanCollection<?>) value).modifyReset();
			}
		} else {
			// BeanCollection so get the additions/deletions
			BeanCollection<?> manyValue = (BeanCollection<?>) value;
			additions = manyValue.getModifyAdditions();
			deletions = manyValue.getModifyRemovals();
			// reset so the changes are only processed once
			manyValue.modifyReset();
		}

		t.depth(+1);

		if (additions != null && !additions.isEmpty()) {
			for (Object other : additions) {
			  EntityBean otherBean = (EntityBean)other;
				// the object from the 'other' side of the ManyToMany
				if (deletions != null && deletions.remove(otherBean)) {
					String m = "Inserting and Deleting same object? " + otherBean;
					if (t.isLogSummary()) {
						t.logSummary(m);
					}
					logger.warn(m);

				} else {
					if (!prop.hasImportedId(otherBean)) {
						String msg = "ManyToMany bean " + otherBean + " does not have an Id value.";
						throw new PersistenceException(msg);

					} else {
						// build a intersection row for 'insert'
						IntersectionRow intRow = prop.buildManyToManyMapBean(saveManyPropRequest.getParentBean(), otherBean);
						SqlUpdate sqlInsert = intRow.createInsert(server);
						executeSqlUpdate(sqlInsert, t);
					}
				}
			}
		}
		if (deletions != null && !deletions.isEmpty()) {
			for (Object other : deletions) {
			  EntityBean otherDelete = (EntityBean)other;
				// the object from the 'other' side of the ManyToMany
				// build a intersection row for 'delete'
				IntersectionRow intRow = prop.buildManyToManyMapBean(saveManyPropRequest.getParentBean(), otherDelete);
				SqlUpdate sqlDelete = intRow.createDelete(server);
				executeSqlUpdate(sqlDelete, t);
			}
		}

		// decrease the depth back to what it was
		t.depth(-1);
	}

	private int deleteAssocManyIntersection(EntityBean bean, BeanPropertyAssocMany<?> many, Transaction t) {

		// delete all intersection rows for this bean
		IntersectionRow intRow = many.buildManyToManyDeleteChildren(bean);
		SqlUpdate sqlDelete = intRow.createDeleteChildren(server);

		return executeSqlUpdate(sqlDelete, t);
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

		BeanPropertyAssocOne<?>[] expOnes = desc.propertiesOneExportedDelete();
		if (expOnes.length > 0) {

			DeleteUnloadedForeignKeys unloaded = null;
			for (int i = 0; i < expOnes.length; i++) {
				BeanPropertyAssocOne<?> prop = expOnes[i];
				if (request.isLoadedProperty(prop)) {
					Object detailBean = prop.getValue(parentBean);
					if (detailBean != null) {
						deleteRecurse(detailBean, t);
					}
				} else {
					if (unloaded == null) {
						unloaded = new DeleteUnloadedForeignKeys(server, request);
					}
					unloaded.add(prop);
				}
			}
			if (unloaded != null) {
				unloaded.queryForeignKeys();
				unloaded.deleteCascade();
			}
		}

		// Many's with delete cascade
		BeanPropertyAssocMany<?>[] manys = desc.propertiesManyDelete();
		for (int i = 0; i < manys.length; i++) {
			if (manys[i].isManyToMany()) {
				// delete associated rows from intersection table
				deleteAssocManyIntersection(parentBean, manys[i], t);

			} else {

				if (ModifyListenMode.REMOVALS.equals(manys[i].getModifyListenMode())) {
					// PrivateOwned ...
					Object details = manys[i].getValue(parentBean);
					if (details instanceof BeanCollection<?>) {
						Set<?> modifyRemovals = ((BeanCollection<?>) details).getModifyRemovals();
						if (modifyRemovals != null && !modifyRemovals.isEmpty()) {

							// delete the orphans that have been removed from the collection
							for (Object detail : modifyRemovals) {
							  EntityBean detailBean = (EntityBean)detail;
								if (manys[i].hasId(detailBean)) {
									deleteRecurse(detailBean, t);
								}
							}
						}
					}
				}

				deleteManyDetails(t, desc, parentBean, manys[i], null);
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
	private void deleteManyDetails(SpiTransaction t, BeanDescriptor<?> desc, EntityBean parentBean,
	        BeanPropertyAssocMany<?> many, ArrayList<Object> excludeDetailIds) {

		if (many.getCascadeInfo().isDelete()) {
			// cascade delete the beans in the collection
			BeanDescriptor<?> targetDesc = many.getTargetDescriptor();
			if (targetDesc.isDeleteRecurseSkippable() && !targetDesc.isBeanCaching()) {
				// Just delete all the children with one statement
				IntersectionRow intRow = many.buildManyDeleteChildren(parentBean, excludeDetailIds);
				SqlUpdate sqlDelete = intRow.createDelete(server);
				executeSqlUpdate(sqlDelete, t);

			} else {
				// Delete recurse using the Id values of the children
				Object parentId = desc.getId(parentBean);
				List<Object> idsByParentId = many.findIdsByParentId(parentId, null, t, excludeDetailIds);
				if (!idsByParentId.isEmpty()) {
				  deleteChildrenById(t, targetDesc, idsByParentId);
				}
			}
		}
	}

	/**
	 * Cascade delete child entities by Id.
	 * <p>
	 * Will use delete by object if the child entity has manyToMany relationships.
	 */
  private void deleteChildrenById(SpiTransaction t, BeanDescriptor<?> targetDesc, List<Object> childIds) {
    
    if (targetDesc.propertiesManyToMany().length > 0) {
      // convert into a list of reference objects and perform delete by object
      List<Object> refList = new ArrayList<Object>(childIds.size());
      for (Object id : childIds) {
        refList.add(targetDesc.createReference(null, id));
      }
      deleteList(refList, t);
      
    } else {
      // perform delete by statement if possible
      delete(targetDesc, null, childIds, t);				
    }
  }

	/**
	 * Save any associated one beans.
	 */
	private void saveAssocOne(PersistRequestBean<?> request, boolean insertMode) {

		BeanDescriptor<?> desc = request.getBeanDescriptor();

		// imported ones with save cascade
		BeanPropertyAssocOne<?>[] ones = desc.propertiesOneImportedSave();

    for (int i = 0; i < ones.length; i++) {
      BeanPropertyAssocOne<?> prop = ones[i];

      // check for partial objects
      if (request.isLoadedProperty(prop)) {
        EntityBean detailBean = prop.getValueAsEntityBean(request.getEntityBean());
        if (detailBean != null
                && !prop.isSaveRecurseSkippable(detailBean)
                && !prop.isReference(detailBean)
                && !request.isParent(detailBean)) {
          SpiTransaction t = request.getTransaction();
          t.depth(-1);
          saveRecurse(detailBean, t, null, insertMode);
          t.depth(+1);
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

		BeanPropertyAssocOne<?>[] ones = request.getBeanDescriptor().propertiesOneImportedDelete();
		for (int i = 0; i < ones.length; i++) {
			if (!request.isLoadedProperty(ones[i])) {
				// we have cascade Delete on a partially populated bean and
				// this property was not loaded (so we are going to have to fetch it)
				if (fkeys == null) {
					fkeys = new DeleteUnloadedForeignKeys(server, request);
				}
				fkeys.add(ones[i]);
			}
		}

		return fkeys;
	}

	/**
	 * Delete any associated one beans.
	 */
	private void deleteAssocOne(PersistRequestBean<?> request) {

		BeanDescriptor<?> desc = request.getBeanDescriptor();
		BeanPropertyAssocOne<?>[] ones = desc.propertiesOneImportedDelete();

		for (int i = 0; i < ones.length; i++) {
			BeanPropertyAssocOne<?> prop = ones[i];
			if (request.isLoadedProperty(prop)) {
				Object detailBean = prop.getValue(request.getEntityBean());
				if (detailBean != null) {
				  EntityBean detail = (EntityBean)detailBean;
				  if (prop.hasId(detail)) {
				    deleteRecurse(detail, request.getTransaction());
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
	private <T> PersistRequestBean<T> createRequest(T bean, Transaction t, Object parentBean, PersistRequest.Type type) {
		BeanManager<T> mgr = getBeanManager(bean);
		if (mgr == null) {
			throw new PersistenceException(errNotRegistered(bean.getClass()));
		}
		return createRequest(bean, t, parentBean, mgr, type, false);
	}

	/**
	 * Create an Insert or Update PersistRequestBean when cascading.
	 * <p>
	 * This call determines the PersistRequest.Type based on bean state and the insert flag (root persist type).
	 */
  private <T> PersistRequestBean<T> createRequest(T bean, Transaction t, Object parentBean, boolean insertMode) {
    BeanManager<T> mgr = getBeanManager(bean);
    if (mgr == null) {
      throw new PersistenceException(errNotRegistered(bean.getClass()));
    }
    BeanDescriptor<T> desc = mgr.getBeanDescriptor();
    EntityBean entityBean = (EntityBean)bean;
    // determine Insert or Update based on bean state and insert flag
    PersistRequest.Type type = desc.isInsertMode(entityBean._ebean_getIntercept(), insertMode) ? Type.INSERT : Type.UPDATE;
    return createRequest(bean, t, parentBean, mgr, type, true);
  }

	/**
	 * Create the Persist Request Object that wraps all the objects used to
	 * perform an insert, update or delete.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> PersistRequestBean<T> createRequest(T bean, Transaction t, Object parentBean, BeanManager<?> mgr, PersistRequest.Type type, boolean saveRecurse) {

		return new PersistRequestBean(server, bean, parentBean, mgr, (SpiTransaction) t, persistExecute, type, saveRecurse);
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
