package io.ebeaninternal.dbmigration;

import io.ebean.config.DatabaseConfig;

/**
 * Detect existence of JAXB
 */
public class Detect {

  /**
   * Return true if JAXB is present.
   */
  public static boolean isJAXBPresent(DatabaseConfig config) {
    return config.getClassLoadConfig().isPresent("javax.xml.bind.JAXBException");
  }
}
