package io.ebean.test.config.platform;

import java.io.File;
import java.util.Locale;

/**
 * Helper to detect if running inside docker and determine host name for that case.
 */
class DockerHost {

  private final boolean runningInDocker;
  private String dockerHost;

  DockerHost() {
    runningInDocker = initInDocker();
  }

  boolean runningInDocker() {
    return runningInDocker;
  }

  String dockerHost() {
    return dockerHost;
  }

  String dockerHost(String explicitHost) {
    if (!runningInDocker) {
      return "localhost";
    }
    dockerHost = explicitHost != null ? explicitHost : defaultDockerHost();
    return dockerHost;
  }

  /**
   * Return true if running inside a docker container (we are using docker in docker).
   */
  boolean initInDocker() {
    return new File("/.dockerenv").exists();
  }

  /**
   * Return the default host name to use when running in docker.
   * <p>
   * Can instead be explicitly specified via <code>ebean.test.dockerHost</code>.
   */
  String defaultDockerHost() {
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    if (os.contains("mac") || os.contains("darwin") || os.contains("win")) {
      return "host.docker.internal";
    } else {
      return "172.17.0.1";
    }
  }

}
