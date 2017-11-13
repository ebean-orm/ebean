package org.tests.basic.encrypt;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DbEncrypt;
import io.ebeaninternal.api.SpiEbeanServer;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.EBasicEncrypt;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestEncrypt extends BaseTestCase {


  @Test
  @ForPlatform(Platform.H2) // only run this on H2 - PGCrypto not happy on CI server
  public void testQueryBind() {

    LoggedSqlCollector.start();
    Ebean.find(EBasicEncrypt.class)
      .where().startsWith("description", "Rob")
      .findList();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("; --bind(****,Rob%)");
  }

  @Test
  @ForPlatform(Platform.H2)
  public void test() {

    Ebean.find(EBasicEncrypt.class).delete();

    EBasicEncrypt e = new EBasicEncrypt();
    e.setName("testname");
    e.setDescription("testdesc");
    e.setDob(new Date(System.currentTimeMillis() - 100000));

    Ebean.save(e);

    SqlQuery q = Ebean.createSqlQuery("select * from e_basicenc where id = :id");
    q.setParameter("id", e.getId());

    SqlRow row = q.findOne();
    row.getString("name");
    row.get("description");

    EBasicEncrypt e1 = Ebean.find(EBasicEncrypt.class, e.getId());

    e1.getDescription();

    e1.setName("testmod");
    e1.setDescription("moddesc");
    e1.setStatus(EBasicEncrypt.Status.ONE);

    Ebean.save(e1);

    EBasicEncrypt e2 = Ebean.find(EBasicEncrypt.class, e.getId());

    assertEquals("moddesc", e2.getDescription());
    assertEquals(EBasicEncrypt.Status.ONE, e2.getStatus());

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    DbEncrypt dbEncrypt = server.getDatabasePlatform().getDbEncrypt();

    if (dbEncrypt == null) {
      // can not test the where clause
      System.out.println("TestEncrypt: Not testing where clause as no DbEncrypt");

    } else {

      List<EBasicEncrypt> list = Ebean.find(EBasicEncrypt.class).where()
        .eq("description", "moddesc").findList();

      assertEquals(1, list.size());

      list = Ebean.find(EBasicEncrypt.class).where().startsWith("description", "modde").findList();

      assertEquals(1, list.size());
    }
  }

}
