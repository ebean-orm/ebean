package io.ebean.typequery;

import io.ebean.*;

import java.util.Collection;

/**
 * Query bean for strongly typed query construction and execution.
 * <p>
 * For each entity bean querybean-generator generates a query bean that implements QueryBean.
 *
 * <h2>Example - usage of QCustomer</h2>
 * <pre>{@code
 *
 *  Date fiveDaysAgo = ...
 *
 *  List<Customer> customers =
 *      new QCustomer()
 *        .name.ilike("rob")
 *        .status.equalTo(Customer.Status.GOOD)
 *        .registered.after(fiveDaysAgo)
 *        .contacts.email.endsWith("@foo.com")
 *        .orderBy()
 *          .name.asc()
 *          .registered.desc()
 *        .findList();
 *
 * }</pre>
 * <p>
 * <h2>Resulting SQL where</h2>
 * <pre>{@code
 *
 *   where lower(t0.name) like ? and t0.status = ? and t0.registered > ? and u1.email like ?
 *   order by t0.name, t0.registered desc;
 *
 *   --bind(rob,GOOD,Mon Jul 27 12:05:37 NZST 2015,%@foo.com)
 *
 * }</pre>
 *
 * @param <T> the entity bean type (normal entity bean type e.g. Customer)
 * @param <R> the specific query bean type (e.g. QCustomer)
 */
public interface IQueryBean<T, R extends IQueryBean<T, R>> extends QueryBuilder<R, T> {

  /**
   * Return the underlying query.
   * <p>
   * Generally it is not expected that you will need to do this but typically use
   * the find methods available on this 'root query bean' instance like findList().
   */
  Query<T> query();

  /**
   * Return the fetch group.
   */
  FetchGroup<T> buildFetchGroup();

  /**
   * Set DISTINCT ON properties. This is a Postgres only SQL feature.
   *
   * @param properties The properties to include in the DISTINCT ON clause.
   */
  @SuppressWarnings("unchecked")
  R distinctOn(TQProperty<R, ?>... properties);

  /**
   * Specify the properties to be loaded on the 'main' root level entity bean.
   * <p>
   * The resulting entities with be "partially loaded" aka partial objects.
   * <p>
   * Alternatively we can use a {@link #select(FetchGroup)} to specify all properties
   * to load on all parts of the graph.
   *
   * <pre>{@code
   *
   *   // alias for the customer properties in select()
   *   QCustomer cust = QCustomer.alias();
   *
   *   // alias for the contact properties in contacts.fetch()
   *   QContact contact = QContact.alias();
   *
   *   List<Customer> customers =
   *     new QCustomer()
   *       // specify the parts of the graph we want to load
   *       .select(cust.id, cust.name)
   *       .contacts.fetch(contact.firstName, contact.lastName, contact.email)
   *
   *       // predicates
   *       .id.gt(1)
   *       .findList();
   *
   * }</pre>
   *
   * @param properties the list of properties to fetch
   */
  @SuppressWarnings("unchecked")
  R select(TQProperty<R, ?>... properties);

  /**
   * Specify the properties to be loaded on the 'main' root level entity bean
   * also allowing for functions to be used like {@link StdOperators#max(Query.Property)}.
   *
   * @param properties the list of properties to fetch
   */
  R select(Query.Property<?>... properties);

  /**
   * Add an expression to the WHERE or HAVING clause.
   */
  R add(Expression expression);

  /**
   * Add EXISTS sub-query predicate.
   */
  R exists(Query<?> subQuery);

  /**
   * Add NOT EXISTS sub-query predicate.
   */
  R notExists(Query<?> subQuery);

  /**
   * EXISTS using a SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  R exists(String sqlSubQuery, Object... bindValues);

  /**
   * Not EXISTS using a SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  R notExists(String sqlSubQuery, Object... bindValues);

  /**
   * Set the Id value to query. This is used with findOne().
   * <p>
   * You can use this to have further control over the query. For example adding fetch joins.
   * <p>
   * <pre>{@code
   *
   * Order order =
   *   new QOrder()
   *     .setId(1)
   *     .fetch("details")
   *     .findOne();
   *
   * // the order details were eagerly fetched
   * List<OrderDetail> details = order.getDetails();
   *
   * }</pre>
   */
  R setId(Object id);

  /**
   * Set a list of Id values to match.
   * <p>
   * <pre>{@code
   *
   * List<Order> orders =
   *   new QOrder()
   *     .setIdIn(42, 43, 44)
   *     .findList();
   *
   * }</pre>
   */
  R setIdIn(Object... ids);

