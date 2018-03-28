package io.ebeaninternal.server.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.ebeaninternal.server.type.DataBind;

/**
 * A QueryPlanlogger that prefixes "EXPLAIN " to the query. This works for Postgres, H2 and MySql.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class QueryPlanLoggerExplain extends QueryPlanLogger {

  @Override
  public void logQueryPlan(Connection conn, CQueryPlan plan, CQueryPredicates predicates)  {
    StringBuilder sb = new StringBuilder();
    sb.append("SQL: ").append(plan.getSql()).append('\n');
    try (PreparedStatement explainStmt = conn.prepareStatement("EXPLAIN " + plan.getSql())) {
      DataBind dataBind = plan.bindEncryptedProperties(explainStmt, conn);
      String bindLog = predicates.bind(dataBind);
      sb.append("Bindlog: ").append(bindLog).append('\n');

      // read the result set and output it as tab delimited text.
      try (ResultSet rset = explainStmt.executeQuery()) {
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
