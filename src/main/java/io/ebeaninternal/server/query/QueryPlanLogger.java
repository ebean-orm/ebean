package io.ebeaninternal.server.query;

import io.ebean.metric.TimedMetric;
import io.ebeaninternal.api.ExtraMetrics;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.type.bindcapture.BindCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class QueryPlanLogger {

  static final Logger queryPlanLog = LoggerFactory.getLogger(QueryPlanLogger.class);

  private final TimedMetric timeCollection;

  private final TimedMetric timeBindCapture;

  protected QueryPlanLogger(ExtraMetrics extraMetrics) {
    this.timeCollection = extraMetrics.getPlanCollect();
    this.timeBindCapture = extraMetrics.getBindCapture();
  }

  abstract DQueryPlanOutput collect(Connection conn, SpiQueryPlan plan, BindCapture bind);

  /**
   * Add timing for bind capture.
   */
  public void addBindTimeSince(long startNanos) {
    timeBindCapture.addSinceNanos(startNanos);
  }

  /**
   * Collect the DB query plan.
   */
  public DQueryPlanOutput collectQueryPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    long startNanos = System.nanoTime();
    try {
      return collect(conn, plan, bind);
    } finally {
      timeCollection.addSinceNanos(startNanos);
    }
  }

  DQueryPlanOutput readQueryPlan(SpiQueryPlan plan, BindCapture bind, ResultSet rset) throws SQLException {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
      sb.append(rset.getMetaData().getColumnLabel(i)).append("\t");
    }
    sb.setLength(sb.length() - 1);
    readPlanData(sb, rset);

    return createPlan(plan, bind.toString(), sb.toString());
  }

  DQueryPlanOutput createPlan(SpiQueryPlan plan, String bind, String planString) {
    return new DQueryPlanOutput(plan.getBeanType(), plan.getName(), plan.getSql(), bind, planString, plan.getProfileLocation());
  }

  DQueryPlanOutput readQueryPlanBasic(SpiQueryPlan plan, BindCapture bind, ResultSet rset) throws SQLException {
    StringBuilder sb = new StringBuilder();
    readPlanData(sb, rset);
    return createPlan(plan, bind.toString(), sb.toString().trim());
  }

  private void readPlanData(StringBuilder sb, ResultSet rset) throws SQLException {
    while (rset.next()) {
      sb.append('\n');
      for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
        sb.append(rset.getString(i)).append("\t");
      }
      sb.setLength(sb.length() - 1);
    }
  }
}
