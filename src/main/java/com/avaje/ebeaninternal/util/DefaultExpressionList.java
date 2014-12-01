package com.avaje.ebeaninternal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.*;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Default implementation of ExpressionList.
 */
public class DefaultExpressionList<T> implements SpiExpressionList<T> {

  private static final long serialVersionUID = -6992345500247035947L;

  protected final List<SpiExpression> list;

  protected final Query<T> query;

  protected final ExpressionList<T> parentExprList;

  protected transient ExpressionFactory expr;

  private final String listAndStart;
  private final String listAndEnd;
  private final String listAndJoin;

  public DefaultExpressionList(Query<T> query, ExpressionList<T> parentExprList) {
    this(query, query.getExpressionFactory(), parentExprList);
  }

  public DefaultExpressionList(Query<T> query, ExpressionFactory expr, ExpressionList<T> parentExprList) {
    this(query, expr, parentExprList, new ArrayList<SpiExpression>());
  }
  
  protected DefaultExpressionList(Query<T> query, ExpressionFactory expr, ExpressionList<T> parentExprList, List<SpiExpression> list) {
    this.list = list;
    this.query = query;
    this.expr = expr;
    this.parentExprList = parentExprList;

    this.listAndStart = "";
    this.listAndEnd = "";
    this.listAndJoin = " and ";
  }

  @Override
  public SpiExpressionList<?> trimPath(int prefixTrim) {
    throw new RuntimeException("Only allowed on FilterExpressionList");
  }

  public List<SpiExpression> internalList() {
    return list;
  }

  /**
   * Set the ExpressionFactory.
   * <p>
   * After deserialisation so that it can be further modified.
   * </p>
   */
  @Override
  public void setExpressionFactory(ExpressionFactory expr) {
    this.expr = expr;
  }

  /**
   * Return a copy of the expression list.
   * <p>
   * Each of the expressions are expected to be immutable and safe to reference.
   * </p>
   */
  public DefaultExpressionList<T> copy(Query<T> query) {
    DefaultExpressionList<T> copy = new DefaultExpressionList<T>(query, expr, null);
    copy.list.addAll(list);
    return copy;
  }

  /**
   * Return true if one of the expressions is related to a Many property.
   */
  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {

    for (int i = 0; i < list.size(); i++) {
      list.get(i).containsMany(desc, whereManyJoins);
    }
  }

  @Override
  public ExpressionList<T> endJunction() {
    return parentExprList == null ? this : parentExprList;
  }

  @Override
  public Query<T> query() {
    return query;
  }

  @Override
  public ExpressionList<T> where() {
    return query.where();
  }

  @Override
  public OrderBy<T> order() {
    return query.order();
  }

  @Override
  public OrderBy<T> orderBy() {
    return query.order();
  }

  @Override
  public Query<T> order(String orderByClause) {
    return query.order(orderByClause);
  }

  @Override
  public Query<T> orderBy(String orderBy) {
    return query.order(orderBy);
  }

  @Override
  public Query<T> setOrderBy(String orderBy) {
    return query.order(orderBy);
  }

  @Override
  public Query<T> apply(PathProperties pathProperties) {
    return query.apply(pathProperties);
  }

  @Override
  public FutureIds<T> findFutureIds() {
    return query.findFutureIds();
  }

  @Override
  public FutureRowCount<T> findFutureRowCount() {
    return query.findFutureRowCount();
  }

  @Override
  public FutureList<T> findFutureList() {
    return query.findFutureList();
  }

  @Override
  public PagedList<T> findPagedList(int pageIndex, int pageSize) {
    return query.findPagedList(pageIndex, pageSize);
  }

  @Override
  public int findRowCount() {
    return query.findRowCount();
  }

  @Override
  public List<Object> findIds() {
    return query.findIds();
  }

  @Override
  public void findEach(QueryEachConsumer<T> consumer) {
    query.findEach(consumer);
  }

  @Override
  public void findEachWhile(QueryEachWhileConsumer<T> consumer) {
    query.findEachWhile(consumer);
  }

  @Override
  public void findVisit(QueryResultVisitor<T> visitor) {
    query.findVisit(visitor);
  }

  @Override
  public QueryIterator<T> findIterate() {
    return query.findIterate();
  }

  @Override
  public List<T> findList() {
    return query.findList();
  }

  @Override
  public Set<T> findSet() {
    return query.findSet();
  }

  @Override
  public Map<?, T> findMap() {
    return query.findMap();
  }

