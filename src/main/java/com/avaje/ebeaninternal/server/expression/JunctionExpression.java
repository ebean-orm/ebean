package com.avaje.ebeaninternal.server.expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.FutureIds;
import com.avaje.ebean.FutureList;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.Junction;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.PagingList;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.QueryListener;
import com.avaje.ebean.QueryResultVisitor;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.util.DefaultExpressionList;

/**
 * Junction implementation.
 */
abstract class JunctionExpression<T> implements Junction<T>, SpiExpression, ExpressionList<T> {

  private static final long serialVersionUID = -7422204102750462676L;

  private static final String OR = " or ";

  private static final String AND = " and ";

  static class Conjunction<T> extends JunctionExpression<T> {

    private static final long serialVersionUID = -645619859900030678L;

    Conjunction(com.avaje.ebean.Query<T> query, ExpressionList<T> parent) {
      super(AND, query, parent);
    }
  }

  static class Disjunction<T> extends JunctionExpression<T> {

    private static final long serialVersionUID = -8464470066692221413L;

    Disjunction(com.avaje.ebean.Query<T> query, ExpressionList<T> parent) {
      super(OR, query, parent);
    }
  }

  // private final ArrayList<SpiExpression> list = new
  // ArrayList<SpiExpression>();
  private final DefaultExpressionList<T> exprList;

  private final String joinType;

  JunctionExpression(String joinType, com.avaje.ebean.Query<T> query, ExpressionList<T> parent) {
    this.joinType = joinType;
    this.exprList = new DefaultExpressionList<T>(query, parent);
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    List<SpiExpression> list = exprList.internalList();

    for (int i = 0; i < list.size(); i++) {
      list.get(i).containsMany(desc, manyWhereJoin);
    }
  }

  public Junction<T> add(Expression item) {
    SpiExpression i = (SpiExpression) item;
    exprList.add(i);
    return this;
  }

  public Junction<T> addAll(ExpressionList<T> addList) {
    exprList.addAll(addList);
    return this;
  }
  
  public void addBindValues(SpiExpressionRequest request) {

    List<SpiExpression> list = exprList.internalList();

    for (int i = 0; i < list.size(); i++) {
      SpiExpression item = list.get(i);
      item.addBindValues(request);
    }
  }

  public void addSql(SpiExpressionRequest request) {

    List<SpiExpression> list = exprList.internalList();

    if (!list.isEmpty()) {
      request.append("(");

      for (int i = 0; i < list.size(); i++) {
        SpiExpression item = list.get(i);
        if (i > 0) {
          request.append(joinType);
        }
        item.addSql(request);
      }

      request.append(") ");
    }
  }

  /**
   * Based on Junction type and all the expression contained.
   */
  public int queryAutoFetchHash() {
    int hc = JunctionExpression.class.getName().hashCode();
    hc = hc * 31 + joinType.hashCode();

    List<SpiExpression> list = exprList.internalList();
    for (int i = 0; i < list.size(); i++) {
      hc = hc * 31 + list.get(i).queryAutoFetchHash();
    }

    return hc;
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {
    int hc = JunctionExpression.class.getName().hashCode();
    hc = hc * 31 + joinType.hashCode();

    List<SpiExpression> list = exprList.internalList();
    for (int i = 0; i < list.size(); i++) {
      hc = hc * 31 + list.get(i).queryPlanHash(request);
    }

    return hc;
  }

  public int queryBindHash() {
    int hc = JunctionExpression.class.getName().hashCode();

    List<SpiExpression> list = exprList.internalList();
    for (int i = 0; i < list.size(); i++) {
      hc = hc * 31 + list.get(i).queryBindHash();
    }

    return hc;
  }

  public ExpressionList<T> endJunction() {
    return exprList.endJunction();
  }

  public ExpressionList<T> allEq(Map<String, Object> propertyMap) {
    return exprList.allEq(propertyMap);
  }

  public ExpressionList<T> and(Expression expOne, Expression expTwo) {
    return exprList.and(expOne, expTwo);
  }

  public ExpressionList<T> between(String propertyName, Object value1, Object value2) {
    return exprList.between(propertyName, value1, value2);
  }

  public ExpressionList<T> betweenProperties(String lowProperty, String highProperty, Object value) {
    return exprList.betweenProperties(lowProperty, highProperty, value);
  }

  public Junction<T> conjunction() {
    return exprList.conjunction();
  }

  public ExpressionList<T> contains(String propertyName, String value) {
    return exprList.contains(propertyName, value);
  }

  public Junction<T> disjunction() {
    return exprList.disjunction();
  }

  public ExpressionList<T> endsWith(String propertyName, String value) {
    return exprList.endsWith(propertyName, value);
  }

  public ExpressionList<T> eq(String propertyName, Object value) {
    return exprList.eq(propertyName, value);
  }

  public ExpressionList<T> exampleLike(Object example) {
    return exprList.exampleLike(example);
  }

  public ExpressionList<T> filterMany(String prop) {
    throw new RuntimeException("filterMany not allowed on Junction expression list");
  }

  public FutureIds<T> findFutureIds() {
    return exprList.findFutureIds();
  }

  public FutureList<T> findFutureList() {
    return exprList.findFutureList();
  }

  public FutureRowCount<T> findFutureRowCount() {
    return exprList.findFutureRowCount();
  }

  public List<Object> findIds() {
    return exprList.findIds();
  }

  public void findVisit(QueryResultVisitor<T> visitor) {
    exprList.findVisit(visitor);
  }

  public QueryIterator<T> findIterate() {
    return exprList.findIterate();
  }

  public List<T> findList() {
    return exprList.findList();
  }

  public Map<?, T> findMap() {
    return exprList.findMap();
  }

  public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType) {
    return exprList.findMap(keyProperty, keyType);
  }

