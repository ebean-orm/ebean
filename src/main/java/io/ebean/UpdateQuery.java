package io.ebean;

/**
 * An update query typically intended to perform a bulk update of many rows that match the query.
 * <p>
 * Also note that you can also just use a raw SQL update via {@link SqlUpdate} which is pretty light and simple.
 * This UpdateQuery is more for the cases where we want to build the where expression of the update using the
 * {@link ExpressionList} "Criteria API" that is used with a normal ORM query.
 * </p>
 * <p>
 * <h4>Example: Simple update</h4>
 * <p>
 * <pre>{@code
 *
 *  int rows = ebeanServer
 *      .update(Customer.class)
 *      .set("status", Customer.Status.ACTIVE)
 *      .set("updtime", new Timestamp(System.currentTimeMillis()))
 *      .where()
 *      .gt("id", 1000)
 *      .update();
 *
 * }</pre>
 * <pre>{@code sql
 *
 *   update o_customer set status=?, updtime=? where id > ?
 *
 * }</pre>
 * <p>
 * Note that if the where() clause contains a join then the SQL update changes to use a
 * <code> WHERE ID IN () </code> form.
 * </p>
 * <p>
 * <h4>Example: Update with a JOIN</h4>
 * <p>
 * In this example the expression <code>.eq("billingAddress.country", nz)</code> requires a join
 * to the address table.
 * </p>
 * <p>
 * <pre>{@code
 *
 *   int rows = ebeanServer
 *       .update(Customer.class)
 *       .set("status", Customer.Status.ACTIVE)
 *       .set("updtime", new Timestamp(System.currentTimeMillis()))
 *       .where()
 *         .eq("status", Customer.Status.NEW)
 *         .eq("billingAddress.country", nz)
 *         .gt("id", 1000)
 *         .update();
 * }</pre>
 * <p>
 * <pre>{@code sql
 *
 *   update o_customer set status=?, updtime=?
 *   where id in (
 *     select t0.id c0
 *     from o_customer t0
 *     left join o_address t1 on t1.id = t0.billing_address_id
 *     where t0.status = ?
 *       and t1.country_code = ?
 *       and t0.id > ? )
 *
 * }</pre>
 *
 * @param <T> The type of entity bean being updated
 * @see SqlUpdate
 */
public interface UpdateQuery<T> {

  /**
   * Set the value of a property.
   * <p>
   * <pre>{@code
   *
   *   int rows = ebeanServer
   *      .update(Customer.class)
   *      .set("status", Customer.Status.ACTIVE)
   *      .set("updtime", new Timestamp(System.currentTimeMillis()))
   *      .where()
   *      .gt("id", 1000)
   *      .update();
   *
   * }</pre>
   *
   * @param property The bean property to be set
   * @param value    The value to set the property to
   */
  UpdateQuery<T> set(String property, Object value);

  /**
   * Set the property to be null.
   * <p>
   * <pre>{@code
   *
   *   int rows = ebeanServer
   *      .update(Customer.class)
   *      .setNull("notes")
   *      .where()
   *      .gt("id", 1000)
   *      .update();
   *
   * }</pre>
   *
   * @param property The property to be set to null.
   */
  UpdateQuery<T> setNull(String property);

  /**
   * Set using a property expression that does not need any bind values.
   * <p>
   * The property expression typically contains database functions.
   * </p>
   * <p>
   * <pre>{@code
   *
   *   int rows = ebeanServer
   *      .update(Customer.class)
   *      .setRaw("status = coalesce(status, 'A')")
   *      .where()
   *      .gt("id", 1000)
   *      .update();
   *
   * }</pre>
   *
   * @param propertyExpression A property expression
   */
  UpdateQuery<T> setRaw(String propertyExpression);

  /**
   * Set using a property expression that can contain <code>?</code> bind value placeholders.
   * <p>
   * For each <code>?</code> in the property expression there should be a matching bind value supplied.
   * </p>
   * <pre>{@code
   *
   *   int rows = ebeanServer
   *      .update(Customer.class)
   *      .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
   *      .where()
   *      .gt("id", 1000)
   *      .update();
   *
   * }</pre>
   *
   * @param propertyExpression A raw property expression
   * @param values             The values to bind with the property expression
   */
  UpdateQuery<T> setRaw(String propertyExpression, Object... values);

  /**
   * Set the profile location of this update query. This is used to relate query execution metrics
   * back to a location like a specific line of code.
   */
  UpdateQuery<T> setProfileLocation(ProfileLocation profileLocation);

  /**
   * Return the query expression list to add predicates to.
   */
  ExpressionList<T> where();

}
