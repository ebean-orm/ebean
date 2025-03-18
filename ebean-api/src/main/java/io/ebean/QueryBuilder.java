package io.ebean;

import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Build and execute an ORM query.
 *
 * @param <SELF> The type of the builder
 * @param <T>    The entity bean type
 */
public interface QueryBuilder<SELF extends QueryBuilder<SELF, T>, T> extends QueryBuilderProjection<SELF, T> {

  /**
   * Set root table alias.
   */
  SELF alias(String alias);

  /**
   * Apply changes to the query using a function.
   *
   * @param apply Function that applies changes to the query.
   */
  SELF also(Consumer<SELF> apply);

  /**
   * Apply changes to the query conditional on the supplied predicate.
   * <p>
   * Typically, the changes are extra predicates etc.
   *
   * @param predicate The predicate which when true the changes are applied
   * @param apply     The changes to apply to the query
   */
  SELF alsoIf(BooleanSupplier predicate, Consumer<SELF> apply);

  /**
   * Perform an 'As of' query using history tables to return the object graph
   * as of a time in the past.
   * <p>
   * To perform this query the DB must have underlying history tables.
   *
   * @param asOf the date time in the past at which you want to view the data
   */
  SELF asOf(Timestamp asOf);

  /**
   * Execute the query against the draft set of tables.
   */
  SELF asDraft();

  /**
   * Convert the query to a DTO bean query.
   * <p>
   * We effectively use the underlying ORM query to build the SQL and then execute
   * and map it into DTO beans.
   */
  <D> DtoQuery<D> asDto(Class<D> dtoClass);

  /**
   * Convert the query to a UpdateQuery.
   * <p>
   * Typically this is used with query beans to covert a query bean
   * query into an UpdateQuery like the examples below.
   * </p>
   *
   * <pre>{@code
   *
   *  int rowsUpdated = new QCustomer()
   *       .name.startsWith("Rob")
   *       .asUpdate()
   *       .set("active", false)
   *       .update();;
   *
   * }</pre>
   *
   * <pre>{@code
   *
   *   int rowsUpdated = new QContact()
   *       .notes.note.startsWith("Make Inactive")
   *       .email.endsWith("@foo.com")
   *       .customer.id.equalTo(42)
   *       .asUpdate()
   *       .set("inactive", true)
   *       .setRaw("email = lower(email)")
   *       .update();
   *
   * }</pre>
   */
  UpdateQuery<T> asUpdate();

  /**
   * Return a copy of the query.
   * <p>
   * This is so that you can use a Query as a "prototype" for creating other
   * query instances. You could create a Query with various where expressions
   * and use that as a "prototype" - using this copy() method to create a new
   * instance that you can then add other expressions then execute.
   * </p>
   */
  SELF copy();

  /**
   * Execute the query using the given transaction.
   */
  SELF usingTransaction(Transaction transaction);

  /**
   * Execute the query using the given connection.
   */
  SELF usingConnection(Connection connection);

  /**
   * Execute the query using the given database.
   */
  SELF usingDatabase(Database database);

  /**
   * Ensure that the master DataSource is used if there is a read only data source
   * being used (that is using a read replica database potentially with replication lag).
   * <p>
   * When the database is configured with a read-only DataSource via
   * say {@link io.ebean.config.DatabaseConfig#setReadOnlyDataSource(DataSource)} then
   * by default when a query is run without an active transaction, it uses the read-only data
   * source. We we use {@code usingMaster()} to instead ensure that the query is executed
   * against the master data source.
   */
  SELF usingMaster();

  /**
   * Set the base table to use for this query.
   * <p>
   * Typically this is used when a table has partitioning and we wish to specify a specific
   * partition/table to query against.
   * </p>
   * <pre>{@code
   *
   *   QOrder()
   *   .setBaseTable("order_2019_05")
   *   .status.equalTo(Status.NEW)
   *   .findList();
   *
   * }</pre>
   */
  SELF setBaseTable(String baseTable);