  /**
   * Set a collection of Id values to match.
   * <p>
   * <pre>{@code
   *
   * Collection<?> ids = ...
   *
   * List<Order> orders =
   *   new QOrder()
   *     .setIdIn(ids)
   *     .findList();
   *
   * }</pre>
   */
  R setIdIn(Collection<?> ids);

  /**
   * Add raw expression with no parameters.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * <p>
   * <pre>{@code
   *
   *   raw("orderQty < shipQty")
   *
   * }</pre>
   *
   * <h4>Subquery example:</h4>
   * <pre>{@code
   *
   *   .raw("t0.customer_id in (select customer_id from customer_group where group_id = any(?::uuid[]))", groupIds)
   *
   * }</pre>
   */
  R raw(String rawExpression);

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? as there are
   * parameters.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   */
  R raw(String rawExpression, Object... bindValues);

  /**
   * Only add the raw expression if the values is not null or empty.
   * <p>
   * This is a pure convenience expression to make it nicer to deal with the pattern where we use
   * raw() expression with a subquery and only want to add the subquery predicate when the collection
   * of values is not empty.
   *
   * <h3>Without inOrEmpty()</h3>
   * <pre>{@code
   *
   *   QCustomer query = new QCustomer() // add some predicates
   *     .status.equalTo(Status.NEW);
   *
   *   // common pattern - we can use rawOrEmpty() instead
   *   if (orderIds != null && !orderIds.isEmpty()) {
   *     query.raw("t0.customer_id in (select o.customer_id from orders o where o.id in (?1))", orderIds);
   *   }
   *
   *   query.findList();
   *
   * }</pre>
   *
   * <h3>Using rawOrEmpty()</h3>
   * Note that in the example below we use the <code>?1</code> bind parameter to get  "parameter expansion"
   * for each element in the collection.
   *
   * <pre>{@code
   *
   *   new QCustomer()
   *     .status.equalTo(Status.NEW)
   *     // only add the expression if orderIds is not empty
   *     .rawOrEmpty("t0.customer_id in (select o.customer_id from orders o where o.id in (?1))", orderIds);
   *     .findList();
   *
   * }</pre>
   *
   * <h3>Postgres ANY</h3>
   * With Postgres we would often use the SQL <code>ANY</code> expression and array parameter binding
   * rather than <code>IN</code>.
   *
   * <pre>{@code
   *
   *   new QCustomer()
   *     .status.equalTo(Status.NEW)
   *     .rawOrEmpty("t0.customer_id in (select o.customer_id from orders o where o.id = any(?))", orderIds);
   *     .findList();
   *
   * }</pre>
   * <p>
   * Note that we need to cast the Postgres array for UUID types like:
   * </p>
   * <pre>{@code
   *
   *   " ... = any(?::uuid[])"
   *
   * }</pre>
   *
   * @param raw    The raw expression that is typically a subquery
   * @param values The values which is typically a list or set of id values.
   */
  R rawOrEmpty(String raw, Collection<?> values);

  /**
   * Add raw expression with a single parameter.
   * <p>
   * The raw expression should contain a single ? at the location of the
   * parameter.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * <p>
   * <h4>Example:</h4>
   * <pre>{@code
   *
   *   // use a database function
   *   raw("add_days(orderDate, 10) < ?", someDate)
   *
   * }</pre>
   *
   * <h4>Subquery example:</h4>
   * <pre>{@code
   *
   *   .raw("t0.customer_id in (select customer_id from customer_group where group_id = any(?::uuid[]))", groupIds)
   *
   * }</pre>
   */
  R raw(String rawExpression, Object bindValue);

  /**
   * In expression using multiple columns.
   */
  R inTuples(InTuples inTuples);

  /**
   * Marker that can be used to indicate that the order by clause is defined after this.
   * <p>
   * <h2>Example: order by customer name, order date</h2>
   * <pre>{@code
   *   List<Order> orders =
   *          new QOrder()
   *            .customer.name.ilike("rob")
   *            .orderBy()
   *              .customer.name.asc()
   *              .orderDate.asc()
   *            .findList();
   *
   * }</pre>
   */
  R orderBy();

  /**
   * @deprecated migrate to {@link #orderBy()}.
   */
  @Deprecated(since = "13.19", forRemoval = true)
  R order();

  /**
   * @deprecated migrate to {@link #orderBy(String)}
   */
  @Deprecated(since = "13.19", forRemoval = true)
  R order(String orderByClause);

