package io.ebean.config;

/**
 * Matches the functionality of javax.persistence.AttributeConverter
 * <p>
 * In general AttributeConverter should be used in preference to this
 * ScalarTypeConverter as it is JPA standard and offers the same functionality.
 * </p>
 * <p>
 * For Ebean we will look to deprecate this interface in preference to AttributeConverter.
 * </p>
 * <p>
 * Used to convert between a value object and a known scalar type. The value
 * object is the logical type used in your application and the scalar type is
 * the value used to persist than to the DB.
 * </p>
 * <p>
 * The Value object should be immutable and scalar (aka not compound) and
 * converts to and from a known scalar type which Ebean will use to persist the
 * value.
 * </p>
 *
 * @param <B> The value object type.
 * @param <S> The scalar object type that is used to persist the value object.
 */
public interface ScalarTypeConverter<B, S> {

  /**
   * Return the value to represent null. Typically this is actually null but for
   * scala.Option and similar type converters this actually returns an instance
   * representing "None".
   */
  B getNullValue();

  /**
   * Convert the scalar type value into the value object.
   * <p>
   * This typically occurs when Ebean reads the value from a resultSet or other
   * data source.
   * </p>
   *
   * @param scalarType the value from the data source
   */
  B wrapValue(S scalarType);

  /**
   * Convert the value object into a scalar value that Ebean knows how to
   * persist.
   * <p>
   * This typically occurs when Ebean is persisting the value object to the data
   * store.
   * </p>
   *
   * @param beanType the value object
   */
  S unwrapValue(B beanType);

}
