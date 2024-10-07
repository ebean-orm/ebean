package io.ebean.test.config;


import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceAlert;
import io.ebean.datasource.DataSourceInitialiseException;
import io.ebean.xtest.ForPlatform;

import io.ebean.xtest.base.PlatformCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.tests.model.basic.EBasicVer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(PlatformCondition.class)
public class TestServerOffline {

  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_default() throws SQLException {

    String url = "jdbc:h2:mem:testoffline1";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {
      Properties props = props(url);
      DatabaseConfig config = config(props);

      assertThatThrownBy(() -> DatabaseFactory.create(config))
        .isInstanceOf(DataSourceInitialiseException.class);
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
        server.runDdl();
        initialized = true;
      }
    }

    @Override
    public void dataSourceDown(DataSource dataSource, SQLException reason) {}

  }

  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_recovery() throws SQLException {

    String url = "jdbc:h2:mem:testoffline3";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {

      Properties props = props(url);

      // to bring up ebean without a database, we must disable various things
      // that happen on startup
      props.setProperty("datasource.h2_offline.failOnStart", "false");
      props.setProperty("ebean.h2_offline.skipDataSourceCheck", "true");
      props.setProperty("ebean.h2_offline.ddl.run", "false");
      DatabaseConfig config = config(props);

      LazyDatasourceInitializer alert = new LazyDatasourceInitializer() ;
      config.getDataSourceConfig().alert(alert);
      config.getDataSourceConfig().heartbeatFreqSecs(1);

      Database h2Offline = DatabaseFactory.create(config);
      alert.server = h2Offline;
      assertThat(h2Offline).isNotNull();
      // DB is online now in offline mode

      // Accessing the DB will throw a PE
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
    config.classes().add(EBasicVer.class);
    return config;
  }

}
