package io.ebean;

import java.util.Collection;
import java.util.Map;

/**
 * Common interface for DSL specific objects like {@link ExpressionList} (executed by the DB)
 * and {@link Filter} (done in memory).
 *
 * Please see {@link Query} and {@link Filter} for further documentation.
 *
 * @param <T> the bean type
 * @param <F> the fluent type
 */
public interface QueryDsl<T, F extends QueryDsl<T, F>> {

  /**
   * Equal To - property equal to the given value.
   */
  F eq(String propertyName, Object value);

  /**
   * Not Equal To - property not equal to the given value.
   */
  F ne(String propertyName, Object value);

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  F ieq(String propertyName, String value);

  /**
   * Case Insensitive Not Equal To - property not equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  F ine(String propertyName, String value);

  /**
   * Between - property between the two given values.
   */
  F between(String propertyName, Object value1, Object value2);

//  CHECKME: can this be implemented in Filter?
//  /**
//   * Between - value between the two properties.
//   */
//  RET betweenProperties(String lowProperty, String highProperty, Object value);

  /**
   * Greater Than - property greater than the given value.
   */
  F gt(String propertyName, Object value);

  /**
   * Greater Than or Equal to - property greater than or equal to the given value.
   */
  F ge(String propertyName, Object value);

  /**
   * Less Than - property less than the given value.
   */
  F lt(String propertyName, Object value);

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  F le(String propertyName, Object value);

  /**
   * Is Null - property is null.
   */
  F isNull(String propertyName);

  /**
   * Is Not Null - property is not null.
   */
  F isNotNull(String propertyName);

  /**
   * Starts With - property like value%.
   */
  F startsWith(String propertyName, String value);

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  F istartsWith(String propertyName, String value);

  /**
   * Ends With - property like %value.
   */
  F endsWith(String propertyName, String value);

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  F iendsWith(String propertyName, String value);

  /**
   * Contains - property contains the string "value".
   */
  F contains(String propertyName, String value);

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  F icontains(String propertyName, String value);

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  F like(String propertyName, String value);

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  F ilike(String propertyName, String value);

  /**
   * In - using a subQuery.
   */
  F in(String propertyName, Query<?> subQuery);

  /**
   * In - property has a value in the array of values.
   */
  F in(String propertyName, Object... values);

  /**
   * In - property has a value contained in the set of values.
   */
  F in(String propertyName, Collection<?> values);

  /**
   * In - using a subQuery.
   * <p>
   * This is exactly the same as in() and provided due to "in" being a Kotlin keyword
   * (and hence to avoid the slightly ugly escaping when using in() in Kotlin)
   */
  default F isIn(String propertyName, Query<?> subQuery) {
    return in(propertyName, subQuery);
  }

  /**
   * In - property has a value in the array of values.
   * <p>
   * This is exactly the same as in() and provided due to "in" being a Kotlin keyword
   * (and hence to avoid the slightly ugly escaping when using in() in Kotlin)
   */
  default F isIn(String propertyName, Object... values) {
    return in(propertyName, values);
  }

  /**
   * In - property has a value in the collection of values.
   * <p>
   * This is exactly the same as in() and provided due to "in" being a Kotlin keyword
   * (and hence to avoid the slightly ugly escaping when using in() in Kotlin)
   */
  default F isIn(String propertyName, Collection<?> values) {
    return in(propertyName, values);
  }

  /**
   * In expression using pairs of value objects.
   */
  F inPairs(Pairs pairs);

  /**
   * Not In - property has a value in the array of values.
   */
  F notIn(String propertyName, Object... values);

  /**
   * Not In - property has a value in the collection of values.
   */
  F notIn(String propertyName, Collection<?> values);

  /**
   * Not In - using a subQuery.
   */
  F notIn(String propertyName, Query<?> subQuery);

//CHECKME: can this be implemented in Filter?
//  /**
//   * Is empty expression for collection properties.
//   */
//  RET isEmpty(String propertyName);
//
//  /**
//   * Is not empty expression for collection properties.
//   */
//  RET isNotEmpty(String propertyName);

//  /**
//   * Id IN a list of id values.
//   */
//  RET idIn(Object... idValues);
//
//  /**
//   * Id IN a collection of id values.
//   */
//  RET idIn(Collection<?> idValues);
//
//  /**
//   * Id Equal to - ID property is equal to the value.
//   */
//  RET idEq(Object value);

