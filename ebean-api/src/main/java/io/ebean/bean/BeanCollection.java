package io.ebean.bean;

import io.ebean.ExpressionList;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Lazy loading capable Maps, Lists and Sets.
 * <p>
 * This also includes the ability to listen for additions and removals to or
 * from the Map Set or List. The purpose of gathering the additions and removals
 * is to support persisting ManyToMany objects. The additions and removals
 * become inserts and deletes from the intersection table.
 * <p>
 * Technically this is <em>NOT</em> an extension of
 * <em>java.util.Collection</em>. The reason being that java.util.Map is not a
 * Collection. I realise this makes this name confusing so I apologise for that.
 */
public interface BeanCollection<E> extends Serializable, ToStringAware {

  enum ModifyListenMode {
    /**
     * The common mode
     */
    NONE,
    /**
     * Mode used for PrivateOwned
     */
    REMOVALS,
    /**
     * Mode used for ManyToMany relationships
     */
    ALL
  }

  /**
   * Set the disableLazyLoad state.
   */
  void setDisableLazyLoad(boolean disableLazyLoad);

  /**
   * Load bean from another collection.
   */
  void loadFrom(BeanCollection<?> other);

  /**
   * Add a bean to the list/set with modifyListen notification.
   */
  void addBean(E bean);

  /**
   * Remove a bean to the list/set with modifyListen notification.
   */
  void removeBean(E bean);

  /**
   * Reset the collection back to an empty state ready for reloading.
   * <p>
   * This is done as part of bean refresh.
   */
  void reset(EntityBean ownerBean, String propertyName);

  /**
   * Return true if the collection is uninitialised or is empty without any held modifications.
   * <p>
   * Returning true means can safely skip cascade save for this bean collection.
   */
  boolean isSkipSave();

  /**
   * Return true if the collection holds modifications.
   */
  boolean holdsModifications();

  /**
   * Return the bean that owns this collection.
   */
  EntityBean owner();

  /**
   * Return the bean property name this collection represents.
   */
  String propertyName();

  /**
   * Check after the lazy load that the underlying collection is not null
   * (handle case where join to many not outer).
   * <p>
   * That is, if the collection was not loaded due to filterMany predicates etc
   * then make sure the collection is set to empty.
   */
  boolean checkEmptyLazyLoad();

  /**
   * Return the filter (if any) that was used in building this collection.
   * <p>
   * This is so that the filter can be applied on refresh.
   * </p>
   */
  ExpressionList<?> filterMany();

  /**
   * Set the filter that was used in building this collection.
   */
  void setFilterMany(ExpressionList<?> filterMany);

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
   * Add the bean to the collection. This is disallowed for BeanMap.
   */
  void internalAdd(Object bean);

  /**
   * Add the bean with a check to see if it is already contained.
   */
  void internalAddWithCheck(Object bean);

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
  Collection<E> actualDetails();

  /**
   * Returns the underlying entries so for Maps this is a collection of
   * Map.Entry.
   * <p>
   * For maps this returns the entrySet as we need the keys of the map.
   */
  Collection<?> actualEntries();

  /**
   * Returns entries, that were lazily added at the end of the list. Might be null.
   */
  default Collection<E> getLazyAddedEntries(boolean reset) {
    return null;
  }

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
   * Return true if the collection is modify listening and has modifications.
   */
  boolean hasModifications();

  /**
   * Set modify listening on or off. This is used to keep track of objects that
   * have been added to or removed from the list set or map.
   * <p>
   * This is required only for ManyToMany collections. The additions and
   * deletions are used to insert or delete entries from the intersection table.
   * Otherwise modifyListening is false.
   */
  void setModifyListening(ModifyListenMode modifyListenMode);

  /**
   * Return the current modify listening mode. Can be null for on newly created beans.
   */
  ModifyListenMode modifyListening();

  /**
   * Add an object to the additions list.
   * <p>
   * This will potentially end up as an insert into a intersection table for a
   * ManyToMany.
   */
  void modifyAddition(E bean);

  /**
   * Add an object to the deletions list.
   * <p>
   * This will potentially end up as an delete from an intersection table for a
   * ManyToMany.
   */
  void modifyRemoval(Object bean);

  /**
   * Return the list of objects added to the list set or map. These will used to
   * insert rows into the intersection table of a ManyToMany.
   */
  Set<E> modifyAdditions();

  /**
   * Return the list of objects removed from the list set or map. These will
   * used to delete rows from the intersection table of a ManyToMany.
   */
  Set<E> modifyRemovals();

  /**
   * Reset the set of additions and deletions. This is called after the
   * additions and removals have been processed.
   */
  void modifyReset();

  /**
   * Has been modified by an addition or removal.
   */
  boolean wasTouched();

  /**
   * Freeze the collection returning an unmodifiable version.
   */
  Object freeze();
}
