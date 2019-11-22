package io.ebean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * Class to determine the ebean version. (
 * @author Roland Praml, FOCONIS AG
 *
 */
public class EbeanVersion {
  private EbeanVersion() {

  }
  private static final Logger logger = LoggerFactory.getLogger(EbeanVersion.class);

  private static String version = "unknown";
  private static String requiredAgentVersion = "unknown";
  private static String currentAgentVersion = null;
  static {
    try {
      Properties prop = new Properties();
      try (InputStream in = Ebean.class.getResourceAsStream("/META-INF/maven/io.ebean/ebean/pom.properties")) {
        if (in != null) {
          prop.load(in);
          in.close();
          version = prop.getProperty("version");
          requiredAgentVersion = prop.getProperty("agent.version");
        }
      }
      try {
        Class cls = Class.forName("io.ebean.enhance.Transformer");
        currentAgentVersion = (String) cls.getMethod("getVersion").invoke(null);
      } catch (Exception e) {
        logger.trace("could not get run-time-agent version", e);
      }
      logger.info("ebean version: {} (depends on agent {})", version, requiredAgentVersion);
      if (currentAgentVersion != null) {
        if (!currentAgentVersion.equals(requiredAgentVersion)) {
          logger.error("Runtime enhancement detected. Agent version mismatch");
          logger.error("      Current:  {}", currentAgentVersion);
          logger.error("      Required: {}", requiredAgentVersion);
          logger.error("THIS CAN LEAD IN UNEXPECTED BEHAVIOUR!");
        } else {
          logger.info("Runtime enhancement detected. Agent version matches with required version.");
        }
      }
    } catch (IOException e) {
      logger.warn("Could not determine ebean version: {}", e.getMessage());
    }
  }

  /**
   * Returns the ebean version (read from /META-INF/maven/io.ebean/ebean/pom.properties)
   */
  public static String getVersion() {
    return version;
  }

}
