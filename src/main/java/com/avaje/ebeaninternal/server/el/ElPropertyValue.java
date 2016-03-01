package com.avaje.ebeaninternal.server.el;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.plugin.ExpressionPath;
import com.avaje.ebean.text.StringParser;

/**
 * The expression language object that can get values.
 * <p>
 * This can be used for local sorting and filtering.
 * </p>
 */
public interface ElPropertyValue extends ElPropertyDeploy, ExpressionPath {

  /**
   * Return the Id values for the given bean value.
   */
  Object[] getAssocOneIdValues(EntityBean bean);

  /**
   * Return the Id expression string.
   * <p>
   * Typically used to produce id = ? expression strings.
   * </p>
   */
  String getAssocOneIdExpr(String prefix, String operator);

  /**
   * Return the logical id value expression taking into account embedded id's.
   */
  String getAssocIdInValueExpr(int size);

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  String getAssocIdInExpr(String prefix);

  /**
   * Return true if this is an ManyToOne or OneToOne associated bean property.
   */
  boolean isAssocId();

  /**
   * Return true if any path of this path contains a Associated One or Many.
   */
  boolean isAssocProperty();

  /**
   * Return true if the property is encrypted via Java.
   */
  boolean isLocalEncrypted();

  /**
   * Return true if the property is encrypted in the DB.
   */
  boolean isDbEncrypted();

  /**
   * Return the deploy order for the property.
   */
  int getDeployOrder();

  /**
   * Return the default StringParser for the scalar property.
   */
  StringParser getStringParser();

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
   * For DateTime capable scalar types convert the long systemTimeMillis into
   * an appropriate java time (Date,Timestamp,Time,Calendar, JODA type etc).
   */
  Object parseDateTime(long systemTimeMillis);

  /**
   * Return the value from a given entity bean.
   */
  Object elGetValue(EntityBean bean);

  /**
   * Return the value ensuring objects prior to the top scalar property are
   * automatically populated.
   */
  Object elGetReference(EntityBean bean);

  /**
   * Set a value given a root level bean.
   * <p>
   * If populate then
   * </p>
   */
  void elSetValue(EntityBean bean, Object value, boolean populate);

  /**
   * Convert the value to the expected type.
   * <p>
   * Typically useful for converting strings to the appropriate number type
   * etc.
   * </p>
   */
  Object elConvertType(Object value);
}