  /**
   * Specify the PersistenceContextScope to use for this query.
   * <p>
   * When this is not set the 'default' configured on {@link io.ebean.config.DatabaseConfig#setPersistenceContextScope(PersistenceContextScope)}
   * is used - this value defaults to {@link PersistenceContextScope#TRANSACTION}.
   * <p>
   * Note that the same persistence Context is used for subsequent lazy loading and query join queries.
   * <p>
   * Note that #findEach uses a 'per object graph' PersistenceContext so this scope is ignored for
   * queries executed as #findIterate, #findEach, #findEachWhile.
   *
   * @param scope The scope to use for this query and subsequent lazy loading.
   */
  SELF setPersistenceContextScope(PersistenceContextScope scope);

  /**
   * Explicitly specify whether to use AutoTune for this query.
   * <p>
   * If you do not call this method on a query the "Implicit AutoTune mode" is
   * used to determine if AutoTune should be used for a given query.
   * <p>
   * AutoTune can add additional fetch paths to the query and specify which
   * properties are included for each path. If you have explicitly defined some
   * fetch paths AutoTune will not remove them.
   */
  SELF setAutoTune(boolean autoTune);

  /**
   * Execute the query allowing properties with invalid JSON to be collected and not fail the query.
   * <pre>{@code
   *
   *   // fetch a bean with JSON content
   *   EBasicJsonList bean= DB.find(EBasicJsonList.class)
   *       .setId(42)
   *       .setAllowLoadErrors()  // collect errors into bean state if we have invalid JSON
   *       .findOne();
   *
   *
   *   // get the invalid JSON errors from the bean state
   *   Map<String, Exception> errors = server().getBeanState(bean).getLoadErrors();
   *
   *   // If this map is not empty tell we have invalid JSON
   *   // and should try and fix the JSON content or inform the user
   *
   * }</pre>
   */
  SELF setAllowLoadErrors();

  /**
   * Set the default lazy loading batch size to use.
   * <p>
   * When lazy loading is invoked on beans loaded by this query then this sets the
   * batch size used to load those beans.
   *
   * @param lazyLoadBatchSize the number of beans to lazy load in a single batch
   */
  SELF setLazyLoadBatchSize(int lazyLoadBatchSize);

  /**
   * Set a label on the query.
   * <p>
   * This label can be used to help identify query performance metrics but we can also use
   * profile location enhancement on Finders so for some that would be a better option.
   */
  SELF setLabel(String label);

  /**
   * Set a SQL query hint.
   * <p>
   * This results in an inline comment that immediately follows
   * after the select keyword in the form: {@code /*+ hint *\/ }
   */
  SELF setHint(String hint);

  /**
   * Set the index(es) to search for a document store which uses partitions.
   * <p>
   * For example, when executing a query against ElasticSearch with daily indexes we can
   * explicitly specify the indexes to search against.
   * </p>
   * <pre>{@code
   *
   *   // explicitly specify the indexes to search
   *   query.setDocIndexName("logstash-2016.11.5,logstash-2016.11.6")
   *
   *   // search today's index
   *   query.setDocIndexName("$today")
   *
   *   // search the last 3 days
   *   query.setDocIndexName("$last-3")
   *
   * }</pre>
   * <p>
   * If the indexName is specified with ${daily} e.g. "logstash-${daily}" ... then we can use
   * $today and $last-x as the search docIndexName like the examples below.
   * </p>
   * <pre>{@code
   *
   *   // search today's index
   *   query.setDocIndexName("$today")
   *
   *   // search the last 3 days
   *   query.setDocIndexName("$last-3")
   *
   * }</pre>
   *
   * @param indexName The index or indexes to search against
   * @return This query
   */
  SELF setDocIndexName(String indexName);

  /**
   * Execute the query including soft deleted rows.
   * <p>
   * This means that Ebean will not add any predicates to the query for filtering out
   * soft deleted rows. You can still add your own predicates for the deleted properties
   * and effectively you have full control over the query to include or exclude soft deleted
   * rows as needed for a given use case.
   */
  SELF setIncludeSoftDeletes();

