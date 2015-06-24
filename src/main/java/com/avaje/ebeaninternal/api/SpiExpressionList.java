package com.avaje.ebeaninternal.api;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Internal extension of ExpressionList.
 */
public interface SpiExpressionList<T> extends ExpressionList<T> {

  /**
   * Return the underlying list of expressions.
   */
  List<SpiExpression> getUnderlyingList();
  
  /**
   * Return a copy of the ExpressionList with the path trimmed for filterMany() expressions.
   */
  SpiExpressionList<?> trimPath(int prefixTrim);
    
	/**
	 * Restore the ExpressionFactory after deserialisation.
	 */
	void setExpressionFactory(ExpressionFactory expr);

  /**
   * Process "Many" properties populating ManyWhereJoins.
   * <p>
   * Predicates on Many properties require an extra independent join clause.
   * </p>
   */
  void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoins);
	
	/**
	 * Return true if this list is empty.
	 */
	boolean isEmpty();

	/**
	 * Concatenate the expression sql into a String.
	 * <p>
	 * The list of expressions are evaluated in order building a sql statement
	 * with bind parameters.
	 * </p>
	 */
	String buildSql(SpiExpressionRequest request);

	/**
	 * Combine the expression bind values into a list.
	 * <p>
	 * Expressions are evaluated in order and all the resulting bind values are
	 * returned as a List.
	 * </p>
	 * 
	 * @return the list of all the bind values in order.
	 */
	ArrayList<Object> buildBindValues(SpiExpressionRequest request);
	
  /**
   * Calculate a hash based on the expressions but excluding the actual bind
   * values.
   */
  void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder);

}
