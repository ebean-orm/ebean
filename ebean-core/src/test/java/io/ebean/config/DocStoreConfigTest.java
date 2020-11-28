package io.ebean.config;

import io.ebean.annotation.DocStoreMode;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DocStoreConfigTest {

  @Test
  public void loadSettings() throws Exception {

    DocStoreConfig config = new DocStoreConfig();

    Properties properties = new Properties();
    properties.setProperty("ebean.docstore.active", "true");
    properties.setProperty("ebean.docstore.bulkBatchSize", "99");
    properties.setProperty("ebean.docstore.url", "http://foo:9800");
    properties.setProperty("ebean.docstore.persist", "IGNORE");
    properties.setProperty("ebean.docstore.allowAllCertificates", "true");
    properties.setProperty("ebean.docstore.username", "fred");
    properties.setProperty("ebean.docstore.password", "rock");

    PropertiesWrapper wrapper = new PropertiesWrapper("ebean", null, properties, null);

    config.loadSettings(wrapper);

    assertTrue(config.isActive());
    assertTrue(config.isAllowAllCertificates());
    assertFalse(config.isGenerateMapping());
    assertFalse(config.isDropCreate());
    assertEquals("http://foo:9800", config.getUrl());
    assertEquals("fred", config.getUsername());
    assertEquals("rock", config.getPassword());
    assertEquals(DocStoreMode.IGNORE, config.getPersist());
    assertEquals(99, config.getBulkBatchSize());
  }

  @Test
  public void loadSettings_generateMapping_dropCreate() throws Exception {

    DocStoreConfig config = new DocStoreConfig();

    Properties properties = new Properties();
    properties.setProperty("ebean.docstore.generateMapping", "true");
    properties.setProperty("ebean.docstore.dropCreate", "true");

    PropertiesWrapper wrapper = new PropertiesWrapper("ebean", null, properties, null);
    config.loadSettings(wrapper);

    assertTrue(config.isGenerateMapping());
    assertTrue(config.isDropCreate());
    assertFalse(config.isCreate());
  }

  @Test
  public void loadSettings_generateMapping_create() throws Exception {

    DocStoreConfig config = new DocStoreConfig();

    Properties properties = new Properties();
    properties.setProperty("ebean.docstore.generateMapping", "true");
    properties.setProperty("ebean.docstore.create", "true");

    PropertiesWrapper wrapper = new PropertiesWrapper("ebean", null, properties, null);
    config.loadSettings(wrapper);

    assertTrue(config.isGenerateMapping());
    assertTrue(config.isCreate());
    assertFalse(config.isDropCreate());
  }
}
