package io.ebean;

import org.jspecify.annotations.NullMarked;

/**
 * Object relational query for finding a List, Set, Map or single entity bean.
 * <p>
 * Example: Create the query using the API.
 *
 * <pre>{@code
 *
 * List<Order> orderList = DB.find(Order.class)
 *     .where()
 *       .like("customer.name","rob%")
 *       .gt("orderDate",lastWeek)
 *     .orderBy("customer.id, id desc")
 *     .setMaxRows(50)
 *     .findList();
 *
 * }</pre>
 * <p>
 * Example: The same query using the query language
 *
 * <pre>{@code
 *
 * String oql =
 *   	+" where customer.name like :custName and orderDate > :minOrderDate "
 *   	+" order by customer.id, id desc "
 *   	+" limit 50 ";
 *
 * List<Order> orderList = DB.createQuery(Order.class, oql)
 *   .setParameter("custName", "Rob%")
 *   .setParameter("minOrderDate", lastWeek)
 *   .findList();
 * ...
 * }</pre>
 * <h3>AutoTune</h3>
 * <p>
 * Ebean has built in support for "AutoTune". This is a mechanism where a query
 * can be automatically tuned based on profiling information that is collected.
 * <p>
 * This is effectively the same as automatically using select() and fetch() to
 * build a query that will fetch all the data required by the application and no
 * more.
 * <p>
 * It is expected that AutoTune will be the default approach for many queries
 * in a system. It is possibly not as useful where the result of a query is sent
 * to a remote client or where there is some requirement for "Read Consistency"
 * guarantees.
 *
 * <h3>Query Language</h3>
 * <p>
 * <b>Partial Objects</b>
 * <p>
 * The <em>find</em> and <em>fetch</em> clauses support specifying a list of
 * properties to fetch. This results in objects that are "partially populated".
 * If you try to get a property that was not populated a "lazy loading" query
 * will automatically fire and load the rest of the properties of the bean (This
 * is very similar behaviour as a reference object being "lazy loaded").
 * <p>
 * Partial objects can be saved just like fully populated objects. If you do
 * this you should remember to include the <em>"Version"</em> property in the
 * initial fetch. If you do not include a version property then optimistic
 * concurrency checking will occur but only include the fetched properties.
 * Refer to "ALL Properties/Columns" mode of Optimistic Concurrency checking.
 *
 * <pre>{@code
 * [ select [ ( * | {fetch properties} ) ] ]
 * [ fetch {path} [ ( * | {fetch properties} ) ] ]
 * [ where {predicates} ]
 * [ order by {order by properties} ]
 * [ limit {max rows} [ offset {first row} ] ]
 * }</pre>
 * <p>
 * <b>SELECT</b> [ ( <i>*</i> | <i>{fetch properties}</i> ) ]
 * <p>
 * With the select you can specify a list of properties to fetch.
 * <p>
 * <b>FETCH</b> <b>{path}</b> [ ( <i>*</i> | <i>{fetch properties}</i> ) ]
 * <p>
 * With the fetch you specify the associated property to fetch and populate. The
 * path is a OneToOne, ManyToOne, OneToMany or ManyToMany property.
 * <p>
 * For fetch of a path we can optionally specify a list of properties to fetch.
 * If you do not specify a list of properties ALL the properties for that bean
 * type are fetched.
 * <p>
 * <b>WHERE</b> <b>{list of predicates}</b>
 * <p>
 * The list of predicates which are joined by AND OR NOT ( and ). They can
 * include named (or positioned) bind parameters. These parameters will need to
 * be bound by {@link Query#setParameter(String, Object)}.
 * <p>
 * <b>ORDER BY</b> <b>{order by properties}</b>
 * <p>
 * The list of properties to order the result. You can include ASC (ascending)
 * and DESC (descending) in the order by clause.
 * <p>
 * <b>LIMIT</b> <b>{max rows}</b> [ OFFSET <i>{first row}</i> ]
 * <p>
 * The limit offset specifies the max rows and first row to fetch. The offset is
 * optional.
 * <h4>Examples of Ebean's Query Language</h4>
 * <p>
 * Find orders fetching its id, shipDate and status properties. Note that the id
 * property is always fetched even if it is not included in the list of fetch
 * properties.
 *
 * <pre>{@code
 *
 * select (shipDate, status)
 *
 * }</pre>
 * <p>
 * Find orders with a named bind variable (that will need to be bound via
 * {@link Query#setParameter(String, Object)}).
 *
 * <pre>{@code
 *
 * where customer.name like :custLike
 *
 * }</pre>
 * <p>
 * Find orders and also fetch the customer with a named bind parameter. This
 * will fetch and populate both the order and customer objects.
 *
 * <pre>{@code
 *
 * fetch customer
 * where customer.id = :custId
 *
 * }</pre>
 * <p>
 * Find orders and also fetch the customer, customer shippingAddress, order
 * details and related product. Note that customer and product objects will be
 * "Partial Objects" with only some of their properties populated. The customer
 * objects will have their id, name and shipping address populated. The product
 * objects (associated with each order detail) will have their id, sku and name
 * populated.
 *
 * <pre>{@code
 *
 * fetch customer (name)
 * fetch customer.shippingAddress
 * fetch details
 * fetch details.product (sku, name)
 *
 * }</pre>
 *
 * @param <T> the type of Entity bean this query will fetch.
 */
