package io.ebean.typequery;

import io.ebean.ExpressionList;

/**
 * Expressions for Query Bean ToMany relationships.
 *
 * @param <T> The entity bean type
 * @param <R> The root query bean type
 */
public interface TQMany<T, R> extends TQBase<R> {

  /**
   * Apply a filter when fetching these beans.
   */
  default R filterMany(ExpressionList<T> filter) {
    @SuppressWarnings("unchecked")
    ExpressionList<T> expressionList = (ExpressionList<T>) _expr().filterMany(_name());
    expressionList.addAll(filter);
    return _root();
  }

  /**
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
   *
   * @param expressions The expressions including and, or, not etc with ? and ?1 bind params.
   * @param params      The bind parameter values
   */
  @Deprecated(forRemoval = true)
  default R filterMany(String expressions, Object... params) {
    _expr().filterMany(_name(), expressions, params);
    return _root();
  }

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
   * @param params The parameter values to bind
   */
  default R filterManyRaw(String rawExpressions, Object... params) {
    _expr().filterManyRaw(_name(), rawExpressions, params);
    return _root();
  }

  /**
   * Is empty for a collection property.
   * <p>
   * This effectively adds a not exists sub-query on the collection property.
   * </p>
   * <p>
   * This expression only works on OneToMany and ManyToMany properties.
   * </p>
   */
  default R isEmpty() {
    _expr().isEmpty(_name());
    return _root();
  }

  /**
   * Is not empty for a collection property.
   * <p>
   * This effectively adds an exists sub-query on the collection property.
   * </p>
   * <p>
   * This expression only works on OneToMany and ManyToMany properties.
   * </p>
   */
  default R isNotEmpty() {
    _expr().isNotEmpty(_name());
    return _root();
  }
}
