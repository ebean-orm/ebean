package io.ebeaninternal.server.query;

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
      log.log(INFO, "queryPlan  db:{0} label:{1} queryTimeMicros:{2} captureMicros:{3} whenCaptured:{4} captureCount:{5} loc:{6} sql:{7} bind:{8} plan:{9}",
        dbName, plan.label(), plan.queryTimeMicros(), plan.captureMicros(), plan.whenCaptured(), plan.captureCount(),
        plan.profileLocation(), plan.sql(), plan.bind(), plan.plan());
    }
  }
}
