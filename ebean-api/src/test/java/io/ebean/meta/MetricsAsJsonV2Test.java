package io.ebean.meta;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsAsJsonV2Test {

  @Test
  void writeV2_usesFamilyNamesAndTags() {
    ServerMetrics metrics = new FakeServerMetrics();
    StringBuilder sb = new StringBuilder();
    new MetricsAsJson(metrics).writeV2(sb);
    String json = sb.toString();

    assertThat(json).contains("\"db\":\"db1\"");
    // query metric -> ebean.query with kind/type/label tags
    assertThat(json).contains("\"name\":\"ebean.query\"");
    assertThat(json).contains("\"tags\":\"kind:orm,label:Customer.findList,type:Customer\"");
    // timed iud metric -> ebean.dml
    assertThat(json).contains("\"name\":\"ebean.dml\"");
    assertThat(json).contains("\"tags\":\"label:User.save\"");
    // count metric (l2n not specially mapped) -> ebean.other
    assertThat(json).contains("\"name\":\"ebean.other\"");
    assertThat(json).contains("\"tags\":\"label:l2n.Customer.hit\"");
  }

  @Test
  void write_v1_unchanged_usesFlatNames() {
    ServerMetrics metrics = new FakeServerMetrics();
    StringBuilder sb = new StringBuilder();
    new MetricsAsJson(metrics).write(sb);
    String json = sb.toString();

    assertThat(json).contains("\"name\":\"orm.Customer.findList\"");
    assertThat(json).contains("\"name\":\"iud.User.save\"");
    assertThat(json).doesNotContain("\"tags\"");
  }

  static final class FakeServerMetrics implements ServerMetrics {
    @Override
    public String name() {
      return "db1";
    }

    @Override
    public ServerMetricsAsJson asJson() {
      return new MetricsAsJson(this);
    }

    @Override
    public List<MetricData> asData() {
      return new java.util.ArrayList<>();
    }

    @Override
    public List<MetaTimedMetric> timedMetrics() {
      return new java.util.ArrayList<>(List.of(new FakeTimed("iud.User.save")));
    }

    @Override
    public List<MetaQueryMetric> queryMetrics() {
      return new java.util.ArrayList<>(List.of(new FakeQuery("orm.Customer.findList", Customer.class)));
    }

    @Override
    public List<MetaCountMetric> countMetrics() {
      return new java.util.ArrayList<>(List.of(new FakeCount("l2n.Customer.hit")));
    }
  }

  static class Customer {
  }

  static class FakeTimed implements MetaTimedMetric {
    private final String name;

    FakeTimed(String name) {
      this.name = name;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String location() {
      return null;
    }

    @Override
    public long count() {
      return 3;
    }

    @Override
    public long total() {
      return 30;
    }

    @Override
    public long max() {
      return 20;
    }

    @Override
    public long mean() {
      return 10;
    }

    @Override
    public boolean initialCollection() {
      return false;
    }
  }

  static final class FakeQuery extends FakeTimed implements MetaQueryMetric {
    private final Class<?> type;

    FakeQuery(String name, Class<?> type) {
      super(name);
      this.type = type;
    }

    @Override
    public Class<?> type() {
      return type;
    }

    @Override
    public String label() {
      return null;
    }

    @Override
    public String sql() {
      return null;
    }

    @Override
    public String hash() {
      return "h1";
    }
  }

  static final class FakeCount implements MetaCountMetric {
    private final String name;

    FakeCount(String name) {
      this.name = name;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public long count() {
      return 5;
    }
  }
}
