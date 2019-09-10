
package io.ebeaninternal.server.query;

import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A QueryPlanlogger for oracle.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class QueryPlanLoggerOracle extends QueryPlanLogger {

  @Override
  public DQueryPlanOutput logQueryPlan(Connection conn, CQueryPlan plan, BindCapture bind) {

    try (Statement stmt = conn.createStatement()) {
      try (PreparedStatement explainStmt = conn.prepareStatement("EXPLAIN PLAN FOR " + plan.getSql())) {
        bind.prepare(explainStmt, conn);
        explainStmt.execute();
      }
      try (ResultSet rset = stmt.executeQuery("select plan_table_output from table(dbms_xplan.display())")) {
        return readQueryPlan(plan, bind, rset);
      }

    } catch (SQLException e) {
      queryPlanLog.error("Could not log query plan", e);
    }
    return null;
  }

}
