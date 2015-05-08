package com.avaje.ebeaninternal.util;

import com.avaje.ebean.*;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.server.expression.FilterExprPath;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterExpressionList<T> extends DefaultExpressionList<T> {

  private static final long serialVersionUID = 2226895827150099020L;

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
  public SpiExpressionList<?> trimPath(int prefixTrim) {
    return new FilterExpressionList<T>(pathPrefix.trimPath(prefixTrim), this);
  }

  public FilterExprPath getPathPrefix() {
    return pathPrefix;
  }

  private String notAllowedMessage = "This method is not allowed on a filter";

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
  public FutureRowCount<T> findFutureRowCount() {
    return rootQuery.findFutureRowCount();
  }

  @Override
  public List<T> findList() {
    return rootQuery.findList();
  }

  @Override
  public Map<?, T> findMap() {
    return rootQuery.findMap();
  }

  @Override
  public int findRowCount() {
    return rootQuery.findRowCount();
  }

  @Override
  public Set<T> findSet() {
    return rootQuery.findSet();
  }

  @Override
  public T findUnique() {
    return rootQuery.findUnique();
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
  public ExpressionList<T> idIn(List<?> idValues) {
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
