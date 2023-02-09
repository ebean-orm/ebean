
package io.ebeaninternal.server.query;

import io.ebean.util.IOUtils;
import io.ebean.util.StringHelper;
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
import java.util.Map;
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

  private final String schema;

  private final boolean create;

  private final String getPlanSql;

  public QueryPlanLoggerDb2(String opts) {
    String template = readReasource("QueryPlanLoggerDb2.sql");
    Map<String, String> map = StringHelper.delimitedToMap(opts, ";", "=");
    String schema = map.get("schema");
    create = !"false" .equals(map.get("create")); // default is create
    if (schema == null || schema.isEmpty()) {
      this.schema = null;
      this.getPlanSql = template.replace("${SCHEMA}", "");
    } else {
      this.schema = schema;
      this.getPlanSql = template.replace("${SCHEMA}", schema + ".");
    }
  }

  private String readReasource(String resName) {
    try (InputStream stream = getClass().getResourceAsStream(resName)) {
      if (stream == null) {
        throw new IllegalStateException("Could not find resource " + resName);
      }
      BufferedReader reader = IOUtils.newReader(stream);
      StringBuilder sb = new StringBuilder();
      reader.lines().forEach(line -> sb.append(line).append('\n'));
      return sb.toString();
    } catch (IOException e) {
      throw new IllegalStateException("Could not read resource " + resName, e);
    }
  }

  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    try (Statement stmt = conn.createStatement()) {
      if (create) {
        if (schema == null) {
          // create explain tables if neccessary
          stmt.execute("BEGIN\n"
            + "IF NOT EXISTS (SELECT * FROM SYSCAT.TABLES WHERE TABSCHEMA = CURRENT USER AND TABNAME = 'EXPLAIN_STREAM') THEN\n"
            + "  call SYSPROC.SYSINSTALLOBJECTS( 'EXPLAIN', 'C' , '', CURRENT USER );\n"
            + "END IF;\n"
            + "END");
        } else {
          stmt.execute("BEGIN\n"
            + "IF NOT EXISTS (SELECT * FROM SYSCAT.TABLES WHERE TABSCHEMA = '" + schema + "' AND TABNAME = 'EXPLAIN_STREAM') THEN\n"
            + "  call SYSPROC.SYSINSTALLOBJECTS( 'EXPLAIN', 'C' , '', '" + schema + "' );\n"
            + "END IF;\n"
            + "END");
        }
        conn.commit();
      }
      String oldSchema = null;
      try {
        int queryNo = rnd.nextInt(Integer.MAX_VALUE);

        try (PreparedStatement explainStmt = conn
          .prepareStatement("EXPLAIN PLAN SET QUERYNO=" + queryNo + " FOR " + plan.sql())) {
          bind.prepare(explainStmt, conn);
          explainStmt.execute();
        }
        if (schema == null) {
          oldSchema = conn.getSchema();
          stmt.execute("set schema current user");
        }
        try (PreparedStatement pstmt = conn.prepareStatement(getPlanSql)) {
          pstmt.setInt(1, queryNo);
          try (ResultSet rset = pstmt.executeQuery()) {
            return readQueryPlan(plan, bind, rset);
          }
        }
      } finally {
        if (oldSchema != null) {
          conn.setSchema(oldSchema);
        }
        // do not keep query plans in DB
        conn.rollback();
      }
    } catch (SQLException e) {
      CoreLog.log.log(WARNING, "Could not log query plan", e);
      return null;
    }
  }
}
