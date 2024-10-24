package io.ebeaninternal.server.core;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceAlert;
import io.ebean.datasource.DataSourceBuilder;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourcePool;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.postgres.Postgres9Platform;
import io.ebean.platform.postgres.PostgresPlatform;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class InitDataSourceTest {

  private DatabaseBuilder.Settings newConfig(String readOnlyUrl) {
    DatabaseBuilder config = new DatabaseConfig();
    DataSourceConfig roConfig = new DataSourceConfig();
    roConfig.url(readOnlyUrl);
    config.setReadOnlyDataSourceConfig(roConfig);
    return config.settings();
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
    var config = newConfig("none");
    config.setAutoReadOnlyDataSource(true);

    final DataSourceBuilder readOnlyConfig = new InitDataSource(config).readOnlyConfig();
    assertThat(readOnlyConfig).isNotNull();
    assertNull(readOnlyConfig.settings().getUrl());
  }

  @Test
  public void readOnlyConfig_when_urlSet() {
    var config = newConfig("foo");

    final DataSourceBuilder roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.settings().getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain() {
    var config = newConfig(null);
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().readOnlyUrl("bar");

    final DataSourceBuilder roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("bar", roConfig.settings().getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain_withNone() {
    var config = newConfig("None");
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().readOnlyUrl("bar");

    final DataSourceBuilder roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("bar", roConfig.settings().getUrl());
  }

  @Test
  public void readOnlyConfig_when_bothReadOnlyUrlsSet() {
    var config = newConfig("one");
    config.getDataSourceConfig().readOnlyUrl("two");

    final DataSourceBuilder roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("one", roConfig.settings().getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain_withNoneNone() {
    var config = newConfig("none");
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().readOnlyUrl("none");

    final DataSourceBuilder roConfig = new InitDataSource(config).readOnlyConfig();
    assertNull(roConfig);
  }

  @Test
  public void readOnlyConfig_when_urlSet_2() {
    DatabaseConfig config = new DatabaseConfig();
    config.getReadOnlyDataSourceConfig().url("foo");

    final DataSourceBuilder roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.settings().getUrl());
  }

  @Test
  void isPostgresAllQuotedIdentifiers_true_when_postgres() {
    DatabaseConfig config = new DatabaseConfig();
    config.setAllQuotedIdentifiers(true);
    config.setDatabasePlatform(new PostgresPlatform());

    assertTrue(new InitDataSource(config).isPostgresAllQuotedIdentifiers());
  }

  @Test
  void isPostgresAllQuotedIdentifiers_true_when_postgres9() {
    DatabaseConfig config = new DatabaseConfig();
    config.setAllQuotedIdentifiers(true);
    config.setDatabasePlatform(new Postgres9Platform());

    assertTrue(new InitDataSource(config).isPostgresAllQuotedIdentifiers());
  }

  @Test
  void isPostgresAllQuotedIdentifiers_false() {
    DatabaseConfig config = new DatabaseConfig();
    config.setAllQuotedIdentifiers(false);
    config.setDatabasePlatform(new PostgresPlatform());

    assertFalse(new InitDataSource(config).isPostgresAllQuotedIdentifiers());
  }

  @Test
  void isPostgresAllQuotedIdentifiers_false_when_notPostgres() {
    DatabaseConfig config = new DatabaseConfig();
    config.setAllQuotedIdentifiers(true);
    config.setDatabasePlatform(new H2Platform());

    assertFalse(new InitDataSource(config).isPostgresAllQuotedIdentifiers());
  }

  @Test
  public void online() {
    DatabaseConfig config = new DatabaseConfig();
    config.getDataSourceConfig().username("sa");
    config.getDataSourceConfig().password("");
    config.getDataSourceConfig().url("jdbc:h2:mem:dsTestOnline");
    config.getDataSourceConfig().driver("org.h2.Driver");
    InitDataSource.init(config);
    DataSourcePool pool = (DataSourcePool) config.getDataSource();
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

  }

  @Test
  public void offline() throws SQLException {
    DatabaseConfig config = new DatabaseConfig();
    DataSourceBuilder dsConfig = config.getDataSourceConfig();
    dsConfig.username("sa");
    dsConfig.password("");
    dsConfig.url("jdbc:h2:mem:dsTestOffline");
    dsConfig.driver("org.h2.Driver");
    dsConfig.offline(true);
    dsConfig.failOnStart(false);
    MyAlert alert = new MyAlert();
    dsConfig.alert(alert);
    config.setDatabasePlatformName("h2");
    InitDataSource.init(config);
    DataSourcePool pool = (DataSourcePool) config.getDataSource();
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
