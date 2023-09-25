package io.ebean.test.config.platform;

import java.util.Properties;

class H2Setup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {
    config.ddlMode("create");
    config.setUsername("sa");
    config.setPassword("");
    config.setUrl("jdbc:h2:mem:${databaseName}");
    config.setDriver("org.h2.Driver");
    var dataSourceConfig = config.datasourceDefaults();

    String nonKeyWords = config.property("ebean.test.nonKeywords", "KEY,VALUE");
    if (!nonKeyWords.isBlank()) {
      dataSourceConfig.addProperty("NON_KEYWORDS", nonKeyWords);
    }
    // return empty properties
    return new Properties();
  }

  @Override
  public void setupExtraDbDataSource(Config config) {
    // not supported yet
  }

  @Override
  public boolean isLocal() {
    return true;
  }
}
