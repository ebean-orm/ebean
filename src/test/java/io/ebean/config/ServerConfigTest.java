package io.ebean.config;

import io.ebean.annotation.PersistBatch;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class ServerConfigTest {

  @Test
  public void testLoadFromEbeanProperties() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.loadFromProperties();

    assertEquals(PersistBatch.NONE, serverConfig.getPersistBatch());
    assertNotNull(serverConfig.getProperties());
  }

  @Test
  public void testLoadWithProperties() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setPersistBatch(PersistBatch.NONE);
    serverConfig.setPersistBatchOnCascade(PersistBatch.NONE);

    Properties props = new Properties();
    props.setProperty("persistBatch", "INSERT");
    props.setProperty("persistBatchOnCascade", "INSERT");
    props.setProperty("dbuuid", "binary");
    props.setProperty("jdbcFetchSizeFindEach", "42");
    props.setProperty("jdbcFetchSizeFindList", "43");
    props.setProperty("backgroundExecutorShutdownSecs", "98");
    props.setProperty("backgroundExecutorSchedulePoolSize", "4");
    props.setProperty("dbOffline", "true");

    serverConfig.loadFromProperties(props);

    assertTrue(serverConfig.isDbOffline());
    assertEquals(PersistBatch.INSERT, serverConfig.getPersistBatch());
    assertEquals(PersistBatch.INSERT, serverConfig.getPersistBatchOnCascade());
    assertEquals(ServerConfig.DbUuid.BINARY, serverConfig.getDbTypeConfig().getDbUuid());
    assertEquals(42, serverConfig.getJdbcFetchSizeFindEach());
    assertEquals(43, serverConfig.getJdbcFetchSizeFindList());
    assertEquals(4, serverConfig.getBackgroundExecutorSchedulePoolSize());
    assertEquals(98, serverConfig.getBackgroundExecutorShutdownSecs());

    serverConfig.setPersistBatch(PersistBatch.NONE);
    serverConfig.setPersistBatchOnCascade(PersistBatch.NONE);

    Properties props1 = new Properties();
    props1.setProperty("ebean.persistBatch", "ALL");
    props1.setProperty("ebean.persistBatchOnCascade", "ALL");

    serverConfig.loadFromProperties(props1);
    serverConfig.loadTestProperties();

    assertEquals(PersistBatch.ALL, serverConfig.getPersistBatch());
    assertEquals(PersistBatch.ALL, serverConfig.getPersistBatchOnCascade());
  }
}
