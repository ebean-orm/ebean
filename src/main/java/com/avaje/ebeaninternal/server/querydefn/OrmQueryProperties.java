package com.avaje.ebeaninternal.server.querydefn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionFactory;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.expression.FilterExprPath;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.util.FilterExpressionList;

/**
 * Represents the Properties of an Object Relational query.
 */
public class OrmQueryProperties implements Serializable {

  private static final long serialVersionUID = -8785582703966455658L;

  private String parentPath;
  private String path;

  private String properties;

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
  private Set<String> included;

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

  public OrmQueryProperties() {
    this(null);
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
  public void setProperties(String properties) {
    this.properties = properties;
    this.trimmedProperties = properties;
    parseProperties();

    if (!isAllProperties()) {
      Set<String> parsed = parseIncluded(trimmedProperties);
      if (parsed.contains("*")) {
        this.included = null;
      } else {
        this.included = parsed;
      }
    } else {
      this.included = null;
    }
  }

  private boolean isAllProperties() {
    return (trimmedProperties == null) || (trimmedProperties.length() == 0) || "*".equals(trimmedProperties);
  }

  /**
   * Return the expressions used to filter on this path. This should be a many path to use this
   * method.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
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
  public void setDefaultProperties(String properties, Set<String> included) {
    this.properties = properties;
    this.trimmedProperties = properties;
    this.included = included;
  }

  /**
   * Set the properties from a matching autofetch tuned properties.
   */
  public void setTunedProperties(OrmQueryProperties tunedProperties) {
    this.properties = tunedProperties.properties;
    this.trimmedProperties = tunedProperties.trimmedProperties;
    this.included = tunedProperties.included;
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
    copy.properties = properties;
    copy.cache = cache;
    copy.readOnly = readOnly;
    copy.queryFetchAll = queryFetchAll;
    copy.queryFetchBatch = queryFetchBatch;
    copy.lazyFetchBatch = lazyFetchBatch;
    copy.filterMany = filterMany;
    if (included != null) {
      copy.included = new HashSet<String>(included);
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

  public String toString() {
    String s = "";
    if (path != null) {
      s += path + " ";
    }
    if (properties != null) {
      s += "(" + properties + ") ";
    } else if (included != null) {
      s += "(" + included + ") ";
    }
    return s;
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

  public int autofetchPlanHash() {

    int hc = (path != null ? path.hashCode() : 1);
    if (properties != null) {
      hc = hc * 31 + properties.hashCode();
    } else {
      hc = hc * 31 + (included != null ? included.hashCode() : 1);
    }

    return hc;
  }

  /**
   * Calculate the query plan hash.
   */
  @SuppressWarnings("unchecked")
  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {

    builder.add(path);
    if (properties != null) {
      builder.add(properties);
    } else {
      builder.add(included);
    }
    builder.add(filterMany != null);
    if (filterMany != null) {
      filterMany.queryPlanHash(request, builder);
    }
  }

  public String getProperties() {
    return properties;
  }

  /**
   * Return true if this has properties.
   */
  public boolean hasProperties() {
    return properties != null || included != null;
  }

  /**
   * Return true if this property is included as a bean join.
   * <p>
   * If a property is included as a bean join then it should not be included as a reference/proxy to
   * avoid duplication.
   * </p>
   */
  public boolean isIncludedBeanJoin(String propertyName) {
    if (includedBeanJoin == null) {
      return false;
    } else {
      return includedBeanJoin.contains(propertyName);
    }
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

  public boolean allProperties() {
    return included == null;
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

    LinkedHashSet<String> temp = new LinkedHashSet<String>(secondaryQueryJoins.size() + included.size());
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
   * Return all the properties including the bean joins. This is the set that will be used by
   * EntityBeanIntercept to determine if a property needs to be lazy loaded.
   */
  public Set<String> getAllIncludedProperties() {

    if (included == null) {
      return null;
    }

    if (includedBeanJoin == null && secondaryQueryJoins == null) {
      return new LinkedHashSet<String>(included);
    }

    LinkedHashSet<String> s = new LinkedHashSet<String>(2 * (included.size() + 5));
    if (included != null) {
      s.addAll(included);
    }
    if (includedBeanJoin != null) {
      s.addAll(includedBeanJoin);
    }
    if (secondaryQueryJoins != null) {
      s.addAll(secondaryQueryJoins);
    }
    return s;
  }

  public boolean isIncluded(String propName) {

    if (includedBeanJoin != null && includedBeanJoin.contains(propName)) {
      return false;
    }
    if (included == null) {
      // all properties included
      return true;
    }
    return included.contains(propName);
  }

  /**
   * Used to convert this join to a query join.
   * 
   * @param queryJoinBatch
   *          where -1 means not a query join and 0 means use the default batch size.
   */
  public OrmQueryProperties setQueryFetchBatch(int queryFetchBatch) {
    this.queryFetchBatch = queryFetchBatch;
    return this;
  }

  public OrmQueryProperties setQueryFetchAll(boolean queryFetchAll) {
    this.queryFetchAll = queryFetchAll;
    return this;
  }

  public OrmQueryProperties setQueryFetch(int batch, boolean queryFetchAll) {
    this.queryFetchBatch = batch;
    this.queryFetchAll = queryFetchAll;
    return this;
  }

  /**
   * Set the lazy loading batch size.
   */
  public OrmQueryProperties setLazyFetchBatch(int lazyFetchBatch) {
    this.lazyFetchBatch = lazyFetchBatch;
    return this;
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

  private void parseProperties() {
    if (trimmedProperties == null) {
      return;
    }
    int pos = trimmedProperties.indexOf("+readonly");
    if (pos > -1) {
      trimmedProperties = StringHelper.replaceString(trimmedProperties, "+readonly", "");
      this.readOnly = true;
    }
    pos = trimmedProperties.indexOf("+cache");
    if (pos > -1) {
      trimmedProperties = StringHelper.replaceString(trimmedProperties, "+cache", "");
      this.cache = true;
    }
    pos = trimmedProperties.indexOf("+query");
    if (pos > -1) {
      queryFetchBatch = parseBatchHint(pos, "+query");
    }
    pos = trimmedProperties.indexOf("+lazy");
    if (pos > -1) {
      lazyFetchBatch = parseBatchHint(pos, "+lazy");
    }

    trimmedProperties = trimmedProperties.trim();
    while (trimmedProperties.startsWith(",")) {
      trimmedProperties = trimmedProperties.substring(1).trim();
    }
  }

  private int parseBatchHint(int pos, String option) {

    int startPos = pos + option.length();

    int endPos = findEndPos(startPos, trimmedProperties);
    if (endPos == -1) {
      trimmedProperties = StringHelper.replaceString(trimmedProperties, option, "");
      return 0;

    } else {

      String batchParam = trimmedProperties.substring(startPos + 1, endPos);

      if (endPos + 1 >= trimmedProperties.length()) {
        trimmedProperties = trimmedProperties.substring(0, pos);
      } else {
        trimmedProperties = trimmedProperties.substring(0, pos) + trimmedProperties.substring(endPos + 1);
      }
      return Integer.parseInt(batchParam);
    }
  }

  private int findEndPos(int pos, String props) {

    if (pos < props.length()) {
      if (props.charAt(pos) == '(') {
        int endPara = props.indexOf(')', pos + 1);
        if (endPara == -1) {
          String m = "Error could not find ')' in " + props + " after position " + pos;
          throw new RuntimeException(m);
        }
        return endPara;
      }
    }
    return -1;
  }

  /**
   * Parse the include separating by comma or semicolon.
   */
  private static Set<String> parseIncluded(String rawList) {

    String[] res = rawList.split(",");

    LinkedHashSet<String> set = new LinkedHashSet<String>(res.length + 3);

    String temp = null;
    for (int i = 0; i < res.length; i++) {
      temp = res[i].trim();
      if (temp.length() > 0) {
        set.add(temp);
      }
    }

    if (set.isEmpty()) {
      return null;
    }

    return Collections.unmodifiableSet(set);
  }
}
