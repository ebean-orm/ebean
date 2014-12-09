package com.avaje.ebeaninternal.server.persist;

import java.util.ArrayList;
import java.util.HashSet;

import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Holds lists of persist requests for beans of a given type.
 * <p>
 * This is used to delay the actual binding of the bean to PreparedStatements.
 * The reason is that we don't have all the bind values yet in the case of inserts
 * with getGeneratedKeys.
 * </p>
 * <p>
 * Has a depth which is used to determine the order in which it should be
 * executed. The lowest depth is executed first.
 * </p>
 */
public class BatchedBeanHolder {

  /**
   * The owning queue.
   */
  private final BatchControl control;

  private final String shortDesc;

  /**
   * The 'depth' which is used to determine the execution order.
   */
  private final int order;

  /**
   * The list of bean insert requests.
   */
  private ArrayList<PersistRequest> inserts;

  /**
   * The list of bean update requests.
   */
  private ArrayList<PersistRequest> updates;

  /**
   * The list of bean delete requests.
   */
  private ArrayList<PersistRequest> deletes;

  private HashSet<Integer> beanHashCodes = new HashSet<Integer>();

  /**
   * Create a new entry with a given type and depth.
   */
  public BatchedBeanHolder(BatchControl control, BeanDescriptor<?> beanDescriptor, int order) {
    this.control = control;
    this.shortDesc = beanDescriptor.getName() + ":" + order;
    this.order = order;
  }

  /**
   * Return the depth.
   */
  public int getOrder() {
    return order;
  }

  /**
   * Execute all the persist requests in this entry.
   * <p>
   * This will Batch all the similar requests into one or more BatchStatements
   * and then execute them.
   * </p>
   */
  public void executeNow() {
    // process the requests. Creates one or more PreparedStatements
    // with binding addBatch() for each request.
    // Note updates and deletes can result in many PreparedStatements
    // if their where clauses differ via use of IS NOT NULL.
    if (inserts != null && !inserts.isEmpty()) {
      control.executeNow(inserts);
      inserts.clear();
    }
    if (updates != null && !updates.isEmpty()) {
      control.executeNow(updates);
      updates.clear();
    }
    if (deletes != null && !deletes.isEmpty()) {
      control.executeNow(deletes);
      deletes.clear();
    }
    beanHashCodes.clear();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(shortDesc.length()+18);
    sb.append(shortDesc);
    if (inserts != null) {
      sb.append(" i:").append(inserts.size());
    }
    if (updates != null) {
      sb.append(" u:").append(updates.size());
    }
    if (deletes != null) {
      sb.append(" d:").append(deletes.size());
    }
    return sb.toString();
  }

  /**
   * Add the request to the appropriate persist list.
   */
  public int append(PersistRequestBean<?> request) {

    Integer objHashCode = new Integer(System.identityHashCode(request.getEntityBean()));
    if (!beanHashCodes.add(objHashCode)) {
      // special case where the same bean instance has already been
      // added to the batch (doesn't really occur with non-batching
      // as the bean gets changed from dirty to loaded earlier)
      return 0;
    }

    request.setBatched();

    switch (request.getType()) {
      case INSERT:
        if (inserts == null) {
          inserts = new ArrayList<PersistRequest>();
        }
        inserts.add(request);
        return inserts.size();

      case UPDATE:
        if (updates == null) {
          updates = new ArrayList<PersistRequest>();
        }
        updates.add(request);
        return updates.size();

      case DELETE:
        if (deletes == null) {
          deletes = new ArrayList<PersistRequest>();
        }
        deletes.add(request);
        return deletes.size();

      default:
        throw new RuntimeException("Invalid type code " + request.getType());
    }
  }

  /**
   * Return true if this is empty containing no batched beans.
   */
  public boolean isEmpty() {
    return beanHashCodes.isEmpty();
  }
}
