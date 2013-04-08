package com.avaje.ebeaninternal.server.core;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.persistence.OptimisticLockException;

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
import com.avaje.ebeaninternal.server.persist.BatchControl;
import com.avaje.ebeaninternal.server.persist.PersistExecute;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.avaje.ebeaninternal.server.transaction.BeanDelta;
import com.avaje.ebeaninternal.server.transaction.BeanPersistIdMap;

/**
 * PersistRequest for insert update or delete of a bean.
 */
public class PersistRequestBean<T> extends PersistRequest implements BeanPersistRequest<T> {

	protected final BeanManager<T> beanManager;

	protected final BeanDescriptor<T> beanDescriptor;

	protected final BeanPersistListener<T> beanPersistListener;

	/**
	 * For per post insert update delete control.
	 */
	protected final BeanPersistController controller;

	/**
	 * The associated intercept.
	 */
	protected final EntityBeanIntercept intercept;

	/**
	 * The parent bean for unidirectional save.
	 */
	protected final Object parentBean;

	protected final boolean isDirty;

	/**
	 * True if this is a vanilla bean.
	 */
	protected final boolean vanilla;

	/**
	 * The bean being persisted.
	 */
	protected final T bean;

	/**
	 * Old values used for concurrency checking.
	 */
	protected T oldValues;

	/**
	 * The concurrency mode used for update or delete.
	 */
	protected ConcurrencyMode concurrencyMode;

	protected final Set<String> loadedProps;

	/**
	 * The unique id used for logging summary.
	 */
	protected Object idValue;

	/**
	 * Hash value used to handle cascade delete both ways in a relationship.
	 */
	protected Integer beanHash;
	protected Integer beanIdentityHash;

	protected final Set<String> changedProps;

	protected boolean notifyCache;

	private boolean statelessUpdate;
	private boolean deleteMissingChildren;
	private boolean updateNullProperties;

	/**
	 * Used for forced update of a bean.
	 */
	public PersistRequestBean(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr, SpiTransaction t,
	        PersistExecute persistExecute, Set<String> updateProps, ConcurrencyMode concurrencyMode) {

		super(server, t, persistExecute);
		this.beanManager = mgr;
		this.beanDescriptor = mgr.getBeanDescriptor();
		this.beanPersistListener = beanDescriptor.getPersistListener();
		this.bean = bean;
		this.parentBean = parentBean;
		
		this.controller = beanDescriptor.getPersistController();
		this.concurrencyMode = beanDescriptor.getConcurrencyMode();

		this.concurrencyMode = concurrencyMode;
		this.loadedProps = updateProps;
		this.changedProps = updateProps;

		this.vanilla = true;
		this.isDirty = true;
		this.oldValues = bean;
		if (bean instanceof EntityBean) {
			this.intercept = ((EntityBean) bean)._ebean_getIntercept();
		} else {
			this.intercept = null;
		}
	}

	@SuppressWarnings("unchecked")
	public PersistRequestBean(SpiEbeanServer server, T bean, Object parentBean, BeanManager<T> mgr,
	        SpiTransaction t, PersistExecute persistExecute) {

		super(server, t, persistExecute);
		this.beanManager = mgr;
		this.beanDescriptor = mgr.getBeanDescriptor();
		this.beanPersistListener = beanDescriptor.getPersistListener();
		this.bean = bean;
		this.parentBean = parentBean;

		this.controller = beanDescriptor.getPersistController();
		this.concurrencyMode = beanDescriptor.getConcurrencyMode();

		if (bean instanceof EntityBean) {
			this.intercept = ((EntityBean) bean)._ebean_getIntercept();
			if (intercept.isReference()) {
				// allowed to delete reference objects
				// with no concurrency checking
				this.concurrencyMode = ConcurrencyMode.NONE;
			}
			// this is ok to not use isNewOrDirty() as used for updates only
			this.isDirty = intercept.isDirty();
			if (!isDirty) {
				this.changedProps = intercept.getChangedProps();
			} else {
				// merge changed properties on the bean with changed embedded beans
				Set<String> beanChangedProps = intercept.getChangedProps();
				Set<String> dirtyEmbedded = beanDescriptor.getDirtyEmbeddedProperties(bean);
				this.changedProps = mergeChangedProperties(beanChangedProps, dirtyEmbedded);
			}
			this.loadedProps = intercept.getLoadedProps();
			this.oldValues = (T) intercept.getOldValues();
			this.vanilla = false;

		} else {
			// have to assume the vanilla bean is dirty
			this.vanilla = true;
			this.isDirty = true;
			this.loadedProps = null;
			this.changedProps = null;
			this.intercept = null;

			// degrade concurrency checking to none for vanilla bean
			if (concurrencyMode.equals(ConcurrencyMode.ALL)) {
				this.concurrencyMode = ConcurrencyMode.NONE;
			}
		}
	}

