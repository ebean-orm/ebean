
package io.ebeaninternal.server.query;

import io.ebean.util.IOUtils;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import static java.lang.System.Logger.Level.WARNING;

/**
 * A QueryPlanLogger for DB2.
 * <p>
 * To use query plan capturing, you have to install the explain tables with
 * <code>SYSPROC.SYSINSTALLOBJECTS( 'EXPLAIN', 'C' , '', CURRENT SCHEMA )</code>.
 * To do this in a repeatable script, you may use this statement:
 *
 * <pre>
 * BEGIN
 * IF NOT EXISTS (SELECT * FROM SYSCAT.TABLES WHERE TABSCHEMA = CURRENT SCHEMA AND TABNAME = 'EXPLAIN_STREAM') THEN
 *    call SYSPROC.SYSINSTALLOBJECTS( 'EXPLAIN', 'C' , '', CURRENT SCHEMA );
 * END IF;
 * END
 * </pre>
 *
 * @author Roland Praml, FOCONIS AG
 */
public final class QueryPlanLoggerDb2 extends QueryPlanLogger {

  private Random rnd = new Random();

  private final String schemaPrefix;
  private final String schemaParam;

  private final String getQuerySql;

  public QueryPlanLoggerDb2(String schema) {
    if (schema == null || schema.isEmpty()) {
      this.schemaPrefix = "";
      this.schemaParam = "CURRENT SCHEMA";
    } else {
      this.schemaPrefix = schema + ".";
      this.schemaParam = "'" + schema + "'";
    }
    this.getQuerySql = readReasource("QueryPlanLoggerDb2.sql", schemaPrefix);
  }

  private String readReasource(String resName, String schemaPrefix) {
    try (InputStream stream = getClass().getResourceAsStream(resName)) {
      if (stream == null) {
        throw new IllegalStateException("Could not find resource " + resName);
      }
      BufferedReader reader = IOUtils.newReader(stream);
      StringBuilder sb = new StringBuilder();
      reader.lines().forEach(line -> sb.append(line).append('\n'));
      return sb.toString().replace("${SCHEMA}", schemaPrefix);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read resource " + resName, e);
    }
  }

  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    try (Statement stmt = conn.createStatement()) {

      // create explain tables if neccessary
      stmt.execute("BEGIN\n"
        + "IF NOT EXISTS (SELECT * FROM SYSCAT.TABLES WHERE TABSCHEMA = " + schemaParam + " AND TABNAME = 'EXPLAIN_STREAM') THEN\n"
        + "  call SYSPROC.SYSINSTALLOBJECTS( 'EXPLAIN', 'C' , '', " + schemaParam + " );\n"
        + "END IF;\n"
        + "END");

      conn.commit();
      try {
        int queryNo = rnd.nextInt(Integer.MAX_VALUE);

        try (PreparedStatement explainStmt = conn
          .prepareStatement("EXPLAIN PLAN SET QUERYNO=" + queryNo + " FOR " + plan.sql())) {
          bind.prepare(explainStmt, conn);
          explainStmt.execute();
        }
        try (PreparedStatement pstmt = conn.prepareStatement(getQuerySql)) {
          pstmt.setInt(1, queryNo);
          try (ResultSet rset = pstmt.executeQuery()) {
            return readQueryPlan(plan, bind, rset);
          }
        }
      } finally {
        // do not keep query plans in DB
        conn.rollback();
      }
    } catch (SQLException e) {
      CoreLog.log.log(WARNING, "Could not log query plan", e);
      return null;
    }
  }
}
