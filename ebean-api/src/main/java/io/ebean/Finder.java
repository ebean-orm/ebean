package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.List;

/**
 * Intended to be used as a base class for 'Finder' implementations that can then
 * be injected or used as public static fields on the associated entity bean.
 * <p>
 * When using dependency injection {@link BeanRepository} and {@link BeanFinder}
 * are expected to be used rather than this Finder.
 * <p>
 * These 'finders' are a place to organise all the finder methods for that bean type
 * and specific finder methods are expected to be added (find by unique properties etc).
 * </p>
 * <h3>Testing</h3>
 * <p>
 * For testing the mocki-ebean project has the ability to replace the finder implementation.
 * </p>
 * <pre>{@code
 *
 * public class CustomerFinder extends Finder<Long,Customer> {
 *
 *   public CustomerFinder() {
 *     super(Customer.class);
 *   }
 *
 *   // Add finder methods ...
 *
 *   public Customer byName(String name) {
 *     return query().eq("name", name).findOne();
 *   }
 *
 *   public List<Customer> findNew() {
 *     return query().where()
 *       .eq("status", Customer.Status.NEW)
 *       .orderBy("name")
 *       .findList()
 *   }
 * }
 *
 * @Entity
 * public class Customer extends BaseModel {
 *
 *   public static final CustomerFinder find = new CustomerFinder();
 *   ...
 *
 * }
 * }</pre>
 * <p>
 *  When the Finder is registered as a field on Customer it can then be used like:
 * </p>
 * <pre>{@code
 *
 *   Customer rob = Customer.find.byName("Rob");
 *
 * }</pre>
 *
 * @see BeanRepository
 * @see BeanFinder
 */
@NullMarked
public class Finder<I, T> {

  /**
   * The entity bean type.
   */
  private final Class<T> type;

  /**
   * The name of the database this finder will use, null for the default database.
   */
  private final String _$dbName;

  /**
   * Create with the type of the entity bean.
   * <pre>{@code
   *
   * public class CustomerFinder extends Finder<Customer> {
   *
   *   public CustomerFinder() {
   *     super(Customer.class);
   *   }
   *
   *   // ... add extra customer specific finder methods
   * }
   *
   * @Entity
   * public class Customer extends BaseModel {
   *
   *   public static final CustomerFinder find = new CustomerFinder();
   *   ...
   *
   * }
   * }</pre>
   */
  public Finder(Class<T> type) {
    this.type = type;
    this._$dbName = null;
  }

  /**
   * Create with the type of the entity bean and specific database name.
   */
  public Finder(Class<T> type, String databaseName) {
    this.type = type;
    this._$dbName = databaseName;
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
   * Return the Database this finder will use.
   */
  public Database db() {
    return DB.byName(_$dbName);
  }

  /**
   * Return typically a different Database to the default.
   * <p>
   * This is equivalent to {@link DB#byName(String)}
   *
   * @param databaseName The name of the Database. If this is null then the default database is returned.
   */
  public Database db(String databaseName) {
    return DB.byName(databaseName);
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
   * <p>
   * Equivalent to {@link Database#find(Class, Object)}
   */
  @Nullable
  public T byId(I id) {
    return db().find(type, id);
  }

  /**
   * Delete a bean by Id.
   * <p>
   * Equivalent to {@link Database#delete(Class, Object)}
   */
  public void deleteById(I id) {
    db().delete(type, id);
  }

  /**
   * Retrieves all entities of the given type.
   */
  public List<T> all() {
    return query().findList();
  }

  /**
   * Creates an update query.
   *
   * <pre>{@code
   *
   *  int rows =
   *      finder.update()
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
  public UpdateQuery<T> update() {
    return db().update(type);
  }

  /**
   * Creates a query.
   * <p>
   * Equivalent to {@link Database#find(Class)}
   */
  public Query<T> query() {
    return db().find(type);
  }

  /**
   * Creates a native sql query.
   */
  public Query<T> nativeSql(String nativeSql) {
    return db().findNative(type, nativeSql);
  }

}
