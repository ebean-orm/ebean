package io.ebean;

/**
 * Builder for ORM Query projection (the select and fetch part).
 *
 * @param <SELF> The builder type
 * @param <T>    The entity bean type
 */
public interface QueryBuilderProjection<SELF extends QueryBuilderProjection<SELF, T>, T> {

  /**
   * Apply the path properties replacing the select and fetch clauses.
   * <p>
   * This is typically used when the FetchPath is applied to both the query and the JSON output.
   */
  SELF apply(FetchPath fetchPath);

  /**
   * Specify the properties to fetch on the root level entity bean in comma delimited format.
   * <p>
   * The Id property is automatically included in the properties to fetch unless setDistinct(true)
   * is set on the query.
   * </p>
   * <p>
   * Use {@link #fetch(String, String)} to specify specific properties to fetch
   * on other non-root level paths of the object graph.
   * </p>
   * <pre>{@code
   *
   * List<Customer> customers = DB.find(Customer.class)
   *     // Only fetch the customer id, name and status.
   *     // This is described as a "Partial Object"
   *     .select("name, status")
   *     .where.ilike("name", "rob%")
   *     .findList();
   *
   * }</pre>
   *
   * @param fetchProperties the properties to fetch for this bean (* = all properties).
   */
  SELF select(String fetchProperties);

  /**
   * Set DISTINCT ON clause. This is a Postgres only SQL feature.
   *
   * @param distinctOn The properties to include in the DISTINCT ON clause.
   */
  SELF distinctOn(String distinctOn);

  /**
   * Apply the fetchGroup which defines what part of the object graph to load.
   */
  SELF select(FetchGroup<T> fetchGroup);

  /**
   * Specify a path to fetch eagerly including specific properties.
   * <p>
   * Ebean will endeavour to fetch this path using a SQL join. If Ebean determines that it can
   * not use a SQL join (due to maxRows or because it would result in a cartesian product) Ebean
   * will automatically convert this fetch query into a "query join" - i.e. use fetchQuery().
   * </p>
   * <pre>{@code
   *
   * // query orders...
   * List<Order> orders = DB.find(Order.class)
   *       // fetch the customer...
   *       // ... getting the customers name and phone number
   *       .fetch("customer", "name, phoneNumber")
   *
   *       // ... also fetch the customers billing address (* = all properties)
   *       .fetch("customer.billingAddress", "*")
   *       .findList();
   * }</pre>
   * <p>
   * If columns is null or "*" then all columns/properties for that path are fetched.
   * </p>
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers = DB.find(Customer.class)
   *     .select("name, status")
   *     .fetch("contacts", "firstName,lastName,email")
   *     .findList();
   *
   * }</pre>
   *
   * @param path            the property path we wish to fetch eagerly.
   * @param fetchProperties properties of the associated bean that you want to include in the
   *                        fetch (* means all properties, null also means all properties).
   */
  SELF fetch(String path, String fetchProperties);

  /**
   * Fetch the path and properties using a "query join" (separate SQL query).
   * <p>
   * This is the same as:
   * </p>
   * <pre>{@code
   *
   *  fetch(path, fetchProperties, FetchConfig.ofQuery())
   *
   * }</pre>
   * <p>
   * This would be used instead of a fetch() when we use a separate SQL query to fetch this
   * part of the object graph rather than a SQL join.
   * <p>
   * We might typically get a performance benefit when the path to fetch is a OneToMany
   * or ManyToMany, the 'width' of the 'root bean' is wide and the cardinality of the many
   * is high.
   *
   * @param path            the property path we wish to fetch eagerly.
   * @param fetchProperties properties of the associated bean that you want to include in the
   *                        fetch (* means all properties, null also means all properties).
   */
  SELF fetchQuery(String path, String fetchProperties);

  /**
   * Fetch the path and properties using L2 bean cache.
   *
   * @param path            The path of the beans we are fetching from L2 cache.
   * @param fetchProperties The properties that should be loaded.
   */
  SELF fetchCache(String path, String fetchProperties);

  /**
   * Fetch the path and properties lazily (via batch lazy loading).
   * <p>
   * This is the same as:
   *
   * <pre>{@code
   *
   *  fetch(path, fetchProperties, FetchConfig.ofLazy())
   *
   * }</pre>
   * <p>
   * The reason for using fetchLazy() is to either:
   * <ul>
   * <li>Control/tune what is fetched as part of lazy loading</li>
   * <li>Make use of the L2 cache, build this part of the graph from L2 cache</li>
   * </ul>
   *
   * @param path            the property path we wish to fetch lazily.
   * @param fetchProperties properties of the associated bean that you want to include in the
   *                        fetch (* means all properties, null also means all properties).
   */
  SELF fetchLazy(String path, String fetchProperties);

  /**
   * Additionally specify a FetchConfig to use a separate query or lazy loading
   * to load this path.
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers = DB.find(Customer.class)
   *     .select("name, status")
   *     .fetch("contacts", "firstName,lastName,email", FetchConfig.ofLazy(10))
   *     .findList();
   *
   * }</pre>
   *
   * @param path the property path we wish to fetch eagerly.
   */
  SELF fetch(String path, String fetchProperties, FetchConfig fetchConfig);

  /**
   * Specify a path to fetch eagerly including all its properties.
   * <p>
   * Ebean will endeavour to fetch this path using a SQL join. If Ebean determines that it can
   * not use a SQL join (due to maxRows or because it would result in a cartesian product) Ebean
   * will automatically convert this fetch query into a "query join" - i.e. use fetchQuery().
   * </p>
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers = DB.find(Customer.class)
   *     // eager fetch the contacts
   *     .fetch("contacts")
   *     .findList();
   *
   * }</pre>
   *
   * @param path the property path we wish to fetch eagerly.
   */
  SELF fetch(String path);

  /**
   * Fetch the path eagerly using a "query join" (separate SQL query).
   * <p>
   * This is the same as:
   * <pre>{@code
   *
   *  fetch(path, FetchConfig.ofQuery())
   *
   * }</pre>
   * <p>
   * This would be used instead of a fetch() when we use a separate SQL query to fetch this
   * part of the object graph rather than a SQL join.
   * <p>
   * We might typically get a performance benefit when the path to fetch is a OneToMany
   * or ManyToMany, the 'width' of the 'root bean' is wide and the cardinality of the many
   * is high.
   *
   * @param path the property path we wish to fetch eagerly
   */
  SELF fetchQuery(String path);

  /**
   * Fetch the path eagerly using L2 cache.
   */
  SELF fetchCache(String path);

  /**
   * Fetch the path lazily (via batch lazy loading).
   * <p>
   * This is the same as:
   * </p>
   * <pre>{@code
   *
   *  fetch(path, FetchConfig.ofLazy())
   *
   * }</pre>
   * <p>
   * The reason for using fetchLazy() is to either:
   * </p>
   * <ul>
   * <li>Control/tune what is fetched as part of lazy loading</li>
   * <li>Make use of the L2 cache, build this part of the graph from L2 cache</li>
   * </ul>
   *
   * @param path the property path we wish to fetch lazily.
   */
  SELF fetchLazy(String path);

  /**
   * Additionally specify a JoinConfig to specify a "query join" and or define
   * the lazy loading query.
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers = DB.find(Customer.class)
   *     // lazy fetch contacts with a batch size of 100
   *     .fetch("contacts", FetchConfig.ofLazy(100))
   *     .findList();
   *
   * }</pre>
   */
  SELF fetch(String path, FetchConfig fetchConfig);

}
