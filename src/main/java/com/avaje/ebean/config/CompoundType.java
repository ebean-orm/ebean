package com.avaje.ebean.config;

/**
 * API from creating and getting property values from an Immutable Compound
 * Value Object.
 * 
 * <p>
 * A Compound Value object should contain multiple properties that are stored
 * separately. If you only have a single scalar value you should instead look to
 * use {@link ScalarTypeConverter}.
 * </p>
 * <p>
 * For each property in the compound type you need to implement the
 * {@link CompoundTypeProperty} interface. These must be returned from
 * {@link #getProperties()} in the same order that the properties appear in the
 * constructor.
 * </p>
 * <p>
 * If your compound type is mutable then you should look to use the JPA Embedded
 * annotation instead of implementing this interface.
 * </p>
 * <p>
 * When using classpath search Ebean will detect and automatically register any
 * implementations of this interface (along with detecting the entity classes
 * etc).
 * </p>
 * 
 * @author rbygrave
 * 
 * @param <V>
 *          The type of the Value Object
 * 
 * @see ScalarTypeConverter
 */
public interface CompoundType<V> {

  /**
   * Create an instance of the compound type given its property values.
   */
  V create(Object[] propertyValues);

  /**
   * Return the properties in the order they appear in the constructor.
   */
  CompoundTypeProperty<V, ?>[] getProperties();
}
