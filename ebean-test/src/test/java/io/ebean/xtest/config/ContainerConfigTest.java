package io.ebean.xtest.config;

import io.ebean.config.ContainerConfig;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerConfigTest {

  @Test
  void loadFromProperties() {
    ContainerConfig containerConfig  = new ContainerConfig();

    assertThat(containerConfig.isActive()).isFalse();
    assertThat(containerConfig.getServiceName()).isNull();
    assertThat(containerConfig.getNamespace()).isNull();
    assertThat(containerConfig.getPodName()).isNull();
    assertThat(containerConfig.getPort()).isEqualTo(0);
  }
}