  @Override
  public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType) {
    return query.findMap(keyProperty, keyType);
  }

  @Override
  public T findUnique() {
    return query.findUnique();
  }

  @Override
  public ExpressionList<T> filterMany(String prop) {
    return query.filterMany(prop);
  }

  @Override
  public Query<T> select(String fetchProperties) {
    return query.select(fetchProperties);
  }

  @Override
  public Query<T> setFirstRow(int firstRow) {
    return query.setFirstRow(firstRow);
  }

  @Override
  public Query<T> setMaxRows(int maxRows) {
    return query.setMaxRows(maxRows);
  }

  @Override
  public Query<T> setMapKey(String mapKey) {
    return query.setMapKey(mapKey);
  }

  @Override
  public Query<T> setUseCache(boolean useCache) {
    return query.setUseCache(useCache);
  }

  @Override
  public ExpressionList<T> having() {
    return query.having();
  }

  @Override
  public ExpressionList<T> add(Expression expr) {
    list.add((SpiExpression) expr);
    return this;
  }

  @Override
  public ExpressionList<T> addAll(ExpressionList<T> exprList) {
    SpiExpressionList<T> spiList = (SpiExpressionList<T>)exprList;
    list.addAll(spiList.getUnderlyingList());
    return this;
  }

  @Override
  public List<SpiExpression> getUnderlyingList() {
    return list;
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public String buildSql(SpiExpressionRequest request) {

    request.append(listAndStart);
    for (int i = 0, size = list.size(); i < size; i++) {
      SpiExpression expression = list.get(i);
      if (i > 0) {
        request.append(listAndJoin);
      }
      expression.addSql(request);
    }
    request.append(listAndEnd);
    return request.getSql();
  }

  @Override
  public ArrayList<Object> buildBindValues(SpiExpressionRequest request) {

    for (int i = 0, size = list.size(); i < size; i++) {
      SpiExpression expression = list.get(i);
      expression.addBindValues(request);
    }
    return request.getBindValues();
  }

  /**
   * Calculate a hash based on the expressions but excluding the actual bind
   * values.
   */
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(DefaultExpressionList.class);
    for (int i = 0, size = list.size(); i < size; i++) {
      SpiExpression expression = list.get(i);
      expression.queryAutoFetchHash(builder);
    }
  }

  /**
   * Calculate a hash based on the expressions but excluding the actual bind
   * values.
   */
  @Override
  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    builder.add(DefaultExpressionList.class);
    for (int i = 0, size = list.size(); i < size; i++) {
      SpiExpression expression = list.get(i);
      expression.queryPlanHash(request, builder);
    }
  }

  /**
   * Calculate a hash based on the expressions.
   */
  public int queryBindHash() {
    int hash = DefaultExpressionList.class.getName().hashCode();
    for (int i = 0, size = list.size(); i < size; i++) {
      SpiExpression expression = list.get(i);
      hash = hash * 31 + expression.queryBindHash();
    }
    return hash;
  }

  @Override
  public ExpressionList<T> eq(String propertyName, Object value) {
    add(expr.eq(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> ieq(String propertyName, String value) {
    add(expr.ieq(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> ne(String propertyName, Object value) {
    add(expr.ne(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> allEq(Map<String, Object> propertyMap) {
    add(expr.allEq(propertyMap));
    return this;
  }

  @Override
  public ExpressionList<T> and(Expression expOne, Expression expTwo) {
    add(expr.and(expOne, expTwo));
    return this;
  }

  @Override
  public ExpressionList<T> between(String propertyName, Object value1, Object value2) {
    add(expr.between(propertyName, value1, value2));
    return this;
  }

  @Override
  public ExpressionList<T> betweenProperties(String lowProperty, String highProperty, Object value) {
    add(expr.betweenProperties(lowProperty, highProperty, value));
    return this;
  }

  @Override
  public Junction<T> conjunction() {
    Junction<T> conjunction = expr.conjunction(query, this);
    add(conjunction);
    return conjunction;
  }

  @Override
  public ExpressionList<T> contains(String propertyName, String value) {
    add(expr.contains(propertyName, value));
    return this;
  }

  @Override
  public Junction<T> disjunction() {
    Junction<T> disjunction = expr.disjunction(query, this);
    add(disjunction);
    return disjunction;
  }

  @Override
  public ExpressionList<T> endsWith(String propertyName, String value) {
    add(expr.endsWith(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> ge(String propertyName, Object value) {
    add(expr.ge(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> gt(String propertyName, Object value) {
    add(expr.gt(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> icontains(String propertyName, String value) {
    add(expr.icontains(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> idIn(List<?> idList) {
    add(expr.idIn(idList));
    return this;
  }

  @Override
  public ExpressionList<T> idEq(Object value) {
    if (query != null && parentExprList == null) {
      query.setId(value);
    } else {
      add(expr.idEq(value));
    }
    return this;
  }

  @Override
  public ExpressionList<T> iendsWith(String propertyName, String value) {
    add(expr.iendsWith(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> ilike(String propertyName, String value) {
    add(expr.ilike(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> in(String propertyName, Query<?> subQuery) {
    add(expr.in(propertyName, subQuery));
    return this;
  }

  @Override
  public ExpressionList<T> in(String propertyName, Collection<?> values) {
    add(expr.in(propertyName, values));
    return this;
  }

  @Override
  public ExpressionList<T> in(String propertyName, Object... values) {
    add(expr.in(propertyName, values));
    return this;
  }

  @Override
  public ExpressionList<T> isNotNull(String propertyName) {
    add(expr.isNotNull(propertyName));
    return this;
  }

  @Override
  public ExpressionList<T> isNull(String propertyName) {
    add(expr.isNull(propertyName));
    return this;
  }

  @Override
  public ExpressionList<T> istartsWith(String propertyName, String value) {
    add(expr.istartsWith(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> le(String propertyName, Object value) {
    add(expr.le(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> exampleLike(Object example) {
    add(expr.exampleLike(example));
    return this;
  }

  @Override
  public ExpressionList<T> iexampleLike(Object example) {
    add(expr.iexampleLike(example));
    return this;
  }

  @Override
  public ExpressionList<T> like(String propertyName, String value) {
    add(expr.like(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> lt(String propertyName, Object value) {
    add(expr.lt(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> not(Expression exp) {
    add(expr.not(exp));
    return this;
  }

  @Override
  public ExpressionList<T> or(Expression expOne, Expression expTwo) {
    add(expr.or(expOne, expTwo));
    return this;
  }

  @Override
  public ExpressionList<T> raw(String raw, Object value) {
    add(expr.raw(raw, value));
    return this;
  }

  @Override
  public ExpressionList<T> raw(String raw, Object[] values) {
    add(expr.raw(raw, values));
    return this;
  }

  @Override
  public ExpressionList<T> raw(String raw) {
    add(expr.raw(raw));
    return this;
  }

  @Override
  public ExpressionList<T> startsWith(String propertyName, String value) {
    add(expr.startsWith(propertyName, value));
    return this;
  }

}
