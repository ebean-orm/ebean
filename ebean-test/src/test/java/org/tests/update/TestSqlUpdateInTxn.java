package org.tests.update;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.idkeys.db.AuditLog;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSqlUpdateInTxn extends BaseTestCase {

  @Test
  public void testSqlTrim() {

    String sql = "  this\nis\ntrimmed ";

    SqlUpdate sqlUpdate = DB.sqlUpdate(sql);
    assertThat(sqlUpdate.getSql()).isEqualTo(sql.trim());
  }

  @Test
  public void test_twoWordSql() {

    if (isH2()) {
      // 2 word syntax ... means no automatic determination of the type of change
      // and table effected (so no automatic L2 cache invalidation)
      String sql = "CHECKPOINT SYNC";

      SqlUpdate sqlUpdate = DB.sqlUpdate(sql);
      sqlUpdate.execute();

      assertThat(sqlUpdate.getSql()).isEqualTo(sql.trim());
    }
  }

  @Test
  public void testExecute_inTransaction_withBatch() {

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      SqlUpdate sqlUpdate = DB.sqlUpdate("update audit_log set description = description where id = ?")
        .setParameter(1, 999999);

      int row = sqlUpdate.execute();
      // update statement using JDBC batch so not executed yet
      assertThat(row).isEqualTo(-1);

      sqlUpdate.setParameter(1, 999998);
      sqlUpdate.execute();

      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertSql(sql.get(0)).contains("update audit_log set description = description where id = ?");
    assertSqlBind(sql, 1, 2);
  }

  @Test
  public void testExecute_inTransaction_withoutBatch() {

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(false);

      SqlUpdate sqlUpdate = DB.sqlUpdate("update audit_log set description = description where id = ?")
        .setParameter(1, 999999);

      int row0 = sqlUpdate.execute();
      assertThat(row0).isEqualTo(0);

      sqlUpdate.setParameter(1, 999998);
      int row1 = sqlUpdate.execute();
      assertThat(row1).isEqualTo(0);

      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("update audit_log set description = description where id = ?; -- bind(999999)");
    assertSql(sql.get(1)).contains("update audit_log set description = description where id = ?; -- bind(999998)");
  }

  @Test
  public void testExecuteNow_inTransaction_withBatch() {

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      int row = DB.sqlUpdate("update audit_log set description = description where id = ?")
        .setParameter(1, 999999)
        .executeNow();

      // update statement executed even though JDBC batch mode is on
      assertThat(row).isEqualTo(0);

      transaction.commit();
    }
  }

  @Test
  public void testSqlUpdateWithWhitespace() {

    String sql = "   \nupdate audit_log \nset description = description \nwhere id = id";

    resetAllMetrics();

    SqlUpdate sqlUpdate = DB.sqlUpdate(sql).setLabel("auditLargeUpdate");
    sqlUpdate.execute();

    assertThat(sqlUpdate.getSql()).isEqualTo(sql.trim());
    assertThat(sqlUpdate.getGeneratedSql()).isEqualTo(sql.trim());

    List<MetaTimedMetric> sqlMetrics = sqlMetrics();
    assertThat(sqlMetrics).hasSize(1);
    assertThat(sqlMetrics.get(0).name()).isEqualTo("sql.update.auditLargeUpdate");
    assertThat(sqlMetrics.get(0).count()).isEqualTo(1);
  }

  @Test
  public void testBasic() {

    AuditLog otherLog = new AuditLog();
    otherLog.setDescription("foo");
    DB.save(otherLog);

    AuditLog log = new AuditLog();
    log.setDescription("foo");

    DB.save(log);

    AuditLog log2 = DB.find(AuditLog.class, log.getId());
    assertEquals("foo", log2.getDescription());

    final Long id = log2.getId();
    final String updateDml = "update audit_log set description = :desc where id = :id";
    final String updateModDml = "update audit_log set modified_description = :desc";

    SqlUpdate sqlUpdate = DB.sqlUpdate(updateDml);
    sqlUpdate.setParameter("desc", "foo2");
    sqlUpdate.setParameter("id", id);
    sqlUpdate.execute();

    SqlUpdate updateMod = DB.sqlUpdate(updateModDml);
    updateMod.setParameter("desc", "mod0");
    updateMod.execute();

    AuditLog log3 = DB.find(AuditLog.class, log.getId());
    assertEquals("foo2", log3.getDescription());
    assertEquals("mod0", log3.getModifiedDescription());

    DB.execute(() -> {
      SqlUpdate update = DB.sqlUpdate(updateDml);
      update.setParameter("desc", "foo3");
      update.setParameter("id", id);
      update.execute();

      SqlUpdate updateMod1 = DB.sqlUpdate(updateModDml);
      updateMod1.setParameter("desc", "mod1");
      updateMod1.execute();
    });

    AuditLog log4 = DB.find(AuditLog.class, log.getId());
    assertEquals("foo3", log4.getDescription());
    assertEquals("mod1", log4.getModifiedDescription());

    try (Transaction txn = DB.beginTransaction()) {
      SqlUpdate update = DB.sqlUpdate(updateDml);
      update.setParameter("desc", "foo4");
      update.setParameter("id", id);
      update.execute();

      updateMod = DB.sqlUpdate(updateModDml);
      updateMod.setParameter("desc", "mod2");
      updateMod.execute();

      txn.commit();
    }
    AuditLog log5 = DB.find(AuditLog.class, log.getId());
    assertEquals("foo4", log5.getDescription());
    assertEquals("mod2", log5.getModifiedDescription());

  }

}
