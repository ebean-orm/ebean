package io.ebean;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchConfigTest {

  @Test
  public void testLazy() {
    FetchConfig config = new FetchConfig().lazy();
    assertThat(config.getBatchSize()).isEqualTo(0);
  }

  @Test
  public void testLazy_withParameter() {
    FetchConfig config = new FetchConfig().lazy(50);
    assertThat(config.getBatchSize()).isEqualTo(50);
  }

  @Test
  public void testQuery() {
    FetchConfig config = new FetchConfig().query();
    assertThat(config.getBatchSize()).isEqualTo(100);
  }

  @Test
  public void testQuery_withParameter() {
    FetchConfig config = new FetchConfig().query(50);
    assertThat(config.getBatchSize()).isEqualTo(50);
  }

  @Test
  public void testQueryFirst() {
    FetchConfig config = new FetchConfig().queryFirst(50);
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
    assertSame(new FetchConfig(), new FetchConfig());
  }

  @Test
  public void testEquals_when_query_50_lazy_40() {
    assertSame(new FetchConfig().query(50), FetchConfig.ofQuery(50));
  }

  @Test
  public void testEquals_when_query_50_lazy() {
    assertSame(new FetchConfig().lazy(), FetchConfig.ofLazy());
  }

  @Test
  public void testEquals_when_query_50() {
    assertSame(new FetchConfig().query(50), new FetchConfig().query(50));
  }

  @Test
  public void testEquals_when_queryFirst_50_lazy_40() {
    assertSame(new FetchConfig().queryFirst(50).lazy(40), FetchConfig.ofLazy(40));
  }

  @Test
  public void testEquals_when_queryFirst_50_lazy() {
    assertSame(new FetchConfig().queryFirst(50).lazy(), new FetchConfig().queryFirst(50).lazy());
  }

  @Test
  public void testEquals_when_queryFirst_50() {
    assertSame(new FetchConfig().queryFirst(50), FetchConfig.ofQuery(50));
  }

  @Test
  public void testNotEquals_when_query_50() {
    assertDifferent(new FetchConfig().query(50), new FetchConfig().query(40));
  }

  @Test
  public void testNotEquals_when_query_50_lazy() {
    assertDifferent(new FetchConfig().query(50), new FetchConfig().query(50).lazy());
  }

  @Test
  public void testNotEquals_when_query_50_lazy_40() {
    assertDifferent(new FetchConfig().query(50), new FetchConfig().query(50).lazy(40));
  }

  @Test
  public void testNotEquals_when_queryFirst_50() {
    assertDifferent(new FetchConfig().queryFirst(50), new FetchConfig().queryFirst(40));
  }

  @Test
  public void testNotEquals_when_queryFirst_50_lazy() {
    assertDifferent(new FetchConfig().queryFirst(50), new FetchConfig().queryFirst(50).lazy());
  }

  @Test
  public void testNotEquals_when_queryFirst_50_lazy_40() {
    assertDifferent(new FetchConfig().queryFirst(50), new FetchConfig().queryFirst(50).lazy(40));
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
