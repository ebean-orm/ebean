package io.ebeaninternal.server.query;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RawSqlQueryPlanKeyTest {


  @Test
  public void test_equals_same_instance() {

    RawSqlQueryPlanKey key = key("select foo", true, "");
    assertThat(key).isEqualTo(key);
    assertThat(key.hashCode()).isEqualTo(key.hashCode());
  }

  @Test
  public void test_equals_diff_instance() {

    assertThat(key("select foo", true, "")).isEqualTo(key("select foo", true, ""));
    assertThat(key("select foo", true, "").hashCode()).isEqualTo(key("select foo", true, "").hashCode());
  }

  @Test
  public void test_notEquals_diff_sql() {

    assertThat(key("select foo", true, "")).isNotEqualTo(key("select bar", true, ""));
    assertThat(key("select foo", true, "").hashCode()).isNotEqualTo(key("select bar", true, "").hashCode());
  }

  @Test
  public void test_notEquals_diff_rawSqlFlag() {

    assertThat(key("select foo", true, "")).isNotEqualTo(key("select foo", false, ""));
    assertThat(key("select foo", true, "").hashCode()).isNotEqualTo(key("select foo", false, "").hashCode());
  }

  @Test
  public void test_notEquals_diff_logWhereSql() {

    assertThat(key("select foo", true, "")).isNotEqualTo(key("select foo", true, "a"));
    assertThat(key("select foo", true, "").hashCode()).isNotEqualTo(key("select foo", true, "a").hashCode());
  }

  private RawSqlQueryPlanKey key(String sql, boolean rawSql, String logWhereSql) {
    return new RawSqlQueryPlanKey(sql, rawSql, logWhereSql);
  }

}
