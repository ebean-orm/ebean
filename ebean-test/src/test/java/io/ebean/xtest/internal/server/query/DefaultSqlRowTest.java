package io.ebean.xtest.internal.server.query;

import io.ebeaninternal.server.query.DefaultSqlRow;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultSqlRowTest {

  @Test
  public void test_keyConversion() {

    DefaultSqlRow row = new DefaultSqlRow(16, 0.5f, "T", false);
    row.put("Foo", "hello");

    assertThat(row.containsKey("foo")).isTrue();
    assertThat(row.getString("foo")).isEqualTo("hello");
    assertThat(row.get("foo")).isEqualTo("hello");

    row.remove("foo");
    assertThat(row.isEmpty()).isTrue();
  }
}
