package org.tests.basic;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Query;
import io.ebean.SqlRow;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.datasource.DataSourceConfig;
import org.tests.model.basic.TOne;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    ServerConfig c = new ServerConfig();
    c.setName("ora");
    c.setDdlExtra(false);

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

    ServerConfig c = new ServerConfig();
    c.setName("pgtest");
    c.setDdlExtra(false);

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

    assertTrue(list.size() == 2);
    assertTrue(list.get(0).isActive());
    assertFalse(!list.get(0).isActive());

    String sql = "select id, name, active from t_oneb order by id";
    List<SqlRow> sqlRows = server.sqlQuery(sql).findList();
    assertTrue(sqlRows.size() == 2);
    Object active0 = sqlRows.get(0).get("active");
    Object active1 = sqlRows.get(1).get("active");

    assertTrue("T".equals(active0));
    assertTrue("F".equals(active1));


    Query<TOne> query = server.find(TOne.class)
      .setAutoTune(false)
      .order("id");

    int rc = query.findCount();
    assertTrue(rc > 0);


    System.out.println("done");
  }
}