  /**
   * All Equal - Map containing property names and their values.
   * <p>
   * Expression where all the property names in the map are equal to the
   * corresponding value.
   * </p>
   *
   * @param propertyMap a map keyed by property names.
   */
  F allEq(Map<String, Object> propertyMap);

//CHECKME: can this be implemented in Filter?
//  /**
//   * Array property contains entries with the given values.
//   */
//  RET arrayContains(String propertyName, Object... values);
//
//  /**
//   * Array does not contain the given values.
//   * <p>
//   * Array support is effectively limited to Postgres at this time.
//   * </p>
//   */
//  RET arrayNotContains(String propertyName, Object... values);
//
//  /**
//   * Array is empty - for the given array property.
//   * <p>
//   * Array support is effectively limited to Postgres at this time.
//   * </p>
//   */
//  RET arrayIsEmpty(String propertyName);
//
//  /**
//   * Array is not empty - for the given array property.
//   * <p>
//   * Array support is effectively limited to Postgres at this time.
//   * </p>
//   */
//  RET arrayIsNotEmpty(String propertyName);

  /**
   * Add expression for ANY of the given bit flags to be set.
   * <pre>{@code
   *
   * where().bitwiseAny("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  F bitwiseAny(String propertyName, long flags);

  /**
   * Add expression for ALL of the given bit flags to be set.
   * <p>
   * <pre>{@code
   *
   * where().bitwiseAll("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  F bitwiseAll(String propertyName, long flags);

  /**
   * Add expression for the given bit flags to be NOT set.
   * <p>
   * <pre>{@code
   *
   * where().bitwiseNot("flags", BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  F bitwiseNot(String propertyName, long flags);

  /**
   * Add bitwise AND expression of the given bit flags to compare with the match/mask.
   * <p>
   * <pre>{@code
   *
   * // Flags Bulk + Size = Size
   * // ... meaning Bulk is not set and Size is set
   *
   * long selectedFlags = BwFlags.HAS_BULK + BwFlags.HAS_SIZE;
   * long mask = BwFlags.HAS_SIZE; // Only Size flag set
   *
   * where().bitwiseAnd("flags", selectedFlags, mask)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  F bitwiseAnd(String propertyName, long flags, long match);

  /**
   * Start a list of expressions that will be joined by AND's
   * returning the expression list the expressions are added to.
   * <p>
   * This is exactly the same as conjunction();
   * </p>
   * <p>
   * Use endAnd() or endJunction() to end the AND junction.
   * </p>
   * <p>
   * Note that a where() clause defaults to an AND junction so
   * typically you only explicitly need to use the and() junction
   * when it is nested inside an or() or not() junction.
   * </p>
   * <p>
   * <pre>{@code
   *
   *  // Example: Nested and()
   *
   *  Ebean.find(Customer.class)
   *    .where()
   *    .or()
   *      .and() // nested and
   *        .startsWith("name", "r")
   *        .eq("anniversary", onAfter)
   *        .endAnd()
   *      .and()
   *        .eq("status", Customer.Status.ACTIVE)
   *        .gt("id", 0)
   *        .endAnd()
   *      .orderBy().asc("name")
   *      .findList();
   * }</pre>
   */
  F and();

  /**
   * End an AND junction.
   */
  F endAnd();

  /**
   * Return a list of expressions that will be joined by OR's.
   * This is exactly the same as disjunction();
   * <p>
   * <p>
   * Use endOr() or endJunction() to end the OR junction.
   * </p>
   * <p>
   * <pre>{@code
   *
   *  // Example: Use or() to join
   *  // two nested and() expressions
   *
   *  Ebean.find(Customer.class)
   *    .where()
   *    .or()
   *      .and()
   *        .startsWith("name", "r")
   *        .eq("anniversary", onAfter)
   *        .endAnd()
   *      .and()
   *        .eq("status", Customer.Status.ACTIVE)
   *        .gt("id", 0)
   *        .endAnd()
   *      .orderBy().asc("name")
   *      .findList();
   *
   * }</pre>
   */
  F or();

  /**
   * End an OR junction.
   */
  F endOr();

  /**
   * Return a list of expressions that will be wrapped by NOT.
   * <p>
   * Use endNot() or endJunction() to end expressions being added to the
   * NOT expression list.
   * </p>
   * <p>
   * <pre>@{code
   *
   *    .where()
   *      .not()
   *        .gt("id", 1)
   *        .eq("anniversary", onAfter)
   *        .endNot()
   *
   * }</pre>
   * <p>
   * <pre>@{code
   *
   * // Example: nested not()
   *
   * Ebean.find(Customer.class)
   *   .where()
   *     .eq("status", Customer.Status.ACTIVE)
   *     .not()
   *       .gt("id", 1)
   *       .eq("anniversary", onAfter)
   *       .endNot()
   *     .orderBy()
   *       .asc("name")
   *     .findList();
   *
   * }</pre>
   */
  F not();

  /**
   * End a NOT junction.
   */
  F endNot();

  /**
   * Applies the given object to an other QueryDsl object.
   */
  <F2 extends QueryDsl<T, F2>> F applyTo(QueryDsl<T, F2> target);

}
