package io.ebeaninternal.server.expression;

import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebean.FutureIds;
import io.ebean.FutureList;
import io.ebean.FutureRowCount;
import io.ebean.Junction;
import io.ebean.OrderBy;
import io.ebean.Query;
import io.ebeaninternal.api.SpiExpressionList;

import javax.persistence.PersistenceException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterExpressionList<T> extends DefaultExpressionList<T> {

  private static final String notAllowedMessage = "This method is not allowed on a filter";

  private final Query<T> rootQuery;

  private final FilterExprPath pathPrefix;

  public FilterExpressionList(FilterExprPath pathPrefix, FilterExpressionList<T> original) {
    super(null, original.expr, null, original.getUnderlyingList());
    this.pathPrefix = pathPrefix;
    this.rootQuery = original.rootQuery;
  }

  public FilterExpressionList(FilterExprPath pathPrefix, ExpressionFactory expr, Query<T> rootQuery) {
    super(null, expr, null);
    this.pathPrefix = pathPrefix;
    this.rootQuery = rootQuery;
  }

  @Override
  protected Junction<T> junction(Junction.Type type) {
    Junction<T> junction = expr.junction(type, rootQuery, this);
    add(junction);
    return junction;
  }

  @Override
  public SpiExpressionList<?> trimPath(int prefixTrim) {
    return new FilterExpressionList<>(pathPrefix.trimPath(prefixTrim), this);
  }

  @Override
  public ExpressionList<T> filterMany(String prop) {
    return rootQuery.filterMany(prop);
  }

  @Override
  public FutureIds<T> findFutureIds() {
    return rootQuery.findFutureIds();
  }

  @Override
  public FutureList<T> findFutureList() {
    return rootQuery.findFutureList();
  }

  @Override
  public FutureRowCount<T> findFutureCount() {
    return rootQuery.findFutureCount();
  }

  @Override
  public List<T> findList() {
    return rootQuery.findList();
  }

  @Override
  public <K> Map<K, T> findMap() {
    return rootQuery.findMap();
  }

  @Override
  public int findCount() {
    return rootQuery.findCount();
  }

  @Override
  public Set<T> findSet() {
    return rootQuery.findSet();
  }

  @Override
  public ExpressionList<T> having() {
    throw new PersistenceException(notAllowedMessage);
  }

  @Override
  public ExpressionList<T> idEq(Object value) {
    throw new PersistenceException(notAllowedMessage);
  }

  @Override
  public ExpressionList<T> idIn(Collection<?> idValues) {
    throw new PersistenceException(notAllowedMessage);
  }

  @Override
  public OrderBy<T> order() {
    return rootQuery.order();
  }

  @Override
  public Query<T> order(String orderByClause) {
    return rootQuery.order(orderByClause);
  }

  @Override
  public Query<T> orderBy(String orderBy) {
    return rootQuery.orderBy(orderBy);
  }

  @Override
  public Query<T> query() {
    return rootQuery;
  }

  @Override
  public Query<T> select(String properties) {
    throw new PersistenceException(notAllowedMessage);
  }

  @Override
  public Query<T> setFirstRow(int firstRow) {
    return rootQuery.setFirstRow(firstRow);
  }

  @Override
  public Query<T> setMapKey(String mapKey) {
    return rootQuery.setMapKey(mapKey);
  }

  @Override
  public Query<T> setMaxRows(int maxRows) {
    return rootQuery.setMaxRows(maxRows);
  }

  @Override
  public Query<T> setUseCache(boolean useCache) {
    return rootQuery.setUseCache(useCache);
  }

  @Override
  public ExpressionList<T> where() {
    return rootQuery.where();
  }


}
