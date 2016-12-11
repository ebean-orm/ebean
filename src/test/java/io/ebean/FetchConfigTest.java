package io.ebean;

import io.ebean.FetchConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchConfigTest {

  @Test
  public void testLazy() throws Exception {

    FetchConfig config = new FetchConfig().lazy();

    assertThat(config.getLazyBatchSize()).isEqualTo(0);
    assertThat(config.getQueryBatchSize()).isEqualTo(-1);
    assertThat(config.isQueryAll()).isEqualTo(false);
  }

  @Test
  public void testLazy_withParameter() throws Exception {

    FetchConfig config = new FetchConfig().lazy(50);

    assertThat(config.getLazyBatchSize()).isEqualTo(50);
    assertThat(config.getQueryBatchSize()).isEqualTo(-1);
    assertThat(config.isQueryAll()).isEqualTo(false);
  }

  @Test
  public void testQuery() throws Exception {

    FetchConfig config = new FetchConfig().query();

    assertThat(config.getLazyBatchSize()).isEqualTo(-1);
    assertThat(config.getQueryBatchSize()).isEqualTo(0);
    assertThat(config.isQueryAll()).isEqualTo(true);
  }

  @Test
  public void testQuery_withParameter() throws Exception {

    FetchConfig config = new FetchConfig().query(50);

    assertThat(config.getLazyBatchSize()).isEqualTo(-1);
    assertThat(config.getQueryBatchSize()).isEqualTo(50);
    assertThat(config.isQueryAll()).isEqualTo(true);
  }

  @Test
  public void testQueryFirst() throws Exception {

    FetchConfig config = new FetchConfig().queryFirst(50);

    assertThat(config.getLazyBatchSize()).isEqualTo(-1);
    assertThat(config.getQueryBatchSize()).isEqualTo(50);
    assertThat(config.isQueryAll()).isEqualTo(false);
  }

  @Test
  public void testQueryAndLazy_withParameters() throws Exception {

    FetchConfig config = new FetchConfig().query(50).lazy(10);

    assertThat(config.getLazyBatchSize()).isEqualTo(10);
    assertThat(config.getQueryBatchSize()).isEqualTo(50);
    assertThat(config.isQueryAll()).isEqualTo(false);
  }

  @Test
  public void testQueryAndLazy() throws Exception {

    FetchConfig config = new FetchConfig().query(50).lazy();

    assertThat(config.getLazyBatchSize()).isEqualTo(0);
    assertThat(config.getQueryBatchSize()).isEqualTo(50);
    assertThat(config.isQueryAll()).isEqualTo(false);
  }


  @Test
  public void testEquals_when_noOptions() throws Exception {

    assertSame(new FetchConfig(), new FetchConfig());
  }

  @Test
  public void testEquals_when_query_50_lazy_40() throws Exception {

    assertSame(new FetchConfig().query(50).lazy(40), new FetchConfig().query(50).lazy(40));
  }

  @Test
  public void testEquals_when_query_50_lazy() throws Exception {

    assertSame(new FetchConfig().query(50).lazy(), new FetchConfig().query(50).lazy());
  }

  @Test
  public void testEquals_when_query_50() throws Exception {

    assertSame(new FetchConfig().query(50), new FetchConfig().query(50));
  }

  @Test
  public void testEquals_when_queryFirst_50_lazy_40() throws Exception {

    assertSame(new FetchConfig().queryFirst(50).lazy(40), new FetchConfig().queryFirst(50).lazy(40));
  }

  @Test
  public void testEquals_when_queryFirst_50_lazy() throws Exception {

    assertSame(new FetchConfig().queryFirst(50).lazy(), new FetchConfig().queryFirst(50).lazy());
  }

  @Test
  public void testEquals_when_queryFirst_50() throws Exception {

    assertSame(new FetchConfig().queryFirst(50), new FetchConfig().queryFirst(50));
  }

  @Test
  public void testNotEquals_when_query_50() throws Exception {

    assertDifferent(new FetchConfig().query(50), new FetchConfig().query(40));
  }

  @Test
  public void testNotEquals_when_query_50_lazy() throws Exception {

    assertDifferent(new FetchConfig().query(50), new FetchConfig().query(50).lazy());
  }

  @Test
  public void testNotEquals_when_query_50_lazy_40() throws Exception {

    assertDifferent(new FetchConfig().query(50), new FetchConfig().query(50).lazy(40));
  }

  @Test
  public void testNotEquals_when_queryFirst_50() throws Exception {

    assertDifferent(new FetchConfig().queryFirst(50), new FetchConfig().queryFirst(40));
  }

  @Test
  public void testNotEquals_when_queryFirst_50_lazy() throws Exception {

    assertDifferent(new FetchConfig().queryFirst(50), new FetchConfig().queryFirst(50).lazy());
  }

  @Test
  public void testNotEquals_when_queryFirst_50_lazy_40() throws Exception {

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