	/**
	 * Merge the changed properties for the bean and embedded beans.
	 */
	private Set<String> mergeChangedProperties(Set<String> beanChangedProps, Set<String> embChanged) {
		if (embChanged == null) {
			return beanChangedProps;
		} else if (beanChangedProps == null) {
			return embChanged;
		} else {
			beanChangedProps.addAll(embChanged);
			return beanChangedProps;
		}
	}

	public boolean isNotify(TransactionEvent txnEvent) {
		return notifyCache || isNotifyPersistListener();
	}

	public boolean isNotifyCache() {
		return notifyCache;
	}

	public boolean isNotifyPersistListener() {
		return beanPersistListener != null;
	}

	public void notifyCache() {
		if (notifyCache) {
			switch (type) {
            case INSERT:
				beanDescriptor.cacheInsert(idValue, this);
				break;
            case UPDATE:
				beanDescriptor.cacheUpdate(idValue, this);
	            break;
            case DELETE:
				beanDescriptor.cacheDelete(idValue, this);
	            break;
            default:
	            throw new IllegalStateException("Invalid type "+type);
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
				return beanPersistListener.updated(bean, getUpdatedProperties());

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
	 * Return true if this bean has been already been persisted 
	 * (inserted/updated or deleted) in this transaction.
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
			Object id = beanDescriptor.getId(bean);
			int hc = 31 * bean.getClass().getName().hashCode();
			if (id != null) {
				hc += id.hashCode();
			}
			beanHash = Integer.valueOf(hc);
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
		if (transaction == null){
			return false;
		} else {
			Integer hash = getBeanHash();
			return transaction.isRegisteredDeleteBean(hash);
		}
	}

	/**
	 * Set the type of this request. One of INSERT, UPDATE, DELETE, UPDATESQL or
	 * CALLABLESQL.
	 */
	@Override
	public void setType(Type type) {
		this.type = type;
		notifyCache = beanDescriptor.isCacheNotify();
		if (type == Type.DELETE || type == Type.UPDATE) {
			if (oldValues == null) {
				oldValues = bean;
			}
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
	 * Return true if this is a stateless update.
	 */
	public boolean isStatelessUpdate() {
		return statelessUpdate;
	}

	/**
	 * Return true if a stateless update should also delete any missing details
	 * beans.
	 */
	public boolean isDeleteMissingChildren() {
		return deleteMissingChildren;
	}

	/**
	 * Return true if null properties should be updated (treated as loaded) for
	 * stateless updates.
	 */
	public boolean isUpdateNullProperties() {
		return updateNullProperties;
	}

	/**
	 * Set to true if this is a stateless update.
	 * <p>
	 * By Stateless it means that the bean was not previously fetched (and so
	 * does not have it's previous state) so we are doing an update on a bean
	 * that was probably created from JSON or XML.
	 * </p>
	 */
	public void setStatelessUpdate(boolean statelessUpdate, boolean deleteMissingChildren, boolean updateNullProperties) {
		this.statelessUpdate = statelessUpdate;
		this.deleteMissingChildren = deleteMissingChildren;
		this.updateNullProperties = updateNullProperties;
	}

	/**
	 * Used to skip updates if we know the bean is not dirty. This is the case
	 * for EntityBeans that have not been modified.
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Return the concurrency mode used for this persist.
	 */
	public ConcurrencyMode getConcurrencyMode() {
		return concurrencyMode;
	}

	/**
	 * Set loaded properties when generated values has added properties such as
	 * created and updated timestamps.
	 */
	public void setLoadedProps(Set<String> additionalProps) {
		if (intercept != null) {
			intercept.setLoadedProps(additionalProps);
		}
	}

	public Set<String> getLoadedProperties() {
		return loadedProps;
	}

	/**
	 * Returns a description of the request. This is typically the bean class
	 * name or the base table for MapBeans.
	 * <p>
	 * Used to determine common persist requests for queueing and statement
	 * batching.
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

	/**
	 * Return the Id value for the bean.
	 */
	public Object getBeanId() {
		return beanDescriptor.getId(bean);
	}

	public BeanDelta createDeltaBean() {
		return new BeanDelta(beanDescriptor, getBeanId());
	}

	/**
	 * Get the old values bean. This is used to perform optimistic concurrency
	 * checking on updates and deletes.
	 */
	public T getOldValues() {
		return oldValues;
	}

	/**
	 * Return the parent bean for cascading save with unidirectional
	 * relationship.
	 */
	public Object getParentBean() {
		return parentBean;
	}

	/**
	 * Return the controller if there is one associated with this type of bean.
	 * This returns null if there is no controller associated.
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
	 * Return true if this property is loaded (full bean or included in partial
	 * bean).
	 */
	public boolean isLoadedProperty(BeanProperty prop) {
		if (loadedProps == null) {
			return true;
		} else {
			return loadedProps.contains(prop.getName());
		}
	}

	@Override
	public int executeNow() {
		switch (type) {
		case INSERT:
			persistExecute.executeInsertBean(this);
			return -1;

		case UPDATE:
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

		boolean batch = transaction.isBatchThisRequest();

		BatchControl control = transaction.getBatchControl();
		if (control != null) {
			return control.executeOrQueue(this, batch);
		}
		if (batch) {
			control = persistExecute.createBatchControl(transaction);
			return control.executeOrQueue(this, batch);

		} else {
			return executeNow();
		}
	}

	/**
	 * Set the generated key back to the bean. Only used for inserts with
	 * getGeneratedKeys.
	 */
	public void setGeneratedKey(Object idValue) {
		if (idValue != null) {

			// set back to the bean so that we can use the same bean later
			// for update [refer ebeanIntercept.setLoaded(true)].
			idValue = beanDescriptor.convertSetId(idValue, bean);

			// remember it for logging summary
			this.idValue = idValue;
		}
	}

	/**
	 * Set the Id value that was bound. Used for the purposes of logging summary
	 * information on this request.
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
	}

	/**
	 * Post processing.
	 */
	public void postExecute() throws SQLException {

		if (controller != null) {
			controllerPost();
		}

		if (intercept != null) {
			// if bean persisted again then should result in an update
			intercept.setLoaded();
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
			transaction.logInternal("Inserted [" + name + "] [" + idValue + "]");
			break;
		case UPDATE:
			transaction.logInternal("Updated [" + name + "] [" + idValue + "]");
			break;
		case DELETE:
			transaction.logInternal("Deleted [" + name + "] [" + idValue + "]");
			break;
		default:
			break;
		}
	}

	/**
	 * Add the bean to the TransactionEvent. This will be used by
	 * TransactionManager to synch Cache, Cluster and text indexes.
	 */
	private void addEvent() {

		TransactionEvent event = transaction.getEvent();
		if (event != null) {
			event.add(this);
		}
	}

	/**
	 * Determine the concurrency mode depending on fully/partially populated
	 * bean.
	 * <p>
	 * Specifically with version concurrency we want to check that the version
	 * property was one of the loaded properties.
	 * </p>
	 */
	public ConcurrencyMode determineConcurrencyMode() {
		if (loadedProps != null) {
			// 'partial bean' update/delete...
			if (concurrencyMode.equals(ConcurrencyMode.VERSION)) {
				// check the version property was loaded
				BeanProperty prop = beanDescriptor.firstVersionProperty();
				if (prop != null && loadedProps.contains(prop.getName())) {
					// OK to use version property
				} else {
					concurrencyMode = ConcurrencyMode.ALL;
				}
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
		return !vanilla && beanDescriptor.isUpdateChangesOnly() || (loadedProps != null);
	}

	/**
	 * Create a GenerateDmlRequest used to generate the DML.
	 * <p>
	 * Will used changed properties or loaded properties depending on the
	 * BeanDescriptor.isUpdateChangesOnly() value.
	 * </p>
	 */
	public GenerateDmlRequest createGenerateDmlRequest(boolean emptyStringAsNull) {
		if (beanDescriptor.isUpdateChangesOnly()) {
			return new GenerateDmlRequest(emptyStringAsNull, changedProps, loadedProps, oldValues);
		} else {
			return new GenerateDmlRequest(emptyStringAsNull, loadedProps, loadedProps, oldValues);
		}
	}

	/**
	 * Return the updated properties. If this returns null then all the
	 * properties on the bean where updated.
	 */
	public Set<String> getUpdatedProperties() {
		if (changedProps != null) {
			return changedProps;
		}
		return loadedProps;
	}

	/**
	 * Test if the property value has changed and if so include it in the
	 * update.
	 */
	public boolean hasChanged(BeanProperty prop) {

		return changedProps.contains(prop.getName());
	}

	public List<DerivedRelationshipData> getDerivedRelationships() {
	    return transaction.getDerivedRelationship(bean);
    }

}
