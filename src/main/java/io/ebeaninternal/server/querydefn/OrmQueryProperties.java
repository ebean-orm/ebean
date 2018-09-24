package io.ebeaninternal.server.querydefn;

import io.ebean.ExpressionFactory;
import io.ebean.FetchConfig;
import io.ebean.OrderBy;
import io.ebean.Query;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionFactory;
import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.expression.FilterExprPath;
import io.ebeaninternal.server.expression.FilterExpressionList;
import io.ebean.util.SplitName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the Properties of an Object Relational query.
 */
public class OrmQueryProperties implements Serializable {

  private static final long serialVersionUID = -8785582703966455658L;

  static final FetchConfig DEFAULT_FETCH = new FetchConfig();

  private final String parentPath;
  private final String path;

  private final String rawProperties;
  private final String trimmedProperties;

  private final LinkedHashSet<String> included;

  private final FetchConfig fetchConfig;

  /**
   * Flag set when this fetch path needs to be a query join.
   */
  private boolean markForQueryJoin;

  private boolean cache;

  private boolean readOnly;

  /**
   * Included bean joins.
   */
  private Set<String> includedBeanJoin;

  /**
   * Add these properties to the select so that the foreign key columns are included in the query.
   */
  private Set<String> secondaryQueryJoins;

  private List<OrmQueryProperties> secondaryChildren;

  /**
   * OrderBy properties that where on the main query but moved here as they relate to this (query join).
   */
  @SuppressWarnings("rawtypes")
  private OrderBy orderBy;

  /**
   * A filter that can be applied to the fetch of this path in the object graph.
   */
  @SuppressWarnings("rawtypes")
  private SpiExpressionList filterMany;

  /**
   * Construct for root so path (and parentPath) are null.
   */
  public OrmQueryProperties() {
    this((String) null);
  }

  /**
   * Construct with a given path.
   */
  public OrmQueryProperties(String path) {
    this.path = path;
    this.parentPath = SplitName.parent(path);
    this.rawProperties = null;
    this.trimmedProperties = null;
    this.included = null;
    this.fetchConfig = DEFAULT_FETCH;
  }

  public OrmQueryProperties(String path, String rawProperties) {
    this(path, rawProperties, null);
  }

  public OrmQueryProperties(String path, String rawProperties, FetchConfig fetchConfig) {

    OrmQueryPropertiesParser.Response response = OrmQueryPropertiesParser.parse(rawProperties);

    this.path = path;
    this.parentPath = SplitName.parent(path);
    this.rawProperties = rawProperties;
    this.trimmedProperties = response.properties;
    this.included = response.included;
    this.cache = response.cache;
    this.readOnly = response.readOnly;
    if (fetchConfig != null) {
      this.fetchConfig = fetchConfig;
    } else {
      this.fetchConfig = response.fetchConfig;
    }
  }

  public OrmQueryProperties(String path, LinkedHashSet<String> parsedProperties) {
    if (parsedProperties == null) {
      throw new IllegalArgumentException("parsedProperties is null");
    }

    this.path = path;
    this.parentPath = SplitName.parent(path);
    // for rawSql parsedProperties can be empty (when only fetching Id property)
    this.included = parsedProperties;
    this.rawProperties = join(parsedProperties);
    this.trimmedProperties = rawProperties;
    this.cache = false;
    this.readOnly = false;
    this.fetchConfig = DEFAULT_FETCH;
  }

