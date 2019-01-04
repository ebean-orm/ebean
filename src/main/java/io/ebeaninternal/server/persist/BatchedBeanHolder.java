package io.ebeaninternal.server.persist;

import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.IdentityHashMap;

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

  private static final Object DUMMY = new Object();

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

  /**
   * Set of beans in this batch. This is used to ensure that a single bean instance is not included
   * in the batch twice (two separate insert requests etc).
   */
  private final IdentityHashMap<Object, Object> persistedBeans = new IdentityHashMap<>();

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
  void executeNow() throws BatchedSqlException {
    // process the requests. Creates one or more PreparedStatements
    // with binding addBatch() for each request.
    // Note updates and deletes can result in many PreparedStatements
    // if their where clauses differ via use of IS NOT NULL.
    if (deletes != null && !deletes.isEmpty()) {
      ArrayList<PersistRequest> bufferedDeletes = deletes;
      deletes = new ArrayList<>();
      control.executeNow(bufferedDeletes);
    }
    if (inserts != null && !inserts.isEmpty()) {
      ArrayList<PersistRequest> bufferedInserts = inserts;
      inserts = new ArrayList<>();
      control.executeNow(bufferedInserts);
    }
    if (updates != null && !updates.isEmpty()) {
      ArrayList<PersistRequest> bufferedUpdates = updates;
      updates = new ArrayList<>();
      control.executeNow(bufferedUpdates);
    }
    persistedBeans.clear();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(shortDesc.length() + 18);
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

    Object alreadyInBatch = persistedBeans.put(request.getEntityBean(), DUMMY);
    if (alreadyInBatch != null) {
      // special case where the same bean instance has already been
      // added to the batch (doesn't really occur with non-batching
      // as the bean gets changed from dirty to loaded earlier)
      return 0;
    }

    request.setBatched();

    switch (request.getType()) {
      case INSERT:
        if (inserts == null) {
          inserts = new ArrayList<>();
        }
        inserts.add(request);
        return inserts.size();

      case UPDATE:
      case DELETE_SOFT:
        if (updates == null) {
          updates = new ArrayList<>();
        }
        updates.add(request);
        return updates.size();

      case DELETE:
        if (deletes == null) {
          deletes = new ArrayList<>();
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
    return persistedBeans.isEmpty();
  }
}
