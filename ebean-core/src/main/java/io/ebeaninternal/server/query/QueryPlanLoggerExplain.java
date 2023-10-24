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
 * A QueryPlanLogger that prefixes "EXPLAIN " to the query. This works for Postgres, H2 and MySql.
 */
public final class QueryPlanLoggerExplain extends QueryPlanLogger {

  private final String prefix;

  public QueryPlanLoggerExplain(String prefix) {
    this.prefix = prefix;
  }

  @SuppressWarnings("all")
  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    try (PreparedStatement explainStmt = conn.prepareStatement(prefix + plan.sql())) {
      bind.prepare(explainStmt, conn);
      try (ResultSet rset = explainStmt.executeQuery()) {
        return readQueryPlan(plan, bind, rset);
      }
    } catch (SQLException e) {
      CoreLog.log.log(WARNING, "Could not log query plan", e);
      return null;
    }
  }

}
