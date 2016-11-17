package com.avaje.tests.basic.encrypt;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Update;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.EBasicEncrypt;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEncrypt extends BaseTestCase {


  @Test
  public void testQueryBind() {

    if (!isH2()) {
      // only run this on H2 - PGCrypto not happy on CI server
      return;
    }

    LoggedSqlCollector.start();
    Ebean.find(EBasicEncrypt.class)
      .where().startsWith("description", "Rob")
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("; --bind(****,Rob%)");
  }

  @Ignore
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

    SqlQuery q = Ebean.createSqlQuery("select * from e_basicenc where id = :id");
    q.setParameter("id", e.getId());

    SqlRow row = q.findUnique();
    row.getString("name");
    row.get("description");

    EBasicEncrypt e1 = Ebean.find(EBasicEncrypt.class, e.getId());

    e1.getDescription();

    e1.setName("testmod");
    e1.setDescription("moddesc");

    Ebean.save(e1);

    EBasicEncrypt e2 = Ebean.find(EBasicEncrypt.class, e.getId());

    e2.getDescription();

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
    }
  }

}
