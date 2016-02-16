package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionFactory;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.expression.FilterExprPath;
import com.avaje.ebeaninternal.server.expression.FilterExpressionList;
import com.avaje.ebeaninternal.server.expression.Same;
import com.avaje.ebeaninternal.server.query.SplitName;

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

  private String parentPath;
  private String path;

  private String rawProperties;
  private String trimmedProperties;

  /**
   * NB: -1 means no +query, 0 means use the default batch size.
   */
  private int queryFetchBatch = -1;
  private boolean queryFetchAll;

  /**
   * NB: -1 means no +lazy, 0 means use the default batch size.
   */
  private int lazyFetchBatch = -1;

  private FetchConfig fetchConfig;

  private boolean cache;

  private boolean readOnly;

  /**
   * Note this SHOULD be a LinkedHashSet to preserve order of the properties. This is to make using
   * SqlSelect easier with predictable property/column ordering.
   */
  private LinkedHashSet<String> included;

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
   * OrderBy properties that where on the main query but moved here as they relate to this (query
   * join).
   */
  @SuppressWarnings("rawtypes")
  private OrderBy orderBy;

  /**
   * A filter that can be applied to the fetch of this path in the object graph.
   */
  @SuppressWarnings("rawtypes")
  private SpiExpressionList filterMany;

  /**
   * Construct with a given path (null == root path).
   */
  public OrmQueryProperties(String path) {
    this.path = path;
    this.parentPath = SplitName.parent(path);
  }

  /**
   * Construct for root so path (and parentPath) are null.
   */
  public OrmQueryProperties() {
  }

  /**
   * Used by query language parser.
   */
  public OrmQueryProperties(String path, String properties) {
    this(path);
    setProperties(properties);
  }

  /**
   * Move a OrderBy.Property from the main query to this query join.
   */
  @SuppressWarnings("rawtypes")
  public void addSecJoinOrderProperty(OrderBy.Property orderProp) {
    if (orderBy == null) {
      orderBy = new OrderBy();
    }
    orderBy.add(orderProp);
  }

  /**
   * Set the Fetch configuration options for this path.
   */
  public void setFetchConfig(FetchConfig fetchConfig) {
    if (fetchConfig != null) {
      this.fetchConfig = fetchConfig;
      lazyFetchBatch = fetchConfig.getLazyBatchSize();
      queryFetchBatch = fetchConfig.getQueryBatchSize();
      queryFetchAll = fetchConfig.isQueryAll();
    }
  }

  public FetchConfig getFetchConfig() {
    return fetchConfig;
  }

  /**
   * Set the comma delimited properties to fetch for this path.
   * <p>
   * This can include the +query and +lazy type hints.
   * </p>
   */
  public void setProperties(String rawProperties) {

    OrmQueryPropertiesParser.Response response = OrmQueryPropertiesParser.parse(rawProperties);

    this.rawProperties = rawProperties;
    this.trimmedProperties = response.properties;
    this.included = response.included;
    this.lazyFetchBatch = response.lazyFetchBatch;
    this.queryFetchBatch = response.queryFetchBatch;
    this.cache = response.cache;
    this.readOnly = response.readOnly;
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
      queryFetchAll = true;
      queryFetchBatch = 100;
      lazyFetchBatch = 100;
    }
    return filterMany;
  }

  /**
   * Return the filterMany expression list (can be null).
   */
  public SpiExpressionList<?> getFilterManyTrimPath(int trimPath) {
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
  }

  /**
   * Set the properties from deployment default FetchTypes.
   */
  public void setDefaultProperties(String properties, LinkedHashSet<String> included) {
    this.rawProperties = properties;
    this.trimmedProperties = properties;
    this.included = included;
  }

  /**
   * Set the properties from a matching AutoTune tuned properties.
   */
  public void setTunedProperties(OrmQueryProperties tunedProperties) {
    if (tunedProperties.hasProperties()) {
      this.rawProperties = tunedProperties.rawProperties;
      this.trimmedProperties = tunedProperties.trimmedProperties;
      this.included = tunedProperties.included;
      this.queryFetchBatch = Math.max(queryFetchBatch, tunedProperties.queryFetchBatch);
      this.lazyFetchBatch = Math.max(lazyFetchBatch, tunedProperties.lazyFetchBatch);
    }
  }

  /**
   * Define the select and joins for this query.
   */
  @SuppressWarnings("unchecked")
  public void configureBeanQuery(SpiQuery<?> query) {

    if (trimmedProperties != null && trimmedProperties.length() > 0) {
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
      for (int i = 0; i < secondaryChildren.size(); i++) {
        OrmQueryProperties p = secondaryChildren.get(i);
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

  /**
   * Creates a copy of the OrmQueryProperties.
   */
  public OrmQueryProperties copy() {
    OrmQueryProperties copy = new OrmQueryProperties();
    copy.parentPath = parentPath;
    copy.path = path;
    copy.rawProperties = rawProperties;
    copy.trimmedProperties = trimmedProperties;
    copy.cache = cache;
    copy.readOnly = readOnly;
    copy.queryFetchAll = queryFetchAll;
    copy.queryFetchBatch = queryFetchBatch;
    copy.lazyFetchBatch = lazyFetchBatch;
    copy.filterMany = filterMany;
    if (included != null) {
      copy.included = new LinkedHashSet<String>(included);
    }
    if (includedBeanJoin != null) {
      copy.includedBeanJoin = new HashSet<String>(includedBeanJoin);
    }
    return copy;
  }

  public boolean hasSelectClause() {
    if ("*".equals(trimmedProperties)) {
      // explicitly selected all properties
      return true;
    }
    // explicitly selected some properties
    return included != null;
  }

  /**
   * Return true if the properties and configuration are empty.
   */
  public boolean isEmpty() {
    return rawProperties == null || rawProperties.isEmpty();
  }

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

  public boolean isChild(OrmQueryProperties possibleChild) {
    return possibleChild.getPath().startsWith(path + ".");
  }

  /**
   * For secondary queries add a child element.
   */
  public void add(OrmQueryProperties child) {
    if (secondaryChildren == null) {
      secondaryChildren = new ArrayList<OrmQueryProperties>();
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
   * Return true if this has properties. Returning false means this part of the
   * path is a partial object.
   */
  public boolean hasProperties() {
    return included != null;
  }

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
  public void includeBeanJoin(String propertyName) {
    if (includedBeanJoin == null) {
      includedBeanJoin = new HashSet<String>();
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

    LinkedHashSet<String> temp = new LinkedHashSet<String>(2 * (secondaryQueryJoins.size() + included.size()));
    temp.addAll(included);
    temp.addAll(secondaryQueryJoins);
    return temp;
  }

  public void addSecondaryQueryJoin(String property) {
    if (secondaryQueryJoins == null) {
      secondaryQueryJoins = new HashSet<String>(4);
    }
    secondaryQueryJoins.add(property);
  }

  /**
   * Return the property set.
   */
  protected Set<String> getIncluded() {
    return included;
  }

  public boolean isIncluded(String propName) {

    if (includedBeanJoin != null && includedBeanJoin.contains(propName)) {
      return false;
    }
    // all properties included
    return included == null || included.contains(propName);
  }

  public void setQueryFetch(int batch, boolean queryFetchAll) {
    this.queryFetchBatch = batch;
    this.queryFetchAll = queryFetchAll;
  }

  public boolean isFetchJoin() {
    return !isQueryFetch() && !isLazyFetch();
  }

  public boolean isQueryFetch() {
    return queryFetchBatch > -1;
  }

  public int getQueryFetchBatch() {
    return queryFetchBatch;
  }

  public boolean isQueryFetchAll() {
    return queryFetchAll;
  }

  public boolean isLazyFetch() {
    return lazyFetchBatch > -1;
  }

  public int getLazyFetchBatch() {
    return lazyFetchBatch;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean isCache() {
    return cache;
  }

  public String getParentPath() {
    return parentPath;
  }

  public String getPath() {
    return path;
  }

  /**
   * Return true if the properties are the same for autoTune purposes.
   */
  public boolean isSameByAutoTune(OrmQueryProperties p2) {
    if (included == null) {
      return p2.included == null;
    }
    return included.equals(p2.included);
  }

  /**
   * Properties are the same for query plan purposes.
   */
  public boolean isSameByPlan(OrmQueryProperties p2) {

    if (!Same.sameByValue(secondaryQueryJoins, p2.secondaryQueryJoins)) return false;
    if (!Same.sameByValue(included, p2.included)) return false;
    if (!Same.sameByNull(filterMany, p2.filterMany)) return false;
    if (filterMany != null && !filterMany.isSameByPlan(p2.filterMany)) return false;

    return lazyFetchBatch == p2.lazyFetchBatch
        && queryFetchBatch == p2.queryFetchBatch
        && queryFetchAll == p2.queryFetchAll;
  }

  /**
   * Calculate the query plan hash.
   */
  public void queryPlanHash(HashQueryPlanBuilder builder) {

    builder.add(path);
    builder.addOrdered(included);
    builder.add(secondaryQueryJoins);

    builder.add(filterMany != null);
    if (filterMany != null) {
      filterMany.queryPlanHash(builder);
    }
    builder.add(lazyFetchBatch);
    builder.add(queryFetchBatch);
    builder.add(queryFetchAll);
  }

}
