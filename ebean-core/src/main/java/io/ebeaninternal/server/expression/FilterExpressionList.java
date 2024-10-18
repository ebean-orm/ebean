package io.ebeaninternal.server.expression;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.*;
import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiQuery;

import jakarta.persistence.PersistenceException;
import java.util.*;

@NullMarked
public final class FilterExpressionList<T> extends DefaultExpressionList<T> {

  private static final String notAllowedMessage = "This method is not allowed on a filter";

  private final Query<T> rootQuery;
  private final FilterExprPath pathPrefix;
  private int firstRow;
  private int maxRows;
  private String orderByClause;

  public FilterExpressionList(FilterExprPath pathPrefix, FilterExpressionList<T> original) {
    super(null, original.expr, null, original.underlyingList());
    this.pathPrefix = pathPrefix;
    this.rootQuery = original.rootQuery;
  }

  public FilterExpressionList(FilterExprPath pathPrefix, ExpressionFactory expr, Query<T> rootQuery) {
    super(null, expr, null, new ArrayList<>());
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

  @Nullable
  @Override
  public T findOne() {
    return rootQuery.findOne();
  }

  @Override
  public Optional<T> findOneOrEmpty() {
    return rootQuery.findOneOrEmpty();
  }

  @Override
  public boolean exists() {
    return rootQuery.exists();
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
  public Query<T> query() {
    return rootQuery;
  }

  @Override
  public Query<T> select(String properties) {
    throw new PersistenceException(notAllowedMessage);
  }

  @Override
  public Query<T> setMapKey(String mapKey) {
    return rootQuery.setMapKey(mapKey);
  }

  @Override
  public OrderBy<T> orderBy() {
    return rootQuery.orderBy();
  }

  @Override
  public ExpressionList<T> orderBy(String orderByClause) {
    this.orderByClause = orderByClause;
    return this;
  }

  @Override
  public ExpressionList<T> setMaxRows(int maxRows) {
    this.maxRows = maxRows;
    return this;
  }

  @Override
  public ExpressionList<T> setFirstRow(int firstRow) {
    this.firstRow = firstRow;
    return this;
  }

  @Override
  public Query<T> setUseCache(boolean useCache) {
    return rootQuery.setUseCache(useCache);
  }

  @Override
  public ExpressionList<T> where() {
    return rootQuery.where();
  }

  @Override
  public void applyRowLimits(SpiQuery<?> query) {
    if (firstRow > 0) {
      query.setFirstRow(firstRow);
    }
    if (maxRows > 0) {
      query.setMaxRows(maxRows);
    }
    if (orderByClause != null) {
      query.order(orderByClause);
    }
  }

}
