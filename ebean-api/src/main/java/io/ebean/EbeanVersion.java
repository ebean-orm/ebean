package io.ebean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Class to determine the ebean version.
 *
 * @author Roland Praml, FOCONIS AG
 */
public final class EbeanVersion {

  private static final Logger log = LoggerFactory.getLogger("io.ebean");

  /**
   * Maintain the minimum ebean-agent version manually based on required ebean-agent bug fixes.
   */
  private static final int MIN_AGENT_MAJOR_VERSION = 12;
  private static final int MIN_AGENT_MINOR_VERSION = 12;
  private static String version = "unknown";
  static {
    readVersion();
    checkAgentVersion();
  }

  private static void readVersion() {
    try {
      try (InputStream in = ClassLoader.getSystemResourceAsStream("META-INF/ebean-maven-version.txt")) {
        if (in != null) {
          try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(in))) {
            version = reader.readLine();
            log.info("ebean version: {}", version);
          }
        }
      }
    } catch (IOException e) {
      log.warn("Could not determine ebean version: {}", e.getMessage());
    }
  }

  private static void checkAgentVersion() {
    try {
      try (InputStream in = ClassLoader.getSystemResourceAsStream("META-INF/maven/io.ebean/ebean-agent/pom.properties")) {
        // often we only have ebean-agent during development (with build time enhancement), null is expected
        if (in != null) {
          String agentVersion = readVersion(in);
          if (agentVersion != null) {
            if (checkMinAgentVersion(agentVersion)) {
              log.error("Expected minimum ebean-agent version {}.{}.0 but we have {}, please update the ebean-agent", MIN_AGENT_MAJOR_VERSION, MIN_AGENT_MINOR_VERSION, agentVersion);
            }
          }
        }
      }
    } catch (IOException e) {
      log.warn("Could not check minimum ebean-agent version {}.{}.0 required due to - {}", MIN_AGENT_MAJOR_VERSION, MIN_AGENT_MINOR_VERSION, e.getMessage());
    }
  }

  /**
   * Return true if ebean-agent is NOT at our minimum version.
   */
  static boolean checkMinAgentVersion(String agentVersion) {
    String[] versionSegments = agentVersion.split("\\.");
    if (versionSegments.length != 3) {
      return true;
    } else {
      int major = Integer.parseInt(versionSegments[0]);
      int minor = Integer.parseInt(versionSegments[1]);
      if (major < MIN_AGENT_MAJOR_VERSION) {
        return true;
      } else {
        return major == MIN_AGENT_MAJOR_VERSION && minor < MIN_AGENT_MINOR_VERSION;
      }
    }
  }

  private static String readVersion(InputStream in) throws IOException {
    Properties prop = new Properties();
    prop.load(in);
    in.close();
    return prop.getProperty("version");
  }

  private EbeanVersion() {
    // hide
  }

  /**
   * Returns the ebean version (read from META-INF/ebean-maven-version.txt)
   */
  public static String getVersion() {
    return version;
  }

}
