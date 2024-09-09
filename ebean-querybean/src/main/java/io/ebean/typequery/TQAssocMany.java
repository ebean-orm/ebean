package io.ebean.typequery;

import io.ebean.ExpressionList;

/**
 * Expressions for Query Bean ToMany relationships.
 *
 * @param <T> The entity bean type
 * @param <R> The root query bean type
 */
public interface TQAssocMany<T, R, QB> {

  /**
   * Filter the beans fetched for this relationship.
   *
   * @param filter The filter to apply
   */
  R filterMany(java.util.function.Consumer<QB> filter);

  /**
   * Filter the beans fetched for this relationship.
   */
  R filterMany(ExpressionList<T> filter);

  /**
   * @param expressions The expressions including and, or, not etc with ? and ?1 bind params.
   * @param params      The bind parameter values
   * @deprecated for removal - migrate to {@link #filterManyRaw(String, Object...)}.
   * <p>
   * Apply a filter when fetching these beans.
   * <p>
   * The expressions can use any valid Ebean expression and contain
   * placeholders for bind values using <code>?</code> or <code>?1</code> style.
   * </p>
   *
   * <pre>{@code
   *
   *     new QCustomer()
   *       .name.startsWith("Postgres")
   *       .contacts.filterMany("firstName istartsWith ?", "Rob")
   *       .findList();
   *
   * }</pre>
   *
   * <pre>{@code
   *
   *     new QCustomer()
   *       .name.startsWith("Postgres")
   *       .contacts.filterMany("whenCreated inRange ? to ?", startDate, endDate)
   *       .findList();
   *
   * }</pre>
   */
  @Deprecated(forRemoval = true)
  R filterMany(String expressions, Object... params);

  /**
   * Add filter expressions for the many path. The expressions can include SQL functions if
   * desired and the property names are translated to column names.
   * <p>
   * The expressions can contain placeholders for bind values using <code>?</code> or <code>?1</code> style.
   *
   * <pre>{@code
   *
   *     new QCustomer()
   *       .name.startsWith("Shrek")
   *       .contacts.filterManyRaw("status = ? and firstName like ?", Contact.Status.NEW, "Rob%")
   *       .findList();
   *
   * }</pre>
   *
   * @param rawExpressions The raw expressions which can include ? and ?1 style bind parameter placeholders
   * @param params         The parameter values to bind
   */
  R filterManyRaw(String rawExpressions, Object... params);

  /**
   * Is empty for a collection property.
   * <p>
   * This effectively adds a not exists sub-query on the collection property.
   * <p>
   * This expression only works on OneToMany and ManyToMany properties.
   */
  R isEmpty();

  /**
   * Is not empty for a collection property.
   * <p>
   * This effectively adds an exists sub-query on the collection property.
   * <p>
   * This expression only works on OneToMany and ManyToMany properties.
   */
  R isNotEmpty();
}