@NullMarked
public interface Query<T> extends CancelableQuery, QueryBuilder<Query<T>, T> {

  /**
   * The lock type (strength) to use with query FOR UPDATE row locking.
   */
  enum LockType {
    /**
     * The default lock type being either UPDATE or NO_KEY_UPDATE based on
     * PlatformConfig.forUpdateNoKey configuration (Postgres option).
     */
    DEFAULT,

    /**
     * FOR UPDATE.
     */
    UPDATE,

    /**
     * FOR NO KEY UPDATE (Postgres only).
     */
    NO_KEY_UPDATE,

    /**
     * FOR SHARE (Postgres only).
     */
    SHARE,

    /**
     * FOR KEY SHARE (Postgres only).
     */
    KEY_SHARE
  }

  /**
   * FOR UPDATE wait mode.
   */
  enum LockWait {
    /**
     * Standard For update clause.
     */
    WAIT,

    /**
     * For update with No Wait option.
     */
    NOWAIT,

    /**
     * For update with Skip Locked option.
     */
    SKIPLOCKED
  }

  /**
   * Return the ExpressionFactory used by this query.
   */
  ExpressionFactory getExpressionFactory();

  /**
   * Returns true if this query was tuned by autoTune.
   */
  boolean isAutoTuned();

  /**
   * Return true if this is countDistinct query.
   */
  boolean isCountDistinct();

  /**
   * @deprecated migrate to {@link #usingTransaction(Transaction)} then delete().
   * <p>
   * Execute as a delete query returning the number of rows deleted using the given transaction.
   * <p>
   * Note that if the query includes joins then the generated delete statement may not be
   * optimal depending on the database platform.
   *
   * @return the number of beans/rows that were deleted.
   */
  @Deprecated(forRemoval = true, since = "14.1.0")
  int delete(Transaction transaction);

  /**
   * @deprecated migrate to {@link #usingTransaction(Transaction)} then update().
   * <p>
   * Execute the UpdateQuery returning the number of rows updated using the given transaction.
   *
   * @return the number of beans/rows updated.
   */
  @Deprecated(forRemoval = true, since = "14.1.0")
  int update(Transaction transaction);

  /**
   * Execute the UpdateQuery returning the number of rows updated.
   *
   * @return the number of beans/rows updated.
   */
  int update();

