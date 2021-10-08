package io.ebean.config;


import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.datasource.DataSourceAlert;
import io.ebean.datasource.DataSourceInitialiseException;

import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestServerOffline {

  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_with_failOnStart() throws SQLException {

    String url = "jdbc:h2:mem:testoffline1";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {
      Properties props = props(url);
      props.setProperty("datasource.h2_offline.failOnStart", "true");
      DatabaseConfig config = config(props);
      assertThatThrownBy(() -> DatabaseFactory.create(config))
        .isInstanceOf(DataSourceInitialiseException.class);
   }

  }

  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_no_failOnStart() throws SQLException {

    String url = "jdbc:h2:mem:testoffline2";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {
      Properties props = props(url);

      DatabaseConfig config = config(props);
      config.setSkipDataSourceCheck(true);
      config.setSkipInitDatabase(true);
      Database h2Offline = DatabaseFactory.create(config);
      assertThat(h2Offline).isNotNull();
    }
  }

  private static class LazyDatasourceInitializer implements DataSourceAlert {

    public Database server;

    private boolean initialized;

    @Override
    public void dataSourceUp(DataSource dataSource) {
      if (!initialized) {
        initDatabase();
      }
    }

    public synchronized void initDatabase() {
      if (!initialized) {
        server.initDatabase(true);
        initialized = true;
      }
    }

    @Override
    public void dataSourceDown(DataSource dataSource, SQLException reason) {}

    @Override
    public void dataSourceWarning(DataSource dataSource, String msg) {}

  }

  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_recovery() throws SQLException {

    String url = "jdbc:h2:mem:testoffline3";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {

      Properties props = props(url);
      LazyDatasourceInitializer alert = new LazyDatasourceInitializer() ;

      DatabaseConfig config = config(props);

      config.getDataSourceConfig().setAlert(alert);
      config.getDataSourceConfig().setHeartbeatFreqSecs(1);
      config.setSkipDataSourceCheck(true);
      config.setSkipInitDatabase(true);

      Database h2Offline = DatabaseFactory.create(config);
      alert.server = h2Offline;
      assertThat(h2Offline).isNotNull();

      assertThatThrownBy(() -> alert.initDatabase())
        .isInstanceOf(PersistenceException.class)
        .hasMessageContaining("Failed to obtain connection to run DDL");

      assertThatThrownBy(() -> h2Offline.find(EBasicVer.class).findCount()).isInstanceOf(PersistenceException.class);

      // so - reset the password so that the server can reconnect
      try (Statement stmt = bootup.createStatement()) {
        stmt.execute("alter user sa set password 'sa'");
      }

      assertThat(alert.initialized).isFalse();
      // next access to ebean should bring DS online
      h2Offline.find(EBasicVer.class).findCount();
      assertThat(alert.initialized).isTrue();

      // check if server is working (ie ddl was run)
      EBasicVer bean = new EBasicVer("foo");
      h2Offline.save(bean);
      assertThat(h2Offline.find(EBasicVer.class).findCount()).isEqualTo(1);
      h2Offline.delete(bean);
    }
  }

  private Properties props(String url) {

    Properties props = new Properties();

    props.setProperty("datasource.h2_offline.username", "sa");
    props.setProperty("datasource.h2_offline.password", "sa");
    props.setProperty("datasource.h2_offline.url", url);
    props.setProperty("datasource.h2_offline.driver", "org.h2.Driver");

    // ensures, that the datasource will come up when DB is not available
    props.setProperty("datasource.h2_offline.failOnStart", "false");

    props.setProperty("ebean.h2_offline.databasePlatformName", "h2");
    props.setProperty("ebean.h2_offline.ddl.extra", "false");

    props.setProperty("ebean.h2_offline.ddl.generate", "true");
    props.setProperty("ebean.h2_offline.ddl.run", "true");

    return props;
  }

  private DatabaseConfig config(Properties props) {
    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2_offline");
    config.loadFromProperties(props);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.getClasses().add(EBasicVer.class);
    return config;
  }

}
