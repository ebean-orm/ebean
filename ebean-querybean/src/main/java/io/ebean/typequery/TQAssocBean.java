package io.ebean.typequery;

import io.ebean.ExpressionList;
import io.ebean.FetchConfig;
import io.ebean.FetchGroup;
import io.ebeaninternal.api.SpiQueryFetch;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base type for associated beans that are not embeddable.
 *
 * @param <T> the entity bean type (normal entity bean type e.g. Customer)
 * @param <R> the specific root query bean type (e.g. QCustomer)
 * @param <QB> the query bean type
 */
@SuppressWarnings("rawtypes")
public abstract class TQAssocBean<T, R, QB> extends TQAssoc<T, R> {

  private static final FetchConfig FETCH_DEFAULT = FetchConfig.ofDefault();
  private static final FetchConfig FETCH_QUERY = FetchConfig.ofQuery();
  private static final FetchConfig FETCH_LAZY = FetchConfig.ofLazy();
  private static final FetchConfig FETCH_CACHE = FetchConfig.ofCache();

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
  public final R fetch() {
    ((TQRootBean) _root).query().fetch(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join".
   */
  public final R fetchQuery() {
    ((TQRootBean) _root).query().fetchQuery(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 bean cache.
   * Cache misses are populated via fetchQuery().
   */
  public final R fetchCache() {
    ((TQRootBean) _root).query().fetchCache(_name);
    return _root;
  }

  /**
   * Use lazy loading for fetching this association.
   */
  public final R fetchLazy() {
    ((TQRootBean) _root).query().fetchLazy(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association with the properties specified.
   */
  public final R fetch(String properties) {
    ((TQRootBean) _root).query().fetch(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join" with the properties specified.
   */
  public final R fetchQuery(String properties) {
    ((TQRootBean) _root).query().fetchQuery(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 cache with the properties specified.
   * Cache misses are populated via  fetchQuery().
   */
  public final R fetchCache(String properties) {
    ((TQRootBean) _root).query().fetchCache(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association loading the specified properties.
   */
  @SafeVarargs @SuppressWarnings("varargs")
  public final R fetch(TQProperty<QB,?>... properties) {
    return fetchWithProperties(FETCH_DEFAULT, properties);
  }

  /**
   * Eagerly fetch this association using a 'query join' loading the specified properties.
   */
  @SafeVarargs @SuppressWarnings("varargs")
  public final R fetchQuery(TQProperty<QB,?>... properties) {
    return fetchWithProperties(FETCH_QUERY, properties);
  }

  /**
   * Eagerly fetch this association using L2 cache.
   */
  @SafeVarargs @SuppressWarnings("varargs")
  public final R fetchCache(TQProperty<QB,?>... properties) {
    return fetchWithProperties(FETCH_CACHE, properties);
  }

  /**
   * Use lazy loading for this association loading the specified properties.
   */
  @SafeVarargs @SuppressWarnings("varargs")
  public final R fetchLazy(TQProperty<QB,?>... properties) {
    return fetchWithProperties(FETCH_LAZY, properties);
  }

  private R fetchWithProperties(FetchConfig config, TQProperty<?, ?>... props) {
    spiQuery().fetchProperties(_name, properties(props), config);
    return _root;
  }

  /**
   * Fetch using the nested FetchGroup.
   */
  public final R fetch(FetchGroup<T> nestedGroup) {
    return fetchNested(nestedGroup, FETCH_DEFAULT);
  }

  /**
   * Fetch query using the nested FetchGroup.
   */
  public final R fetchQuery(FetchGroup<T> nestedGroup) {
    return fetchNested(nestedGroup, FETCH_QUERY);
  }

  /**
   * Fetch cache using the nested FetchGroup.
   */
  public final R fetchCache(FetchGroup<T> nestedGroup) {
    return fetchNested(nestedGroup, FETCH_CACHE);
  }

  private R fetchNested(FetchGroup<T> nestedGroup, FetchConfig fetchConfig) {
    OrmQueryDetail nestedDetail = ((SpiFetchGroup) nestedGroup).underlying();
    spiQuery().addNested(_name, nestedDetail, fetchConfig);
    return _root;
  }

  private SpiQueryFetch spiQuery() {
    return (SpiQueryFetch) ((TQRootBean) _root).query();
  }

  private Set<String> properties(TQProperty<?, ?>... props) {
    Set<String> set = new LinkedHashSet<>();
    for (TQProperty<?, ?> prop : props) {
      set.add(prop.propertyName());
    }
    return set;
  }


  /**
   * Apply a filter when fetching these beans.
   */
  public final R filterMany(ExpressionList<T> filter) {
    @SuppressWarnings("unchecked")
    ExpressionList<T> expressionList = (ExpressionList<T>) expr().filterMany(_name);
    expressionList.addAll(filter);
    return _root;
  }

  /**
   * Deprecated for removal - migrate to filterManyRaw()
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
  public final R filterMany(String expressions, Object... params) {
    expr().filterMany(_name, expressions, params);
    return _root;
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
   *       .contacts.filterMany("status = ? and firstName like ?", Contact.Status.NEW, "Rob%")
   *       .findList();
   *
   * }</pre>
   *
   * @param rawExpressions The raw expressions which can include ? and ?1 style bind parameter placeholders
   * @param params The parameter values to bind
   */
  public final R filterManyRaw(String rawExpressions, Object... params) {
    expr().filterManyRaw(_name, rawExpressions, params);
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
  public final R isEmpty() {
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
  public final R isNotEmpty() {
    expr().isNotEmpty(_name);
    return _root;
  }

}
