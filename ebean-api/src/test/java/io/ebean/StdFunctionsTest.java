package io.ebean;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.ebean.StdOperators.*;
import static org.assertj.core.api.Assertions.assertThat;

class StdFunctionsTest {

  final Query.Property<Number> amount = Query.Property.of("amount");
  final Query.Property<String> foo = Query.Property.of("foo");
  final Query.Property<String> bar = Query.Property.of("bar");

  @Test
  void testAvg() {
    assertThat(avg(foo).toString()).isEqualTo("avg(foo)");
  }

  @Test
  void testCount() {
    assertThat(count(foo).toString()).isEqualTo("count(foo)");
  }

  @Test
  void testMax() {
    assertThat(max(foo).toString()).isEqualTo("max(foo)");
  }

  @Test
  void testMin() {
    assertThat(min(foo).toString()).isEqualTo("min(foo)");
  }

  @Test
  void testSum() {
    assertThat(sum(amount).toString()).isEqualTo("sum(amount)");
  }

  @Test
  void testLower() {
    assertThat(lower(foo).toString()).isEqualTo("lower(foo)");
  }

  @Test
  void testUpper() {
    assertThat(upper(foo).toString()).isEqualTo("upper(foo)");
  }

  @Test
  void testConcat() {
    assertThat(concat(foo, "+").toString()).isEqualTo("concat(foo,'+')");
    assertThat(concat(foo, 42).toString()).isEqualTo("concat(foo,'42')");
    assertThat(concat(foo, LocalDate.of(2022, 9, 1)).toString()).isEqualTo("concat(foo,'2022-09-01')");
    assertThat(concat(foo, bar).toString()).isEqualTo("concat(foo,bar)");
    assertThat(concat(foo, ":", 42, bar).toString()).isEqualTo("concat(foo,':','42',bar)");
  }

  @Test
  void testCoalesce() {
    assertThat(coalesce(foo, bar).toString()).isEqualTo("coalesce(foo,bar)");
    assertThat(coalesce(foo, 42).toString()).isEqualTo("coalesce(foo,42)");
    assertThat(coalesce(foo, 0).toString()).isEqualTo("coalesce(foo,0)");
    assertThat(coalesce(foo, "apple").toString()).isEqualTo("coalesce(foo,'apple')");
    assertThat(coalesce(foo, "banana").toString()).isEqualTo("coalesce(foo,'banana')");
  }

}
