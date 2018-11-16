package io.ebeaninternal.server.el;

import io.ebean.Filter;
import io.ebean.Junction;
import io.ebean.Pairs;
import io.ebean.Query;
import io.ebean.QueryDsl;
import io.ebeaninternal.api.filter.Expression3VL;
import io.ebeaninternal.api.filter.FilterContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.filter.DefaultFilterContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the Filter interface.
 */
public final class ElFilter<T> implements Filter<T> {

  private final BeanDescriptor<T> beanDescriptor;

  private int firstRow;

  private int maxRows;

  private String sortByClause;

  private final ElFilterNode<T> root = new ElFilterNode<T>(this, this, Junction.Type.AND);

  public ElFilter(BeanDescriptor<T> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
  }


  ElPropertyValue getElGetValue(String propertyName) {

    ElPropertyValue elValue = beanDescriptor.getElGetValue(propertyName);
    if (elValue == null) {
      throw new IllegalArgumentException("Cannot find property [" + propertyName + "] in " + beanDescriptor.getBeanType());
    }
    return elValue;
  }

  @Override
  public Filter<T> maxRows(int maxRows) {
    this.maxRows = maxRows;
    return this;
  }

  @Override
  public Filter<T> firstRow(int firstRow) {
    this.firstRow = firstRow;
    return this;
  }

  @Override
  public Filter<T> sort(String sortByClause) {
    this.sortByClause = sortByClause;
    return this;
  }




  @Override
  public List<T> filter(List<T> list) {

    ArrayList<T> filterList = new ArrayList<>();
    if (firstRow > 0 || maxRows > 0) {
      // we have a LIMIT clause, so we must first sort and then filter.
      if (sortByClause != null) {
        // create shallow copy and sort
        list = new ArrayList<>(list);
        beanDescriptor.sort(list, sortByClause);
      }

      DefaultFilterContext ctx = new DefaultFilterContext();
      int count = 0;
      for (T t : list) {
        if (root.isMatchAnyPermuation(t, ctx)) {
          count++;
          if (count > firstRow) {
            filterList.add(t);
            if (maxRows > 0 && filterList.size() >= maxRows) {
              break;
            }
          }
        }
        ctx.reset();
      }
    } else {
      // no LIMIT clause, so let's filter and then sort
      DefaultFilterContext ctx = new DefaultFilterContext();
      for (T t : list) {
        if (root.isMatchAnyPermuation(t, ctx)) {
          filterList.add(t);
        }
        ctx.reset();
      }
      if (sortByClause != null) {
        beanDescriptor.sort(filterList, sortByClause);
      }
    }
    return filterList;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("filter ").append(beanDescriptor);
    if (!root.matches.isEmpty()) {
      sb.append(" where ");
      root.toString(sb);
    }
    if (sortByClause != null) {
      sb.append(" order by ").append(sortByClause);
    }
    if (firstRow > 0) {
      sb.append(" offset ").append(firstRow);
    }
    if (maxRows > 0) {
      sb.append(" limit ").append(maxRows);
    }
    return sb.toString();
  }

  @Override
  public <F2 extends QueryDsl<T, F2>> Filter<T> applyTo(QueryDsl<T, F2> target) {
    return root.applyTo(target);
  }

  // ======= DELEGATE METHODS =========

  @Override
  public int getFirstRow() {
    return firstRow;
  }

  @Override
  public int getMaxRows() {
    return maxRows;
  }

  @Override
  public String getSort() {
    return sortByClause;
  }

  @Override
  public Matcher<T> matcher() {
    DefaultFilterContext ctx = new DefaultFilterContext();
    return bean -> root.isMatchAnyPermuation(bean, ctx);
  }

  public Expression3VL isMatch(T bean, FilterContext ctx) {
    return root.isMatch(bean, ctx);
  }

  @Override
  public Filter<T> in(String propertyName, Collection<?> matchingValues) {
    return root.in(propertyName, matchingValues);
  }

  @Override
  public Filter<T> in(String propertyName, Object... matchingValues) {
    return root.in(propertyName, matchingValues);
  }

  @Override
  public Filter<T> in(String propertyName, Query<?> subQuery) {
    return root.in(propertyName, subQuery);
  }

