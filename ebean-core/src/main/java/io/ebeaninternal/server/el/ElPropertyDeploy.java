package io.ebeaninternal.server.el;

import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to parse expressions in queries (where, orderBy etc).
 * <p>
 * Maps an expression to deployment information such as
 * the DB column and elPrefix/elPlaceHolder is used determine
 * joins and set place holders for table alias'.
 * </p>
 */
public interface ElPropertyDeploy {

  /**
   * This is the elPrefix for all root level properties.
   */
  String ROOT_ELPREFIX = "${}";

  /**
   * Return true if the property is a formula with a join clause.
   */
  boolean containsFormulaWithJoin();

  /**
   * Return true if there is a property on the path that is a many property.
   */
  boolean containsMany();

  /**
   * Return true if there is a property is on the path after sinceProperty
   * that is a 'many' property.
   */
  boolean containsManySince(String sinceProperty);

  /**
   * Return the prefix path of the property.
   * <p>
   * This is use to determine joins required to support
   * this property.
   * </p>
   */
  String elPrefix();

  /**
   * Return the place holder in the form of ${elPrefix}dbColumn.
   * <p>
   * The ${elPrefix} is replaced by the appropriate table alias.
   * </p>
   */
  String elPlaceholder(boolean encrypted);

  /**
   * Return the name of the property.
   */
  String name();

  /**
   * The ElPrefix plus name.
   */
  String elName();

  /**
   * Return the deployment db column for this property.
   */
  String dbColumn();

  /**
   * Return the underlying bean property.
   */
  BeanProperty beanProperty();

  /**
   * Return true if this is an aggregation property.
   */
  boolean isAggregation();

  /**
   * Return the fetch preference. This can be used to control which ToMany relationship
   * is left as a 'join' and which get converted to query join.
   */
  int fetchPreference();
}
