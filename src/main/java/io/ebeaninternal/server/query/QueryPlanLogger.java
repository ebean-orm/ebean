package io.ebeaninternal.server.query;

import io.ebeaninternal.server.type.bindcapture.BindCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class QueryPlanLogger {

  static final Logger queryPlanLog = LoggerFactory.getLogger(QueryPlanLogger.class);

  public abstract DQueryPlanOutput logQueryPlan(Connection conn, CQueryPlan plan, BindCapture bind);

  DQueryPlanOutput readQueryPlan(CQueryPlan plan, BindCapture bind, ResultSet rset) throws SQLException {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
      sb.append(rset.getMetaData().getColumnLabel(i)).append("\t");
    }
    sb.setLength(sb.length() - 1);
    readPlanData(sb, rset);

    return createPlan(plan, bind.toString(), sb.toString());
  }

  DQueryPlanOutput createPlan(CQueryPlan plan, String bind, String planString) {
    return new DQueryPlanOutput(plan.getBeanType(), plan.getName(), plan.getSql(), bind, planString, plan.getProfileLocation());
  }

  DQueryPlanOutput readQueryPlanBasic(CQueryPlan plan, BindCapture bind, ResultSet rset) throws SQLException {
    StringBuilder sb = new StringBuilder();
    readPlanData(sb, rset);
    return createPlan(plan, bind.toString(), sb.toString().trim());
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