  /**
   * Disable read auditing for this query.
   * <p>
   * This is intended to be used when the query is not a user initiated query and instead
   * part of the internal processing in an application to load a cache or document store etc.
   * In these cases we don't want the query to be part of read auditing.
   */
  SELF setDisableReadAuditing();

  /**
   * Set true if you want to disable lazy loading.
   * <p>
   * That is, once the object graph is returned further lazy loading is disabled.
   */
  SELF setDisableLazyLoading(boolean disableLazyLoading);

  /**
   * Set whether this query uses DISTINCT.
   */
  SELF setDistinct(boolean distinct);

  /**
   * Restrict the query to only return subtypes of the given inherit type.
   * <pre>{@code
   *
   *   List<Animal> animals =
   *     new QAnimal()
   *       .name.startsWith("Fluffy")
   *       .setInheritType(Cat.class)
   *       .findList();
   *
   * }</pre>
   */
  SELF setInheritType(Class<? extends T> type);

  /**
   * Set the first row to return for this query.
   *
   * @param firstRow the first row to include in the query result.
   */
  SELF setFirstRow(int firstRow);

  /**
   * Set the maximum number of rows to return in the query.
   *
   * @param maxRows the maximum number of rows to return in the query.
   */
  SELF setMaxRows(int maxRows);

  SELF setPaging(Paging paging);

  /**
   * Set RawSql to use for this query.
   */
  SELF setRawSql(RawSql rawSql);

  /**
   * Extended version for setDistinct in conjunction with "findSingleAttributeList";
   *
   * <pre>{@code
   *
   *  List<CountedValue<Order.Status>> orderStatusCount =
   *
   *     DB.find(Order.class)
   *      .select("status")
   *      .where()
   *      .gt("orderDate", LocalDate.now().minusMonths(3))
   *
   *      // fetch as single attribute with a COUNT
   *      .setCountDistinct(CountDistinctOrder.COUNT_DESC_ATTR_ASC)
   *      .findSingleAttributeList();
   *
   *     for (CountedValue<Order.Status> entry : orderStatusCount) {
   *       System.out.println(" count:" + entry.getCount()+" orderStatus:" + entry.getValue() );
   *     }
   *
   *   // produces
   *
   *   count:3 orderStatus:NEW
   *   count:1 orderStatus:SHIPPED
   *   count:1 orderStatus:COMPLETE
   *
   * }</pre>
   */
  SELF setCountDistinct(CountDistinctOrder orderBy);

  /**
   * Set the property to use as keys for a map.
   * <p>
   * If no property is set then the id property is used.
   * </p>
   * <pre>{@code
   *
   * // Assuming sku is unique for products...
   *
   * Map<String,Product> productMap = DB.find(Product.class)
   *     .setMapKey("sku")  // sku map keys...
   *     .findMap();
   *
   * }</pre>
   *
   * @param mapKey the property to use as keys for a map.
   */
  SELF setMapKey(String mapKey);

  /**
   * Set to true if this query should execute against the doc store.
   * <p>
   * When setting this you may also consider disabling lazy loading.
   */
  SELF setUseDocStore(boolean useDocStore);

  /**
   * When set to true when you want the returned beans to be read only.
   */
  SELF setReadOnly(boolean readOnly);

  /**
   * Set a timeout on this query.
   * <p>
   * This will typically result in a call to setQueryTimeout() on a
   * preparedStatement. If the timeout occurs an exception will be thrown - this
   * will be a SQLException wrapped up in a PersistenceException.
   * </p>
   *
   * @param secs the query timeout limit in seconds. Zero means there is no limit.
   */
  SELF setTimeout(int secs);

