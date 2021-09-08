package io.ebean.plugin;

import io.ebean.bean.EntityBean;
import io.ebean.text.StringParser;

/**
 * A dot notation expression path.
 */
public interface ExpressionPath {

  /**
   * Return true if there is a property on the path that is a many property.
   */
  boolean containsMany();

  /**
   * Return the value from a given entity bean.
   */
  Object pathGet(Object bean);

  /**
   * Set a value to the bean for this expression path.
   *
   * @param bean  the bean to set the value on
   * @param value the value to set
   */
  void pathSet(Object bean, Object value);

  /**
   * Convert the value to the expected type.
   * <p>
   * Typically useful for converting strings to the appropriate number type etc.
   * </p>
   */
  Object convert(Object value);

  /**
   * Return the default StringParser for the scalar property.
   */
  StringParser stringParser();

  /**
   * Deprecated migrate to stringParser().
   */
  @Deprecated
  default StringParser getStringParser() {
    return stringParser();
  }

  /**
   * For DateTime capable scalar types convert the long systemTimeMillis into
   * an appropriate java time (Date,Timestamp,Time,Calendar, JODA type etc).
   */
  Object parseDateTime(long systemTimeMillis);

  /**
   * Return true if the last type is "DateTime capable" - can support
   * {@link #parseDateTime(long)}.
   */
  boolean isDateTimeCapable();

  /**
   * Return the underlying JDBC type or 0 if this is not a scalar type.
   */
  int jdbcType();

  /**
   * Deprecated migrate to jdbcType().
   */
  @Deprecated
  default int getJdbcType() {
    return jdbcType();
  }

  /**
   * Return true if this is an ManyToOne or OneToOne associated bean property.
   */
  boolean isAssocId();

  /**
   * Return the Id expression string.
   * <p>
   * Typically used to produce id = ? expression strings.
   * </p>
   */
  String assocIdExpression(String propName, String bindOperator);

  /**
   * Deprecated migrate to assocIdExpression().
   */
  @Deprecated
  default String getAssocIdExpression(String propName, String bindOperator) {
    return assocIdExpression(propName, bindOperator);
  }

  /**
   * Return the Id values for the given bean value.
   */
  Object[] assocIdValues(EntityBean bean);

  /**
   * Deprecated migrate to assocIdValues().
   */
  @Deprecated
  default Object[] getAssocIdValues(EntityBean bean) {
    return assocIdValues(bean);
  }

  /**
   * Return the underlying bean property.
   */
  Property property();

  /**
   * Deprecated migrate to property().
   */
  @Deprecated
  default Property getProperty() {
    return property();
  }

  /**
   * The ElPrefix plus name.
   */
  String elName();

  /**
   * Deprecated migrate to elName().
   */
  @Deprecated
  default String getElName() {
    return elName();
  }
}
