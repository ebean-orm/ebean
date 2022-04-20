package io.ebean.xtest.base;

import io.ebean.FetchConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchConfigTest {

  @Test
  public void testLazy() {
    FetchConfig config = FetchConfig.ofLazy();
    assertThat(config.getBatchSize()).isEqualTo(0);
  }

  @Test
  public void testLazy_withParameter() {
    FetchConfig config = FetchConfig.ofLazy(50);
    assertThat(config.getBatchSize()).isEqualTo(50);
  }

  @Test
  public void testQuery() {
    FetchConfig config = FetchConfig.ofQuery();
    assertThat(config.getBatchSize()).isEqualTo(100);
  }

  @Test
  public void testQuery_withParameter() {
    FetchConfig config = FetchConfig.ofQuery(50);
    assertThat(config.getBatchSize()).isEqualTo(50);
  }

  @Test
  public void testQueryFirst() {
    FetchConfig config = FetchConfig.ofQuery(50);
    assertThat(config.getBatchSize()).isEqualTo(50);
  }

  @Test
  public void testQueryAndLazy_withParameters() {
    FetchConfig config = FetchConfig.ofLazy(10);
    assertThat(config.getBatchSize()).isEqualTo(10);
  }

  @Test
  public void testQueryAndLazy() {
    FetchConfig config = FetchConfig.ofQuery(50);
    assertThat(config.getBatchSize()).isEqualTo(50);
  }


  @Test
  public void testEquals_when_noOptions() {
    assertSame(FetchConfig.ofDefault(), FetchConfig.ofDefault());
  }

  @Test
  public void testEquals_when_query_50_lazy_40() {
    assertSame(FetchConfig.ofQuery(50), FetchConfig.ofQuery(50));
  }

  @Test
  public void testEquals_when_query_50_lazy() {
    assertSame(FetchConfig.ofLazy(), FetchConfig.ofLazy());
  }

  @Test
  public void testEquals_when_query_50() {
    assertSame(FetchConfig.ofQuery(50), FetchConfig.ofQuery(50));
  }

  @Test
  public void testEquals_when_queryFirst_50_lazy_40() {
    assertSame(FetchConfig.ofLazy(40), FetchConfig.ofLazy(40));
  }

  @Test
  public void testEquals_when_queryFirst_50_lazy() {
    assertSame(FetchConfig.ofLazy(), FetchConfig.ofLazy());
  }

  @Test
  public void testEquals_when_queryFirst_50() {
    assertSame(FetchConfig.ofQuery(50), FetchConfig.ofQuery(50));
  }

  @Test
  public void testNotEquals_when_query_50() {
    assertDifferent(FetchConfig.ofQuery(50), FetchConfig.ofQuery(40));
  }

  @Test
  public void testNotEquals_when_query_50_lazy() {
    assertDifferent(FetchConfig.ofQuery(50), FetchConfig.ofLazy(50));
  }

  @Test
  public void testNotEquals_when_query_50_lazy_40() {
    assertDifferent(FetchConfig.ofQuery(50), FetchConfig.ofLazy(40));
  }

  @Test
  public void testNotEquals_when_queryFirst_50() {
    assertDifferent(FetchConfig.ofQuery(50), FetchConfig.ofQuery(40));
  }

  @Test
  public void testNotEquals_when_queryFirst_50_lazy() {
    assertDifferent(FetchConfig.ofQuery(50), FetchConfig.ofLazy());
  }

  @Test
  public void testNotEquals_when_queryFirst_50_lazy_40() {
    assertDifferent(FetchConfig.ofQuery(50), FetchConfig.ofLazy(40));
  }

  void assertDifferent(FetchConfig v1, FetchConfig v2) {
    assertThat(v1).isNotEqualTo(v2);
    assertThat(v1.hashCode()).isNotEqualTo(v2.hashCode());
  }

  void assertSame(FetchConfig v1, FetchConfig v2) {
    assertThat(v1).isEqualTo(v2);
    assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
  }
}
