package io.ebeaninternal.server.persist;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TrimLogSqlTest {

  @Test
  public void trim() {
    assertThat(TrimLogSql.trim("hello")).isEqualTo("hello");
    assertThat(TrimLogSql.trim("hello\nthere")).isEqualTo("hello\\n there");
    assertThat(TrimLogSql.trim("a\nb\nc\nd")).isEqualTo("a\\n b\\n c\\n d");
    assertThat(TrimLogSql.trim("a\n\nb")).isEqualTo("a\\n \\n b");
    assertThat(TrimLogSql.trim("\n\n\n")).isEqualTo("\\n \\n \\n ");
  }

}
