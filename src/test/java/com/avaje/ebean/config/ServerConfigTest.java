package com.avaje.ebean.config;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class ServerConfigTest {


  @Test
  public void testLoadFromEbeanProperties() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.loadFromProperties();

    assertEquals(PersistBatch.NONE, serverConfig.getPersistBatch());
    assertEquals(PersistBatch.ALL, serverConfig.getPersistBatchOnCascade());

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
    props.setProperty("dbuuid","binary");

    serverConfig.loadFromProperties(props);

    assertEquals(PersistBatch.INSERT, serverConfig.getPersistBatch());
    assertEquals(PersistBatch.INSERT, serverConfig.getPersistBatchOnCascade());
    assertEquals(ServerConfig.DbUuid.BINARY, serverConfig.getDbUuid());

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