package io.ebeaninternal.server.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.ebeaninternal.server.type.DataBind;

/**
 * A QueryPlanlogger for oracle.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class QueryPlanLoggerOracle extends QueryPlanLogger {

  @Override
  public void logQueryPlan(Connection conn, CQueryPlan plan, CQueryPredicates predicates)  {
    StringBuilder sb = new StringBuilder();
    sb.append("SQL: ").append(plan.getSql()).append('\n');
    try (Statement stmt = conn.createStatement()) {
    try (PreparedStatement explainStmt = conn.prepareStatement("EXPLAIN PLAN FOR " + plan.getSql())) {
      DataBind dataBind = plan.bindEncryptedProperties(explainStmt, conn);
      String bindLog = predicates.bind(dataBind);
      sb.append("Bindlog: ").append(bindLog).append('\n');
      explainStmt.execute();
    }
      try (ResultSet rset = stmt.executeQuery("select plan_table_output from table(dbms_xplan.display())")) {
        for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
          sb.append(rset.getMetaData().getColumnLabel(i)).append("\t");
        }
        sb.setLength(sb.length()-1);
        while (rset.next()) {
          sb.append('\n');
          for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
            sb.append(rset.getString(i)).append("\t");
          }
          sb.setLength(sb.length()-1);
        }
        queryplanLog.debug(sb.toString());
      }

    } catch (SQLException e) {
      queryplanLog.error("Could not log query plan", e);
    }
  }

}
