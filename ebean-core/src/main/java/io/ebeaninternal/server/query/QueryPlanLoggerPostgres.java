package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A QueryPlanLogger for Postgres that prefixes "EXPLAIN ANALYZE" to the query.
 */
public class QueryPlanLoggerPostgres extends QueryPlanLogger {

  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    String explain = "explain analyze " + plan.getSql();
    try (PreparedStatement explainStmt = conn.prepareStatement(explain)) {
      bind.prepare(explainStmt, conn);
      try (ResultSet rset = explainStmt.executeQuery()) {
        return readQueryPlanBasic(plan, bind, rset);
      }
    } catch (SQLException e) {
      queryPlanLog.error("Could not log query plan: " + explain, e);
      return null;
    }
  }
}
