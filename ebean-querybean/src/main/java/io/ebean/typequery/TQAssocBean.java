package io.ebean.typequery;

import io.ebean.Expr;
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
    ((QueryBean) _root).query().fetch(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join".
   */
  public final R fetchQuery() {
    ((QueryBean) _root).query().fetchQuery(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 bean cache.
   * Cache misses are populated via fetchQuery().
   */
  public final R fetchCache() {
    ((QueryBean) _root).query().fetchCache(_name);
    return _root;
  }

  /**
   * Use lazy loading for fetching this association.
   */
  public final R fetchLazy() {
    ((QueryBean) _root).query().fetchLazy(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association with the properties specified.
   */
  public final R fetch(String properties) {
    ((QueryBean) _root).query().fetch(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join" with the properties specified.
   */
  public final R fetchQuery(String properties) {
    ((QueryBean) _root).query().fetchQuery(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 cache with the properties specified.
   * Cache misses are populated via  fetchQuery().
   */
  public final R fetchCache(String properties) {
    ((QueryBean) _root).query().fetchCache(_name, properties);
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
   * Fetch this association with config for the type of fetch and the specified properties.
   *
   * @param config Fetch configuration to define the type of fetch to use
   * @param properties The properties to fetch
   */
  @SafeVarargs @SuppressWarnings("varargs")
  public final R fetch(FetchConfig config, TQProperty<QB,?>... properties) {
    return fetchWithProperties(config, properties);
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
    return (SpiQueryFetch) ((QueryBean) _root).query();
  }

  private Set<String> properties(TQProperty<?, ?>... props) {
    Set<String> set = new LinkedHashSet<>();
    for (TQProperty<?, ?> prop : props) {
      set.add(prop.propertyName());
    }
    return set;
  }

  protected final R _filterMany(ExpressionList<T> filter) {
    @SuppressWarnings("unchecked")
    ExpressionList<T> expressionList = (ExpressionList<T>) expr().filterMany(_name);
    expressionList.addAll(filter);
    return _root;
  }

  /** Deprecated(forRemoval = true) */
  protected final R _filterMany(String expressions, Object... params) {
    expr().filterMany(_name, expressions, params);
    return _root;
  }

  protected final R _filterManyRaw(String rawExpressions, Object... params) {
    expr().filterManyRaw(_name, rawExpressions, params);
    return _root;
  }

  protected final R _isEmpty() {
    expr().isEmpty(_name);
    return _root;
  }

  protected final R _isNotEmpty() {
    expr().isNotEmpty(_name);
    return _root;
  }

  protected final <S> ExpressionList<S> _newExpressionList() {
    return Expr.factory().expressionList();
  }

}
