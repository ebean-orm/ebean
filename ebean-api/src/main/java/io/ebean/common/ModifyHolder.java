package io.ebean.common;

import io.ebean.bean.EntityBean;

import java.io.Serializable;
import java.util.*;

/**
 * Holds sets of additions and deletions from a 'owner' List Set or Map.
 * <p>
 * These sets of additions and deletions are used to support persisting
 * ManyToMany relationships. The additions becoming inserts into the
 * intersection table and the removals becoming deletes from the intersection
 * table.
 * </p>
 */
class ModifyHolder<E> implements Serializable {

  private static final long serialVersionUID = 2572572897923801083L;

  /**
   * Deletions list for manyToMany persistence.
   */
  private Map<E,Object> modifyDeletions = new IdentityHashMap<>();

  /**
   * Additions list for manyToMany persistence.
   */
  private Map<E,Object> modifyAdditions = new IdentityHashMap<>();

  private boolean touched;

  void reset() {
    touched = false;
    modifyDeletions = new IdentityHashMap<>();
    modifyAdditions = new IdentityHashMap<>();
  }

  /**
   * Used by BeanList.addAll() methods.
   */
  void modifyAdditionAll(Collection<? extends E> c) {
    if (c != null) {
      for (E e : c) {
        modifyAddition(e);
      }
    }
  }

  private boolean undoDeletion(E bean) {
    return (bean != null) && modifyDeletions.remove(bean) != null;
  }

  void modifyAddition(E bean) {
    if (bean != null) {
      touched = true;
      if (bean instanceof EntityBean) {
        ((EntityBean) bean)._ebean_getIntercept().setDeletedFromCollection(false);
      }
      // If it is to delete then just remove the deletion
      if (!undoDeletion(bean)) {
        modifyAdditions.put(bean, bean);
      }
    }
  }

  private boolean undoAddition(Object bean) {
    return (bean != null) && modifyAdditions.remove(bean) != null;
  }

  @SuppressWarnings("unchecked")
  void modifyRemoval(Object bean) {
    if (bean != null) {
      touched = true;
      if (bean instanceof EntityBean) {
        ((EntityBean) bean)._ebean_getIntercept().setDeletedFromCollection(true);
      }
      // If it is to be added then just remove the addition
      if (!undoAddition(bean)) {
        modifyDeletions.put((E) bean, bean);
      }
    }
  }

  Set<E> getModifyAdditions() {
    return modifyAdditions.keySet();
  }

  Set<E> getModifyRemovals() {
    return modifyDeletions.keySet();
  }

  /**
   * Return true if the collection was touched in some way. This is still true even if
   * a bean was removed and then added (which is not held as a modification).
   */
  boolean wasTouched() {
    return touched;
  }

  /**
   * Return true if there additions or removals.
   */
  boolean hasModifications() {
    return !modifyDeletions.isEmpty() || !modifyAdditions.isEmpty();
  }
}
