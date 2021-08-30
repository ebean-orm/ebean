package io.ebeaninternal.server.persist;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Used to sort BatchedBeanHolder by their depth.
 * <p>
 * Beans are queued and put into BatchedBeanHolder along with their depth. This
 * delays the actually binding to PreparedStatements until the
 * BatchedBeanHolder's are flushed. This is so that we can get the generated
 * keys from inserts. These values are required to persist the 'detail' beans.
 * </p>
 */
final class BatchDepthComparator implements Comparator<BatchedBeanHolder>, Serializable {

  private static final long serialVersionUID = 264611821665757991L;

  @Override
  public int compare(BatchedBeanHolder b1, BatchedBeanHolder b2) {

    return Integer.compare(b1.getOrder(), b2.getOrder());
  }

}
