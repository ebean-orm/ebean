package com.avaje.ebeaninternal.server.deploy;

import java.util.Collection;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.BeanCollection;

/**
 * Utility methods for BeanCollections.
 */
public class BeanCollectionUtil {

  /**
   * Return the details of the collection or map taking care to avoid
   * unnecessary fetching of the data.
   */
  public static Collection<?> getActualEntries(Object o) {
    if (o == null) {
      return null;
    }
    if (o instanceof BeanCollection<?>) {
      BeanCollection<?> bc = (BeanCollection<?>) o;
      if (!bc.isPopulated()) {
        return null;
      }
      // For maps this is a collection of Map.Entry, otherwise it
      // returns a collection of beans
      return bc.getActualEntries();
    }

    if (o instanceof Map<?, ?>) {
      // yes, we want the entrySet (to set the keys)
      return ((Map<?, ?>) o).entrySet();

    } else if (o instanceof Collection<?>) {
      return ((Collection<?>) o);
    }
    throw new PersistenceException("expecting a Map or Collection but got [" + o.getClass().getName() + "]");
  }
}
