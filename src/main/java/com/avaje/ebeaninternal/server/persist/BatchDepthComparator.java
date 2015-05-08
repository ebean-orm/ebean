package com.avaje.ebeaninternal.server.persist;

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
public class BatchDepthComparator implements Comparator<BatchedBeanHolder>, Serializable {

  private static final long serialVersionUID = 264611821665757991L;

  public int compare(BatchedBeanHolder b1, BatchedBeanHolder b2) {

    if (b1.getOrder() < b2.getOrder()) {
      return -1;
    }
    if (b1.getOrder() == b2.getOrder()) {
      return 0;
    }
    return 1;
  }

}
