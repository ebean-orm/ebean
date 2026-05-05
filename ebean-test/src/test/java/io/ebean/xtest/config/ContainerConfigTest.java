package io.ebean.xtest.config;

import io.ebean.config.ContainerConfig;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerConfigTest {

  @Test
  void loadFromProperties() {
    Properties p = new Properties();
    p.setProperty("ebean.cluster.active", "true");
    p.setProperty("ebean.cluster.serviceName", "a");
    p.setProperty("ebean.cluster.namespace", "b");
    p.setProperty("ebean.cluster.podName", "c");
    p.setProperty("ebean.cluster.port", "42");

    ContainerConfig containerConfig  = new ContainerConfig();
    containerConfig.loadFromProperties(p);

    assertThat(containerConfig.isActive()).isTrue();
    assertThat(containerConfig.getServiceName()).isEqualTo("a");
    assertThat(containerConfig.getNamespace()).isEqualTo("b");
    assertThat(containerConfig.getPodName()).isEqualTo("c");
    assertThat(containerConfig.getPort()).isEqualTo(42);
  }
}
