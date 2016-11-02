package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a property to be an aggregation formula.
 * <p>
 * The aggregation formula should be a sum, count, avg, min or max.
 * By default aggregation properties are treated as transient and not
 * included in a query. To populate the aggregation property it must be
 * explicitly included in the select().
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 *
 * @Aggregation("count(details)")
 * Long totalCount;
 *
 * @Aggregation("sum(details.quantity*details.unitPrice)")
 * Long totalAmount;
 *
 * }</pre>
 *
 * <h3>Example query</h3>
 * <pre>{@code
 *
 *  List<TEventOne> list = Ebean.find(TEventOne.class)
 *       .select("name, totalCount, totalUnits, totalAmount")
 *       .where()
 *         .startsWith("logs.description", "a")
 *       .having()
 *         .ge("count", 1)
 *       .orderBy().asc("name")
 *       .findList();
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Aggregation {

  /**
   * Aggregation formula using sum, count, avg, min, max.
   */
  String value();
}
