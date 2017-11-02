package io.ebeaninternal.server.query;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RawSqlQueryPlanKeyTest {


  @Test
  public void test_equals_same_instance() {

    RawSqlQueryPlanKey key = key("select foo", true, true, "");
    assertThat(key).isEqualTo(key);
    assertThat(key.hashCode()).isEqualTo(key.hashCode());
  }

  @Test
  public void test_equals_diff_instance() {

    assertThat(key("select foo", true, true, "")).isEqualTo(key("select foo", true, true, ""));
    assertThat(key("select foo", true, true, "").hashCode()).isEqualTo(key("select foo", true, true, "").hashCode());
  }

  @Test
  public void test_notEquals_diff_sql() {

    assertThat(key("select foo", true, true, "")).isNotEqualTo(key("select bar", true, true, ""));
    assertThat(key("select foo", true, true, "").hashCode()).isNotEqualTo(key("select bar", true, true, "").hashCode());
  }

  @Test
  public void test_notEquals_diff_rawSqlFlag() {

    assertThat(key("select foo", true, true, "")).isNotEqualTo(key("select foo", false, true, ""));
    assertThat(key("select foo", true, true, "").hashCode()).isNotEqualTo(key("select foo", false, true, "").hashCode());
  }

  @Test
  public void test_notEquals_diff_rowNumberIncluded() {

    assertThat(key("select foo", true, true, "")).isNotEqualTo(key("select foo", true, false, ""));
    assertThat(key("select foo", true, true, "").hashCode()).isNotEqualTo(key("select foo", true, false, "").hashCode());
  }


  @Test
  public void test_notEquals_diff_logWhereSql() {

    assertThat(key("select foo", true, true, "")).isNotEqualTo(key("select foo", true, true, "a"));
    assertThat(key("select foo", true, true, "").hashCode()).isNotEqualTo(key("select foo", true, true, "a").hashCode());
  }

  private RawSqlQueryPlanKey key(String sql, boolean rawSql, boolean rowNumberIncluded, String logWhereSql) {
    return new RawSqlQueryPlanKey(sql, rawSql, rowNumberIncluded, logWhereSql);
  }

}
