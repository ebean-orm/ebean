package io.ebeaninternal.server.core;

import io.ebean.config.ServerConfig;
import io.ebean.datasource.DataSourceConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class InitDataSourceTest {

  private ServerConfig newConfig(String readOnlyUrl) {
    ServerConfig config = new ServerConfig();
    DataSourceConfig roConfig = new DataSourceConfig();
    roConfig.setUrl(readOnlyUrl);
    config.setReadOnlyDataSourceConfig(roConfig);
    return config;
  }

  @Test
  public void readOnlyConfig_nullByDefault() {
    InitDataSource init = new InitDataSource(new ServerConfig());
    assertNull(init.readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_null_whenSetNullExplicitly() {
    ServerConfig config = new ServerConfig();
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
    ServerConfig config = new ServerConfig();
    config.setAutoReadOnlyDataSource(true);

    assertNotNull(new InitDataSource(config).readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_when_autoReadOnlyDataSource_expect_setToNull() {
    ServerConfig config = newConfig("none");
    config.setAutoReadOnlyDataSource(true);

    final DataSourceConfig readOnlyConfig = new InitDataSource(config).readOnlyConfig();
    assertNull(readOnlyConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_urlSet() {
    ServerConfig config = newConfig("foo");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain() {
    ServerConfig config = newConfig(null);
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().setReadOnlyUrl("bar");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("bar", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain_withNone() {
    ServerConfig config = newConfig("None");
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().setReadOnlyUrl("bar");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("bar", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_bothReadOnlyUrlsSet() {
    ServerConfig config = newConfig("one");
    config.getDataSourceConfig().setReadOnlyUrl("two");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("one", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_readOnlyUrlSetOnMain_withNoneNone() {
    ServerConfig config = newConfig("none");
    // alternate location to set read-only url for developer convenience
    config.getDataSourceConfig().setReadOnlyUrl("none");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNull(roConfig);
  }

  @Test
  public void readOnlyConfig_when_urlSet_2() {
    ServerConfig config = new ServerConfig();
    config.getReadOnlyDataSourceConfig().setUrl("foo");

    final DataSourceConfig roConfig = new InitDataSource(config).readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.getUrl());
  }
}