  /**
   * A hint which for JDBC translates to the Statement.fetchSize().
   * <p>
   * Gives the JDBC driver a hint as to the number of rows that should be
   * fetched from the database when more rows are needed for ResultSet.
   * </p>
   * <p>
   * Note that internally findEach and findEachWhile will set the fetch size
   * if it has not already as these queries expect to process a lot of rows.
   * If we didn't then Postgres and MySql for example would eagerly pull back
   * all the row data and potentially consume a lot of memory in the process.
   * </p>
   * <p>
   * As findEach and findEachWhile automatically set the fetch size we don't have
   * to do so generally but we might still wish to for tuning a specific use case.
   * </p>
   */
  SELF setBufferFetchSizeHint(int fetchSize);

  /**
   * Set the mode to use the bean cache when executing this query.
   * <p>
   * By default, "find by id" and "find by natural key" will use the bean cache
   * when bean caching is enabled. Setting this to false means that the query
   * will not use the bean cache and instead hit the database.
   * <p>
   * By default, findList() with natural keys will not use the bean cache. In that
   * case we need to explicitly use the bean cache.
   */
  SELF setBeanCacheMode(CacheMode beanCacheMode);

  /**
   * Set the {@link CacheMode} to use the query for executing this query.
   */
  SELF setUseQueryCache(CacheMode cacheMode);

  /**
   * Set this to false to not use the bean cache.
   * <p>
   * This method is now superseded by {@link #setBeanCacheMode(CacheMode)}
   * which provides more explicit options controlled bean cache use.
   * <p>
   * This method is likely to be deprecated in the future with migration
   * over to setUseBeanCache().
   */
  default SELF setUseCache(boolean useCache) {
    return setBeanCacheMode(useCache ? CacheMode.ON : CacheMode.OFF);
  }

  /**
   * Calls {@link #setUseQueryCache(CacheMode)} with <code>ON</code> or <code>OFF</code>.
   */
  default SELF setUseQueryCache(boolean enabled) {
    return setUseQueryCache(enabled ? CacheMode.ON : CacheMode.OFF);
  }

  /**
   * Set the order by clause replacing the existing order by clause if there is
   * one.
   * <p>
   * This follows SQL syntax using commas between each property with the
   * optional asc and desc keywords representing ascending and descending order
   * respectively.
   */
  SELF orderBy(String orderByClause);

  /**
   * Set an OrderBy object to replace any existing OrderBy clause.
   */
  SELF setOrderBy(OrderBy<T> orderBy);

  /**
   * Controls, if paginated queries should always append an 'order by id' statement at the end to
   * guarantee a deterministic sort result. This may affect performance.
   * If this is not enabled, and an orderBy is set on the query, it's up to the programmer that
   * this query provides a deterministic result.
   */
  SELF orderById(boolean orderById);

  /**
   * Execute the query with the given lock type and WAIT.
   * <p>
   * Note that <code>forUpdate()</code> is the same as
   * <code>withLock(LockType.UPDATE)</code>.
   * <p>
   * Provides us with the ability to explicitly use Postgres
   * SHARE, KEY SHARE, NO KEY UPDATE and UPDATE row locks.
   */
  SELF withLock(Query.LockType lockType);

  /**
   * Execute the query with the given lock type and lock wait.
   * <p>
   * Note that <code>forUpdateNoWait()</code> is the same as
   * <code>withLock(LockType.UPDATE, LockWait.NOWAIT)</code>.
   * <p>
   * Provides us with the ability to explicitly use Postgres
   * SHARE, KEY SHARE, NO KEY UPDATE and UPDATE row locks.
   */
  SELF withLock(Query.LockType lockType, Query.LockWait lockWait);

  /**
   * Execute using "for update" clause which results in the DB locking the record.
   * <p>
   * The same as <code>withLock(LockType.UPDATE, LockWait.WAIT)</code>.
   */
  SELF forUpdate();

  /**
   * Execute using "for update" clause with "no wait" option.
   * <p>
   * This is typically a Postgres and Oracle only option at this stage.
   * <p>
   * The same as <code>withLock(LockType.UPDATE, LockWait.NOWAIT)</code>.
   */
  SELF forUpdateNoWait();

