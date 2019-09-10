package io.ebeaninternal.server.expression;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RawExpressionBuilderTest {

  @Test
  public void buildSingle_noop() {

    RawExpression exp = RawExpressionBuilder.buildSingle("foo = ?", 42);
    assertThat(exp.sql).isEqualTo("foo = ?");
  }

  @Test
  public void buildSingle_noExpand() {

    RawExpression exp = RawExpressionBuilder.buildSingle("foo = ?", asList(42, 43));
    assertThat(exp.sql).isEqualTo("foo = ?");
  }

  @Test
  public void buildSingle_expand() {

    RawExpression exp = RawExpressionBuilder.buildSingle("foo in (?1)", asList(42, 43));
    assertThat(exp.sql).isEqualTo("foo in (?,?)");
    assertThat(exp.values).contains(42, 43);
  }

  @Test
  public void buildSingle_expand_more() {

    RawExpression exp = RawExpressionBuilder.buildSingle("foo in (?1)", asList(42, 43, 44, 45));
    assertThat(exp.sql).isEqualTo("foo in (?,?,?,?)");
    assertThat(exp.values).contains(42, 43, 44, 45);
  }


  @Test
  public void buildSingle_expand_single() {

    RawExpression exp = RawExpressionBuilder.buildSingle("foo in (?1)", asList(42));
    assertThat(exp.sql).isEqualTo("foo in (?)");
    assertThat(exp.values).contains(42);
  }


  @Test
  public void build_noop() {
    RawExpression exp = RawExpressionBuilder.build("foo = ?", asArray(42));
    assertThat(exp.sql).isEqualTo("foo = ?");
  }

  @Test
  public void build_noExpand() {

    RawExpression exp = RawExpressionBuilder.buildSingle("foo = ? and bar = any(?)", asArray(44, asList(42, 43)));
    assertThat(exp.sql).isEqualTo("foo = ? and bar = any(?)");
  }

  @Test
  public void build_expand() {

    RawExpression exp = RawExpressionBuilder.build("foo in (?) and bar in (?2)", asArray(44, asList(42, 43)));
    assertThat(exp.sql).isEqualTo("foo in (?) and bar in (?,?)");
    assertThat(exp.values).contains(44, 42, 43);
  }

  @Test
  public void build_expand2() {

    RawExpression exp = RawExpressionBuilder.build("foo in (?) and bar in (?2) (?3)", asArray(44, asList(42, 43), asList(91, 92, 93)));
    assertThat(exp.sql).isEqualTo("foo in (?) and bar in (?,?) (?,?,?)");
    assertThat(exp.values).containsExactly(44, 42, 43, 91, 92, 93);
  }

  @Test
  public void build_expand3() {

    RawExpression exp = RawExpressionBuilder.build("foo in (?) and bar in (?2) (?3) and ?", asArray(44, asList(42, 43), asList(91, 92, 93), 87));
    assertThat(exp.sql).isEqualTo("foo in (?) and bar in (?,?) (?,?,?) and ?");
    assertThat(exp.values).containsExactly(44, 42, 43, 91, 92, 93, 87);
  }

  private Object[] asArray(Object... values) {
    return values;
  }
}
