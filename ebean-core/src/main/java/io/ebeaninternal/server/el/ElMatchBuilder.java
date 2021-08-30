package io.ebeaninternal.server.el;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Contains the various ElMatcher implementations.
 */
final class ElMatchBuilder {

  /**
   * Case insensitive equals.
   */
  static final class RegularExpr<T> implements ElMatcher<T> {

    final ElPropertyValue elGetValue;
    final String value;
    final Pattern pattern;

    RegularExpr(ElPropertyValue elGetValue, String value, int options) {
      this.elGetValue = elGetValue;
      this.value = value;
      this.pattern = Pattern.compile(value, options);
    }

    @Override
    public boolean isMatch(T bean) {
      String v = (String) elGetValue.pathGet(bean);
      return pattern.matcher(v).matches();
    }
  }

  /**
   * Case insensitive equals.
   */
  static abstract class BaseString<T> implements ElMatcher<T> {

    final ElPropertyValue elGetValue;
    final String value;

    public BaseString(ElPropertyValue elGetValue, String value) {
      this.elGetValue = elGetValue;
      this.value = value;
    }

    @Override
    public abstract boolean isMatch(T bean);
  }

  static final class Ieq<T> extends BaseString<T> {
    Ieq(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean isMatch(T bean) {
      String v = (String) elGetValue.pathGet(bean);
      return value.equalsIgnoreCase(v);
    }
  }

  /**
   * Case insensitive starts with matcher.
   */
  static final class IStartsWith<T> implements ElMatcher<T> {

    final ElPropertyValue elGetValue;
    final CharMatch charMatch;

    IStartsWith(ElPropertyValue elGetValue, String value) {
      this.elGetValue = elGetValue;
      this.charMatch = new CharMatch(value);
    }

    @Override
    public boolean isMatch(T bean) {

      String v = (String) elGetValue.pathGet(bean);
      return charMatch.startsWith(v);
    }
  }

  /**
   * Case insensitive ends with matcher.
   */
  static final class IEndsWith<T> implements ElMatcher<T> {

    final ElPropertyValue elGetValue;
    final CharMatch charMatch;

    IEndsWith(ElPropertyValue elGetValue, String value) {
      this.elGetValue = elGetValue;
      this.charMatch = new CharMatch(value);
    }

    @Override
    public boolean isMatch(T bean) {

      String v = (String) elGetValue.pathGet(bean);
      return charMatch.endsWith(v);
    }
  }

  static final class StartsWith<T> extends BaseString<T> {
    StartsWith(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean isMatch(T bean) {
      String v = (String) elGetValue.pathGet(bean);
      return value.startsWith(v);
    }
  }

  static final class EndsWith<T> extends BaseString<T> {
    EndsWith(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean isMatch(T bean) {
      String v = (String) elGetValue.pathGet(bean);
      return value.endsWith(v);
    }
  }

  static final class IsNull<T> implements ElMatcher<T> {

    final ElPropertyValue elGetValue;

    public IsNull(ElPropertyValue elGetValue) {
      this.elGetValue = elGetValue;
    }

    @Override
    public boolean isMatch(T bean) {
      return (null == elGetValue.pathGet(bean));
    }
  }

  static final class IsNotNull<T> implements ElMatcher<T> {

    final ElPropertyValue elGetValue;

    public IsNotNull(ElPropertyValue elGetValue) {
      this.elGetValue = elGetValue;
    }

    @Override
    public boolean isMatch(T bean) {
      return (null != elGetValue.pathGet(bean));
    }
  }

  static abstract class Base<T> implements ElMatcher<T> {

    final Object filterValue;

    final ElComparator<T> comparator;

    public Base(Object filterValue, ElComparator<T> comparator) {
      this.filterValue = filterValue;
      this.comparator = comparator;
    }

    @Override
    public abstract boolean isMatch(T value);
  }

  static final class InSet<T> implements ElMatcher<T> {

    final Set<?> set;
    final ElPropertyValue elGetValue;

    @SuppressWarnings({"unchecked"})
    public InSet(Set<?> set, ElPropertyValue elGetValue) {
      this.set = new HashSet(set);
      this.elGetValue = elGetValue;
    }

    @Override
    public boolean isMatch(T bean) {
      Object value = elGetValue.pathGet(bean);
      return value != null && set.contains(value);
    }
  }

  /**
   * Equal To.
   */
  static final class Eq<T> extends Base<T> {

    public Eq(Object filterValue, ElComparator<T> comparator) {
      super(filterValue, comparator);
    }

    @Override
    public boolean isMatch(T value) {
      return comparator.compareValue(filterValue, value) == 0;
    }
  }

  /**
   * Not Equal To.
   */
  static final class Ne<T> extends Base<T> {

    public Ne(Object filterValue, ElComparator<T> comparator) {
      super(filterValue, comparator);
    }

    @Override
    public boolean isMatch(T value) {
      return comparator.compareValue(filterValue, value) != 0;
    }
  }

  /**
   * Between.
   */
  static final class Between<T> implements ElMatcher<T> {

    final Object min;
    final Object max;
    final ElComparator<T> comparator;

    Between(Object min, Object max, ElComparator<T> comparator) {
      this.min = min;
      this.max = max;
      this.comparator = comparator;
    }

    @Override
    public boolean isMatch(T value) {
      return (comparator.compareValue(min, value) <= 0
        && comparator.compareValue(max, value) >= 0);
    }
  }

  /**
   * Greater Than.
   */
  static final class Gt<T> extends Base<T> {
    Gt(Object filterValue, ElComparator<T> comparator) {
      super(filterValue, comparator);
    }

    @Override
    public boolean isMatch(T value) {
      return comparator.compareValue(filterValue, value) == -1;
    }
  }

  /**
   * Greater Than or Equal To.
   */
  static final class Ge<T> extends Base<T> {
    Ge(Object filterValue, ElComparator<T> comparator) {
      super(filterValue, comparator);
    }

    @Override
    public boolean isMatch(T value) {
      return comparator.compareValue(filterValue, value) >= 0;
    }
  }

  /**
   * Less Than or Equal To.
   */
  static final class Le<T> extends Base<T> {
    Le(Object filterValue, ElComparator<T> comparator) {
      super(filterValue, comparator);
    }

    @Override
    public boolean isMatch(T value) {
      return comparator.compareValue(filterValue, value) <= 0;
    }
  }

  /**
   * Less Than.
   */
  static final class Lt<T> extends Base<T> {
    Lt(Object filterValue, ElComparator<T> comparator) {
      super(filterValue, comparator);
    }

    @Override
    public boolean isMatch(T value) {
      return comparator.compareValue(filterValue, value) == 1;
    }
  }
}
