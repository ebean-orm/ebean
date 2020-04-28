package io.ebeaninternal.server.core;

import io.ebean.config.ServerConfig;
import io.ebean.datasource.DataSourceConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class InitDataSourceTest {

  @Test
  public void readOnlyConfig_nullByDefault() {
    InitDataSource init = new InitDataSource(new ServerConfig());
    assertNull(init.readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_null_whenSetNullExplicitly() {
    ServerConfig config = new ServerConfig();
    config.setReadOnlyDataSourceConfig(null);

    InitDataSource init = new InitDataSource(config);
    assertNull(init.readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_null_whenSetNullExplicitly_2() {
    ServerConfig config = new ServerConfig();
    DataSourceConfig roConfig = new DataSourceConfig();
    roConfig.setUrl(null);
    config.setReadOnlyDataSourceConfig(roConfig);

    InitDataSource init = new InitDataSource(config);
    assertNull(init.readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_when_autoReadOnlyDataSource() {
    ServerConfig config = new ServerConfig();
    config.setAutoReadOnlyDataSource(true);

    InitDataSource init = new InitDataSource(config);
    assertNotNull(init.readOnlyConfig());
  }

  @Test
  public void readOnlyConfig_when_urlSet() {
    ServerConfig config = new ServerConfig();
    DataSourceConfig dsConfig = new DataSourceConfig();
    dsConfig.setUrl("foo");
    config.setReadOnlyDataSourceConfig(dsConfig);

    InitDataSource init = new InitDataSource(config);
    final DataSourceConfig roConfig = init.readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.getUrl());
  }

  @Test
  public void readOnlyConfig_when_urlSet_2() {
    ServerConfig config = new ServerConfig();
    config.getReadOnlyDataSourceConfig().setUrl("foo");

    InitDataSource init = new InitDataSource(config);
    final DataSourceConfig roConfig = init.readOnlyConfig();
    assertNotNull(roConfig);
    assertEquals("foo", roConfig.getUrl());
  }
}