  /**
   * Begin a list of expressions added by 'OR'.
   * <p>
   * Use endOr() or endJunction() to stop added to OR and 'pop' to the parent expression list.
   * <p>
   * <h2>Example</h2>
   * <p>
   * This example uses an 'OR' expression list with an inner 'AND' expression list.
   * </p>
   * <pre>{@code
   *
   *    List<Customer> customers =
   *          new QCustomer()
   *            .status.equalTo(Customer.Status.GOOD)
   *            .or()
   *              .id.greaterThan(1000)
   *              .and()
   *                .name.startsWith("super")
   *                .registered.after(fiveDaysAgo)
   *              .endAnd()
   *            .endOr()
   *            .orderBy().id.desc()
   *            .findList();
   *
   * }</pre>
   * <h2>Resulting SQL where clause</h2>
   * <pre>{@code sql
   *
   *    where t0.status = ?  and (t0.id > ?  or (t0.name like ?  and t0.registered > ? ) )
   *    order by t0.id desc;
   *
   *    --bind(GOOD,1000,super%,Wed Jul 22 00:00:00 NZST 2015)
   *
   * }</pre>
   */
  R or();

  /**
   * Begin a list of expressions added by 'AND'.
   * <p>
   * Use endAnd() or endJunction() to stop added to AND and 'pop' to the parent expression list.
   * <p>
   * Note that typically the AND expression is only used inside an outer 'OR' expression.
   * This is because the top level expression list defaults to an 'AND' expression list.
   *
   * <h2>Example</h2>
   * <p>
   * This example uses an 'OR' expression list with an inner 'AND' expression list.
   * </p>
   * <pre>{@code
   *
   *    List<Customer> customers =
   *          new QCustomer()
   *            .status.equalTo(Customer.Status.GOOD)
   *            .or() // OUTER 'OR'
   *              .id.greaterThan(1000)
   *              .and()  // NESTED 'AND' expression list
   *                .name.startsWith("super")
   *                .registered.after(fiveDaysAgo)
   *                .endAnd()
   *              .endOr()
   *            .orderBy().id.desc()
   *            .findList();
   *
   * }</pre>
   * <h2>Resulting SQL where clause</h2>
   * <pre>{@code sql
   *
   *    where t0.status = ?  and (t0.id > ?  or (t0.name like ?  and t0.registered > ? ) )
   *    order by t0.id desc;
   *
   *    --bind(GOOD,1000,super%,Wed Jul 22 00:00:00 NZST 2015)
   *
   * }</pre>
   */
  R and();

  /**
   * Begin a list of expressions added by NOT.
   * <p>
   * Use endNot() or endJunction() to stop added to NOT and 'pop' to the parent expression list.
   */
  R not();

  /**
   * End a list of expressions added by 'OR'.
   */
  R endJunction();

  /**
   * End OR junction - synonym for endJunction().
   */
  R endOr();

  /**
   * End AND junction - synonym for endJunction().
   */
  R endAnd();

  /**
   * End NOT junction - synonym for endJunction().
   */
  R endNot();

  /**
   * Add expression after this to the WHERE expression list.
   * <p>
   * For queries against the normal database (not the doc store) this has no effect.
   * <p>
   * This is intended for use with Document Store / ElasticSearch where expressions can be put into either
   * the "query" section or the "filter" section of the query. Full text expressions like MATCH are in the
   * "query" section but many expression can be in either - expressions after the where() are put into the
   * "filter" section which means that they don't add to the relevance and are also cache-able.
   */
  R where();

  /**
   * Return the expression list that has been built for this query.
   */
  ExpressionList<T> getExpressionList();

  /**
   * Start adding expressions to the having clause when using @Aggregation properties.
   *
   * <pre>{@code
   *
   *   new QMachineUse()
   *   // where ...
   *   .date.inRange(fromDate, toDate)
   *
   *   .having()
   *   .sumHours.greaterThan(1)
   *   .findList()
   *
   *   // The sumHours property uses @Aggregation
   *   // e.g. @Aggregation("sum(hours)")
   *
   * }</pre>
   */
  R having();

  /**
   * Return the underlying having clause to typically when using dynamic aggregation formula.
   * <p>
   * Note that after this we no longer have the query bean so typically we use this right
   * at the end of the query.
   *
   * <pre>{@code
   *
   *  // sum(distanceKms) ... is a "dynamic formula"
   *  // so we use havingClause() for it like:
   *
   *  List<MachineUse> machineUse =
   *
   *    new QMachineUse()
   *      .select("machine, sum(fuelUsed), sum(distanceKms)")
   *
   *      // where ...
   *      .date.greaterThan(LocalDate.now().minusDays(7))
   *
   *      .havingClause()
   *        .gt("sum(distanceKms)", 2)
   *        .findList();
   *
   * }</pre>
   */
  ExpressionList<T> havingClause();

  /**
   * Set the profile location of this query. This is used to relate query execution metrics
   * back to a location like a specific line of code.
   */
  R setProfileLocation(ProfileLocation profileLocation);
}
