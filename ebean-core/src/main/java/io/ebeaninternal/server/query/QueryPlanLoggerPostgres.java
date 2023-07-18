package io.ebeaninternal.server.query;

import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.lang.System.Logger.Level.WARNING;

/**
 * A QueryPlanLogger for Postgres that prefixes "EXPLAIN ANALYZE" to the query.
 */
public final class QueryPlanLoggerPostgres extends QueryPlanLogger {

  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    String explain = "explain (analyze, buffers) " + plan.sql();
    try (PreparedStatement explainStmt = conn.prepareStatement(explain)) {
      bind.prepare(explainStmt, conn);
      try (ResultSet rset = explainStmt.executeQuery()) {
        return readQueryPlanBasic(plan, bind, rset);
      }
    } catch (SQLException e) {
      CoreLog.log.log(WARNING, "Could not log query plan: " + explain, e);
      return null;
    }
  }
}
