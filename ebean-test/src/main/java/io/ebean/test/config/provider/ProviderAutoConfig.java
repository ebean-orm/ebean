package io.ebean.test.config.provider;

import io.avaje.applog.AppLog;
import io.ebean.DatabaseBuilder;
import io.ebean.config.*;

import java.util.Properties;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/**
 * Auto configuration of User and Tenant providers and Encrypt key manager for testing purposes.
 */
public class ProviderAutoConfig {

  private static final System.Logger log = AppLog.getLogger("io.ebean.test");

  private final DatabaseBuilder.Settings config;
  private final Properties properties;

  public ProviderAutoConfig(DatabaseBuilder.Settings config) {
    this.config = config;
    this.properties = config.getProperties();
  }

  public void run() {
    int providerSetFlag = 0;

    CurrentUserProvider provider = config.getCurrentUserProvider();
    if (provider == null) {
      providerSetFlag = 1;
      config.setCurrentUserProvider(new WhoUserProvider());
    }

    CurrentTenantProvider tenantProvider = config.getCurrentTenantProvider();
    if (tenantProvider == null) {
      if (Boolean.parseBoolean(properties.getProperty("ebean.test.registerTestTenantProvider", "false"))) {
        providerSetFlag += 2;
        config.setCurrentTenantProvider(new WhoTenantProvider());
      }
    }

    EncryptKeyManager keyManager = config.getEncryptKeyManager();
    if (keyManager == null) {
      // Must be 16 Chars for Oracle function
      String keyVal = properties.getProperty("ebean.test.encryptKey", "simple0123456789");
      log.log(DEBUG, "for testing purposes using FixedEncryptKeyManager() keyVal:{0}", keyVal);
      config.setEncryptKeyManager(new FixedEncryptKeyManager(keyVal));
    }

    if (providerSetFlag > 0) {
      log.log(INFO, msg(providerSetFlag));
    }
  }

  String msg(int providerSetFlag) {
    String msg = msgProvider(providerSetFlag);
    String usage = msgUsage(providerSetFlag);
    return "for testing purposes " + msg + " has been configured. Use io.ebean.test.UserContext to " + usage + " in tests.";
  }

  private String msgProvider(int providerSetFlag) {
    switch (providerSetFlag) {
      case 1:
        return "a current user provider";
      case 2:
        return "a current tenant provider";
      case 3:
        return "a current user and tenant provider";
    }
    return "[unexpected??]";
  }

  private String msgUsage(int providerSetFlag) {
    switch (providerSetFlag) {
      case 1:
        return "set current user";
      case 2:
        return "set current tenant";
      case 3:
        return "set current user and tenant";
    }
    return "[unexpected??]";
  }
}
