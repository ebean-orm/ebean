package com.avaje.ebeaninternal.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.FutureIds;
import com.avaje.ebean.FutureList;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.PagingList;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.server.expression.FilterExprPath;

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
    
    public SpiExpressionList<?> trimPath(int prefixTrim) {
        return new FilterExpressionList<T>(pathPrefix.trimPath(prefixTrim), this);
    }
    
    public FilterExprPath getPathPrefix() {
        return pathPrefix;
    }

    private String notAllowedMessage = "This method is not allowed on a filter";
        
    public ExpressionList<T> filterMany(String prop) {
        return rootQuery.filterMany(prop);
    }
    
    public FutureIds<T> findFutureIds() {
        return rootQuery.findFutureIds();
    }

    public FutureList<T> findFutureList() {
        return rootQuery.findFutureList();
    }

    public FutureRowCount<T> findFutureRowCount() {
        return rootQuery.findFutureRowCount();
    }

    public List<T> findList() {
        return rootQuery.findList();
    }

    public Map<?, T> findMap() {
        return rootQuery.findMap();
    }

    public PagingList<T> findPagingList(int pageSize) {
        return rootQuery.findPagingList(pageSize);
    }

    public int findRowCount() {
        return rootQuery.findRowCount();
    }

    public Set<T> findSet() {
        return rootQuery.findSet();
    }

    public T findUnique() {
        return rootQuery.findUnique();
    }

    public ExpressionList<T> having() {
        throw new PersistenceException(notAllowedMessage);        
    }

    public ExpressionList<T> idEq(Object value) {
        throw new PersistenceException(notAllowedMessage);        
    }

    public ExpressionList<T> idIn(List<?> idValues) {
        throw new PersistenceException(notAllowedMessage);        
    }

    public Query<T> join(String assocProperty, String assocProperties) {
        throw new PersistenceException(notAllowedMessage);        
    }

    public Query<T> join(String assocProperties) {
        throw new PersistenceException(notAllowedMessage);        
    }

    public OrderBy<T> order() {
        return rootQuery.order();
    }

    public Query<T> order(String orderByClause) {
        return rootQuery.order(orderByClause);
    }

    public Query<T> orderBy(String orderBy) {
        return rootQuery.orderBy(orderBy);
    }

    public Query<T> query() {
        return rootQuery;
    }

    public Query<T> select(String properties) {
        throw new PersistenceException(notAllowedMessage);        
    }

    public Query<T> setFirstRow(int firstRow) {
        return rootQuery.setFirstRow(firstRow);
    }

    public Query<T> setMapKey(String mapKey) {
        return rootQuery.setMapKey(mapKey);
    }

    public Query<T> setMaxRows(int maxRows) {
        return rootQuery.setMaxRows(maxRows);
    }

    public Query<T> setUseCache(boolean useCache) {
        return rootQuery.setUseCache(useCache);
    }

    public ExpressionList<T> where() {
        return rootQuery.where();
    }

    
}
