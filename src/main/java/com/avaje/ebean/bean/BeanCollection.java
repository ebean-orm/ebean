package com.avaje.ebean.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;

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

  public enum ModifyListenMode {
    /** The common mode */
    NONE,
    /** Mode used for PrivateOwned */
    REMOVALS,
    /** Mode used for ManyToMany relationships */
    ALL
  }

  /**
   * Return the bean that owns this collection.
   */
  public Object getOwnerBean();

  /**
   * Return the bean property name this collection represents.
   */
  public String getPropertyName();

  /**
   * Return the index position of this collection in the lazy/query loader.
   * <p>
   * Used for batch loading of collections.
   * </p>
   */
  public int getLoaderIndex();

  /**
   * Check after the lazy load that the underlying collection is not null
   * (handle case where join to many not outer).
   * <p>
   * That is, if the collection was not loaded due to filterMany predicates etc
   * then make sure the collection is set to empty.
   * </p>
   */
  public boolean checkEmptyLazyLoad();

  /**
   * Return the filter (if any) that was used in building this collection.
   * <p>
   * This is so that the filter can be applied on refresh.
   * </p>
   */
  public ExpressionList<?> getFilterMany();

  /**
   * Set the filter that was used in building this collection.
   */
  public void setFilterMany(ExpressionList<?> filterMany);

  /**
   * Set when this collection is being loaded via a background thread.
   * <p>
   * Refer to {@link Query#setBackgroundFetchAfter(int)}
   * </p>
   */
  public void setBackgroundFetch(Future<Integer> future);

  /**
   * Wait for the fetch to complete with a given timeout.
   * <p>
   * Refer to {@link Query#setBackgroundFetchAfter(int)}
   * </p>
   */
  public void backgroundFetchWait(long wait, TimeUnit timeUnit);

  /**
   * Wait for the fetch to complete.
   * <p>
   * Refer to {@link Query#setBackgroundFetchAfter(int)}
   * </p>
   */
  public void backgroundFetchWait();

  /**
   * Set a listener to be notified when the BeanCollection is first touched.
   */
  public void setBeanCollectionTouched(BeanCollectionTouched notify);

  /**
   * Set the loader that will be used to lazy/query load this collection.
   */
  public void setLoader(int beanLoaderIndex, BeanCollectionLoader beanLoader);

  /**
   * Set to true if you want the BeanCollection to be treated as read only. This
   * means no elements can be added or removed etc.
   */
  public void setReadOnly(boolean readOnly);

  /**
   * Return true if the collection should be treated as readOnly and no elements
   * can be added or removed etc.
   */
  public boolean isReadOnly();

  /**
   * Add the bean to the collection.
   * <p>
   * This is disallowed for BeanMap.
   * </p>
   */
  public void internalAdd(Object bean);

  /**
   * Returns the underlying List Set or Map object.
   */
  public Object getActualCollection();

  /**
   * Return the number of elements in the List Set or Map.
   */
  public int size();

  /**
   * Return true if the List Set or Map is empty.
   */
  public boolean isEmpty();

  /**
   * Returns the underlying details as an iterator.
   * <p>
   * Note that for maps this returns the entrySet as we need the keys of the
   * map.
   * </p>
   */
  public Collection<E> getActualDetails();

  /**
   * Set to true if maxRows was hit and there are actually more rows available.
   * <p>
   * Can be used by client code that is paging through results using
   * setFirstRow() setMaxRows(). If this returns true then the client can
   * display a 'next' button etc.
   * </p>
   */
  public boolean hasMoreRows();

  /**
   * Set to true when maxRows is hit but there are actually more rows available.
   * This is set so that client code knows that there is more data available.
   */
  public void setHasMoreRows(boolean hasMoreRows);

  /**
   * Returns true if the fetch has finished. False if the fetch is continuing in
   * a background thread.
   */
  public boolean isFinishedFetch();

  /**
   * Set to true when a fetch has finished. Used when a fetch continues in the
   * background.
   */
  public void setFinishedFetch(boolean finishedFetch);

  /**
   * return true if there are real rows held. Return false is this is using
   * Deferred fetch to lazy load the rows and the rows have not yet been
   * fetched.
   */
  public boolean isPopulated();

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  public boolean isReference();

  /**
   * Set modify listening on or off. This is used to keep track of objects that
   * have been added to or removed from the list set or map.
   * <p>
   * This is required only for ManyToMany collections. The additions and
   * deletions are used to insert or delete entries from the intersection table.
   * Otherwise modifyListening is false.
   * </p>
   */
  public void setModifyListening(ModifyListenMode modifyListenMode);

  /**
   * Add an object to the additions list.
   * <p>
   * This will potentially end up as an insert into a intersection table for a
   * ManyToMany.
   * </p>
   */
  public void modifyAddition(E bean);

  /**
   * Add an object to the deletions list.
   * <p>
   * This will potentially end up as an delete from an intersection table for a
   * ManyToMany.
   * </p>
   */
  public void modifyRemoval(Object bean);

  /**
   * Return the list of objects added to the list set or map. These will used to
   * insert rows into the intersection table of a ManyToMany.
   */
  public Set<E> getModifyAdditions();

  /**
   * Return the list of objects removed from the list set or map. These will
   * used to delete rows from the intersection table of a ManyToMany.
   */
  public Set<E> getModifyRemovals();

  /**
   * Reset the set of additions and deletions. This is called after the
   * additions and removals have been processed.
   */
  public void modifyReset();
}
