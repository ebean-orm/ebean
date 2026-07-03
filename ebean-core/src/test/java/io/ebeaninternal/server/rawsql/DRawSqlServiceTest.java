package io.ebeaninternal.server.rawsql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DRawSqlServiceTest {

  private final DRawSqlService dRawSqlService = new DRawSqlService();

  @Test
  public void combine() {

    assertEquals("mycol", dRawSqlService.combine(null, null, "mycol"));
    assertEquals("mytable.mycol", dRawSqlService.combine(null, "mytable", "mycol"));
    assertEquals("myschema.mytable.mycol", dRawSqlService.combine("myschema", "mytable", "mycol"));
    assertEquals("myschema.mycol", dRawSqlService.combine("myschema", null, "mycol"));
  }

  @Test
  void withPlaceholders_where() {
    String sql = "with cte as (select a, b from t ${where} group by a) select a, b from cte order by a";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.isParsed()).isTrue();
    assertThat(result.getPreFrom()).isEmpty();
    assertThat(result.getPreWhere()).isEqualTo("with cte as (select a, b from t");
    assertThat(result.getPreHaving()).isEqualTo("group by a) select a, b from cte order by a");
    assertThat(result.isAndWhereExpr()).isFalse();
  }

  @Test
  void withPlaceholders_andWhere() {
    String sql = "with cte as (select a from t where x=1 ${andWhere} group by a) select a from cte";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.getPreWhere()).isEqualTo("with cte as (select a from t where x=1");
    assertThat(result.isAndWhereExpr()).isTrue();
  }

  @Test
  void withPlaceholders_requiresPlaceholder() {
    assertThatThrownBy(() -> DRawSqlParser.parseAsTemplate("select a from t"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("${where}");
  }

  @Test
  void withPlaceholders_havingOnly_noWherePlaceholder() {
    String sql = "select a, sum(b) as total from t group by a ${having} order by a";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.getPreWhere()).isEqualTo("select a, sum(b) as total from t group by a");
    assertThat(result.getPreHaving()).isNull();
    assertThat(result.isAndHavingExpr()).isFalse();
    // trailing static SQL after the placeholder is preserved and emitted after any dynamic having.
    // There is no ${orderBy}/${andOrderBy} placeholder so no dynamic order-by injection point exists -
    // the static text is carried as preOrderBy and orderBy remains null/unused (getOrderByPrefix()
    // falls back to its "order by" default but that value is never used - the gating in
    // CQueryBuilderRawSql.orderBy() means no dynamic order by is ever appended in this case).
    assertThat(result.getOrderBy()).isNull();
    assertThat(result.getPreOrderBy()).isEqualTo("order by a");
    assertThat(result.isOrderByPlaceholder()).isFalse();
  }

  @Test
  void withPlaceholders_andHavingOnly_noWherePlaceholder() {
    String sql = "select a, sum(b) as total from t group by a having total > 0 ${andHaving} order by a";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.getPreWhere()).isEqualTo("select a, sum(b) as total from t group by a having total > 0");
    assertThat(result.getPreHaving()).isNull();
    assertThat(result.isAndHavingExpr()).isTrue();
    assertThat(result.getPreOrderBy()).isEqualTo("order by a");
    assertThat(result.isOrderByPlaceholder()).isFalse();
  }

  @Test
  void withPlaceholders_whereAndHaving_bothPresent() {
    String sql = "select a, sum(b) as total from t ${where} group by a ${having} order by a";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.getPreWhere()).isEqualTo("select a, sum(b) as total from t");
    assertThat(result.getPreHaving()).isEqualTo("group by a");
    // no data loss - trailing "order by a" preserved and emitted after the dynamic having clause
    assertThat(result.getPreOrderBy()).isEqualTo("order by a");
    assertThat(result.isOrderByPlaceholder()).isFalse();
  }

  @Test
  void withPlaceholders_orderBy() {
    String sql = "select a, b from t ${where} ${orderBy}";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.getPreWhere()).isEqualTo("select a, b from t");
    assertThat(result.getPreHaving()).isEmpty();
    assertThat(result.getOrderByPrefix()).isEqualTo("order by");
    assertThat(result.getOrderBy()).isNull();
    assertThat(result.isOrderByPlaceholder()).isTrue();
  }

  @Test
  void withPlaceholders_andOrderBy() {
    String sql = "select a, b from t ${where} order by a ${andOrderBy}";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.getPreWhere()).isEqualTo("select a, b from t");
    assertThat(result.getPreHaving()).isEqualTo("order by a");
    assertThat(result.getOrderByPrefix()).isEqualTo(",");
    assertThat(result.getOrderBy()).isNull();
    assertThat(result.isOrderByPlaceholder()).isTrue();
  }

  @Test
  void withPlaceholders_whereHavingAndOrderBy_allThreePresent() {
    String sql = "select a, sum(b) as total from t ${where} group by a ${having} ${orderBy}";
    SpiRawSql.Sql result = DRawSqlParser.parseAsTemplate(sql);

    assertThat(result.getPreWhere()).isEqualTo("select a, sum(b) as total from t");
    assertThat(result.getPreHaving()).isEqualTo("group by a");
    assertThat(result.getPreOrderBy()).isEmpty();
    assertThat(result.getOrderByPrefix()).isEqualTo("order by");
    assertThat(result.isOrderByPlaceholder()).isTrue();
  }
}
