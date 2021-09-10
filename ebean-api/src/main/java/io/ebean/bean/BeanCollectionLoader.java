package io.ebean.bean;

/**
 * Loads a entity bean collection.
 * <p>
 * Typically invokes lazy loading for a single or batch of collections.
 * </p>
 */
public interface BeanCollectionLoader {

  /**
   * Return the name of the associated Database.
   */
  String name();

  /**
   * Invoke the lazy loading for this bean collection.
   */
  void loadMany(BeanCollection<?> collection, boolean onlyIds);

}
