package io.ebeaninternal.server.el;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import io.ebean.Pairs;
import io.ebean.Query;
import io.ebean.QueryDsl;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.filter.Expression3VL;
import io.ebeaninternal.api.filter.ExpressionTest;
import io.ebeaninternal.api.filter.FilterContext;


/**
 * Contains the various ElMatcher implementations.
 */
class ElMatchBuilder {

  abstract static class Base<T,V> implements ElMatcher<T>, ExpressionTest {

    final ElPropertyValue elGetValue;

    public Base(ElPropertyValue elGetValue) {
      this.elGetValue = elGetValue;
    }

    @Override
    public Expression3VL isMatch(T bean, FilterContext ctx) {
      return elGetValue.pathTest(bean, ctx, this);
    }

    @Override
    public Expression3VL test(Object value) {
      return match((V) value) ? Expression3VL.TRUE : Expression3VL.FALSE;
    }

    /**
     * Test the value, if it matches the filter
     */
    abstract boolean match(V value);
  }

  static abstract class BaseValue<T,V> extends Base<T,V> {

    final V testValue;

    public BaseValue(ElPropertyValue elGetValue, V testValue) {
      super(elGetValue);
      this.testValue = testValue;
    }

    String getLiteral() {
      return "UNKNOWN";
    }


    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue.getElName()).append(' ').append(getLiteral()).append(" '").append(testValue).append('\'');
    }
  }

  /**
   * Internal helper class, to append patterns and literals to a regexp.
   */
  static class RegexAppender {

    final StringBuilder pattern;
    final StringBuilder literalBuffer;

    RegexAppender(int size) {
      pattern = new StringBuilder(size);
      literalBuffer = new StringBuilder(size);
    }

    private void flush() {
      if (literalBuffer.length() != 0) {
        String literal = literalBuffer.toString();
        if (literal.indexOf("\\E") == -1) {
          pattern.append("\\Q").append(literal).append("\\E");
        } else {
          pattern.append(Pattern.quote(literal));
        }
        literalBuffer.setLength(0);
      }
    }

    void appendPattern(String value) {
      flush();
      pattern.append(value);
    }

    void appendLiteral(char ch) {
      literalBuffer.append(ch);
    }

    void appendLiteral(String s) {
      literalBuffer.append(s);
    }
    @Override
    public String toString() {
      flush();
      return pattern.toString();
    }
  }

  /**
   * Case insensitive equals.
   */
  static class RegularExpr<T> extends Base<T,String> {

    final Pattern pattern;

    RegularExpr(ElPropertyValue elGetValue, String value, int options) {
      super(elGetValue);
      this.pattern = Pattern.compile(value, options);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("regexp(").append(elGetValue.getElName()).append(", '").append(pattern).append("')");
    }

    @Override
    public boolean match(String v) {
      return pattern.matcher(v).matches();
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      throw new UnsupportedOperationException("regexp not supported");
    }
  }

  static class Like<T> extends RegularExpr<T> {

    private final String like;
    private final boolean ignoreCase;

    private static String asPattern(String like) {
      RegexAppender regex = new RegexAppender(like.length()+32);
      for (int i = 0; i < like.length(); i++) {
        char ch = like.charAt(i);
        // currently no escaping is done!
        // if (ch == '|') {
        // if (i < like.length()) {
        // i++;
        // ch = like.charAt(i);
        // }
        // regex.appendLiteral(ch);
        // } else
        if (ch == '%') {
          regex.appendPattern(".*");
        } else if (ch == '_') {
          regex.appendPattern(".*");
        } else {
          regex.appendLiteral(ch);
        }
      }
      return regex.toString();
    }

    Like(ElPropertyValue elGetValue, String like, boolean ignoreCase) {
      super(elGetValue, asPattern(like), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
      this.like = like;
      this.ignoreCase = ignoreCase;
    }

    @Override
    public void toString(StringBuilder sb) {
      if (ignoreCase) {
        sb.append("iLike(");
      } else {
        sb.append("like");
      }
      sb.append(elGetValue.getElName()).append(", '").append(pattern).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      if (ignoreCase) {
        target.ilike(elGetValue.getElName(), like);
      } else {
        target.like(elGetValue.getElName(), like);
      }
    }
  }

  static class Ends<T> extends RegularExpr<T> {

    private final String value;
    private final boolean ignoreCase;

    private static String asPattern(String s) {
      RegexAppender regex = new RegexAppender(s.length()+32);
      regex.appendPattern(".*");
      regex.appendLiteral(s);
      return regex.toString();
    }

    Ends(ElPropertyValue elGetValue, String value, boolean ignoreCase) {
      super(elGetValue, asPattern(value), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
      this.value = value;
      this.ignoreCase = ignoreCase;
    }

    @Override
    public void toString(StringBuilder sb) {
      if (ignoreCase) {
        sb.append("iEnds(");
      } else {
        sb.append("ends");
      }
      sb.append(elGetValue.getElName()).append(", '").append(pattern).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      if (ignoreCase) {
        target.iendsWith(elGetValue.getElName(), value);
      } else {
        target.endsWith(elGetValue.getElName(), value);
      }
    }
  }

  static class Ieq<T> extends BaseValue<T, String> {
    Ieq(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean match(String v) {
      return v.equalsIgnoreCase(testValue);
    }

    @Override
    String getLiteral() {
      return "=~";
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.ieq(elGetValue.getElName(), testValue);
    }
  }

  static class Ine<T> extends BaseValue<T, String> {
    Ine(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean match(String v) {
      return !v.equalsIgnoreCase(testValue);
    }

    @Override
    String getLiteral() {
      return "!=~";
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.ine(elGetValue.getElName(), testValue);
    }
  }


  /**
   * Case insensitive starts with matcher.
   */
  static class IStartsWith<T> extends BaseValue<T, String> {

    private final CharMatch charMatch;

    IStartsWith(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
      this.charMatch = new CharMatch(value);
    }

    @Override
    public boolean match(String v) {
      return charMatch.startsWith(v);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("iStartsWith(").append(elGetValue.getElName()).append(", '").append(testValue).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.istartsWith(elGetValue.getElName(), testValue);
    }
  }

  /**
   * Case insensitive ends with matcher.
   */
  static class IEndsWith<T> extends BaseValue<T, String> {

    final CharMatch charMatch;

    IEndsWith(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
      this.charMatch = new CharMatch(value);
    }

    @Override
    public boolean match(String v) {
      return charMatch.endsWith(v);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("iEndsWith(").append(elGetValue.getElName()).append(", '").append(testValue).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.iendsWith(elGetValue.getElName(), testValue);
    }
  }

  /**
   * Case insensitive ends with matcher.
   */
  static class IContains<T> extends BaseValue<T, String> {

    final CharMatch charMatch;

    IContains(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
      this.charMatch = new CharMatch(value);
    }

    @Override
    public boolean match(String v) {
      return charMatch.contains(v);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("iContains(").append(elGetValue.getElName()).append(", '").append(testValue).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.icontains(elGetValue.getElName(), testValue);
    }
  }

  static class StartsWith<T> extends BaseValue<T, String> {
    StartsWith(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean match(String v) {
      return v.startsWith(testValue);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("startsWith(").append(elGetValue.getElName()).append(", '").append(testValue).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.startsWith(elGetValue.getElName(), testValue);
    }
  }

  static class EndsWith<T> extends BaseValue<T, String> {
    EndsWith(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean match(String v) {
      return v.endsWith(testValue);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("endsWith(").append(elGetValue.getElName()).append(", '").append(testValue).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.endsWith(elGetValue.getElName(), testValue);
    }
  }

  static class Contains<T> extends BaseValue<T, String> {

    Contains(ElPropertyValue elGetValue, String value) {
      super(elGetValue, value);
    }

    @Override
    public boolean match(String v) {
      return v.contains(testValue);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("contains(").append(elGetValue.getElName()).append(", '").append(testValue).append("')");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.contains(elGetValue.getElName(), testValue);
    }
  }


  static class IsNull<T> extends Base<T, Object> {

    public IsNull(ElPropertyValue elGetValue) {
      super(elGetValue);
    }

    @Override
    public boolean match(Object v) {
      return false;
    }

    @Override
    public Expression3VL testNull() {
      return Expression3VL.TRUE;
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue).append(" is null");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.isNull(elGetValue.getElName());
    }
  }

  static class IsNotNull<T> extends Base<T, Object> {

    public IsNotNull(ElPropertyValue elGetValue) {
      super(elGetValue);
    }

    @Override
    public boolean match(Object value) {
      return true;
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue).append(" is not null");
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.isNotNull(elGetValue.getElName());
    }
  }

  static class InSet<T, V> extends Base<T, V> {

    final Set<V> set;

    public InSet(ElPropertyValue elGetValue, Set<V> set) {
      super(elGetValue);
      this.set = set;
    }

    @Override
    public boolean match(V value) {
      return set.contains(value);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue).append(" in ").append(set);
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.in(elGetValue.getElName(), set);
    }
  }

  static class InPairs<T> implements ElMatcher<T> {


    private Pairs pairs;
    private ElFilterNode<T> convertedPairs;

    public InPairs(Pairs pairs, ElFilterNode<T> convertedPairs) {
      this.pairs = pairs;
      this.convertedPairs = convertedPairs;
    }

    @Override
    public Expression3VL isMatch(T bean, FilterContext ctx) {
      if (convertedPairs == null) {
        return Expression3VL.FALSE;
      } else {
        return convertedPairs.isMatch(bean, ctx);
      }
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append("iPairs(").append(pairs).append(')');
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.inPairs(pairs);
    }
  }


  static class NotInSet<T, V> extends Base<T, V> {
    final Set<V> set;

    public NotInSet(ElPropertyValue elGetValue, Set<V> set) {
      super(elGetValue);
      this.set = set;
    }

    @Override
    public boolean match(V value) {
      return !set.contains(value);
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue).append(" not in ").append(set);
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.notIn(elGetValue.getElName(), set);
    }
  }

  /**
   * Special case: In-Query. The subquery must not be executed at construction, as this may return the wrong data.
   */
  static class InQuery<T, V> extends Base<T, V> {

    private Query<?> query;

    private class Tester implements ExpressionTest {
      final Set<V> set = new HashSet<>(query.findSingleAttributeList());
      @Override
      public Expression3VL test(Object value) {
        if (elGetValue.isAssocId()) {
          value = elGetValue.getAssocIdValues((EntityBean) value)[0];
        }
        return set.contains(value) ? Expression3VL.TRUE : Expression3VL.FALSE;
      }
    }

    public InQuery(ElPropertyValue elGetValue, Query<?> query) {
      super(elGetValue);
      this.query = query;
    }

    @Override
    public Expression3VL isMatch(T bean, FilterContext ctx) {
      ExpressionTest test = ctx.computeIfAbsent(this, Tester::new);
      return elGetValue.pathTest(bean, ctx, test);
    }

    @Override
    boolean match(V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue).append(" in [").append(query).append(']');
    }
    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.in(elGetValue.getElName(), query);
    }
  }

  /**
   * Equal To.
   */
  static class Eq<T, V> extends BaseValue<T, V> {

    public Eq(ElPropertyValue elGetValue, V value) {
      super(elGetValue, value);
    }

    @Override
    String getLiteral() {
      return "=";
    }

    @Override
    public boolean match(V v) {
      return Objects.equals(v, testValue);
    }

    @Override
    public Expression3VL testNull() {
      return testValue == null ? Expression3VL.TRUE : Expression3VL.UNKNOWN;
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.eq(elGetValue.getElName(), testValue);
    }
  }

  /**
   * Not Equal To.
   */
  static class Ne<T, V> extends BaseValue<T, V>  {

    public Ne(ElPropertyValue elGetValue, V value) {
      super(elGetValue, value);
    }

    @Override
    String getLiteral() {
      return "!=";
    }

    @Override
    public boolean match(V v) {
      return !Objects.equals(testValue, v);
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.ne(elGetValue.getElName(), testValue);
    }
  }


  /**
   * Between.
   */
  static class Between<T,V> extends Base<T,V> {

    final Comparable<V> min;
    final Comparable<V> max;

    Between(ElPropertyValue elGetValue, Comparable<V> min, Comparable<V> max) {
      super(elGetValue);
      this.min = min;
      this.max = max;
    }

    @Override
    public boolean match(V value) {
      return min.compareTo(value) <= 0
          && max.compareTo(value) >= 0;
    }

    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue).append(" between '").append(min).append("' and '").append(max).append('\'');
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.between(elGetValue.getElName(), min, max);
    }
  }

  /**
   * Greater Than.
   */
  static class Gt<T,V extends Comparable<V>> extends BaseValue<T,V> {
    Gt(ElPropertyValue elGetValue, V testValue) {
      super(elGetValue, testValue);
    }

    @Override
    String getLiteral() {
      return ">";
    }

    @Override
    public boolean match(V value) {
      return value.compareTo(testValue) > 0;
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.gt(elGetValue.getElName(), testValue);
    }
  }

  /**
   * Greater Than or Equal To.
   */
  static class Ge<T,V extends Comparable<V>> extends BaseValue<T,V> {
    Ge(ElPropertyValue elGetValue, V testValue) {
      super(elGetValue, testValue);
    }

    @Override
    String getLiteral() {
      return ">=";
    }

    @Override
    public boolean match(V value) {
      return value.compareTo(testValue) >= 0;
    }
    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.ge(elGetValue.getElName(), testValue);
    }
  }

  /**
   * Less Than or Equal To.
   */
  static class Le<T, V extends Comparable<V>> extends BaseValue<T, V> {
    Le(ElPropertyValue elGetValue, V testValue) {
      super(elGetValue, testValue);
    }

    @Override
    String getLiteral() {
      return "<=";
    }

    @Override
    public boolean match(V value) {
      return value.compareTo(testValue) <= 0;
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.le(elGetValue.getElName(), testValue);
    }
  }

  /**
   * Less Than.
   */
  static class Lt<T, V extends Comparable<V>> extends BaseValue<T, V> {
    Lt(ElPropertyValue elGetValue, V testValue) {
      super(elGetValue, testValue);
    }

    @Override
    String getLiteral() {
      return "<";
    }

    @Override
    public boolean match(V value) {
      return value.compareTo(testValue) < 0;
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      target.lt(elGetValue.getElName(), testValue);
    }
  }

  /**
   * Bitwise And.
   */
  static class BitAnd<T> extends Base<T, Long> {

    public enum Type {
      ALL, AND, ANY, NOT;
    }
    private long flags;
    private boolean eq;
    private long match;
    private Type type;

    public BitAnd(ElPropertyValue elGetValue, long flags, boolean eq, long match, Type type) {
      super(elGetValue);
      this.flags = flags;
      this.eq = eq;
      this.match = match;
      this.type = type;
    }

    @Override
    public boolean match(Long v) {
      if (eq) {
        return (v.longValue() & flags) == match;
      } else {
        return (v.longValue() & flags) != match;
      }
    }
    @Override
    public void toString(StringBuilder sb) {
      sb.append(elGetValue).append(" & ").append(flags);
      if (eq) {
        sb.append(" = ");
      } else {
        sb.append(" != ");
      }
      sb.append(match);
    }

    @Override
    public <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target) {
      switch (type) {
      case ALL:
        target.bitwiseAll(elGetValue.getElName(), flags);
        break;

      case AND:
        target.bitwiseAnd(elGetValue.getElName(), flags, match);
        break;

      case ANY:
        target.bitwiseAny(elGetValue.getElName(), flags);
        break;

      case NOT:
        target.bitwiseNot(elGetValue.getElName(), flags);
        break;
      default:
        throw new UnsupportedOperationException();
      }
    }
  }
}
