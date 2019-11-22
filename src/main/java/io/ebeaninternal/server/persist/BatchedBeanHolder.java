package io.ebeaninternal.server.persist;

import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;

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
class BatchedBeanHolder {

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

}