  @Override
  public Filter<T> inPairs(Pairs pairs) {
    return root.inPairs(pairs);
  }

  @Override
  public Filter<T> notIn(String propertyName, Collection<?> matchingValues) {
    return root.notIn(propertyName, matchingValues);
  }

  @Override
  public Filter<T> notIn(String propertyName, Object... matchingValues) {
    return root.notIn(propertyName, matchingValues);
  }

  @Override
  public Filter<T> notIn(String propertyName, Query<?> subQuery) {
    return root.notIn(propertyName, subQuery);
  }

  @Override
  public Filter<T> eq(String propertyName, Object value) {
    return root.eq(propertyName, value);
  }

  @Override
  public Filter<T> allEq(Map<String, Object> propertyMap) {
    return root.allEq(propertyMap);
  }

  @Override
  public Filter<T> ne(String propertyName, Object value) {
    return root.ne(propertyName, value);
  }

  @Override
  public Filter<T> between(String propertyName, Object min, Object max) {
    return root.between(propertyName, min, max);
  }

  @Override
  public Filter<T> gt(String propertyName, Object value) {
    return root.gt(propertyName, value);
  }

  @Override
  public Filter<T> ge(String propertyName, Object value) {
    return root.ge(propertyName, value);
  }

  @Override
  public Filter<T> le(String propertyName, Object value) {
    return root.le(propertyName, value);
  }

  @Override
  public Filter<T> lt(String propertyName, Object value) {
    return root.lt(propertyName, value);
  }

  @Override
  public Filter<T> ieq(String propertyName, String value) {
    return root.ieq(propertyName, value);
  }

  @Override
  public Filter<T> ine(String propertyName, String value) {
    return root.ine(propertyName, value);
  }

  @Override
  public Filter<T> isNotNull(String propertyName) {
    return root.isNotNull(propertyName);
  }

  @Override
  public Filter<T> isNull(String propertyName) {
    return root.isNull(propertyName);
  }

  public Filter<T> regex(String propertyName, String regEx) {
    return root.regex(propertyName, regEx);
  }

  public Filter<T> regex(String propertyName, String regEx, int options) {
    return root.regex(propertyName, regEx, options);
  }

  @Override
  public Filter<T> contains(String propertyName, String value) {
    return root.contains(propertyName, value);
  }

  @Override
  public Filter<T> icontains(String propertyName, String value) {
    return root.icontains(propertyName, value);
  }

  @Override
  public Filter<T> like(String propertyName, String value) {
    return root.like(propertyName, value);
  }

  @Override
  public Filter<T> ilike(String propertyName, String value) {
    return root.ilike(propertyName, value);
  }

  @Override
  public Filter<T> endsWith(String propertyName, String value) {
    return root.endsWith(propertyName, value);
  }

  @Override
  public Filter<T> startsWith(String propertyName, String value) {
    return root.startsWith(propertyName, value);
  }

  @Override
  public Filter<T> iendsWith(String propertyName, String value) {
    return root.iendsWith(propertyName, value);
  }

  @Override
  public Filter<T> istartsWith(String propertyName, String value) {
    return root.istartsWith(propertyName, value);
  }


  @Override
  public Filter<T> bitwiseAll(String propertyName, long flags) {
    return root.bitwiseAll(propertyName, flags);
  }

  @Override
  public Filter<T> bitwiseAny(String propertyName, long flags) {
    return root.bitwiseAny(propertyName, flags);
  }

  @Override
  public Filter<T> bitwiseNot(String propertyName, long flags) {
    return root.bitwiseNot(propertyName, flags);
  }

  @Override
  public Filter<T> bitwiseAnd(String propertyName, long flags, long match) {
    return root.bitwiseAnd(propertyName, flags, match);
  }

  @Override
  public Filter<T> and() {
    return root.and();
  }

  @Override
  public Filter<T> or() {
    return root.or();
  }

  @Override
  public Filter<T> not() {
    return root.not();
  }

  @Override
  public Filter<T> endAnd() {
    return root.endAnd();
  }

  @Override
  public Filter<T> endOr() {
    return root.endOr();
  }

  @Override
  public Filter<T> endNot() {
    return root.endNot();
  }

}
