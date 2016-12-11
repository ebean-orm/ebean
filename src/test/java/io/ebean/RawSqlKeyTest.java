package io.ebean;


import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RawSqlKeyTest {

  @Test
  public void equals_when_sameParsedSql() {

    RawSql.Key key = RawSqlBuilder.parse("select id from customer").create().getKey();
    RawSql.Key key1 = RawSqlBuilder.parse("select id from customer").create().getKey();

    assertSame(key, key1);
  }

  @Test
  public void equals_when_diffParsedSql() {

    RawSql.Key key = RawSqlBuilder.parse("select id from customer").create().getKey();
    RawSql.Key key1 = RawSqlBuilder.parse("select name from customer").create().getKey();

    assertDifferent(key, key1);
  }

  @Test
  public void equals_when_sameColumnMapping() {

    RawSql.Key key = RawSqlBuilder.parse("select id from customer").columnMapping("id", "b").create().getKey();
    RawSql.Key key1 = RawSqlBuilder.parse("select id from customer").columnMapping("id", "b").create().getKey();

    assertSame(key, key1);
  }

  @Test
  public void equals_when_diffColumnMapping() {

    RawSql.Key key = RawSqlBuilder.parse("select a from customer").columnMapping("a", "b").create().getKey();
    RawSql.Key key1 = RawSqlBuilder.parse("select a from customer").columnMapping("a", "c").create().getKey();

    assertDifferent(key, key1);
  }

  @Test
  public void equals_when_parseToUnpased() {

    RawSql.Key key = RawSqlBuilder.parse("select a from customer").columnMapping("a", "b").create().getKey();
    RawSql.Key key1 = RawSqlBuilder.unparsed("select a from customer").columnMapping("a", "c").create().getKey();

    assertDifferent(key, key1);
  }

  private void assertSame(RawSql.Key key, RawSql.Key key1) {
    assertThat(key).isEqualTo(key1);
    assertThat(key.hashCode()).isEqualTo(key1.hashCode());
  }

  private void assertDifferent(RawSql.Key key, RawSql.Key key1) {
    assertThat(key).isNotEqualTo(key1);
    assertThat(key.hashCode()).isNotEqualTo(key1.hashCode());
  }
}
