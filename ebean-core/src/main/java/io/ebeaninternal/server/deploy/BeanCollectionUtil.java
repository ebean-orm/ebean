package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;

import jakarta.persistence.PersistenceException;
import java.util.Collection;
import java.util.Map;

/**
 * Utility methods for BeanCollections.
 */
public final class BeanCollectionUtil {

  /**
   * Return true if this is a bean collection and not considered dirty.
   */
  public static boolean isModified(Object collection) {
    if ((collection instanceof BeanCollection<?>)) {
      return ((BeanCollection<?>) collection).holdsModifications();
    }
    return true;
  }

  /**
   * Return the details (map entry set) of the collection or map taking care to avoid
   * unnecessary fetching of the data.
   */
  public static Collection<?> getActualEntries(Object o) {
    if (o == null) {
      return null;
    }
    if (o instanceof BeanCollection<?>) {
      BeanCollection<?> bc = (BeanCollection<?>) o;
      if (!bc.isPopulated()) {
        return bc.getLazyAddedEntries(true);
      }
      // For maps this is a collection of Map.Entry, otherwise it
      // returns a collection of beans
      return bc.actualEntries();
    }
    if (o instanceof Collection<?>) {
      return ((Collection<?>) o);
    } else if (o instanceof Map<?, ?>) {
      return ((Map<?, ?>) o).entrySet();
    }
    throw new PersistenceException("expecting a Map or Collection but got " + o.getClass().getName());
  }

  /**
   * Return the details (map values) of the collection or map taking care to avoid
   * unnecessary fetching of the data.
   */
  public static Collection<?> getActualDetails(Object o) {
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
      return bc.actualDetails();
    }
    if (o instanceof Map<?, ?>) {
      // yes, we want the entrySet (to set the keys)
      return ((Map<?, ?>) o).values();

    } else if (o instanceof Collection<?>) {
      return ((Collection<?>) o);
    }
    throw new PersistenceException("expecting a Map or Collection but got " + o.getClass().getName());
  }
}
