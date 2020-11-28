package io.ebean.typequery;

import io.ebean.ExpressionList;

/**
 * Base type for associated beans.
 *
 * @param <T> the entity bean type (normal entity bean type e.g. Customer)
 * @param <R> the specific root query bean type (e.g. QCustomer)
 */
@SuppressWarnings("rawtypes")
public abstract class TQAssocBean<T, R> extends TQProperty<R> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name the name of the property
   * @param root the root query bean instance
   */
  public TQAssocBean(String name, R root) {
    this(name, root, null);
  }

  /**
   * Construct with additional path prefix.
   */
  public TQAssocBean(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Eagerly fetch this association fetching all the properties.
   */
  public R fetch() {
    ((TQRootBean) _root).query().fetch(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join".
   */
  public R fetchQuery() {
    ((TQRootBean) _root).query().fetchQuery(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 bean cache.
   * Cache misses are populated via fetchQuery().
   */
  public R fetchCache() {
    ((TQRootBean) _root).query().fetchCache(_name);
    return _root;
  }

  /**
   * Use lazy loading for fetching this association.
   */
  public R fetchLazy() {
    ((TQRootBean) _root).query().fetchLazy(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association with the properties specified.
   */
  public R fetch(String properties) {
    ((TQRootBean) _root).query().fetch(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join" with the properties specified.
   */
  public R fetchQuery(String properties) {
    ((TQRootBean) _root).query().fetchQuery(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 cache with the properties specified.
   * Cache misses are populated via  fetchQuery().
   */
  public R fetchCache(String properties) {
    ((TQRootBean) _root).query().fetchCache(_name, properties);
    return _root;
  }

  /**
   * Deprecated in favor of fetch().
   *
   * @deprecated
   */
  public R fetchAll() {
    return fetch();
  }

  /**
   * Eagerly fetch this association fetching some of the properties.
   */
  @SafeVarargs
  protected final R fetchProperties(TQProperty<?>... props) {
    ((TQRootBean) _root).query().fetch(_name, properties(props));
    return _root;
  }

  /**
   * Eagerly fetch query this association fetching some of the properties.
   */
  @SafeVarargs
  protected final R fetchQueryProperties(TQProperty<?>... props) {
    ((TQRootBean) _root).query().fetchQuery(_name, properties(props));
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 bean cache.
   */
  @SafeVarargs
  protected final R fetchCacheProperties(TQProperty<?>... props) {
    ((TQRootBean) _root).query().fetchCache(_name, properties(props));
    return _root;
  }

  /**
   * Eagerly fetch query this association fetching some of the properties.
   */
  @SafeVarargs
  protected final R fetchLazyProperties(TQProperty<?>... props) {
    ((TQRootBean) _root).query().fetchLazy(_name, properties(props));
    return _root;
  }

  /**
   * Append the properties as a comma delimited string.
   */
  @SafeVarargs
  protected final String properties(TQProperty<?>... props) {
    StringBuilder selectProps = new StringBuilder(50);
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        selectProps.append(",");
      }
      selectProps.append(props[i].propertyName());
    }
    return selectProps.toString();
  }

  /**
   * Is equal to by ID property.
   */
  public R eq(T other) {
    expr().eq(_name, other);
    return _root;
  }

  /**
   * Is equal to by ID property.
   */
  public R equalTo(T other) {
    return eq(other);
  }

  /**
   * Is not equal to by ID property.
   */
  public R ne(T other) {
    expr().ne(_name, other);
    return _root;
  }

  /**
   * Is not equal to by ID property.
   */
  public R notEqualTo(T other) {
    return ne(other);
  }

  /**
   * Apply a filter when fetching these beans.
   */
  public R filterMany(ExpressionList<T> filter) {

    @SuppressWarnings("unchecked")
    ExpressionList<T> expressionList = (ExpressionList<T>) expr().filterMany(_name);
    expressionList.addAll(filter);
    return _root;
  }

  /**
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
  public R filterMany(String expressions, Object... params) {
    expr().filterMany(_name, expressions, params);
    return _root;
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
  public R isEmpty() {
    expr().isEmpty(_name);
    return _root;
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
  public R isNotEmpty() {
    expr().isNotEmpty(_name);
    return _root;
  }

}
