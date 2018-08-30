package io.ebeaninternal.server.query;

import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A QueryPlanlogger that prefixes "EXPLAIN " to the query. This works for Postgres, H2 and MySql.
 */
public class QueryPlanLoggerPostgres extends QueryPlanLogger {

  @Override
  public QueryPlanOutput logQueryPlan(Connection conn, CQueryPlan plan, BindCapture bind)  {

    try (PreparedStatement explainStmt = conn.prepareStatement("EXPLAIN " + plan.getSql())) {
      bind.prepare(explainStmt, conn);
      try (ResultSet rset = explainStmt.executeQuery()) {
        return readQueryPlanBasic(plan, bind, rset);
      }

    } catch (SQLException e) {
      queryPlanLog.error("Could not log query plan", e);
    }
    return null;
  }

}
