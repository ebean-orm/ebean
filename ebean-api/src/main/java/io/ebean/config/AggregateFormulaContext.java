package io.ebean.config;

import java.util.Set;

/**
 * Used when parsing formulas to determine if they are aggregation formulas like
 * sum, min, max, avg, count etc.
 * <p>
 * Ebean needs to determine if they are aggregation formulas to determine which
 * properties should be included in a GROUP BY clause etc.
 */
public interface AggregateFormulaContext {

  /**
   * Return true if the outer function is an aggregate function (like sum, count, min, max, avg etc).
   */
  boolean isAggregate(String outerFunction);

  /**
   * Return true if the aggregate function returns a BIGINT type.
   * This is true for functions like count that return a numeric value regardless of the
   * type of the property or expression inside the outer function.
   */
  boolean isCount(String outerFunction);

  /**
   * Return true if the aggregate function returns a VARCHAR type.
   * This is true for functions that return a string concatenation like group_concat etc
   * regardless of the type of the property used inside the outer function.
   */
  boolean isConcat(String outerFunction);

  /**
   * Return a builder for the AggregateFormulaContext.
   */
  static Builder builder() {
    return new AggregateFormulaContextBuilder();
  }

  /**
   * A builder for the AggregateFormulaContext.
   */
  interface Builder {

    /**
     * Override the default set of aggregation functions.
     */
    Builder aggregateFunctions(Set<String> count);

    /**
     * Override the default set of concat functions.
     */
    Builder concatFunctions(Set<String> concat);

    /**
     * Override the default set of count functions.
     */
    Builder countFunctions(Set<String> count);

    /**
     * Build the AggregateFormulaContext.
     */
    AggregateFormulaContext build();
  }

}
