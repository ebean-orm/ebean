package io.ebean.test.config.platform;

import java.util.Properties;

interface PlatformSetup {

  /**
   * Return true if a local database (H2 and Sqlite - don't need a database name to be configured).
   */
  boolean isLocal();

  /**
   * Run the setup for the given platform (set DataSource and DDL configuration).
   *
   * Return the properties used to configure the docker container.
   */
  Properties setup(Config dbConfig);

  /**
   * Set DataSource configuration for the extra database.
   */
  void setupExtraDbDataSource(Config config);
}
