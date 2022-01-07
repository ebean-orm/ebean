
package io.ebeaninternal.server.query;

import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.type.bindcapture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * A QueryPlanLogger for DB2.
 * 
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
  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    try (Statement stmt = conn.createStatement()) {
      int queryNo = rnd.nextInt(Integer.MAX_VALUE);
      try (PreparedStatement explainStmt = conn
          .prepareStatement("EXPLAIN PLAN SET QUERYNO=" + queryNo + " FOR " + plan.getSql())) {
        bind.prepare(explainStmt, conn);
        explainStmt.execute();
      }

      try (ResultSet rset = stmt.executeQuery("select * from EXPLAIN_STATEMENT where QUERYNO=" + queryNo)) {
        return readQueryPlan(plan, bind, rset);
      }
    } catch (SQLException e) {
      CoreLog.log.warn("Could not log query plan", e);
      return null;
    }
  }

}
