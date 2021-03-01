package io.ebean;

import io.ebean.bean.EntityBean;


/**
 * A MappedSuperclass base class that provides convenience methods for inserting, updating and
 * deleting beans.
 * <p>
 * By having your entity beans extend this it provides a 'Active Record' style programming model for
 * Ebean users.
 * <p>
 * Note that there is a ebean-mocker project that enables you to use Mockito or similar
 * tools to still mock out the underlying 'default Database' for testing purposes.
 * <p>
 * You may choose not use this Model mapped superclass if you don't like the 'Active Record' style
 * or if you believe it 'pollutes' your entity beans.
 * <p>
 * You can use Dependency Injection like Guice or Spring to construct and wire a Database instance
 * and have that same instance used with this Model and Finder. The way that works is that when the
 * DI container creates the Database instance it can be registered with DB. In this
 * way the Database instance can be injected as per normal Guice / Spring dependency injection and
 * that same instance also used to support the Model and Finder active record style.
 * <p>
 * If you choose to use the Model mapped superclass you will probably also chose to additionally add
 * a {@link Finder} as a public static field to complete the active record pattern and provide a
 * relatively nice clean way to write queries.
 * <p>
 * <h3>Typical common @MappedSuperclass</h3>
 * <pre>{@code
 *
 *     // Typically there is a common base model that has some
 *     // common properties like the ones below
 *
 *   @MappedSuperclass
 *   public class BaseModel extends Model {
 *
 *     @Id Long id;
 *
 *     @Version Long version;
 *
 *     @WhenCreated Timestamp whenCreated;
 *
 *     @WhenUpdated Timestamp whenUpdated;
 *
 *     ...
 *   }
 * }</pre>
 * <p>
 * <h3>Extend the Model</h3>
 * <pre>{@code
 *
 *     // Extend the mappedSuperclass
 *
 *     @Entity @Table(name="o_account")
 *     public class Customer extends BaseModel {
 *
 *       String name;
 *       ...
 *     }
 *
 * }</pre>
 * <p>
 * <h3>Modal: save()</h3>
 * <pre>{@code
 *
 *     // Active record style ... save(), delete() etc
 *     Customer customer = new Customer();
 *     customer.setName("AC234");
 *
 *     // save() method inherited from Model
 *     customer.save();
 *
 * }</pre>
 */
public abstract class Model {

  /**
   * The name of the database this entity will use, null for the default database.
   */
  private final String _$dbName;

  /**
   * Create using the default database.
   */
  public Model() {
    this._$dbName = null;
  }

  /**
   * Create with a named database (typically not the default database).
   */
  public Model(String dbName) {
    this._$dbName = dbName;
  }

  /**
   * Return the underlying 'default' Database.
   * <p>
   * This provides full access to the API such as explicit transaction demarcation etc.
   * <p>
   * Example:
   * <pre>{@code
   *
   * try (Transaction transaction = Customer.db().beginTransaction()) {
   *
   *   // turn off cascade persist for this transaction
   *   transaction.setPersistCascade(false);
   *
   *   // extra control over jdbc batching for this transaction
   *   transaction.setBatchGetGeneratedKeys(false);
   *   transaction.setBatchMode(true);
   *   transaction.setBatchSize(20);
   *
   *   Customer customer = new Customer();
   *   customer.setName(&quot;Roberto&quot;);
   *   customer.save();
   *
   *   Customer otherCustomer = new Customer();
   *   otherCustomer.setName("Franko");
   *   otherCustomer.save();
   *
   *   transaction.commit();
   *
   * }
   *
   * }</pre>
   */
  public Database db() {
    return DB.byName(_$dbName);
  }

  /**
   * Marks the entity bean as dirty.
   * <p>
   * This is used so that when a bean that is otherwise unmodified is updated the version
   * property is updated.
   * <p>
   * An unmodified bean that is saved or updated is normally skipped and this marks the bean as
   * dirty so that it is not skipped.
   * <p>
   * <pre>{@code
   *
   * Customer customer = Customer.find.byId(id);
   *
   * // mark the bean as dirty so that a save() or update() will
   * // increment the version property
   * customer.markAsDirty();
   * customer.save();
   *
   * }</pre>
   *
   * @see Database#markAsDirty(Object)
   */
  public void markAsDirty() {
    db().markAsDirty(this);
  }

  /**
   * Mark the property as unset or 'not loaded'.
   * <p>
   * This would be used to specify a property that we did not wish to include in a stateless update.
   * </p>
   * <pre>{@code
   *
   *   // populate an entity bean from JSON or whatever
   *   User user = ...;
   *
   *   // mark the email property as 'unset' so that it is not
   *   // included in a 'stateless update'
   *   user.markPropertyUnset("email");
   *
   *   user.update();
   *
   * }</pre>
   *
   * @param propertyName the name of the property on the bean to be marked as 'unset'
   */
  public void markPropertyUnset(String propertyName) {
    ((EntityBean) this)._ebean_getIntercept().setPropertyLoaded(propertyName, false);
  }

  /**
   * Insert or update this entity depending on its state.
   * <p>
   * Ebean will detect if this is a new bean or a previously fetched bean and perform either an
   * insert or an update based on that.
   *
   * @see Database#save(Object)
   */
  public void save() {
    db().save(this);
  }

  /**
   * Save this entity with an explicit transaction.
   */
  public void save(Transaction transaction) {
    db().save(this, transaction);
  }

  /**
   * Flush any batched changes to the database.
   * <p>
   * When using JDBC batch flushing occurs automatically at commit() time or when the batch size
   * is reached. This provides the ability to manually flush the batch.
   * </p>
   */
  public void flush() {
    db().flush();
  }

  /**
   * Update this entity.
   *
   * @see Database#update(Object)
   */
  public void update() {
    db().update(this);
  }

  /**
   * Update this entity with an explicit transaction.
   */
  public void update(Transaction transaction) {
    db().update(this, transaction);
  }

  /**
   * Insert this entity.
   *
   * @see Database#insert(Object)
   */
  public void insert() {
    db().insert(this);
  }

  /**
   * Insert with an explicit transaction.
   */
  public void insert(Transaction transaction) {
    db().insert(this, transaction);
  }

  /**
   * Delete this bean.
   * <p>
   * This will return true if the bean was deleted successfully or JDBC batch is being used.
   * </p>
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * </p>
   * <p>
   * If the Bean does not have a version property (or loaded version property) and
   * the bean does not exist then this returns false indicating that nothing was
   * deleted. Note that, if JDBC batch mode is used then this always returns true.
   * </p>
   *
   * @see Database#delete(Object)
   */
  public boolean delete() {
    return db().delete(this);
  }

  /**
   * Delete this entity with an explicit transaction.
   */
  public boolean delete(Transaction transaction) {
    return db().delete(this, transaction);
  }

  /**
   * Delete a bean permanently without soft delete.
   * <p>
   * This is used when the bean contains a <code>@SoftDelete</code> property and we
   * want to perform a hard/permanent delete.
   * </p>
   *
   * @see Database#deletePermanent(Object)
   */
  public boolean deletePermanent() {
    return db().deletePermanent(this);
  }

  /**
   * Delete a bean permanently without soft delete using an explicit transaction.
   */
  public boolean deletePermanent(Transaction transaction) {
    return db().deletePermanent(this, transaction);
  }

  /**
   * Refreshes this entity from the database.
   *
   * @see Database#refresh(Object)
   */
  public void refresh() {
    db().refresh(this);
  }

}
