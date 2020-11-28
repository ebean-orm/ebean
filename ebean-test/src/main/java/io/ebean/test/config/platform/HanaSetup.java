package io.ebean.test.config.platform;

import java.util.Properties;

class HanaSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    config.setDatabasePlatformName();

    config.ddlMode("dropCreate");
    int instanceNumber = Integer.parseInt(config.getPlatformKey("instanceNumber", "90"));
    if (instanceNumber >= 0 && instanceNumber <= 99) {
      config.setDefaultPort(30017 + (instanceNumber * 100));
    } else {
      config.setDefaultPort(39017);
    }
    config.setUsernameDefault();
    config.setUsername("SYSTEM");
    config.setPassword("HXEHana1");
    config.setDatabaseName("HXE");
    config.setUrl("jdbc:sap://localhost:${port}/?databaseName=${databaseName}");
    String schema = config.getSchema();
    if (schema != null && !schema.equals(config.getUsername())) {
      config.urlAppend("&currentSchema=" + schema);
    }
    config.setDriver("com.sap.db.jdbc.Driver");
    config.datasourceDefaults();

    return dockerProperties(config);
  }

  private Properties dockerProperties(Config dbConfig) {

    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }

    dbConfig.setDockerVersion("latest");

    setDockerProperty("agreeToSapLicense", String.valueOf(false), dbConfig);
    setDockerProperty("passwordsUrl", null, dbConfig);
    setDockerProperty("mountsDirectory", null, dbConfig);
    setDockerProperty("instanceNumber", null, dbConfig);

    return dbConfig.getDockerProperties();
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public void setupExtraDbDataSource(Config config) {
    // not supported yet
  }

  private void setDockerProperty(String key, String defaultValue, Config dbConfig) {
    String value = dbConfig.getPlatformKey(key, defaultValue);
    if (value != null) {
      dbConfig.getDockerProperties().put("hana." + key, value);
    }
  }

}
