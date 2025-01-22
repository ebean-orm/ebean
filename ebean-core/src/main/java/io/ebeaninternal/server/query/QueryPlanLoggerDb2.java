
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

  private static final String GET_PLAN_TEMPLATE = readReasource("QueryPlanLoggerDb2.sql");

  private static final String CREATE_TEMPLATE = "BEGIN\n"
    + "IF NOT EXISTS (SELECT * FROM SYSCAT.TABLES WHERE TABSCHEMA = ${SCHEMA} AND TABNAME = 'EXPLAIN_STREAM') THEN\n"
    + "  CALL SYSPROC.SYSINSTALLOBJECTS( 'EXPLAIN', 'C' , '', ${SCHEMA} );\n"
    + "END IF;\n"
    + "END";

  public QueryPlanLoggerDb2(String opts) {
    Map<String, String> map = StringHelper.delimitedToMap(opts, ";", "=");
    create = !"false" .equals(map.get("create")); // default is create
    String schema = map.get("schema"); // should be null or SYSTOOLS
    if (schema == null || schema.isEmpty()) {
      this.schema = null;
    } else {
      this.schema = schema.toUpperCase();
    }
  }

  private static String readReasource(String resName) {
    try (InputStream stream = QueryPlanLoggerDb2.class.getResourceAsStream(resName)) {
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
        // create explain tables if neccessary
        if (schema == null) {
          stmt.execute(CREATE_TEMPLATE.replace("${SCHEMA}", "CURRENT USER"));
        } else {
          stmt.execute(CREATE_TEMPLATE.replace("${SCHEMA}", "'" + schema + "'"));
        }
        conn.commit();
      }

      try {
        int queryNo = rnd.nextInt(Integer.MAX_VALUE);

        String sql = "EXPLAIN PLAN SET QUERYNO = " + queryNo + " FOR " + plan.sql();
        try (PreparedStatement explainStmt = conn.prepareStatement(sql)) {
          bind.prepare(explainStmt, conn);
          explainStmt.execute();
        }

        sql = schema == null
          ? GET_PLAN_TEMPLATE.replace("${SCHEMA}", conn.getMetaData().getUserName().toUpperCase())
          : GET_PLAN_TEMPLATE.replace("${SCHEMA}", schema);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setInt(1, queryNo);
          try (ResultSet rset = pstmt.executeQuery()) {
            return readQueryPlan(plan, bind, rset);
          }
        }
      } finally {
        conn.rollback(); // do not keep query plans in DB
      }
    } catch (SQLException e) {
      CoreLog.log.log(WARNING, "Could not log query plan", e);
      return null;
    }
  }
}
