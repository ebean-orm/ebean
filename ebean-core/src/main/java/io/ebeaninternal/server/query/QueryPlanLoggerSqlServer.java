package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A QueryPlanLogger for SqlServer. It will return the plan as XML, which can be opened in
 * Microsoft SQL Server Management Studio.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class QueryPlanLoggerSqlServer extends QueryPlanLogger {

  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("set statistics xml on");
      stmt.execute("begin transaction");
      try (PreparedStatement explainStmt = conn.prepareStatement(plan.getSql())) {
        bind.prepare(explainStmt, conn);

        try (ResultSet rset = explainStmt.executeQuery()) {
          // unfortunately, this will execute the query, so we execute this in a transaction
        }
        stmt.execute("rollback transaction");
        String xml = null;
        if (explainStmt.getMoreResults()) {
          try (ResultSet rset = explainStmt.getResultSet()) {
            if (rset.next()) {
              xml = rset.getString(1);
            }
          }
        }
        return createPlan(plan, bind.toString(), xml);

      } catch (SQLException e) {
        queryPlanLog.error("Could not log query plan", e);
        return null;
      } finally {
        stmt.execute("set statistics xml off");
      }
    } catch (SQLException e) {
      queryPlanLog.error("Could not log query plan", e);
      return null;
    }
  }

}
