package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A QueryPlanLogger that prefixes "EXPLAIN " to the query. This works for Postgres, H2 and MySql.
 */
public class QueryPlanLoggerExplain extends QueryPlanLogger {

  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    try (PreparedStatement explainStmt = conn.prepareStatement("EXPLAIN " + plan.getSql())) {
      bind.prepare(explainStmt, conn);
      try (ResultSet rset = explainStmt.executeQuery()) {
        return readQueryPlan(plan, bind, rset);
      }
    } catch (SQLException e) {
      queryPlanLog.error("Could not log query plan", e);
      return null;
    }
  }

}