  /**
   * Set a named bind parameter. Named parameters have a colon to prefix the name.
   * <pre>{@code
   *
   * // a query with a named parameter
   * String oql = "find order where status = :orderStatus";
   *
   * List<Order> list = DB.find(Order.class, oql)
   *   .setParameter("orderStatus", OrderStatus.NEW)
   *   .findList();
   *
   * }</pre>
   *
   * @param name  the parameter name
   * @param value the parameter value
   */
  Query<T> setParameter(String name, Object value);

  /**
   * Set an ordered bind parameter according to its position. Note that the
   * position starts at 1 to be consistent with JDBC PreparedStatement. You need
   * to set a parameter value for each ? you have in the query.
   * <pre>{@code
   *
   * // a query with a positioned parameter
   * String oql = "where status = ? order by id desc";
   *
   * List<Order> list = DB.createQuery(Order.class, oql)
   *   .setParameter(1, OrderStatus.NEW)
   *   .findList();
   *
   * }</pre>
   *
   * @param position the parameter bind position starting from 1 (not 0)
   * @param value    the parameter bind value.
   */
  Query<T> setParameter(int position, Object value);

  /**
   * Bind the next positioned parameter.
   *
   * <pre>{@code
   *
   * // a query with a positioned parameters
   * String oql = "where status = ? and name = ?";
   *
   * List<Order> list = DB.createQuery(Order.class, oql)
   *   .setParameter(OrderStatus.NEW)
   *   .setParameter("Rob")
   *   .findList();
   *
   * }</pre>
   */
  Query<T> setParameter(Object value);

  /**
   * Bind all the positioned parameters.
   * <p>
   * A convenience for multiple calls to {@link #setParameter(Object)}
   */
  Query<T> setParameters(Object... values);

  /**
   * Set the Id value to query. This is used with findOne().
   * <p>
   * You can use this to have further control over the query. For example adding
   * fetch joins.
   *
   * <pre>{@code
   *
   * Order order = DB.find(Order.class)
   *     .setId(1)
   *     .fetch("details")
   *     .findOne();
   *
   * // the order details were eagerly fetched
   * List<OrderDetail> details = order.getDetails();
   *
   * }</pre>
   */
  Query<T> setId(Object id);

  /**
   * Return the Id value.
   */
  Object getId();

  /**
   * Add a single Expression to the where clause returning the query.
   * <pre>{@code
   *
   * List<Order> newOrders = DB.find(Order.class)
   * 		.where().eq("status", Order.NEW)
   * 		.findList();
   * ...
   *
   * }</pre>
   */
  Query<T> where(Expression expression);

  /**
   * Add Expressions to the where clause with the ability to chain on the
   * ExpressionList. You can use this for adding multiple expressions to the
   * where clause.
   * <pre>{@code
   *
   * List<Order> orders = DB.find(Order.class)
   *     .where()
   *       .eq("status", Order.NEW)
   *       .ilike("customer.name","rob%")
   *     .findList();
   *
   * }</pre>
   *
   * @return The ExpressionList for adding expressions to.
   * @see Expr
   */
  ExpressionList<T> where();

  /**
   * Add Full text search expressions for Document store queries.
   * <p>
   * This is currently ElasticSearch only and provides the full text
   * expressions such as Match and Multi-Match.
   * <p>
   * This automatically makes this query a "Doc Store" query and will execute
   * against the document store (ElasticSearch).
   * <p>
   * Expressions added here are added to the "query" section of an ElasticSearch
   * query rather than the "filter" section.
   * <p>
   * Expressions added to the where() are added to the "filter" section of an
   * ElasticSearch query.
   */
  ExpressionList<T> text();

