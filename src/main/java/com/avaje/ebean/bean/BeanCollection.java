package com.avaje.ebean.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.avaje.ebean.ExpressionList;

/**
 * Lazy loading capable Maps, Lists and Sets.
 * <p>
 * This also includes the ability to listen for additions and removals to or
 * from the Map Set or List. The purpose of gathering the additions and removals
 * is to support persisting ManyToMany objects. The additions and removals
 * become inserts and deletes from the intersection table.
 * </p>
 * <p>
 * Technically this is <em>NOT</em> an extension of
 * <em>java.util.Collection</em>. The reason being that java.util.Map is not a
 * Collection. I realise this makes this name confusing so I apologise for that.
 * </p>
 */
public interface BeanCollection<E> extends Serializable {

  enum ModifyListenMode {
    /** The common mode */
    NONE,
    /** Mode used for PrivateOwned */
    REMOVALS,
    /** Mode used for ManyToMany relationships */
    ALL
  }

  /**
   * Reset the collection back to an empty state ready for reloading.
   * <p>
   * This is done as part of bean refresh.
   */
  void reset(EntityBean ownerBean, String propertyName);

  /**
   * Return true if the collection is empty and untouched. Used to detect if a
   * collection was 'cleared' deliberately or just un-initialised.
   */
  boolean isEmptyAndUntouched();

  /**
   * Return the bean that owns this collection.
   */
  EntityBean getOwnerBean();

  /**
   * Return the bean property name this collection represents.
   */
  String getPropertyName();

  /**
   * Check after the lazy load that the underlying collection is not null
   * (handle case where join to many not outer).
   * <p>
   * That is, if the collection was not loaded due to filterMany predicates etc
   * then make sure the collection is set to empty.
   * </p>
   */
  boolean checkEmptyLazyLoad();

  /**
   * Return the filter (if any) that was used in building this collection.
   * <p>
   * This is so that the filter can be applied on refresh.
   * </p>
   */
  ExpressionList<?> getFilterMany();

  /**
   * Set the filter that was used in building this collection.
   */
  void setFilterMany(ExpressionList<?> filterMany);

  /**
   * Set a listener to be notified when the BeanCollection is first touched.
   */
  void setBeanCollectionTouched(BeanCollectionTouched notify);

  /**
   * Return true if the collection has been registered with the batch loading context.
   */
  boolean isRegisteredWithLoadContext();

  /**
   * Set the loader that will be used to lazy/query load this collection.
   * <p>
   * This is effectively the batch loading context this collection is registered with.
   * </p>
   */
  void setLoader(BeanCollectionLoader beanLoader);

  /**
   * Set to true if you want the BeanCollection to be treated as read only. This
   * means no elements can be added or removed etc.
   */
  void setReadOnly(boolean readOnly);

  /**
   * Return true if the collection should be treated as readOnly and no elements
   * can be added or removed etc.
   */
  boolean isReadOnly();

  /**
   * Add the bean to the collection.
   * <p>
   * This is disallowed for BeanMap.
   * </p>
   */
  void internalAdd(Object bean);

  /**
   * Return the number of elements in the List Set or Map.
   */
  int size();

  /**
   * Return true if the List Set or Map is empty.
   */
  boolean isEmpty();

  /**
   * Returns the underlying collection of beans from the Set, Map or List.
   */
  Collection<E> getActualDetails();

  /**
   * Returns the underlying entries so for Maps this is a collection of
   * Map.Entry.
   * <p>
   * For maps this returns the entrySet as we need the keys of the map.
   * </p>
   */
  Collection<?> getActualEntries();

  /**
   * return true if there are real rows held. Return false is this is using
   * Deferred fetch to lazy load the rows and the rows have not yet been
   * fetched.
   */
  boolean isPopulated();

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  boolean isReference();

  /**
   * Set modify listening on or off. This is used to keep track of objects that
   * have been added to or removed from the list set or map.
   * <p>
   * This is required only for ManyToMany collections. The additions and
   * deletions are used to insert or delete entries from the intersection table.
   * Otherwise modifyListening is false.
   * </p>
   */
  void setModifyListening(ModifyListenMode modifyListenMode);

  /**
   * Add an object to the additions list.
   * <p>
   * This will potentially end up as an insert into a intersection table for a
   * ManyToMany.
   * </p>
   */
  void modifyAddition(E bean);

  /**
   * Add an object to the deletions list.
   * <p>
   * This will potentially end up as an delete from an intersection table for a
   * ManyToMany.
   * </p>
   */
  void modifyRemoval(Object bean);

  /**
   * Return the list of objects added to the list set or map. These will used to
   * insert rows into the intersection table of a ManyToMany.
   */
  Set<E> getModifyAdditions();

  /**
   * Return the list of objects removed from the list set or map. These will
   * used to delete rows from the intersection table of a ManyToMany.
   */
  Set<E> getModifyRemovals();

  /**
   * Reset the set of additions and deletions. This is called after the
   * additions and removals have been processed.
   */
  void modifyReset();
}
