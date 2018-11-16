package io.ebeaninternal.server.el;

import io.ebean.Filter;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Default implementation of the Filter interface.
 */
public final class ElFilter<T> implements Filter<T> {

  private final BeanDescriptor<T> beanDescriptor;

  private final ArrayList<ElMatcher<T>> matches = new ArrayList<>();

  private int maxRows;

  private String sortByClause;

  public ElFilter(BeanDescriptor<T> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
  }

  private Object convertValue(String propertyName, Object value) {
    // convert type of value to match expected type
    ElPropertyValue elGetValue = beanDescriptor.getElGetValue(propertyName);
    return elGetValue.convert(value);
  }

  private ElComparator<T> getElComparator(String propertyName) {

    return beanDescriptor.getElComparator(propertyName);
  }

  private ElPropertyValue getElGetValue(String propertyName) {

    return beanDescriptor.getElGetValue(propertyName);
  }

  @Override
  public Filter<T> sort(String sortByClause) {
    this.sortByClause = sortByClause;
    return this;
  }

  protected boolean isMatch(T bean) {
    for (ElMatcher<T> matcher : matches) {
      if (!matcher.isMatch(bean)) {
        return false;
      }
    }
    return true;
  }


  @Override
  public Filter<T> in(String propertyName, Set<?> matchingValues) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.InSet<>(matchingValues, elGetValue));
    return this;
  }

  @Override
  public Filter<T> eq(String propertyName, Object value) {

    value = convertValue(propertyName, value);
    ElComparator<T> comparator = getElComparator(propertyName);

    matches.add(new ElMatchBuilder.Eq<>(value, comparator));
    return this;
  }


  @Override
  public Filter<T> ne(String propertyName, Object value) {

    value = convertValue(propertyName, value);
    ElComparator<T> comparator = getElComparator(propertyName);

    matches.add(new ElMatchBuilder.Ne<>(value, comparator));
    return this;
  }

  @Override
  public Filter<T> between(String propertyName, Object min, Object max) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    min = elGetValue.convert(min);
    max = elGetValue.convert(max);

    ElComparator<T> elComparator = getElComparator(propertyName);

    matches.add(new ElMatchBuilder.Between<>(min, max, elComparator));
    return this;
  }


  @Override
  public Filter<T> gt(String propertyName, Object value) {

    value = convertValue(propertyName, value);
    ElComparator<T> comparator = getElComparator(propertyName);

    matches.add(new ElMatchBuilder.Gt<>(value, comparator));
    return this;
  }

  @Override
  public Filter<T> ge(String propertyName, Object value) {

    value = convertValue(propertyName, value);
    ElComparator<T> comparator = getElComparator(propertyName);

    matches.add(new ElMatchBuilder.Ge<>(value, comparator));
    return this;
  }

  @Override
  public Filter<T> ieq(String propertyName, String value) {

    ElPropertyValue elGetValue = getElGetValue(propertyName);

    matches.add(new ElMatchBuilder.Ieq<>(elGetValue, value));
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


  @Override
  public Filter<T> le(String propertyName, Object value) {

    value = convertValue(propertyName, value);
    ElComparator<T> comparator = getElComparator(propertyName);

    matches.add(new ElMatchBuilder.Le<>(value, comparator));
    return this;
  }


  @Override
  public Filter<T> lt(String propertyName, Object value) {

    value = convertValue(propertyName, value);
    ElComparator<T> comparator = getElComparator(propertyName);

    matches.add(new ElMatchBuilder.Lt<>(value, comparator));
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

    String quote = ".*" + Pattern.quote(value) + ".*";

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.RegularExpr<>(elGetValue, quote, 0));
    return this;
  }

  @Override
  public Filter<T> icontains(String propertyName, String value) {

    String quote = ".*" + Pattern.quote(value) + ".*";

    ElPropertyValue elGetValue = getElGetValue(propertyName);
    matches.add(new ElMatchBuilder.RegularExpr<>(elGetValue, quote, Pattern.CASE_INSENSITIVE));
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
    this.maxRows = maxRows;
    return this;
  }

  @Override
  public List<T> filter(List<T> list) {

    if (sortByClause != null) {
      // create shallow copy and sort
      list = new ArrayList<>(list);
      beanDescriptor.sort(list, sortByClause);
    }

    ArrayList<T> filterList = new ArrayList<>();

    for (T t : list) {
      if (isMatch(t)) {
        filterList.add(t);
        if (maxRows > 0 && filterList.size() >= maxRows) {
          break;
        }
      }
    }

    return filterList;
  }

}
