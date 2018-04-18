package io.ebean.config;

import io.ebean.annotation.PersistBatch;
import io.ebean.config.dbplatform.IdType;
import org.avaje.datasource.DataSourceConfig;
import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ServerConfigTest {

  @Test
  public void testLoadFromEbeanProperties() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.loadFromProperties();

    assertEquals(PersistBatch.NONE, serverConfig.getPersistBatch());
    assertNotNull(serverConfig.getProperties());
  }

  @Test
  public void evalPropertiesInput() {

    String home = System.getenv("HOME");

    Properties props = new Properties();
    props.setProperty("ddl.initSql", "${HOME}/initSql");

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.loadFromProperties(props);

    String ddlInitSql = serverConfig.getDdlInitSql();
    assertThat(ddlInitSql).isEqualTo(home+"/initSql");
  }

  @Test
  public void testLoadWithProperties() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setPersistBatch(PersistBatch.NONE);
    serverConfig.setPersistBatchOnCascade(PersistBatch.NONE);
    serverConfig.setAutoReadOnlyDataSource(false);
    serverConfig.setReadOnlyDataSource(null);
    serverConfig.setReadOnlyDataSourceConfig(new DataSourceConfig());

    Properties props = new Properties();
    props.setProperty("persistBatch", "INSERT");
    props.setProperty("persistBatchOnCascade", "INSERT");
    props.setProperty("dbuuid", "binary");
    props.setProperty("jdbcFetchSizeFindEach", "42");
    props.setProperty("jdbcFetchSizeFindList", "43");
    props.setProperty("backgroundExecutorShutdownSecs", "98");
    props.setProperty("backgroundExecutorSchedulePoolSize", "4");
    props.setProperty("dbOffline", "true");
    props.setProperty("jsonDateTime", "ISO8601");
    props.setProperty("autoReadOnlyDataSource", "true");
    props.setProperty("disableL2Cache", "true");
    props.setProperty("notifyL2CacheInForeground", "true");
    props.setProperty("idType", "SEQUENCE");
    props.setProperty("mappingLocations", "classpath:/foo;bar");
    props.setProperty("namingConvention", "io.ebean.config.MatchingNamingConvention");


    serverConfig.loadFromProperties(props);

    assertTrue(serverConfig.isDisableL2Cache());
    assertTrue(serverConfig.isNotifyL2CacheInForeground());
    assertTrue(serverConfig.isDbOffline());
    assertTrue(serverConfig.isAutoReadOnlyDataSource());
    assertThat(serverConfig.getNamingConvention()).isInstanceOf(MatchingNamingConvention.class);

    assertEquals(IdType.SEQUENCE, serverConfig.getIdType());
    assertEquals(PersistBatch.INSERT, serverConfig.getPersistBatch());
    assertEquals(PersistBatch.INSERT, serverConfig.getPersistBatchOnCascade());
    assertEquals(PlatformConfig.DbUuid.BINARY, serverConfig.getPlatformConfig().getDbUuid());
    assertEquals(JsonConfig.DateTime.ISO8601, serverConfig.getJsonDateTime());

    assertEquals(42, serverConfig.getJdbcFetchSizeFindEach());
    assertEquals(43, serverConfig.getJdbcFetchSizeFindList());
    assertEquals(4, serverConfig.getBackgroundExecutorSchedulePoolSize());
    assertEquals(98, serverConfig.getBackgroundExecutorShutdownSecs());

    assertThat(serverConfig.getMappingLocations()).containsExactly("classpath:/foo","bar");

    serverConfig.setPersistBatch(PersistBatch.NONE);
    serverConfig.setPersistBatchOnCascade(PersistBatch.NONE);

    Properties props1 = new Properties();
    props1.setProperty("ebean.persistBatch", "ALL");
    props1.setProperty("ebean.persistBatchOnCascade", "ALL");

    serverConfig.setNotifyL2CacheInForeground(true);
    serverConfig.setDisableL2Cache(true);
    props1.setProperty("ebean.disableL2Cache", "false");
    props1.setProperty("ebean.notifyL2CacheInForeground", "false");

    serverConfig.loadFromProperties(props1);
    assertFalse(serverConfig.isDisableL2Cache());
    assertFalse(serverConfig.isNotifyL2CacheInForeground());

    assertEquals(PersistBatch.ALL, serverConfig.getPersistBatch());
    assertEquals(PersistBatch.ALL, serverConfig.getPersistBatchOnCascade());
  }
}
