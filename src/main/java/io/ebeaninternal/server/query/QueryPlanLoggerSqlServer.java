package io.ebeaninternal.server.query;

import io.ebean.meta.QueryPlanOutput;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A QueryPlanlogger for sqlserver. It will return the plan as XML, which can be opened in
 * Microsoft SQL Server Management Studio.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class QueryPlanLoggerSqlServer extends QueryPlanLogger {

  @Override
  public QueryPlanOutput logQueryPlan(Connection conn, CQueryPlan plan, BindCapture bind) {

    try (Statement stmt = conn.createStatement()) {
      stmt.execute("SET STATISTICS XML ON");
      try (PreparedStatement explainStmt = conn.prepareStatement(plan.getSql())) {
        bind.prepare(explainStmt, conn);

        try (ResultSet rset = explainStmt.executeQuery()) {
          // unfortunately, this will execute the
        }
        if (explainStmt.getMoreResults()) {
          try (ResultSet rset = explainStmt.getResultSet()) {
            StringBuilder sb = new StringBuilder();
            while (rset.next()) {
              sb.append("XML: ").append(rset.getString(1));
            }
            return createPlan(plan, bind.toString(), sb.toString());
          }
        }

      } catch (SQLException e) {
        queryPlanLog.error("Could not log query plan", e);

      } finally {
        stmt.execute("SET STATISTICS XML OFF");
      }
    } catch (SQLException e) {
      queryPlanLog.error("Could not log query plan", e);
    }
    return null;
  }

}
