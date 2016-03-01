package com.avaje.ebean.config;

import com.avaje.ebean.annotation.DocStoreEvent;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class DocStoreConfigTest {

  @Test
  public void testLoadSettings() throws Exception {

    DocStoreConfig config = new DocStoreConfig();

    Properties properties = new Properties();
    properties.setProperty("ebean.docstore.active", "true");
    properties.setProperty("ebean.docstore.bulkBatchSize", "99");
    properties.setProperty("ebean.docstore.url", "http://foo:9800");
    properties.setProperty("ebean.docstore.persist", "IGNORE");

    PropertiesWrapper wrapper = new PropertiesWrapper("ebean", null, properties);

    config.loadSettings(wrapper);

    assertTrue(config.isActive());
    assertEquals("http://foo:9800", config.getUrl());
    assertEquals(DocStoreEvent.IGNORE, config.getPersist());
    assertEquals(99, config.getBulkBatchSize());

  }
}