  /**
   * Execute using "for update" clause with "skip locked" option.
   * <p>
   * This is typically a Postgres and Oracle only option at this stage.
   * <p>
   * The same as <code>withLock(LockType.UPDATE, LockWait.SKIPLOCKED)</code>.
   */
  SELF forUpdateSkipLocked();

  /**
   * Returns the set of properties or paths that are unknown (do not map to known properties or paths).
   * <p>
   * Validate the query checking the where and orderBy expression paths to confirm if
   * they represent valid properties or paths for the given bean type.
   */
  Set<String> validate();

  /**
   * Return the sql that was generated for executing this query.
   * <p>
   * This is only available after the query has been executed and provided only
   * for informational purposes.
   */
  String getGeneratedSql();

  /**
   * Return the type of beans being queried.
   */
  Class<T> getBeanType();

  /**
   * Execute as a delete query deleting the 'root level' beans that match the predicates
   * in the query.
   * <p>
   * Note that if the query includes joins then the generated delete statement may not be
   * optimal depending on the database platform.
   *
   * @return the number of beans/rows that were deleted.
   */
  int delete();

  /**
   * Execute the query returning true if a row is found.
   * <p>
   * The query is executed using max rows of 1 and will only select the id property.
   * This method is really just a convenient way to optimise a query to perform a
   * 'does a row exist in the db' check.
   *
   * <h2>Example using a query bean:</h2>
   * <pre>{@code
   *
   *   boolean userExists =
   *     new QContact()
   *       .email.equalTo("rob@foo.com")
   *       .exists();
   *
   * }</pre>
   *
   * <h2>Example:</h2>
   * <pre>{@code
   *
   *   boolean userExists = query()
   *     .where().eq("email", "rob@foo.com")
   *     .exists();
   *
   * }</pre>
   *
   * @return True if the query finds a matching row in the database
   */
  boolean exists();

  /**
   * Execute the query returning either a single bean or null (if no matching
   * bean is found).
   * <p>
   * If more than 1 row is found for this query then a PersistenceException is
   * thrown.
   * <p>
   * This is useful when your predicates dictate that your query should only
   * return 0 or 1 results.
   * <p>
   * <pre>{@code
   *
   * // assuming the sku of products is unique...
   * Product product =
   *     new QProduct()
   *         .sku.equalTo("aa113")
   *         .findOne();
   * ...
   * }</pre>
   * <p>
   * It is also useful with finding objects by their id when you want to specify
   * further join information to optimise the query.
   * <p>
   * <pre>{@code
   *
   * // Fetch order 42 and additionally fetch join its order details...
   * Order order =
   *     new QOrder()
   *         .fetch("details") // eagerly load the order details
   *         .id.equalTo(42)
   *         .findOne();
   *
   * // the order details were eagerly loaded
   * List<OrderDetail> details = order.getDetails();
   * ...
   * }</pre>
   */
  @Nullable
  T findOne();

  /**
   * Execute the query returning an optional bean.
   */
  Optional<T> findOneOrEmpty();

  /**
   * Execute the query returning the list of objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * <p>
   * <pre>{@code
   *
   * List<Customer> customers =
   *     new QCustomer()
   *       .name.ilike("rob%")
   *       .findList();
   *
   * }</pre>
   *
   * @see Query#findList()
   */
  List<T> findList();

  /**
   * Execute the query returning the result as a Stream.
   * <p>
   * Note that this can support very large queries iterating
   * any number of results. To do so internally it can use
   * multiple persistence contexts.
   * </p>
   * <pre>{@code
   *
   *  // use try with resources to ensure Stream is closed
   *
   *  try (Stream<Customer> stream = query.findStream()) {
   *    stream
   *    .map(...)
   *    .collect(...);
   *  }
   *
   * }</pre>
   */
  Stream<T> findStream();

  /**
   * Execute the query returning the set of objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * <p>
   * <pre>{@code
   *
   * Set<Customer> customers =
   *     new QCustomer()
   *       .name.ilike("rob%")
   *       .findSet();
   *
   * }</pre>
   *
   * @see Query#findSet()
   */
  Set<T> findSet();

