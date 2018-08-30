package io.ebeaninternal.server.query;

import io.ebeaninternal.server.type.bindcapture.BindCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class QueryPlanLogger {

  static final Logger queryPlanLog = LoggerFactory.getLogger(QueryPlanLogger.class);

  public abstract QueryPlanOutput logQueryPlan(Connection conn, CQueryPlan plan, BindCapture bind);

  QueryPlanOutput readQueryPlan(CQueryPlan plan, BindCapture bind, ResultSet rset) throws SQLException {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
      sb.append(rset.getMetaData().getColumnLabel(i)).append("\t");
    }
    sb.setLength(sb.length() - 1);
    readPlanData(sb, rset);

    return new QueryPlanOutput(plan.getSql(), bind.toString(), sb.toString());
  }

  QueryPlanOutput readQueryPlanBasic(CQueryPlan plan, BindCapture bind, ResultSet rset) throws SQLException {
    StringBuilder sb = new StringBuilder();
    readPlanData(sb, rset);
    return new QueryPlanOutput(plan.getSql(), bind.toString(), sb.toString().trim());
  }

  private void readPlanData(StringBuilder sb, ResultSet rset) throws SQLException {
    while (rset.next()) {
      sb.append('\n');
      for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
        sb.append(rset.getString(i)).append("\t");
      }
      sb.setLength(sb.length()-1);
    }
  }
}
