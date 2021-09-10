package io.ebean.config;


import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerConfigSqlServerTest {

  @Disabled
  @ForPlatform({Platform.SQLSERVER})
  @Test //expected = PersistenceException.class
  public void need_explicitPlatform() {

    Properties props = props("some_sqlserver");

    // no explicit databasePlatformName set ..
    //props.setProperty("ebean.some_sqlserver.databasePlatformName", "sqlserver17");

    DatabaseConfig config = new DatabaseConfig();
    config.setName("some_sqlserver");
    config.loadFromProperties(props);

    // not explicitly set ... so we fail to start
//    config.setDatabasePlatform(new SqlServer17Platform());
//    config.setDatabasePlatformName("sqlserver17");

    config.setDefaultServer(false);
    config.setRegister(false);
    config.getClasses().add(EBasicVer.class);

    Database sqlServer = DatabaseFactory.create(config);

    assertThat(sqlServer).isNotNull();
    sqlServer.shutdown();

//    javax.persistence.PersistenceException: java.lang.IllegalArgumentException: For SqlServer please choose the more specific sqlserver16 or sqlserver17 platform via DatabaseConfig.setDatabasePlatformName. Refer to issue #1340 for details
//
//    at io.ebeaninternal.server.core.DatabasePlatformFactory.create(DatabasePlatformFactory.java:62)
//    at io.ebeaninternal.server.core.DefaultContainer.setDatabasePlatform(DefaultContainer.java:266)
//    at io.ebeaninternal.server.core.DefaultContainer.createServer(DefaultContainer.java:126)
//    at io.ebeaninternal.server.core.DefaultContainer.createServer(DefaultContainer.java:45)
//    at io.ebean.EbeanServerFactory.createInternal(EbeanServerFactory.java:109)
//    at io.ebean.EbeanServerFactory.create(EbeanServerFactory.java:70)

  }

  @Disabled
  @ForPlatform({Platform.SQLSERVER})
  @Test
  public void explicit_17() {

    String name = "testsqlserver17";

    DatabaseConfig config = new DatabaseConfig();
    config.setName(name);

    Properties props = props(name);

    // set via properties
    //props.setProperty("ebean.testsqlserver17.databasePlatformName", "sqlserver17");

    // or set programmatically ...
    config.setDatabasePlatformName("sqlserver17");

    config.setDefaultServer(false);
    config.setRegister(false);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.loadFromProperties(props);
    config.getClasses().add(EBasicVer.class);

    Database sqlServer = DatabaseFactory.create(config);

    assertThat(sqlServer).isNotNull();
    sqlServer.shutdown();
  }

  @Disabled
  @ForPlatform({Platform.SQLSERVER})
  @Test
  public void explicit_16() {

    String name = "testsqlserver16";
    Properties props = props(name);
    //props.setProperty("ebean.testsqlserver16.databasePlatformName", "sqlserver16");

    DatabaseConfig config = new DatabaseConfig();
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setDdlGenerate(true);
    config.setDdlRun(true);

    config.setName(name); // match dataSource
    config.setDatabasePlatformName("sqlserver16");
    config.loadFromProperties(props);
    config.getClasses().add(EBasicVer.class);

    Database sqlServer = DatabaseFactory.create(config);

    assertThat(sqlServer).isNotNull();
    sqlServer.shutdown();
  }

  private Properties props(String dbName) {

    Properties props = new Properties();

    // automatically start docker sqlserver 2017 container ...
    props.setProperty("ebean.test.platform", "sqlserver");
    props.setProperty("ebean.test.dbName", "test_ebean");
    props.setProperty("ebean.test.ddlMode", "dropCreate");
    //props.setProperty("ebean.test.containerMode","dropCreate");


    props.setProperty(key(dbName, "username"), "test_ebean");
    props.setProperty(key(dbName, "password"), "SqlS3rv#r");
    props.setProperty(key(dbName, "url"), "jdbc:sqlserver://localhost:1433;databaseName=test_ebean");
    props.setProperty(key(dbName, "driver"), "com.microsoft.sqlserver.jdbc.SQLServerDriver");

    return props;
  }

  private String key(String dbName, String key) {
    return "datasource." + dbName + "." + key;
  }
}
