package io.ebeaninternal.api;

import io.ebean.Expression;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;


/**
 * An expression that becomes part of a Where clause or Having clause.
 */
public interface SpiExpression extends Expression {

  String SQL_TRUE = "1=1";
  String SQL_FALSE = "1=0";

  /**
   * Simplify nested expressions if possible.
   */
  void simplify();

  /**
   * Process "Many" properties populating ManyWhereJoins.
   * <p>
   * Predicates on Many properties require an extra independent join clause.
   * </p>
   */
  void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins);

  /**
   * Prepare the expression. For example, compile sub-query expressions etc.
   */
  void prepareExpression(BeanQueryRequest<?> request);

  /**
   * Calculate a hash value used to identify a query for AutoTune tuning.
   * <p>
   * That is, if the hash changes then the query will be considered different
   * from an AutoTune perspective and get different tuning.
   * </p>
   */
  void queryPlanHash(StringBuilder builder);

  /**
   * Build the key for bind values of the query.
   */
  void queryBindKey(BindValuesKey key);

  /**
   * Return true if the expression is the same with respect to bind values.
   */
  boolean isSameByBind(SpiExpression other);

  /**
   * Add some sql to the query.
   * <p>
   * This will contain ? as a place holder for each associated bind values.
   * </p>
   * <p>
   * The 'sql' added to the query can contain object property names rather
   * than db tables and columns. This 'sql' is later parsed converting the
   * logical property names to their full database column names.
   * </p>
   *
   * @param request the associated request.
   */
  void addSql(SpiExpressionRequest request);

  /**
   * Add the parameter values to be set against query. For each ? place holder
   * there should be a corresponding value that is added to the bindList.
   *
   * @param request the associated request.
   */
  void addBindValues(SpiExpressionRequest request);

  /**
   * Validate all the properties/paths associated with this expression.
   */
  void validate(SpiExpressionValidation validation);

  /**
   * Return a copy of the expression for use in the query plan key.
   */
  SpiExpression copyForPlanKey();

  /**
   * Return the bind Id value if this is a "equal to" expression for the id property.
   */
  Object getIdEqualTo(String idName);

  /**
   * Check for match to a natural key query returning false if it doesn't match.
   */
  boolean naturalKey(NaturalKeyQueryData<?> data);

  /**
   * Apply property prefix when filterMany expressions included into main query.
   */
  void prefixProperty(String path);

  /**
   * Return a copy of the expression (as part of creating a query copy).
   */
  default SpiExpression copy() {
    return this;
  }
}