  public PagingList<T> findPagingList(int pageSize) {
    return exprList.findPagingList(pageSize);
  }

  public int findRowCount() {
    return exprList.findRowCount();
  }

  public Set<T> findSet() {
    return exprList.findSet();
  }

  public T findUnique() {
    return exprList.findUnique();
  }

  public ExpressionList<T> ge(String propertyName, Object value) {
    return exprList.ge(propertyName, value);
  }

  public ExpressionList<T> gt(String propertyName, Object value) {
    return exprList.gt(propertyName, value);
  }

  public ExpressionList<T> having() {
    throw new RuntimeException("having() not allowed on Junction expression list");
  }

  public ExpressionList<T> icontains(String propertyName, String value) {
    return exprList.icontains(propertyName, value);
  }

  public ExpressionList<T> idEq(Object value) {
    return exprList.idEq(value);
  }

  public ExpressionList<T> idIn(List<?> idValues) {
    return exprList.idIn(idValues);
  }

  public ExpressionList<T> iendsWith(String propertyName, String value) {
    return exprList.iendsWith(propertyName, value);
  }

  public ExpressionList<T> ieq(String propertyName, String value) {
    return exprList.ieq(propertyName, value);
  }

  public ExpressionList<T> iexampleLike(Object example) {
    return exprList.iexampleLike(example);
  }

  public ExpressionList<T> ilike(String propertyName, String value) {
    return exprList.ilike(propertyName, value);
  }

  public ExpressionList<T> in(String propertyName, Collection<?> values) {
    return exprList.in(propertyName, values);
  }

  public ExpressionList<T> in(String propertyName, Object... values) {
    return exprList.in(propertyName, values);
  }

  public ExpressionList<T> in(String propertyName, com.avaje.ebean.Query<?> subQuery) {
    return exprList.in(propertyName, subQuery);
  }

  public ExpressionList<T> isNotNull(String propertyName) {
    return exprList.isNotNull(propertyName);
  }

  public ExpressionList<T> isNull(String propertyName) {
    return exprList.isNull(propertyName);
  }

  public ExpressionList<T> istartsWith(String propertyName, String value) {
    return exprList.istartsWith(propertyName, value);
  }

  public com.avaje.ebean.Query<T> join(String assocProperty, String assocProperties) {
    return exprList.join(assocProperty, assocProperties);
  }

  public com.avaje.ebean.Query<T> join(String assocProperties) {
    return exprList.join(assocProperties);
  }

  public ExpressionList<T> le(String propertyName, Object value) {
    return exprList.le(propertyName, value);
  }

  public ExpressionList<T> like(String propertyName, String value) {
    return exprList.like(propertyName, value);
  }

  public ExpressionList<T> lt(String propertyName, Object value) {
    return exprList.lt(propertyName, value);
  }

  public ExpressionList<T> ne(String propertyName, Object value) {
    return exprList.ne(propertyName, value);
  }

  public ExpressionList<T> not(Expression exp) {
    return exprList.not(exp);
  }

  public ExpressionList<T> or(Expression expOne, Expression expTwo) {
    return exprList.or(expOne, expTwo);
  }

  public OrderBy<T> order() {
    return exprList.order();
  }

  public com.avaje.ebean.Query<T> order(String orderByClause) {
    return exprList.order(orderByClause);
  }

  public OrderBy<T> orderBy() {
    return exprList.orderBy();
  }

  public com.avaje.ebean.Query<T> orderBy(String orderBy) {
    return exprList.orderBy(orderBy);
  }

  public com.avaje.ebean.Query<T> query() {
    return exprList.query();
  }

  public ExpressionList<T> raw(String raw, Object value) {
    return exprList.raw(raw, value);
  }

  public ExpressionList<T> raw(String raw, Object[] values) {
    return exprList.raw(raw, values);
  }

  public ExpressionList<T> raw(String raw) {
    return exprList.raw(raw);
  }

  public com.avaje.ebean.Query<T> select(String properties) {
    return exprList.select(properties);
  }

  public com.avaje.ebean.Query<T> setBackgroundFetchAfter(int backgroundFetchAfter) {
    return exprList.setBackgroundFetchAfter(backgroundFetchAfter);
  }

  public com.avaje.ebean.Query<T> setFirstRow(int firstRow) {
    return exprList.setFirstRow(firstRow);
  }

  public com.avaje.ebean.Query<T> setListener(QueryListener<T> queryListener) {
    return exprList.setListener(queryListener);
  }

  public com.avaje.ebean.Query<T> setMapKey(String mapKey) {
    return exprList.setMapKey(mapKey);
  }

  public com.avaje.ebean.Query<T> setMaxRows(int maxRows) {
    return exprList.setMaxRows(maxRows);
  }

  public com.avaje.ebean.Query<T> setOrderBy(String orderBy) {
    return exprList.setOrderBy(orderBy);
  }

  public com.avaje.ebean.Query<T> setUseCache(boolean useCache) {
    return exprList.setUseCache(useCache);
  }

  public ExpressionList<T> startsWith(String propertyName, String value) {
    return exprList.startsWith(propertyName, value);
  }

  public ExpressionList<T> where() {
    return exprList.where();
  }

}
