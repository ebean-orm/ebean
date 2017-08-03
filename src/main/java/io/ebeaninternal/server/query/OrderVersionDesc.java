package io.ebeaninternal.server.query;

import io.ebean.Version;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Comparator;

/**
 * Compare Version beans in descending order with nulls last.
 */
class OrderVersionDesc implements Comparator<Version<?>>, Serializable {

  static final OrderVersionDesc INSTANCE = new OrderVersionDesc();

  private static final long serialVersionUID = -3681686029998263310L;

  @Override
  public int compare(Version<?> o1, Version<?> o2) {

    Timestamp v1 = o1.getStart();
    if (v1 == null) {
      return 1;
    }
    Timestamp v2 = o2.getStart();
    if (v2 == null) {
      return -1;
    }
    return v1.compareTo(v2) * -1;
  }

}
