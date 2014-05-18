package com.avaje.ebeaninternal.api;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;


/**
 * An expression that becomes part of a Where clause or Having clause.
 */
public interface SpiExpression extends Expression {

  /**
   * Process "Many" properties populating ManyWhereJoins.
   * <p>
   * Predicates on Many properties require an extra independent join clause.
   * </p>
   */
	public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins);
	
	/**
	 * Calculate a hash value used to identify a query for AutoFetch tuning.
	 * <p>
	 * That is, if the hash changes then the query will be considered different
	 * from an AutoFetch perspective and get different tuning.
	 * </p>
	 */
	public void queryAutoFetchHash(HashQueryPlanBuilder builder);
	
	/**
	 * Calculate a hash value for the expression.
	 * This includes the expression type and property but should exclude
	 * the bind values.
	 * <p>
	 * This is used where queries are the same except for the bind values, in which
	 * case the query execution plan can be reused.
	 * </p>
	 */
	public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder);
	
	/**
	 * Return the hash value for the values that will be bound.
	 */
	public int queryBindHash();
	
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
	 * @param request
	 *            the associated request.
	 */
	public void addSql(SpiExpressionRequest request);

	/**
	 * Add the parameter values to be set against query. For each ? place holder
	 * there should be a corresponding value that is added to the bindList.
	 * 
	 * @param request
	 *            the associated request.
	 */
	public void addBindValues(SpiExpressionRequest request);
}
