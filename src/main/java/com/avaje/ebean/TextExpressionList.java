package com.avaje.ebean;

import com.avaje.ebean.search.Match;

/**
 * An list of Full text query expressions.
 * <p>
 * For ElasticSearch these expression go into the "query" section rather than the "filter" section.
 * </p>
 */
public interface TextExpressionList<T> extends ExpressionList<T> {

  /**
   * Add a match expression.
   *
   * @param propertyName The property name for the match
   * @param search The search value
   */
  TextExpressionList<T> match(String propertyName, String search);

  /**
   * Add a match expression with options.
   *
   * @param propertyName The property name for the match
   * @param search The search value
   */
  TextExpressionList<T> match(String propertyName, String search, Match options);

  /**
   * Start a list of expressions that will be joined by MUST.
   */
  TextJunction<T> must();

  /**
   * Start a list of expressions that will be joined by SHOULD.
   */
  TextJunction<T> should();

  /**
   * Start a list of expressions that will be joined by MUST NOT.
   */
  TextJunction<T> mustNot();

  /**
   * End the list of MUST expressions.
   */
  TextExpressionList<T> endMust();

  /**
   * End the list of SHOULD expressions.
   */
  TextExpressionList<T> endShould();

  /**
   * End the list of MUST NOT expressions.
   */
  TextExpressionList<T> endMustNot();
}
