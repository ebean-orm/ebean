package io.ebeaninternal.server.core;

import io.ebean.config.QueryPlanCapture;
import io.ebean.config.QueryPlanListener;
import io.ebean.meta.MetaQueryPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultQueryPlanListener implements QueryPlanListener {

  static final QueryPlanListener INSTANT = new DefaultQueryPlanListener();

  private static final Logger log = LoggerFactory.getLogger("io.ebean.QUERYPLAN");

  @Override
  public void process(QueryPlanCapture capture) {
    // better to log this in JSON form?
    String dbName = capture.getDatabase().getName();
    for (MetaQueryPlan plan : capture.getPlans()) {
      log.info("queryPlan  db:{} label:{} queryTimeMicros:{} loc:{} sql:{} bind:{} plan:{}",
        dbName, plan.getLabel(), plan.getQueryTimeMicros(), plan.getProfileLocation(),
        plan.getSql(), plan.getBind(), plan.getPlan());
    }
  }
}
