package org.tests.basic;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Query;
import io.ebean.SqlRow;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.postgres.PostgresPlatform;
import io.ebean.datasource.DataSourceConfig;
import org.tests.model.basic.TOne;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Used to run some tests manually on a specific Database type.
 */
public class MainDbBoolean {

  public static void main(String[] args) {
    MainDbBoolean me = new MainDbBoolean();

    Database server = me.createEbeanServer();
    me.simpleCheck(server);

    Database oraServer = me.createOracleEbeanServer();
    me.simpleCheck(oraServer);
  }

  /**
   * Create a server for running small oracle specific tests manually.
   * DDL generation etc.
   */
  private Database createOracleEbeanServer() {
    DatabaseBuilder c = new DatabaseConfig();
    c.setName("ora");
    c.setDdlExtra(false);

    // requires oracle driver in class path
    DataSourceConfig oraDb = new DataSourceConfig();
    oraDb.driver("oracle.jdbc.driver.OracleDriver");
    oraDb.username("junk");
    oraDb.password("junk");
    oraDb.url("jdbc:oracle:thin:junk/junk@localhost:1521:XE");
    oraDb.heartbeatSql("select count(*) from dual");


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

    return DatabaseFactory.create(c);
  }

  private Database createEbeanServer() {
    DatabaseBuilder c = new DatabaseConfig();
    c.setName("pgtest");
    c.setDdlExtra(false);

    // requires postgres driver in class path
    DataSourceConfig postgresDb = new DataSourceConfig();
    postgresDb.driver("org.postgresql.Driver");
    postgresDb.username("test");
    postgresDb.password("test");
    postgresDb.url("jdbc:postgresql://127.0.0.1:5432/test");
    postgresDb.heartbeatSql("select count(*) from t_one");

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
    return DatabaseFactory.create(c);
  }

  private void simpleCheck(Database server) {
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
      .orderBy("id")
      .findList();

    assertThat(list).hasSize(2);
    assertTrue(list.get(0).isActive());

    String sql = "select id, name, active from t_oneb order by id";
    List<SqlRow> sqlRows = server.sqlQuery(sql).findList();
    assertThat(sqlRows).hasSize(2);
    Object active0 = sqlRows.get(0).get("active");
    Object active1 = sqlRows.get(1).get("active");

    assertEquals("T", active0);
    assertEquals("F", active1);

    Query<TOne> query = server.find(TOne.class)
      .setAutoTune(false)
      .orderBy("id");

    int rc = query.findCount();
    assertThat(rc).isGreaterThan(0);

    System.out.println("done");
  }
}
