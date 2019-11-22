package io.ebeaninternal.server.core;

import java.util.List;

/**
 * Supports padding bind parameters for IN expressions.
 * <p>
 * We do this in order to get better hit ratio on DB query plans.
 * </p>
 */
public final class BindPadding {

  /**
   * Pad out the Ids into common bucket sizes.
   *
   * @param idCollection The collection of Ids values being bound.
   */
  public static void padIds(List<Object> idCollection) {

    int extraIds = padding(idCollection.size());
    if (extraIds > 0) {
      // for performance make up the Id's to the batch size
      // so we get the same query (for Ebean and the db)
      Object firstId = idCollection.get(0);
      for (int i = 0; i < extraIds; i++) {
        // just add the first Id again
        idCollection.add(firstId);
      }
    }
  }

  /**
   * Extra padding on binding id's in order to get better hit ratio on DB prepared statements / query plans.
   */
  static int padding(int size) {
    if (size == 1) {
      return 0;
    }
    if (size <= 5) {
      return 5 - size;
    }
    if (size <= 10) {
      return 10 - size;
    }
    if (size <= 20) {
      return 20 - size;
    }
    if (size <= 40) {
      return 40 - size;
    }
    if (size <= 50) {
      return 50 - size;
    }
    return size <= 100 ? 100 - size : 0;
  }

}
