package io.ebeaninternal.server.rawsql;


import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RawSqlKeyTest {

  private SpiRawSql.Key key(String sqlStatement) {
    return ((SpiRawSql) RawSqlBuilder.parse(sqlStatement).create()).getKey();
  }

  private SpiRawSql.Key key(RawSql rawSql) {
    return ((SpiRawSql)rawSql).getKey();
  }

  @Test
  public void equals_when_sameParsedSql() {

    SpiRawSql.Key key = key("select id from customer");
    SpiRawSql.Key key1 = key("select id from customer");

    assertSame(key, key1);
  }

  @Test
  public void equals_when_diffParsedSql() {

    SpiRawSql.Key key = key("select id from customer");
    SpiRawSql.Key key1 = key("select name from customer");

    assertDifferent(key, key1);
  }

  @Test
  public void equals_when_sameColumnMapping() {

    SpiRawSql.Key key = key(RawSqlBuilder.parse("select id from customer").columnMapping("id", "b").create());
    SpiRawSql.Key key1 = key(RawSqlBuilder.parse("select id from customer").columnMapping("id", "b").create());

    assertSame(key, key1);
  }

  @Test
  public void equals_when_diffColumnMapping() {

    SpiRawSql.Key key = key(RawSqlBuilder.parse("select a from customer").columnMapping("a", "b").create());
    SpiRawSql.Key key1 = key(RawSqlBuilder.parse("select a from customer").columnMapping("a", "c").create());

    assertDifferent(key, key1);
  }

  @Test
  public void equals_when_parseToUnpased() {

    SpiRawSql.Key key = key(RawSqlBuilder.parse("select a from customer").columnMapping("a", "b").create());
    SpiRawSql.Key key1 = key(RawSqlBuilder.unparsed("select a from customer").columnMapping("a", "c").create());

    assertDifferent(key, key1);
  }

  private void assertSame(SpiRawSql.Key key, SpiRawSql.Key key1) {
    assertThat(key).isEqualTo(key1);
    assertThat(key.hashCode()).isEqualTo(key1.hashCode());
  }

  private void assertDifferent(SpiRawSql.Key key, SpiRawSql.Key key1) {
    assertThat(key).isNotEqualTo(key1);
    assertThat(key.hashCode()).isNotEqualTo(key1.hashCode());
  }
}
