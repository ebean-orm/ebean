package org.tests.basic;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Query;
import io.ebean.SqlRow;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import org.tests.model.basic.TOne;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;
import org.avaje.datasource.DataSourceConfig;
import org.junit.Assert;

import java.util.List;

/**
 * Used to run some tests manually on a specific Database type.
 */
public class MainDbBoolean {

  public static void main(String[] args) {

    MainDbBoolean me = new MainDbBoolean();

    EbeanServer server = me.createEbeanServer();
    me.simpleCheck(server);

    EbeanServer oraServer = me.createOracleEbeanServer();
    me.simpleCheck(oraServer);
  }

  /**
   * Create a server for running small oracle specific tests manually.
   * DDL generation etc.
   */
  private EbeanServer createOracleEbeanServer() {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig c = new ServerConfig();
    c.setName("ora");

    // requires oracle driver in class path
    DataSourceConfig oraDb = new DataSourceConfig();
    oraDb.setDriver("oracle.jdbc.driver.OracleDriver");
    oraDb.setUsername("junk");
    oraDb.setPassword("junk");
    oraDb.setUrl("jdbc:oracle:thin:junk/junk@localhost:1521:XE");
    oraDb.setHeartbeatSql("select count(*) from dual");


    c.loadFromProperties();
    c.setDdlGenerate(true);
    c.setDdlRun(true);
    c.setDefaultServer(false);
    c.setRegister(false);
    c.setDataSourceConfig(oraDb);

    //c.setDatabaseBooleanTrue("1");
    //c.setDatabaseBooleanFalse("0");
    c.setDatabaseBooleanTrue("T");
    c.setDatabaseBooleanFalse("F");

    c.addClass(TOne.class);
    c.addClass(TSMaster.class);
    c.addClass(TSDetail.class);

    return EbeanServerFactory.create(c);

  }

  private EbeanServer createEbeanServer() {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig c = new ServerConfig();
    c.setName("pgtest");

    // requires postgres driver in class path
    DataSourceConfig postgresDb = new DataSourceConfig();
    postgresDb.setDriver("org.postgresql.Driver");
    postgresDb.setUsername("test");
    postgresDb.setPassword("test");
    postgresDb.setUrl("jdbc:postgresql://127.0.0.1:5432/test");
    postgresDb.setHeartbeatSql("select count(*) from t_one");

    c.loadFromProperties();
    c.setDdlGenerate(true);
    c.setDdlRun(true);
    c.setDefaultServer(false);
    c.setRegister(false);
    c.setDataSourceConfig(postgresDb);

    //c.setDatabaseBooleanTrue("1");
    //c.setDatabaseBooleanFalse("0");
    c.setDatabaseBooleanTrue("T");
    c.setDatabaseBooleanFalse("F");

    c.setDatabasePlatform(new PostgresPlatform());

    c.addClass(TOne.class);

    return EbeanServerFactory.create(c);

  }

  private void simpleCheck(EbeanServer server) {

    TOne o = new TOne();
    o.setName("banan");
    o.setDescription("this one is true");
    o.setActive(true);

    server.save(o);

    TOne o2 = new TOne();
    o2.setName("banan");
    o2.setDescription("this one is false");
    o2.setActive(false);

    server.save(o2);


    List<TOne> list = server.find(TOne.class)
      .setAutoTune(false)
      .order("id")
      .findList();

    Assert.assertTrue(list.size() == 2);
    Assert.assertTrue(list.get(0).isActive());
    Assert.assertFalse(!list.get(0).isActive());

    String sql = "select id, name, active from t_oneb order by id";
    List<SqlRow> sqlRows = server.createSqlQuery(sql).findList();
    Assert.assertTrue(sqlRows.size() == 2);
    Object active0 = sqlRows.get(0).get("active");
    Object active1 = sqlRows.get(1).get("active");

    Assert.assertTrue("T".equals(active0));
    Assert.assertTrue("F".equals(active1));


    Query<TOne> query = server.find(TOne.class)
      .setAutoTune(false)
      .order("id");

    int rc = query.findCount();
    Assert.assertTrue(rc > 0);


    System.out.println("done");
  }
}
