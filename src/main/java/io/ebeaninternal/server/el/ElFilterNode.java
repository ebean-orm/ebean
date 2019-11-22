package io.ebeaninternal.server.el;

import io.ebean.Filter;
import io.ebean.Junction;
import io.ebean.Pairs;
import io.ebean.Query;
import io.ebean.QueryDsl;
import io.ebeaninternal.api.filter.Expression3VL;
import io.ebeaninternal.api.filter.FilterContext;
import io.ebeaninternal.server.el.ElMatchBuilder.BitAnd.Type;
import io.ebeaninternal.server.filter.DefaultFilterContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Default implementation of the Filter interface.
 */
class ElFilterNode<T> implements Filter<T>, ElMatcher<T> {

  private final ElFilter<T> elFilter;

  public final List<ElMatcher<T>> matches = new ArrayList<>();

  private final Filter<T> parent;

  private final Junction.Type type;

  ElFilterNode(ElFilter<T> elFilter, Filter<T> parent, Junction.Type type) {
    this.elFilter = elFilter;
    this.parent = parent;
    this.type = type;
  }

  private ElPropertyValue getElGetValue(String propertyName) {
    return elFilter.getElGetValue(propertyName);
  }

  @Override
  public Filter<T> sort(String sortByClause) {
    elFilter.sort(sortByClause);
    return this;
  }


  @Override
  public int getFirstRow() {
    return elFilter.getFirstRow();
  }

  @Override
  public int getMaxRows() {
    return elFilter.getMaxRows();
  }

  @Override
  public String getSort() {
    return elFilter.getSort();
  }

  @Override
  public Matcher<T> matcher() {
    return elFilter.matcher();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    toString(sb);
    return sb.toString();
  }

  @Override
  public void toString(StringBuilder sb) {
    sb.append(type.prefix());
    sb.append("(");
    for (int i = 0; i < matches.size(); i++) {
      if (i > 0) {
        sb.append(type.literal());
      }
      matches.get(i).toString(sb);
    }
    sb.append(")");
  }

