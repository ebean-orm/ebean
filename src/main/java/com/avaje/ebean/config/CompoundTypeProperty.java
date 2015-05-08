package com.avaje.ebean.config;

/**
 * Represents a Property of a Compound Value Object.
 * <p>
 * For each property in a {@link CompoundType} you need an implementation of
 * this CompoundTypeProperty interface.
 * 
 * </p>
 * 
 * @author rbygrave
 * 
 * @param <V>
 *          The type of the Compound value object
 * @param <P>
 *          The type of the property
 * 
 * @see CompoundType
 * @see ScalarTypeConverter
 */
public interface CompoundTypeProperty<V, P> {

  /**
   * The name of this property.
   */
  String getName();

  /**
   * Return the property value from the containing compound value object.
   * 
   * @param valueObject
   *          the compound value object
   * @return the property value.
   */
  P getValue(V valueObject);

  /**
   * This should <b>ONLY</b> be used when the persistence type is different from
   * the logical type returned. It most cases just return 0 and Ebean will
   * persist the logical type.
   * <p>
   * Typically this should be used when the logical type is long but the
   * persistence type is java.sql.Timestamp. In this case return
   * java.sql.Types.TIMESTAMP (rather than 0).
   * </p>
   * 
   * @return Return the java.sql.Type that you want to use to persist this
   *         property or 0 and Ebean will use the logical type.
   */
  int getDbType();
}
