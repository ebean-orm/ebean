package com.avaje.tests.update;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.avaje.tests.idkeys.db.AuditLog;
import org.junit.Assert;
import org.junit.Test;

public class TestSqlUpdateInTxn extends BaseTestCase {

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

    SqlUpdate update = Ebean.createSqlUpdate(updateDml);
    update.setParameter("desc", "foo4");
    update.setParameter("id", id);
    update.execute();

    updateMod = Ebean.createSqlUpdate(updateModDml);
    updateMod.setParameter("desc", "mod2");
    updateMod.execute();

    Ebean.commitTransaction();

    AuditLog log5 = Ebean.find(AuditLog.class, log.getId());
    Assert.assertEquals("foo4", log5.getDescription());
    Assert.assertEquals("mod2", log5.getModifiedDescription());

  }

}