  @Override
  public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
    switch (type) {
    case AND:
      target = target.and();
      for (ElMatcher<T> matcher : matches) {
        matcher.visitDsl(target);
      }
      target.endAnd();
      break;

    case OR:
      target = target.or();
      for (ElMatcher<T> matcher : matches) {
        matcher.visitDsl(target);
      }
      target.endOr();
      break;

    case NOT:
      target = target.not();
      for (ElMatcher<T> matcher : matches) {
        matcher.visitDsl(target);
      }
      target.endNot();
      break;

    default:
      throw new IllegalArgumentException("Unknown type " + type);
    }
  }

  @Override
  public <F2 extends QueryDsl<T, F2>> Filter<T> applyTo(QueryDsl<T, F2> target) {
    for (ElMatcher<T> matcher : matches) {
      matcher.visitDsl(target);
    }
    return this;
  }

  protected boolean isMatchAnyPermuation(T bean, DefaultFilterContext ctx) {
    do {
      if (isMatch(bean, ctx) == Expression3VL.TRUE) {
        return true;
      }
    } while (ctx.nextPermutation());
    return false;
  }

  @Override
  public Expression3VL isMatch(T bean, FilterContext ctx) {
    Expression3VL ret;
    switch (type) {
    case AND:
      ret = Expression3VL.TRUE;
      for (ElMatcher<T> matcher : matches) {
        ret = ret.and(matcher.isMatch(bean, ctx));
        if (ret == Expression3VL.FALSE) {
          break;
        }
      }
      return ret;

    case OR:
      ret = Expression3VL.FALSE;
      for (ElMatcher<T> matcher : matches) {
        ret = ret.or(matcher.isMatch(bean, ctx));
        if (ret == Expression3VL.TRUE) {
          break;
        }
      }
      return ret;

    case NOT:
      ret = Expression3VL.TRUE;
      for (ElMatcher<T> matcher : matches) {
        ret = ret.and(matcher.isMatch(bean, ctx));
        if (ret == Expression3VL.FALSE) {
          return Expression3VL.TRUE;
        }
      }
      return Expression3VL.FALSE;

    default:
      throw new IllegalArgumentException("Unknown type " + type);
    }
  }

  @Override
  public Filter<T> in(String propertyName, Collection<?> matchingValues) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    Set<Object> set = new HashSet<>();
    for (Object value : matchingValues) {
      set.add(elGetValue.convert(value));
    }

    matches.add(new ElMatchBuilder.InSet<>(elGetValue, set));
    return this;
  }

  @Override
  public Filter<T> in(String propertyName, Object... matchingValues) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    Set<Object> set = new HashSet<>();
    for (Object value : matchingValues) {
      set.add(elGetValue.convert(value));
    }

    matches.add(new ElMatchBuilder.InSet<>(elGetValue, set));
    return this;
  }

  @Override
  public Filter<T> in(String propertyName, Query<?> subQuery) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.InQuery<>(elGetValue, subQuery));
    return this;
  }

  @Override
  public Filter<T> inPairs(Pairs pairs) {
    if (pairs.getEntries().isEmpty()) {
      matches.add(new ElMatchBuilder.InPairs<>(pairs, null));
    } else {
      ElFilterNode<T> convertedPairs = new ElFilterNode<>(elFilter, null, Junction.Type.OR);
      for (Pairs.Entry entry : pairs.getEntries()) {
        convertedPairs.and() // emulate inPairs
            .eq(pairs.getProperty0(), entry.getA()) //
            .eq(pairs.getProperty1(), entry.getB()) //
            .endAnd();
      }
      matches.add(new ElMatchBuilder.InPairs<>(pairs, convertedPairs));
    }

    return this;
  }

  @Override
  public Filter<T> notIn(String propertyName, Collection<?> matchingValues) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    Set<Object> set = new HashSet<>();
    for (Object value : matchingValues) {
      set.add(elGetValue.convert(value));
    }

    matches.add(new ElMatchBuilder.NotInSet<>(elGetValue, set));
    return this;
  }

  @Override
  public Filter<T> notIn(String propertyName, Object... matchingValues) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    Set<Object> set = new HashSet<>();
    for (Object value : matchingValues) {
      set.add(elGetValue.convert(value));
    }

    matches.add(new ElMatchBuilder.NotInSet<>(elGetValue, set));
    return this;
  }

  @Override
  public Filter<T> notIn(String propertyName, Query<?> subQuery) {

    return notIn(propertyName, subQuery.findSingleAttributeList());
  }

  @Override
  public Filter<T> eq(String propertyName, Object value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    value = elGetValue.convert(value);
    matches.add(new ElMatchBuilder.Eq<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> allEq(Map<String, Object> propertyMap) {
    if (type != Junction.Type.AND) {
      return and().allEq(propertyMap).endAnd();
    } else {
      for (Entry<String, Object> entry : propertyMap.entrySet()) {
        ElPropertyValue elGetValue = getElGetValue(entry.getKey());
        Object value = elGetValue.convert(entry.getValue());
        matches.add(new ElMatchBuilder.Eq<>(elGetValue, value));
      }
      return this;
    }
  }
  @Override
  public Filter<T> ne(String propertyName, Object value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    value = elGetValue.convert(value);

    matches.add(new ElMatchBuilder.Ne<>(elGetValue, value));
    return this;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Filter<T> between(String propertyName, Object min, Object max) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    Comparable minC = (Comparable) elGetValue.convert(min);
    Comparable maxC = (Comparable) elGetValue.convert(max);

    matches.add(new ElMatchBuilder.Between<>(elGetValue, minC, maxC));
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Filter<T> gt(String propertyName, Object value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    Comparable comp = (Comparable) elGetValue.convert(value);

    matches.add(new ElMatchBuilder.Gt<>(elGetValue, comp));
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Filter<T> ge(String propertyName, Object value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    Comparable comp = (Comparable) elGetValue.convert(value);

    matches.add(new ElMatchBuilder.Ge<>(elGetValue, comp));
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Filter<T> le(String propertyName, Object value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    Comparable comp = (Comparable) elGetValue.convert(value);

    matches.add(new ElMatchBuilder.Le<>(elGetValue, comp));
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Filter<T> lt(String propertyName, Object value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    Comparable comp = (Comparable) elGetValue.convert(value);

    matches.add(new ElMatchBuilder.Lt<>(elGetValue, comp));
    return this;
  }

  @Override
  public Filter<T> ieq(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.Ieq<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> ine(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.Ine<>(elGetValue, value));
    return this;
  }


  @Override
  public Filter<T> isNotNull(String propertyName) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.IsNotNull<>(elGetValue));
    return this;
  }

  @Override
  public Filter<T> isNull(String propertyName) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.IsNull<>(elGetValue));
    return this;
  }

  public Filter<T> regex(String propertyName, String regEx) {
    return regex(propertyName, regEx, 0);
  }

  public Filter<T> regex(String propertyName, String regEx, int options) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.RegularExpr<>(elGetValue, regEx, options));
    return this;
  }


  @Override
  public Filter<T> contains(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.Contains<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> icontains(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.IContains<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> like(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.Like<>(elGetValue, value, false));
    return this;
  }

  @Override
  public Filter<T> ilike(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.Like<>(elGetValue, value, true));
    return this;
  }

  @Override
  public Filter<T> endsWith(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.EndsWith<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> startsWith(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.StartsWith<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> iendsWith(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.IEndsWith<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> istartsWith(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.IStartsWith<>(elGetValue, value));
    return this;
  }

  @Override
  public Filter<T> maxRows(int maxRows) {
    elFilter.maxRows(maxRows);
    return this;
  }

  @Override
  public Filter<T> firstRow(int firstRow) {
    elFilter.firstRow(firstRow);
    return this;
  }

  @Override
  public Filter<T> bitwiseAll(String propertyName, long flags) {
    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.BitAnd<>(elGetValue, flags, true, flags, Type.ALL));
    return this;
  }

  @Override
  public Filter<T> bitwiseAny(String propertyName, long flags) {
    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.BitAnd<>(elGetValue, flags, false, 0, Type.ANY));
    return this;
  }

  @Override
  public Filter<T> bitwiseNot(String propertyName, long flags) {
    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.BitAnd<>(elGetValue, flags, true, 0, Type.NOT));
    return this;
  }

  @Override
  public Filter<T> bitwiseAnd(String propertyName, long flags, long match) {
    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.BitAnd<>(elGetValue, flags, true, match, Type.AND));
    return this;
  }

  @Override
  public List<T> filter(List<T> list) {
    return elFilter.filter(list);
  }

  private Filter<T> endJunction() {
    if (parent == null) {
      throw new IllegalStateException("Cannot close non-open junction");
    }
    return parent;
  }

  @Override
  public Filter<T> and() {
    ElFilterNode<T> filter = new ElFilterNode<>(elFilter, this, Junction.Type.AND);
    matches.add(filter);
    return filter;
  }

  @Override
  public Filter<T> or() {
    ElFilterNode<T> filter = new ElFilterNode<>(elFilter, this, Junction.Type.OR);
    matches.add(filter);
    return filter;
  }

  @Override
  public Filter<T> not() {
    ElFilterNode<T> filter = new ElFilterNode<>(elFilter, this, Junction.Type.NOT);
    matches.add(filter);
    return filter;
  }

  @Override
  public Filter<T> endAnd() {
    return endJunction();
  }

  @Override
  public Filter<T> endOr() {
    return endJunction();
  }

  @Override
  public Filter<T> endNot() {
    return endJunction();
  }

//  /**
//   * Simplify nested expressions where possible.
//   * <p>
//   * This is expected to only used after expressions are built via query language parsing.
//   * </p>
//   */
//  @SuppressWarnings("unchecked")
//  @Override
//  public void simplify() {
//    exprList.simplifyEntries();
//
//    List<SpiExpression> list = exprList.list;
//    if (list.size() == 1 && list.get(0) instanceof JunctionExpression) {
//      @SuppressWarnings("rawtypes")
//      JunctionExpression nested = (JunctionExpression) list.get(0);
//      if (type == Type.AND && !nested.type.isText()) {
//        // and (and (a, b, c)) -> and (a, b, c)
//        // and (not (a, b, c)) -> not (a, b, c)
//        // and (or  (a, b, c)) -> or  (a, b, c)
//        this.exprList = nested.exprList;
//        this.type = nested.type;
//      } else if (type == Type.NOT && nested.type == Type.AND) {
//        // not (and (a, b, c)) -> not (a, b, c)
//        this.exprList = nested.exprList;
//      }
//    }
//  }
}