  /**
   * Join the set of properties into a comma delimited string.
   */
  private String join(LinkedHashSet<String> parsedProperties) {
    StringBuilder sb = new StringBuilder(50);
    boolean first = true;
    for (String property : parsedProperties) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      sb.append(property);
    }
    return sb.toString();
  }

  /**
   * Copy constructor.
   */
  private OrmQueryProperties(OrmQueryProperties source, FetchConfig sourceFetchConfig) {
    this.fetchConfig = sourceFetchConfig;
    this.parentPath = source.parentPath;
    this.path = source.path;
    this.rawProperties = source.rawProperties;
    this.trimmedProperties = source.trimmedProperties;
    this.cache = source.cache;
    this.readOnly = source.readOnly;
    this.filterMany = source.filterMany;
    this.included = (source.included == null) ? null : new LinkedHashSet<>(source.included);
  }

  /**
   * Creates a copy of the OrmQueryProperties.
   */
  public OrmQueryProperties copy() {
    return new OrmQueryProperties(this, this.fetchConfig);
  }

  /**
   * Create a copy with the given fetch config.
   */
  public OrmQueryProperties copy(FetchConfig fetchConfig) {
    return new OrmQueryProperties(this, fetchConfig);
  }

  /**
   * Move a OrderBy.Property from the main query to this query join.
   */
  @SuppressWarnings("rawtypes")
  void addSecJoinOrderProperty(OrderBy.Property orderProp) {
    if (orderBy == null) {
      orderBy = new OrderBy();
    }
    orderBy.add(orderProp);
  }

  public FetchConfig getFetchConfig() {
    return fetchConfig;
  }

  /**
   * Return the expressions used to filter on this path. This should be a many path to use this
   * method.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> SpiExpressionList<T> filterMany(Query<T> rootQuery) {
    if (filterMany == null) {
      FilterExprPath exprPath = new FilterExprPath(path);
      SpiExpressionFactory queryEf = (SpiExpressionFactory) rootQuery.getExpressionFactory();
      ExpressionFactory filterEf = queryEf.createExpressionFactory();// exprPath);
      filterMany = new FilterExpressionList(exprPath, filterEf, rootQuery);
      // by default we need to make this a 'query join' now
      markForQueryJoin = true;
    }
    return filterMany;
  }

  /**
   * Return the filterMany expression list (can be null).
   */
  private SpiExpressionList<?> getFilterManyTrimPath(int trimPath) {
    if (filterMany == null) {
      return null;
    }
    return filterMany.trimPath(trimPath);
  }

  /**
   * Return the filterMany expression list (can be null).
   */
  public SpiExpressionList<?> getFilterMany() {
    return filterMany;
  }

  /**
   * Set the filterMany expression list.
   */
  public void setFilterMany(SpiExpressionList<?> filterMany) {
    this.filterMany = filterMany;
    this.markForQueryJoin = true;
  }

  /**
   * Define the select and joins for this query.
   */
  @SuppressWarnings("unchecked")
  public void configureBeanQuery(SpiQuery<?> query) {

    if (trimmedProperties != null && !trimmedProperties.isEmpty()) {
      query.select(trimmedProperties);
    }

    if (filterMany != null) {
      SpiExpressionList<?> trimPath = filterMany.trimPath(path.length() + 1);
      List<SpiExpression> underlyingList = trimPath.getUnderlyingList();
      for (SpiExpression spiExpression : underlyingList) {
        query.where().add(spiExpression);
      }
    }

    if (secondaryChildren != null) {
      int trimPath = path.length() + 1;
      for (OrmQueryProperties p : secondaryChildren) {
        String path = p.getPath();
        path = path.substring(trimPath);
        query.fetch(path, p.getProperties(), p.getFetchConfig());
        query.setFilterMany(path, p.getFilterManyTrimPath(trimPath));
      }
    }

    if (orderBy != null) {
      query.setOrder(orderBy.copyWithTrim(path));
    }
  }

  public boolean hasSelectClause() {
    if ("*".equals(trimmedProperties)) {
      // explicitly selected all properties
      return true;
    }
    // explicitly selected some properties
    return included != null || filterMany != null;
  }

  /**
   * Return true if the properties and configuration are empty.
   */
  public boolean isEmpty() {
    return rawProperties == null || rawProperties.isEmpty();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(40);
    append("", sb);
    return sb.toString();
  }

  public String append(String prefix, StringBuilder sb) {
    sb.append(prefix);
    if (path != null) {
      sb.append(path).append(" ");
    }
    if (!isEmpty()) {
      sb.append("(").append(rawProperties).append(") ");
    }
    return sb.toString();
  }

  boolean isChild(OrmQueryProperties possibleChild) {
    return possibleChild.getPath().startsWith(path + ".");
  }

  /**
   * For secondary queries add a child element.
   */
  public void add(OrmQueryProperties child) {
    if (secondaryChildren == null) {
      secondaryChildren = new ArrayList<>();
    }
    secondaryChildren.add(child);
  }

  /**
   * Return the raw properties.
   */
  public String getProperties() {
    return rawProperties;
  }

  /**
   * Return true if this includes all properties on the path.
   */
  public boolean allProperties() {
    return included == null;
  }

  /**
   * Return true if this property is included as a bean join.
   * <p>
   * If a property is included as a bean join then it should not be included as a reference/proxy to
   * avoid duplication.
   * </p>
   */
  public boolean isIncludedBeanJoin(String propertyName) {
    return includedBeanJoin != null && includedBeanJoin.contains(propertyName);
  }

  /**
   * Add a bean join property.
   */
  void includeBeanJoin(String propertyName) {
    if (includedBeanJoin == null) {
      includedBeanJoin = new HashSet<>();
    }
    includedBeanJoin.add(propertyName);
  }

  /**
   * This excludes the bean joined properties.
   * <p>
   * This is because bean joins will have there own node in the SqlTree.
   * </p>
   */
  public Set<String> getSelectProperties() {

    if (secondaryQueryJoins == null) {
      return included;
    }

    LinkedHashSet<String> temp = new LinkedHashSet<>(2 * (secondaryQueryJoins.size() + included.size()));
    temp.addAll(included);
    temp.addAll(secondaryQueryJoins);
    return temp;
  }

  void addSecondaryQueryJoin(String property) {
    if (secondaryQueryJoins == null) {
      secondaryQueryJoins = new HashSet<>(4);
    }
    secondaryQueryJoins.add(property);
  }

  /**
   * Return the property set.
   */
  public Set<String> getIncluded() {
    return included;
  }

  boolean isIncluded(String propName) {

    if (includedBeanJoin != null && includedBeanJoin.contains(propName)) {
      return false;
    }
    // all properties included
    return included == null || included.contains(propName);
  }

  /**
   * Mark this path as needing to be a query join.
   */
  void markForQueryJoin() {
    markForQueryJoin = true;
  }

  /**
   * Return true if this path is a 'query join'.
   */
  public boolean isQueryFetch() {
    return markForQueryJoin || getQueryFetchBatch() > -1;
  }

  /**
   * Return true if this path is a 'fetch join'.
   */
  boolean isFetchJoin() {
    return !isQueryFetch() && !isLazyFetch();
  }

  /**
   * Return true if this path is a lazy fetch.
   */
  boolean isLazyFetch() {
    return getLazyFetchBatch() > -1;
  }

  /**
   * Return the batch size to use for the query join.
   */
  public int getQueryFetchBatch() {
    return fetchConfig.getQueryBatchSize();
  }

  /**
   * Return true if a query join should eagerly fetch 'all' rather than the 'first'.
   */
  public boolean isQueryFetchAll() {
    return fetchConfig.isQueryAll();
  }

  /**
   * Return the batch size to use for lazy loading.
   */
  public int getLazyFetchBatch() {
    return fetchConfig.getLazyBatchSize();
  }

  /**
   * Return true if this path has the +readonly option.
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Return true if this path has the +cache option to hit the cache.
   */
  public boolean isCache() {
    return cache;
  }

  /**
   * Return the parent path.
   */
  String getParentPath() {
    return parentPath;
  }

  /**
   * Return the path relative to the root of the graph.
   */
  public String getPath() {
    return path;
  }

  /**
   * Return true if the properties are the same for autoTune purposes.
   */
  boolean isSameByAutoTune(OrmQueryProperties p2) {
    if (included == null) {
      return p2 == null || p2.included == null;
    } else if (p2 == null) {
      return false;
    }
    return included.equals(p2.included);
  }

  /**
   * Calculate the query plan hash.
   */
  public void queryPlanHash(StringBuilder builder) {

    builder.append("qpp[");
    builder.append(path);
    if (included != null){
      builder.append(" included:").append(included);
    }
    if (secondaryQueryJoins != null) {
      builder.append(" secondary:").append(secondaryQueryJoins);
    }

    if (filterMany != null) {
      builder.append(" filterMany[");
      filterMany.queryPlanHash(builder);
      builder.append("]");
    }

    if (fetchConfig != null) {
      builder.append(" config:").append(fetchConfig.hashCode());
    }
    builder.append("]");
  }

}
