package io.ebeaninternal.server.core;

import io.ebean.config.QueryPlanCapture;
import io.ebean.config.QueryPlanListener;
import io.ebean.meta.MetaQueryPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DefaultQueryPlanListener implements QueryPlanListener {

  static final QueryPlanListener INSTANT = new DefaultQueryPlanListener();

  private static final Logger log = LoggerFactory.getLogger("io.ebean.QUERYPLAN");

  @Override
  public void process(QueryPlanCapture capture) {
    // better to log this in JSON form?
    String dbName = capture.getDatabase().name();
    for (MetaQueryPlan plan : capture.getPlans()) {
      log.info("queryPlan  db:{} label:{} queryTimeMicros:{} loc:{} sql:{} bind:{} plan:{}",
        dbName, plan.label(), plan.queryTimeMicros(), plan.profileLocation(),
        plan.sql(), plan.bind(), plan.plan());
    }
  }
}
