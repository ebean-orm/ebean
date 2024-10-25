package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Provides finder functionality for use with "Dependency Injection style" use of Ebean.
 * <p>
 * Note that typically users would extend BeanRepository rather than BeanFinder.
 * </p>
 * <pre>{@code
 *
 * @Component
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
 *
 * @see BeanRepository
 */
@NullMarked
public abstract class BeanFinder<I,T> {

  protected final Database database;
  protected final Class<T> type;

  /**
   * Create with the given bean type and Database instance.
   *
   * @param type The bean type
   * @param database The Database instance typically created via Spring factory or equivalent.
   */
  protected BeanFinder(Class<T> type, Database database) {
    this.type = type;
    this.database = database;
  }

  /**
   * Return the Database to use.
   */
  public Database db() {
    return database;
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
   * @param name The name of the Database. If this is null then the default Database is returned.
   */
  public Database db(String name) {
    return DB.byName(name);
  }

  /**
   * Creates an entity reference for this ID.
   * <p>
   * Equivalent to {@link Database#reference(Class, Object)}
   */
  public T ref(I id) {
    return db().reference(type, id);
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

}