  /**
   * This applies a filter on the 'many' property list rather than the root
   * level objects.
   * <p>
   * Typically, you will use this in a scenario where the cardinality is high on
   * the 'many' property you wish to join to. Say you want to fetch customers
   * and their associated orders... but instead of getting all the orders for
   * each customer you only want to get the new orders they placed since last
   * week. In this case you can use filterMany() to filter the orders.
   *
   * <pre>{@code
   *
   * List<Customer> list = DB.find(Customer.class)
   *     .fetch("orders")
   *     .where().ilike("name", "rob%")
   *     .filterMany("orders").eq("status", Order.Status.NEW).gt("orderDate", lastWeek)
   *     .findList();
   *
   * }</pre>
   * <p>
   * Please note you have to be careful that you add expressions to the correct
   * expression list - as there is one for the 'root level' and one for each
   * filterMany that you have.
   *
   * @param propertyName the name of the many property that you want to have a filter on.
   * @return the expression list that you add filter expressions for the many to.
   */
  ExpressionList<T> filterMany(String propertyName);

  /**
   * Add Expressions to the Having clause return the ExpressionList.
   * <p>
   * Currently only beans based on raw sql will use the having clause.
   * <p>
   * Note that this returns the ExpressionList (so you can add multiple
   * expressions to the query in a fluent API way).
   *
   * @return The ExpressionList for adding more expressions to.
   * @see Expr
   */
  ExpressionList<T> having();

  /**
   * Add an expression to the having clause returning the query.
   * <p>
   * Currently only beans based on raw sql will use the having clause.
   * <p>
   * This is similar to {@link #having()} except it returns the query rather
   * than the ExpressionList. This is useful when you want to further specify
   * something on the query.
   *
   * @param addExpressionToHaving the expression to add to the having clause.
   * @return the Query object
   */
  Query<T> having(Expression addExpressionToHaving);

  /**
   * @deprecated migrate to {@link #orderBy()}.
   */
  @Deprecated(since = "13.19", forRemoval = true)
  default Query<T> order(String orderByClause) {
    return orderBy(orderByClause);
  }

  /**
   * @deprecated migrate to {@link #orderBy()}.
   */
  @Deprecated(since = "13.19", forRemoval = true)
  default OrderBy<T> order() {
    return orderBy();
  }

  /**
   * @deprecated migrate to {@link #setOrderBy(OrderBy)}.
   */
  @Deprecated(since = "13.19", forRemoval = true)
  default Query<T> setOrder(OrderBy<T> orderBy) {
    return setOrderBy(orderBy);
  }

  /**
   * Return the OrderBy so that you can append an ascending or descending
   * property to the order by clause.
   * <p>
   * This will never return a null. If no order by clause exists then an 'empty'
   * OrderBy object is returned.
   * <p>
   * This is the same as <code>order()</code>
   */
  OrderBy<T> orderBy();

  /**
   * Return the first row value.
   */
  int getFirstRow();

  /**
   * Return the max rows for this query.
   */
  int getMaxRows();

  /**
   * Return true if this query has forUpdate set.
   */
  boolean isForUpdate();

  /**
   * Return the "for update" wait mode to use.
   */
  LockWait getForUpdateLockWait();

  /**
   * Return the lock type (strength) to use with "for update".
   */
  LockType getForUpdateLockType();

  /**
   * Returns the inherit type. This is normally the same as getBeanType() returns as long as no other type is set.
   */
  Class<? extends T> getInheritType();

  /**
   * Return the type of query being executed.
   */
  QueryType getQueryType();

  /**
   * Set the profile location of this query. This is used to relate query execution metrics
   * back to a location like a specific line of code.
   */
  Query<T> setProfileLocation(ProfileLocation profileLocation);

  /**
   * Type safe query bean properties and expressions (marker interface).
   * <p>
   * Implemented by query bean properties and expressions based on those properties.
   * <p>
   * The base type determines which {@link StdOperators} can be used on the property.
   *
   * @param <T> The property type.
   */
  interface Property<T> {

    /**
     * Return a property given the expression.
     */
    static <T> Property<T> of(String expression) {
      return new SimpleProperty<>(expression);
    }

    /**
     * Return the property in string expression form.
     * <p>
     * This is a path to a database column (like "name" or "billingAddress.city") or a function
     * wrapping a path (like <em>lower(name)</em>, <em>concat(name, '-', billingAddress.city)</em>
     */
    @Override
    String toString();
  }
}
