package io.ebean.xtest.config;

import io.ebean.config.ContainerConfig;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainerConfigTest {

  @Test
  public void loadFromProperties() {

    Properties p = new Properties();
    p.setProperty("ebean.cluster.active", "true");
    p.setProperty("ebean.cluster.serviceName", "a");
    p.setProperty("ebean.cluster.namespace", "b");
    p.setProperty("ebean.cluster.podName", "c");
    p.setProperty("ebean.cluster.port", "42");


    ContainerConfig containerConfig  = new ContainerConfig();
    containerConfig.loadFromProperties(p);

    assertEquals(true, containerConfig.isActive());
    assertEquals("a", containerConfig.getServiceName());
    assertEquals("b", containerConfig.getNamespace());
    assertEquals("c", containerConfig.getPodName());
    assertEquals(42, containerConfig.getPort());
  }
}
