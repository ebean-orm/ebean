package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.meta.MetaTimedMetric;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;
import org.tests.idkeys.db.AuditLog;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSqlUpdateInTxn extends BaseTestCase {

  @Test
  public void testSqlTrim() {

    String sql = "  this\nis\ntrimmed ";

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
    assertThat(sqlUpdate.getSql()).isEqualTo(sql.trim());
  }

  @Test
  public void test_twoWordSql() {

    if (isH2()) {
      // 2 word syntax ... means no automatic determination of the type of change
      // and table effected (so no automatic L2 cache invalidation)
      String sql = "CHECKPOINT SYNC";

      SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
      sqlUpdate.execute();

      assertThat(sqlUpdate.getSql()).isEqualTo(sql.trim());
    }
  }

  @Test
  public void testExecute_inTransaction_withBatch() {

    LoggedSqlCollector.start();

    try (Transaction transaction = Ebean.beginTransaction()) {
      transaction.setBatchMode(true);

      SqlUpdate sqlUpdate = Ebean.createSqlUpdate("update audit_log set description = description where id = ?")
        .setParameter(1, 999999);

      int row = sqlUpdate.execute();
      // update statement using JDBC batch so not executed yet
      assertThat(row).isEqualTo(-1);

      sqlUpdate.setParameter(1, 999998);
      sqlUpdate.execute();

      transaction.commit();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("update audit_log set description = description where id = ?");
    assertSqlBind(sql, 1, 2);
  }

  @Test
  public void testExecute_inTransaction_withoutBatch() {

    LoggedSqlCollector.start();

    try (Transaction transaction = Ebean.beginTransaction()) {
      transaction.setBatchMode(false);

      SqlUpdate sqlUpdate = Ebean.createSqlUpdate("update audit_log set description = description where id = ?")
        .setParameter(1, 999999);

      int row0 = sqlUpdate.execute();
      assertThat(row0).isEqualTo(0);

      sqlUpdate.setParameter(1, 999998);
      int row1 = sqlUpdate.execute();
      assertThat(row1).isEqualTo(0);

      transaction.commit();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("update audit_log set description = description where id = ?; -- bind(999999)");
    assertSql(sql.get(1)).contains("update audit_log set description = description where id = ?; -- bind(999998)");
  }

  @Test
  public void testExecuteNow_inTransaction_withBatch() {

    try (Transaction transaction = Ebean.beginTransaction()) {
      transaction.setBatchMode(true);

      int row = Ebean.createSqlUpdate("update audit_log set description = description where id = ?")
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

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql).setLabel("auditLargeUpdate");
    sqlUpdate.execute();

    assertThat(sqlUpdate.getSql()).isEqualTo(sql.trim());
    assertThat(sqlUpdate.getGeneratedSql()).isEqualTo(sql.trim());

    List<MetaTimedMetric> sqlMetrics = sqlMetrics();
    assertThat(sqlMetrics).hasSize(1);
    assertThat(sqlMetrics.get(0).getName()).isEqualTo("sql.update.auditLargeUpdate");
    assertThat(sqlMetrics.get(0).getCount()).isEqualTo(1);
  }

  @Test
  public void testBasic() {

    AuditLog otherLog = new AuditLog();
    otherLog.setDescription("foo");
    Ebean.save(otherLog);

    AuditLog log = new AuditLog();
    log.setDescription("foo");

    Ebean.save(log);

    AuditLog log2 = Ebean.find(AuditLog.class, log.getId());
    Assert.assertEquals("foo", log2.getDescription());

    final Long id = log2.getId();
    final String updateDml = "update audit_log set description = :desc where id = :id";
    final String updateModDml = "update audit_log set modified_description = :desc";

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate(updateDml);
    sqlUpdate.setParameter("desc", "foo2");
    sqlUpdate.setParameter("id", id);
    sqlUpdate.execute();

    SqlUpdate updateMod = Ebean.createSqlUpdate(updateModDml);
    updateMod.setParameter("desc", "mod0");
    updateMod.execute();

    AuditLog log3 = Ebean.find(AuditLog.class, log.getId());
    Assert.assertEquals("foo2", log3.getDescription());
    Assert.assertEquals("mod0", log3.getModifiedDescription());

    Ebean.execute(() -> {
      SqlUpdate update = Ebean.createSqlUpdate(updateDml);
      update.setParameter("desc", "foo3");
      update.setParameter("id", id);
      update.execute();

      SqlUpdate updateMod1 = Ebean.createSqlUpdate(updateModDml);
      updateMod1.setParameter("desc", "mod1");
      updateMod1.execute();
    });

    AuditLog log4 = Ebean.find(AuditLog.class, log.getId());
    Assert.assertEquals("foo3", log4.getDescription());
    Assert.assertEquals("mod1", log4.getModifiedDescription());


    Ebean.beginTransaction();
    try {
      SqlUpdate update = Ebean.createSqlUpdate(updateDml);
      update.setParameter("desc", "foo4");
      update.setParameter("id", id);
      update.execute();

      updateMod = Ebean.createSqlUpdate(updateModDml);
      updateMod.setParameter("desc", "mod2");
      updateMod.execute();

      Ebean.commitTransaction();
    } finally {
      Ebean.endTransaction();
    }
    AuditLog log5 = Ebean.find(AuditLog.class, log.getId());
    Assert.assertEquals("foo4", log5.getDescription());
    Assert.assertEquals("mod2", log5.getModifiedDescription());

  }

}
