package io.ebeaninternal.server.persist;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TrimLogSqlTest {

  @Test
  public void trim() throws Exception {

    assertThat(TrimLogSql.trim("hello")).isEqualTo("hello");
    assertThat(TrimLogSql.trim("hello\nthere")).isEqualTo("hello\\n there");
  }

}