  /**
   * Execute the query returning the list of Id's.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   *
   * @see Query#findIds()
   */
  <A> List<A> findIds();

  /**
   * Execute the query returning a map of the objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * <p>
   * You can use setMapKey() or asMapKey() to specify the property to be used as keys
   * on the map. If one is not specified then the id property is used.
   * <p>
   * <pre>{@code
   *
   * Map<String, Product> map =
   *   new QProduct()
   *     .sku.asMapKey()
   *     .findMap();
   *
   * }</pre>
   *
   * @see Query#findMap()
   */
  <K> Map<K, T> findMap();

  /**
   * Execute the query iterating over the results.
   * <p>
   * Note that findIterate (and findEach and findEachWhile) uses a "per graph"
   * persistence context scope and adjusts jdbc fetch buffer size for large
   * queries. As such it is better to use findList for small queries.
   * <p>
   * Remember that with {@link QueryIterator} you must call {@link QueryIterator#close()}
   * when you have finished iterating the results (typically in a finally block).
   * <p>
   * findEach() and findEachWhile() are preferred to findIterate() as they ensure
   * the jdbc statement and resultSet are closed at the end of the iteration.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   * <pre>{@code
   *
   *  Query<Customer> query =
   *    new QCustomer()
   *     .status.equalTo(Customer.Status.NEW)
   *     .orderBy()
   *       id.asc()
   *     .query();
   *
   *  try (QueryIterator<Customer> it = query.findIterate()) {
   *    while (it.hasNext()) {
   *      Customer customer = it.next();
   *      // do something with customer ...
   *    }
   *  }
   *
   * }</pre>
   */
  QueryIterator<T> findIterate();

  /**
   * Execute the query returning a list of values for a single property.
   * <p>
   * <h3>Example</h3>
   * <pre>{@code
   *
   *  List<String> names =
   *    new QCustomer()
   *      .setDistinct(true)
   *      .select(name)
   *      .findSingleAttributeList();
   *
   * }</pre>
   *
   * @return the list of values for the selected property
   */
  <A> List<A> findSingleAttributeList();

  /**
   * Execute the query returning a single value or null for a single property.
   * <p>
   * <h3>Example</h3>
   * <pre>{@code
   *
   *  LocalDate maxDate =
   *    new QCustomer()
   *      .select("max(startDate)")
   *      .findSingleAttribute();
   *
   * }</pre>
   *
   * @return a single value or null for the selected property
   */
  @Nullable
  <A> A findSingleAttribute();

  /**
   * Execute the query returning a single optional attribute value.
   * <p>
   * <h3>Example</h3>
   * <pre>{@code
   *
   *  Optional<String> maybeName =
   *    new QCustomer()
   *      .select(name)
   *      .id.eq(42)
   *      .status.eq(NEW)
   *      .findSingleAttributeOrEmpty();
   *
   * }</pre>
   *
   * @return an optional value for the selected property
   */
  <A> Optional<A> findSingleAttributeOrEmpty();

  /**
   * Execute the query returning a hashset of values for a single property.
   */
  <A> Set<A> findSingleAttributeSet();

  /**
   * Execute the query processing the beans one at a time.
   * <p>
   * This method is appropriate to process very large query results as the
   * beans are consumed one at a time and do not need to be held in memory
   * (unlike #findList #findSet etc)
   * <p>
   * Note that internally Ebean can inform the JDBC driver that it is expecting larger
   * resultSet and specifically for MySQL this hint is required to stop it's JDBC driver
   * from buffering the entire resultSet. As such, for smaller resultSets findList() is
   * generally preferable.
   * <p>
   * Compared with #findEachWhile this will always process all the beans where as
   * #findEachWhile provides a way to stop processing the query result early before
   * all the beans have been read.
   * <p>
   * This method is functionally equivalent to findIterate() but instead of using an
   * iterator uses the Consumer interface which is better suited to use with closures.
   *
   * <pre>{@code
   *
   *  new QCustomer()
   *     .status.equalTo(Status.NEW)
   *     .orderBy().id.asc()
   *     .findEach((Customer customer) -> {
   *
   *       // do something with customer
   *       System.out.println("-- visit " + customer);
   *     });
   *
   * }</pre>
   *
   * @param consumer the consumer used to process the queried beans.
   */
  void findEach(Consumer<T> consumer);

