package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.config.ClassLoadConfig;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.Assert.assertTrue;

public class InsertTimestampFactoryTest {

  InsertTimestampFactory factory = new InsertTimestampFactory(new ClassLoadConfig());

  @Test
  public void test_createdTimestamp_Instant() {

    DeployBeanProperty prop = new DeployBeanProperty(null, Instant.class, null, null);

    GeneratedProperty insertTimestamp = factory.createInsertTimestamp(prop);
    Object value = insertTimestamp.getInsertValue(null, null, System.currentTimeMillis());
    assertTrue(value instanceof Instant);
  }

  @Test
  public void test_createdTimestamp_LocalDateTime() {

    DeployBeanProperty prop = new DeployBeanProperty(null, LocalDateTime.class, null, null);

    GeneratedProperty insertTimestamp = factory.createInsertTimestamp(prop);
    Object value = insertTimestamp.getInsertValue(null, null, System.currentTimeMillis());
    assertTrue(value instanceof LocalDateTime);
  }

  @Test
  public void test_createdTimestamp_OffsetDateTime() {

    DeployBeanProperty prop = new DeployBeanProperty(null, OffsetDateTime.class, null, null);

    GeneratedProperty insertTimestamp = factory.createInsertTimestamp(prop);
    Object value = insertTimestamp.getInsertValue(null, null, System.currentTimeMillis());
    assertTrue(value instanceof OffsetDateTime);
  }

  @Test
  public void test_createdTimestamp_Timestamp() {

    DeployBeanProperty prop = new DeployBeanProperty(null, Timestamp.class, null, null);

    GeneratedProperty insertTimestamp = factory.createInsertTimestamp(prop);
    Object value = insertTimestamp.getInsertValue(null, null, System.currentTimeMillis());
    assertTrue(value instanceof Timestamp);
  }

  @Test
  public void test_createdTimestamp_utilDate() {

    DeployBeanProperty prop = new DeployBeanProperty(null, java.util.Date.class, null, null);

    GeneratedProperty insertTimestamp = factory.createInsertTimestamp(prop);
    Object value = insertTimestamp.getInsertValue(null, null, System.currentTimeMillis());
    assertTrue(value instanceof java.util.Date);
  }

}
