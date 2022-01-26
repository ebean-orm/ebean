package io.ebean.test.config.platform;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DockerHostTest {

  @Test
  void runningInDocker_when_false_alwaysUseLocalhost() {
    DockerHost dockerHost = new DockerHost();
    assertFalse(dockerHost.runningInDocker());
    assertEquals("localhost", dockerHost.dockerHost("foo"));
  }

  @Test
  void runningInDocker_when_true_useExplicit() {
    TDDockerHost dockerHost = new TDDockerHost();
    assertTrue(dockerHost.runningInDocker());

    assertEquals("my-host", dockerHost.dockerHost("my-host"));
  }

  @Disabled
  @Test
  void runningInDocker_when_trueAndLinux_useDefault() {
    TDDockerHost dockerHost = new TDDockerHost();
    assertTrue(dockerHost.runningInDocker());

    assertEquals("172.17.0.1", dockerHost.dockerHost(null));
  }

  @Test
  void runningInDocker_when_windowsDefault() {
    TDDockerHost dockerHost = new TDDockerHost();
    assertTrue(dockerHost.runningInDocker());

    String origName = System.getProperty("os.name");
    System.setProperty("os.name", "win");
    try {
      assertEquals("host.docker.internal",dockerHost.defaultDockerHost());
      assertEquals("host.docker.internal", dockerHost.dockerHost(null));
    } finally {
      System.setProperty("os.name", origName);
    }
  }

  @Test
  void runningInDocker_when_macDefault() {
    TDDockerHost dockerHost = new TDDockerHost();
    assertTrue(dockerHost.runningInDocker());

    String origName = System.getProperty("os.name");
    System.setProperty("os.name", "mac");
    try {
      assertEquals("host.docker.internal",dockerHost.defaultDockerHost());
      assertEquals("host.docker.internal", dockerHost.dockerHost(null));
    } finally {
      System.setProperty("os.name", origName);
    }
  }


  @Test
  void runningInDocker_when_linuxDefault() {
    TDDockerHost dockerHost = new TDDockerHost();
    assertTrue(dockerHost.runningInDocker());

    String origName = System.getProperty("os.name");
    System.setProperty("os.name", "linux");
    try {
      assertEquals("172.17.0.1",dockerHost.defaultDockerHost());
      assertEquals("172.17.0.1", dockerHost.dockerHost(null));
    } finally {
      System.setProperty("os.name", origName);
    }
  }

  static class TDDockerHost extends DockerHost {

    @Override
    boolean initInDocker() {
      return true;
    }
  }
}
