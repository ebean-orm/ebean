package io.ebeaninternal.server.core;

import io.avaje.applog.AppLog;
import io.ebean.config.QueryPlanCapture;
import io.ebean.config.QueryPlanListener;
import io.ebean.meta.MetaQueryPlan;

import static java.lang.System.Logger.Level.INFO;

final class DefaultQueryPlanListener implements QueryPlanListener {

  static final QueryPlanListener INSTANT = new DefaultQueryPlanListener();

  private static final System.Logger log = AppLog.getLogger("io.ebean.QUERYPLAN");

  @Override
  public void process(QueryPlanCapture capture) {
    // better to log this in JSON form?
    String dbName = capture.database().name();
    for (MetaQueryPlan plan : capture.plans()) {
      log.log(INFO, "queryPlan  db:{0} label:{1} queryTimeMicros:{2} loc:{3} sql:{4} bind:{5} plan:{6}",
        dbName, plan.label(), plan.queryTimeMicros(), plan.profileLocation(),
        plan.sql(), plan.bind(), plan.plan());
    }
  }
}
