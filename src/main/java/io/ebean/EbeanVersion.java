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
  static {
    try {
      Properties prop = new Properties();
      try (InputStream in = Ebean.class.getResourceAsStream("/META-INF/maven/io.ebean/ebean/pom.properties")) {
        if (in != null) {
          prop.load(in);
          in.close();
          version = prop.getProperty("version");
        }
      }
      logger.info("ebean version: {}", version);
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
