package com.avaje.ebean;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Intended to be used as a base class for 'Finder' implementations that can then
 * be injected or used as public static fields on the associated entity bean.
 * <p>
 * These 'finders' are a place to organise all the finder methods for that bean type
 * and specific finder methods are expected to be added (find by unique properties etc).
 * </p>
 *
 * <h3>Testing</h3>
 * <p>
 *   For testing the mocki-ebean project has the ability to replace the finder implementation
 *
 * </p>
 * <pre>{@code
 *
 * public class CustomerFinder extends Finder<Customer> {
 *
 *   public CustomerFinder() {
 *     super(Customer.class);
 *   }
 *
 *   // Add your customer finder methods ...
 *
 *   public Customer byName(String name) {
 *     return query().eq("name", name).findUnique();
 *   }
 *
 *   public List<Customer> findNew() {
 *     return query()
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
 * }</pre>
 */
public class Finder<I, T> {

  /**
   * The entity bean type.
   */
  private final Class<T> type;

  /**
   * The name of the EbeanServer, null for the default server.
   */
  private final String serverName;

  /**
   * Create with the type of the entity bean.
   *
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
   * }</pre>
   */
  public Finder(Class<T> type) {
    this.type = type;
    this.serverName = null;
  }

  /**
   * Create with the type of the entity bean and specific server name.
   */
  public Finder(Class<T> type, String serverName) {
    this.type = type;
    this.serverName = serverName;
  }

  /**
   * Return the underlying 'default' EbeanServer.
   *
   * <p>
   * This provides full access to the API such as explicit transaction demarcation etc.
   *
   */
  public EbeanServer db() {
    return Ebean.getServer(serverName);
  }

  /**
   * Return typically a different EbeanServer to the default.
   * <p>
   * This is equivalent to {@link Ebean#getServer(String)}
   *
   * @param server
   *          The name of the EbeanServer. If this is null then the default EbeanServer is
   *          returned.
   */
  public EbeanServer db(String server) {
    return Ebean.getServer(server);
  }

  /**
   * Creates an entity reference for this ID.
   *
   * <p>
   * Equivalent to {@link EbeanServer#getReference(Class, Object)}
   */
  public T ref(I id) {
    return db().getReference(type, id);
  }

  /**
   * Retrieves an entity by ID.
   *
   * <p>
   * Equivalent to {@link EbeanServer#find(Class, Object)}
   */
  @Nullable
  public T byId(I id) {
    return db().find(type, id);
  }

  /**
   * Delete a bean by Id.
   * <p>
   * Equivalent to {@link EbeanServer#delete(Class, Object)}
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
   * Creates a query.
   * <p>
   * Equivalent to {@link EbeanServer#find(Class)}
   */
  public Query<T> query() {
    return db().find(type);
  }

}
