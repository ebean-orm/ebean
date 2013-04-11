package com.avaje.ebeaninternal.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.transaction.BeanDelta;
import com.avaje.ebeaninternal.server.transaction.DeleteByIdMap;
import com.avaje.ebeaninternal.server.transaction.IndexInvalidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information for a transaction. There is one TransactionEvent instance
 * per Transaction instance.
 * <p>
 * When the associated Transaction commits or rollback this information is sent
 * to the TransactionEventManager.
 * </p>
 */
public class TransactionEvent implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(TransactionEvent.class);
    
	private static final long serialVersionUID = 7230903304106097120L;

	/**
	 * Flag indicating this is a local transaction (not from another server in
	 * the cluster).
	 */
	private transient boolean local;

    private boolean invalidateAll;

    private TransactionEventTable eventTables;
    
	private transient TransactionEventBeans eventBeans;

    private transient List<BeanDelta> beanDeltas;

    private transient DeleteByIdMap deleteByIdMap;
	
	private transient Set<IndexInvalidate> indexInvalidations;
	
	private transient Set<String> pauseIndexInvalidate;
	
	/**
	 * Create the TransactionEvent, one per Transaction.
	 */
	public TransactionEvent() {
		this.local = true;
	}
	
	/**
	 * Set this to true to invalidate all table dependent cached objects.
	 */
	public void setInvalidateAll(boolean isInvalidateAll) {
		this.invalidateAll = isInvalidateAll;
	}

	/**
	 * Return true if all table states should be invalidated. This will cause
	 * all cached objects to be invalidated.
	 */
	public boolean isInvalidateAll() {
		return invalidateAll;
	}
	
	/**
     * Temporarily pause/ignore any index invalidation for this bean type.
     */
	public void pauseIndexInvalidate(Class<?> beanType) {
	    if (pauseIndexInvalidate == null){
	        pauseIndexInvalidate = new HashSet<String>();
	    }
	    pauseIndexInvalidate.add(beanType.getName());
	}
	
    /**
     * Resume listening for index invalidation for this bean type.
     */
	public void resumeIndexInvalidate(Class<?> beanType) {
	    if (pauseIndexInvalidate != null){
            pauseIndexInvalidate.remove(beanType.getName());
        }
    }
	
	/**
	 * Add an IndexInvalidation notices to the transaction.
	 */
	public void addIndexInvalidate(IndexInvalidate indexEvent){
	    if (pauseIndexInvalidate != null && pauseIndexInvalidate.contains(indexEvent.getIndexName())){
	        logger.debug("--- IGNORE Invalidate on "+indexEvent.getIndexName());
	        return;
	    }
	    if (indexInvalidations == null){
	        indexInvalidations = new HashSet<IndexInvalidate>();
	    }
	    indexInvalidations.add(indexEvent);
	}
	
	public void addDeleteById(BeanDescriptor<?> desc, Object id){
	    if (deleteByIdMap == null){
	        deleteByIdMap = new DeleteByIdMap();
	    }
	    deleteByIdMap.add(desc, id);
	}
	
    public void addDeleteByIdList(BeanDescriptor<?> desc, List<Object> idList) {
        if (deleteByIdMap == null) {
            deleteByIdMap = new DeleteByIdMap();
        }
        deleteByIdMap.addList(desc, idList);
    }
	
    public DeleteByIdMap getDeleteByIdMap() {
        return deleteByIdMap;
    }

    public void addBeanDelta(BeanDelta delta) {
        if (beanDeltas == null) {
            beanDeltas = new ArrayList<BeanDelta>();
        }
        beanDeltas.add(delta);
    }

    public List<BeanDelta> getBeanDeltas() {
        return beanDeltas;
    }
	
	/**
	 * Return true if this was a local transaction. Returns false if this
	 * transaction originated on another server in the cluster.
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * For BeanListeners the requests they are interested in.
	 */
	public TransactionEventBeans getEventBeans() {
		return eventBeans;
	}

	public TransactionEventTable getEventTables() {
		return eventTables;
	}
	
	public Set<IndexInvalidate> getIndexInvalidations() {
        return indexInvalidations;
    }

    public void add(String tableName, boolean inserts, boolean updates, boolean deletes){
		if (eventTables == null){
			eventTables = new TransactionEventTable();
		}
		eventTables.add(tableName, inserts, updates, deletes);		
	}
	
	public void add(TransactionEventTable table){
		if (eventTables == null){
			eventTables = new TransactionEventTable();
		}
		eventTables.add(table);
	}
	
	/**
	 * Add a inserted updated or deleted bean to the event.
	 */
	public void add(PersistRequestBean<?> request) {

		if (request.isNotify(this)){
			// either a BeanListener or Cache is interested
			if (eventBeans == null) {
				eventBeans = new TransactionEventBeans();
			}
			eventBeans.add(request);
		}
	}

	/**
	 * Notify the cache of bean changes.
	 * <p>
	 * This returns the TransactionEventTable so that if any 
	 * general table changes can also be used to invalidate 
	 * parts of the cache.
	 * </p>
	 */
	public void notifyCache(){
		if (eventBeans != null){
			eventBeans.notifyCache();
		}
		if (deleteByIdMap != null) {
		    deleteByIdMap.notifyCache();
		}
	}

}
