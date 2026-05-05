package io.ebean.meta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryPlanInitTest {

  @Test
  void initialQueryPlanInit() {
    var init = new QueryPlanInit();
    assertThat(init.isEmpty()).isTrue();
    assertThat(init.isAll()).isFalse();
    assertThat(init.thresholdMicros()).isEqualTo(0L);
  }

  @Test
  void add_all() {
    var init = new QueryPlanInit();
    init.add("all", 57L);

    assertThat(init.isEmpty()).isFalse();
    assertThat(init.isAll()).isTrue();
    assertThat(init.thresholdMicros()).isEqualTo(57L);
  }

  @Test
  void addWithThresholds() {
    var init = new QueryPlanInit();
    init.thresholdMicros(1000);
    init.add("xOne", 0);
    init.add("xTwo", 2000L);

    assertThat(init.isEmpty()).isFalse();
    assertThat(init.isAll()).isFalse();
    assertThat(init.includeHash("xJunk")).isFalse();
    assertThat(init.includeHash("xOne")).isTrue();
    assertThat(init.includeHash("xTwo")).isTrue();
    assertThat(init.thresholdMicros("xJunk")).isEqualTo(1000L);
    assertThat(init.thresholdMicros("xOne")).isEqualTo(1000L);
    assertThat(init.thresholdMicros("xTwo")).isEqualTo(2000L);
  }
}
