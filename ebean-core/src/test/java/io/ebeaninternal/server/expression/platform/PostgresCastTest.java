package io.ebeaninternal.server.expression.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class PostgresCastTest {

  @Test
  public void cast() {

    assertThat(PostgresCast.cast(1)).isEqualTo("::integer");
    assertThat(PostgresCast.cast(1L)).isEqualTo("::bigint");
    assertThat(PostgresCast.cast(1.0D)).isEqualTo("::decimal");
    assertThat(PostgresCast.cast("")).isEqualTo("");
  }

  @Test
  public void cast1() {

    assertThat(PostgresCast.cast(1, true)).isEqualTo("::integer[]");
    assertThat(PostgresCast.cast(1L, true)).isEqualTo("::bigint[]");
    assertThat(PostgresCast.cast(1.0D, true)).isEqualTo("::decimal[]");
    assertThat(PostgresCast.cast("", true)).isEqualTo("");
  }

}