  /**
   * Execute findEach streaming query batching the results for consuming.
   * <p>
   * This query execution will stream the results and is suited to consuming
   * large numbers of results from the database.
   * <p>
   * Typically, we use this batch consumer when we want to do further processing on
   * the beans and want to do that processing in batch form, for example - 100 at
   * a time.
   *
   * @param batch    The number of beans processed in the batch
   * @param consumer Process the batch of beans
   */
  void findEach(int batch, Consumer<List<T>> consumer);

  /**
   * Execute the query using callbacks to a visitor to process the resulting
   * beans one at a time.
   * <p>
   * This method is functionally equivalent to findIterate() but instead of using an
   * iterator uses the Predicate interface which is better suited to use with closures.
   *
   * <pre>{@code
   *
   *  new QCustomer()
   *     .status.equalTo(Status.NEW)
   *     .orderBy().id.asc()
   *     .findEachWhile((Customer customer) -> {
   *
   *       // do something with customer
   *       System.out.println("-- visit " + customer);
   *
   *       // return true to continue processing or false to stop
   *       return (customer.getId() < 40);
   *     });
   *
   * }</pre>
   *
   * @param consumer the consumer used to process the queried beans.
   */
  void findEachWhile(Predicate<T> consumer);

  /**
   * Return versions of a @History entity bean.
   * <p>
   * Generally this query is expected to be a find by id or unique predicates query.
   * It will execute the query against the history returning the versions of the bean.
   */
  List<Version<T>> findVersions();

  /**
   * Return versions of a @History entity bean between a start and end timestamp.
   * <p>
   * Generally this query is expected to be a find by id or unique predicates query.
   * It will execute the query against the history returning the versions of the bean.
   */
  List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end);

  /**
   * Return the count of entities this query should return.
   * <p>
   * This is the number of 'top level' or 'root level' entities.
   */
  int findCount();

  /**
   * Execute find row count query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   *
   * @return a Future object for the row count query
   */
  FutureRowCount<T> findFutureCount();

  /**
   * Execute find Id's query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   *
   * @return a Future object for the list of Id's
   */
  FutureIds<T> findFutureIds();

  /**
   * Execute find list query in a background thread.
   * <p>
   * This query will execute in it's own PersistenceContext and using its own transaction.
   * What that means is that it will not share any bean instances with other queries.
   *
   * @return a Future object for the list result of the query
   */
  FutureList<T> findFutureList();

  /**
   * Execute find map query in a background thread.
   * <p>
   * This query will execute in it's own PersistenceContext and using its own transaction.
   * What that means is that it will not share any bean instances with other queries.
   *
   * @return a Future object for the map result of the query
   */
  <K> FutureMap<K,T> findFutureMap();

  /**
   * Return a PagedList for this query using firstRow and maxRows.
   * <p>
   * The benefit of using this over findList() is that it provides functionality to get the
   * total row count etc.
   * <p>
   * If maxRows is not set on the query prior to calling findPagedList() then a
   * PersistenceException is thrown.
   * <p>
   * <pre>{@code
   *
   *  PagedList<Order> pagedList =
   *    new QOrder()
   *       .setFirstRow(50)
   *       .setMaxRows(20)
   *       .findPagedList();
   *
   *       // fetch the total row count in the background
   *       pagedList.loadRowCount();
   *
   *       List<Order> orders = pagedList.getList();
   *       int totalRowCount = pagedList.getTotalRowCount();
   *
   * }</pre>
   *
   * @return The PagedList
   */
  PagedList<T> findPagedList();

}
