package io.ebean.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateFormulaContextTest {

  @Test
  void defaultContext() {
    var defaultContext = AggregateFormulaContext.builder().build();
    for (String aggFunction : List.of("count", "max", "min", "avg", "sum", "group_concat", "string_agg", "listagg")) {
      assertThat(defaultContext.isAggregate(aggFunction)).isTrue();
    }
    for (String c : List.of("concat", "group_concat", "string_agg", "listagg")) {
      assertThat(defaultContext.isConcat(c)).isTrue();
    }
    for (String c : List.of("count")) {
      assertThat(defaultContext.isCount(c)).isTrue();
    }

    assertThat(defaultContext.isConcat("junk")).isFalse();
    assertThat(defaultContext.isCount("junk")).isFalse();
    assertThat(defaultContext.isAggregate("junk")).isFalse();
  }

  @Test
  void overrideAggregateFunctions() {
    AggregateFormulaContext mySum = AggregateFormulaContext.builder()
      .aggregateFunctions(Set.of("my_sum"))
      .build();

    assertThat(mySum.isAggregate("my_sum")).isTrue();
    assertThat(mySum.isAggregate("avg")).isFalse();
    assertThat(mySum.isCount("count")).isTrue();
    assertThat(mySum.isConcat("group_concat")).isTrue();
  }

  @Test
  void overrideConcatFunctions() {
    AggregateFormulaContext myConcat = AggregateFormulaContext.builder()
      .concatFunctions(Set.of("my_concat"))
      .build();

    assertThat(myConcat.isAggregate("avg")).isTrue();
    assertThat(myConcat.isCount("count")).isTrue();
    assertThat(myConcat.isConcat("group_concat")).isFalse();
    assertThat(myConcat.isConcat("my_concat")).isTrue();
  }

  @Test
  void overrideCountFunctions() {
    AggregateFormulaContext myCount = AggregateFormulaContext.builder()
      .countFunctions(Set.of("my_count"))
      .build();

    assertThat(myCount.isAggregate("avg")).isTrue();
    assertThat(myCount.isCount("count")).isFalse();
    assertThat(myCount.isCount("my_count")).isTrue();
    assertThat(myCount.isConcat("group_concat")).isTrue();
  }
}
