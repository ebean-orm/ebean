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
  StringParser getStringParser();

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
  int getJdbcType();

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
  String getAssocIdExpression(String propName, String bindOperator);

  /**
   * Return the Id values for the given bean value.
   */
  Object[] getAssocIdValues(EntityBean bean);
}
