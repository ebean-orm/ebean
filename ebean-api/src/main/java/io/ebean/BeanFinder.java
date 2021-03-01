package io.ebean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Provides finder functionality for use with "Dependency Injection style" use of Ebean.
 * <p>
 * Note that typically users would extend BeanRepository rather than BeanFinder.
 * </p>
 * <pre>{@code
 *
 * public class CustomerFinder extends BeanFinder<Long,Customer> {
 *
 *   @Inject
 *   public CustomerFinder(Database database) {
 *     super(Customer.class, database);
 *   }
 *
 *   // ... add customer specific finders
 * }
 *
 * }</pre>
 *
 * @param <I> The ID type
 * @param <T> The Bean type
 */
public abstract class BeanFinder<I,T> {

  protected final Database server;

  protected final Class<T> type;

  /**
   * Create with the given bean type and Database instance.
   *
   * @param type The bean type
   * @param server The Database instance typically created via Spring factory or equivalent.
   */
  protected BeanFinder(Class<T> type, Database server) {
    this.type = type;
    this.server = server;
  }

  /**
   * Return the Database to use.
   */
  public Database db() {
    return server;
  }

  /**
   * Return the current transaction.
   */
  public Transaction currentTransaction() {
    return db().currentTransaction();
  }

  /**
   * Flush the JDBC batch on the current transaction.
   */
  public void flush() {
    db().flush();
  }

  /**
   * Return typically a different Database to the default.
   * <p>
   * This is equivalent to {@link DB#byName(String)}
   *
   * @param server The name of the Database. If this is null then the default Database is returned.
   */
  public Database db(String server) {
    return DB.byName(server);
  }

  /**
   * Creates an entity reference for this ID.
   * <p>
   * Equivalent to {@link Database#getReference(Class, Object)}
   */
  @Nonnull
  public T ref(I id) {
    return db().getReference(type, id);
  }

  /**
   * Retrieves an entity by ID.
   */
  @Nullable
  public T findById(I id) {
    return db().find(type, id);
  }

  /**
   * Find an entity by ID returning an Optional.
   */
  @Nullable
  public Optional<T> findByIdOrEmpty(I id) {
    return db().find(type).setId(id).findOneOrEmpty();
  }

  /**
   * Delete a bean by Id.
   */
  public void deleteById(I id) {
    db().delete(type, id);
  }

  /**
   * Retrieves all entities of the given type.
   */
  @Nonnull
  public List<T> findAll() {
    return query().findList();
  }

  /**
   * Creates an update query.
   *
   * <pre>{@code
   *
   *  int rows =
   *      updateQuery()
   *      .set("status", Customer.Status.ACTIVE)
   *      .set("updtime", new Timestamp(System.currentTimeMillis()))
   *      .where()
   *        .gt("id", 1000)
   *        .update();
   *
   * }</pre>
   *
   * <p>
   * Equivalent to {@link Database#update(Class)}
   */
  protected UpdateQuery<T> updateQuery() {
    return db().update(type);
  }

  /**
   * Creates a query.
   * <p>
   * Equivalent to {@link Database#find(Class)}
   */
  protected Query<T> query() {
    return db().find(type);
  }

  /**
   * Creates a native sql query.
   */
  protected Query<T> nativeSql(String nativeSql) {
    return db().findNative(type, nativeSql);
  }

  /**
   * Creates a query using the ORM query language.
   */
  protected Query<T> query(String ormQuery) {
    return db().createQuery(type, ormQuery);
  }
}
