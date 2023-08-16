package io.ebeaninternal.server.expression;

import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InLiteralsTest {

  private static String using(Object value) {
    return using(value, Platform.GENERIC);
  }

  private static String using(Object value, Platform platform) {
    InLiterals litInt = InLiterals.of(value, platform);
    StringBuilder sb = new StringBuilder();
    litInt.append(sb, value);
    return sb.toString();
  }

  @Test
  void of_numbers() {
    assertThat(using(42)).isEqualTo("42");
    assertThat(using(42d)).isEqualTo("42.0");
    assertThat(using(42L)).isEqualTo("42");
    assertThat(using(new BigDecimal("42.34"))).isEqualTo("42.34");
    assertThat(using(new BigInteger("42"))).isEqualTo("42");
    assertThat(using(Integer.valueOf(42))).isEqualTo("42");
    assertThat(using(Long.valueOf(42))).isEqualTo("42");
  }

  @Test
  void of_str() {
    assertThat(using("hi")).isEqualTo("'hi'");
    assertThat(using("there")).isEqualTo("'there'");
  }

  @Test
  void of_uuid() {
    UUID uuid = UUID.randomUUID();
    assertThat(using(uuid)).isEqualTo("'" + uuid + "'");
  }

  @Test
  void of_localDate() {
    assertThat(using(LocalDate.of(2023, 3, 7))).isEqualTo("date '2023-03-07'");
  }

  @Test
  void of_localDate_mysql() {
    assertThat(using(LocalDate.of(2023, 3, 7), Platform.MYSQL)).isEqualTo("{d '2023-03-07'}");
  }
}
