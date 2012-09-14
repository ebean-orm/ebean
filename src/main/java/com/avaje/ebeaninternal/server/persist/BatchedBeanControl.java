package com.avaje.ebeaninternal.server.persist;

import java.util.ArrayList;
import java.util.HashMap;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Holds all the batched beans.
 * <p>
 * The beans are held here which delays the binding to a PreparedStatement. This
 * 'delayed' binding is required as the beans need to be bound and executed in
 * the correct order (according to the depth).
 * </p>
 */
public class BatchedBeanControl {

	/**
	 * Map of the BatchedBeanHolder objects. They each have a depth and are later
	 * sorted by their depth to get the execution order.
	 */
	private final HashMap<String, BatchedBeanHolder> beanHoldMap = new HashMap<String, BatchedBeanHolder>();
	
	private final SpiTransaction transaction;

	private final BatchControl batchControl;

	private int topOrder;

	public BatchedBeanControl(SpiTransaction t, BatchControl batchControl) {
		this.transaction = t;
		this.batchControl = batchControl;
	}

  public ArrayList<PersistRequest> getPersistList(PersistRequestBean<?> request) {
    return getBeanHolder(request).getList(request);
  }
	
	/**
	 * Return an entry for the given type description. The type description is
	 * typically the bean class name (or table name for MapBeans).
	 */
	private BatchedBeanHolder getBeanHolder(PersistRequestBean<?> request) {
		
		BeanDescriptor<?> beanDescriptor = request.getBeanDescriptor();
		BatchedBeanHolder batchBeanHolder = beanHoldMap.get(beanDescriptor.getFullName());
		if (batchBeanHolder == null) {
			int relativeDepth = transaction.depth(0);
			if (relativeDepth == 0){
				topOrder++;
			}
			int stmtOrder = topOrder*100 + relativeDepth;
			
			batchBeanHolder = new BatchedBeanHolder(batchControl, beanDescriptor, stmtOrder);
			beanHoldMap.put(beanDescriptor.getFullName(), batchBeanHolder);
		}
		return batchBeanHolder;
	}

	/**
	 * Return true if this holds no persist requests.
	 */
	public boolean isEmpty() {
		return beanHoldMap.isEmpty();
	}

	/**
	 * Return the BatchedBeanHolder's ready for sorting and executing.
	 */
	public BatchedBeanHolder[] getArray() {
		BatchedBeanHolder[] bsArray = new BatchedBeanHolder[beanHoldMap.size()];
		beanHoldMap.values().toArray(bsArray);
		return bsArray;
	}

}
