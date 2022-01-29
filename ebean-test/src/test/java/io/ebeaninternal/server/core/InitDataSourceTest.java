package io.ebeaninternal.server.core;

import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceAlert;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.pool.ConnectionPool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import javax.sql.DataSource;

public class InitDataSourceTest {

  private DatabaseConfig newConfig(String readOnlyUrl) {
    DatabaseConfig config = new DatabaseConfig();
    DataSourceConfig roConfig = new DataSourceConfig();
    roConfig.setUrl(readOnlyUrl);
    config.setReadOnlyDataSourceConfig(roConfig);
    return config;
  }

  @Test
  public void readOnlyConfig_nullByDefault() {
    InitDataSource init = new InitDataSource(new DatabaseConfig());
    assertNull(init.readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_null_whenSetNullExplicitly() {
    DatabaseConfig config = new DatabaseConfig();
    config.setReadOnlyDataSourceConfig(null);

    assertNull(new InitDataSource(config).readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_null_whenSetNullExplicitly_2() {
    assertNull(new InitDataSource(newConfig(null)).readOnlyConfig());
    assertNull(new InitDataSource(newConfig("")).readOnlyConfig());
    assertNull(new InitDataSource(newConfig(" ")).readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_null_whenValueNONE() {
    assertNull(new InitDataSource(newConfig("none")).readOnlyConfig());
    assertNull(new InitDataSource(newConfig("None")).readOnlyConfig());
    assertNull(new InitDataSource(newConfig("NONE")).readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_when_autoReadOnlyDataSource() {
    DatabaseConfig config = new DatabaseConfig();
    config.setAutoReadOnlyDataSource(true);

    assertNotNull(new InitDataSource(config).readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_when_autoReadOnlyDataSource_expect_setToNull() {
    DatabaseConfig config = newConfig("none");
    config.setAutoReadOnlyDataSource(true);

    final DataSourceConfig readOnlyConfig = new InitDataSource(config).readOnlyConfig();
    assertNull(readOnlyConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_urlSet() {
    DatabaseConfig config = newConfig("foo");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain() {
    DatabaseConfig config = newConfig(null);
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().setReadOnlyUrl("bar");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("bar", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain_withNone() {
    DatabaseConfig config = newConfig("None");
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().setReadOnlyUrl("bar");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("bar", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_bothReadOnlyUrlsSet() {
    DatabaseConfig config = newConfig("one");
    config.getDataSourceConfig().setReadOnlyUrl("two");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("one", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain_withNoneNone() {
    DatabaseConfig config = newConfig("none");
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().setReadOnlyUrl("none");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNull(roConfig);
  }

  @Test
  public void readOnlyConfig_when_urlSet_2() {
    DatabaseConfig config = new DatabaseConfig();
    config.getReadOnlyDataSourceConfig().setUrl("foo");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.getUrl());
  }
  
  
  @Test
  public void online() {
    DatabaseConfig config = new DatabaseConfig();
    config.getDataSourceConfig().setUsername("sa");
    config.getDataSourceConfig().setPassword("");
    config.getDataSourceConfig().setUrl("jdbc:h2:mem:dsTestOnline");
    config.getDataSourceConfig().setDriver("org.h2.Driver");
    InitDataSource.init(config);
    ConnectionPool pool = (ConnectionPool) config.getDataSource();
    assertThat(pool.isDataSourceUp()).isTrue();
    pool.shutdown();
  }

  static class MyAlert implements DataSourceAlert {

    int up;

    @Override
    public void dataSourceUp(DataSource dataSource) {
      up++;
    }

    @Override
    public void dataSourceDown(DataSource dataSource, SQLException reason) {
    }

    @Override
    public void dataSourceWarning(DataSource dataSource, String msg) {
    }

  }

  @Test
  public void offline() throws SQLException {
    DatabaseConfig config = new DatabaseConfig();
    config.getDataSourceConfig().setUsername("sa");
    config.getDataSourceConfig().setPassword("");
    config.getDataSourceConfig().setUrl("jdbc:h2:mem:dsTestOffline");
    config.getDataSourceConfig().setDriver("org.h2.Driver");
    config.getDataSourceConfig().setOffline(true);
    config.getDataSourceConfig().setFailOnStart(false);
    MyAlert alert = new MyAlert();
    config.getDataSourceConfig().setAlert(alert);
    config.setDatabasePlatformName("h2");
    InitDataSource.init(config);
    ConnectionPool pool = (ConnectionPool) config.getDataSource();
    assertThat(pool).isNotNull();
    // make some additional tests with the pool
    assertThat(pool.isDataSourceUp()).isFalse();
    assertThat(alert.up).isEqualTo(0);
    pool.online();
    assertThat(alert.up).isEqualTo(1);
    assertThat(pool.isDataSourceUp()).isTrue();
    pool.shutdown();
  }

}
