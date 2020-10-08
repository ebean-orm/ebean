package io.ebean.test.config.platform;

import java.util.Properties;

class SqliteSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    config.ddlMode("dropCreate");
    config.setUsername("");
    config.setPassword("");
    config.setUrl("jdbc:sqlite:${databaseName}");
    config.setDriver("org.sqlite.JDBC");
    config.datasourceProperty("isolationlevel", "read_uncommitted");
    config.datasourceDefaults();

    // return empty properties
    return new Properties();
  }

  @Override
  public void setupExtraDbDataSource(Config config) {
    // not supported
  }

  @Override
  public boolean isLocal() {
    return true;
  }
}
