package io.ebeaninternal.server.profile;

import io.ebean.meta.MetaQueryMetric;
import io.ebeaninternal.metric.QueryPlanCollector;

import java.util.ArrayList;
import java.util.List;

class DQueryPlanCollector implements QueryPlanCollector {

  private final boolean reset;

  private final List<MetaQueryMetric> list = new ArrayList<>();

  DQueryPlanCollector(boolean reset) {
    this.reset = reset;
  }

  @Override
  public boolean isReset() {
    return reset;
  }

  @Override
  public void add(MetaQueryMetric stats) {
    list.add(stats);
  }

  @Override
  public List<MetaQueryMetric> complete() {
    return list;
  }
}
