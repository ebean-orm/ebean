package com.avaje.tests.basic.encrypt;

import java.sql.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Update;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.EBasicEncrypt;

public class TestEncrypt extends BaseTestCase {

  @Test
  public void test() {

    Update<EBasicEncrypt> deleteAll = Ebean.createUpdate(EBasicEncrypt.class, "delete from EBasicEncrypt");
    deleteAll.execute();
    
    EBasicEncrypt e = new EBasicEncrypt();
    e.setName("testname");
    e.setDescription("testdesc");
    e.setDob(new Date(System.currentTimeMillis() - 100000));

    Ebean.save(e);

    Date earlyDob = new Date(System.currentTimeMillis() - 500000);

    List<EBasicEncrypt> qlList = Ebean
        .createQuery(EBasicEncrypt.class, "where description like :d and dob >= :dob")
        .setParameter("d", "testde%").setParameter("dob", earlyDob).findList();

    Assert.assertTrue(qlList.size() > 0);

    qlList = Ebean
        .createQuery(EBasicEncrypt.class, "find e (id, description) where description = :d")
        .setParameter("d", "testdesc").findList();

    Assert.assertTrue(qlList.size() == 1);

    SqlQuery q = Ebean.createSqlQuery("select * from e_basicenc where id = :id");
    q.setParameter("id", e.getId());

    SqlRow row = q.findUnique();
    String name = row.getString("name");
    Object desc = row.get("description");
    System.out.println("SqlRow: " + name + " " + desc);

    EBasicEncrypt e1 = Ebean.find(EBasicEncrypt.class, e.getId());

    String desc1 = e1.getDescription();
    System.out.println("Decrypted: " + desc1 + "  " + e1.getDob());

    e1.setName("testmod");
    e1.setDescription("moddesc");

    Ebean.save(e1);

    EBasicEncrypt e2 = Ebean.find(EBasicEncrypt.class, e.getId());

    String desc2 = e2.getDescription();
    System.out.println("moddesc=" + desc2);

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    DbEncrypt dbEncrypt = server.getDatabasePlatform().getDbEncrypt();

    if (dbEncrypt == null) {
      // can not test the where clause
      System.out.println("TestEncrypt: Not testing where clause as no DbEncrypt");

    } else {

      List<EBasicEncrypt> list = Ebean.find(EBasicEncrypt.class).where()
          .eq("description", "moddesc").findList();

      Assert.assertEquals(1, list.size());

      list = Ebean.find(EBasicEncrypt.class).where().startsWith("description", "modde").findList();

      Assert.assertEquals(1, list.size());

      list = Ebean.createQuery(EBasicEncrypt.class, "where description like :d")
          .setParameter("d", "modde%").findList();

      Assert.assertNotNull(list);
    }
  }

}
