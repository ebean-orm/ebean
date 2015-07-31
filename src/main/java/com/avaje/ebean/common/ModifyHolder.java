package com.avaje.ebean.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
  private Set<E> modifyDeletions = new LinkedHashSet<E>();

  /**
   * Additions list for manyToMany persistence.
   */
  private Set<E> modifyAdditions = new LinkedHashSet<E>();

  void reset() {
    modifyDeletions = new LinkedHashSet<E>();
    modifyAdditions = new LinkedHashSet<E>();
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

  void modifyAddition(E bean) {
    if (bean != null) {
      // If it is to delete then just remove the deletion
      if (!modifyDeletions.remove(bean)) {
        // Insert
        modifyAdditions.add(bean);
      }
    }
  }

  @SuppressWarnings("unchecked")
  void modifyRemoval(Object bean) {
    if (bean != null) {
      // If it is to be added then just remove the addition
      if (!modifyAdditions.remove(bean)) {
        modifyDeletions.add((E) bean);
      }
    }
  }

  Set<E> getModifyAdditions() {
    return modifyAdditions;
  }

  Set<E> getModifyRemovals() {
    return modifyDeletions;
  }
}
