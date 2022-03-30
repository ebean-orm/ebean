package io.ebean.platform.hana;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Hana platform provider.
 */
public class HanaPlatformProvider implements DatabasePlatformProvider {

  @Override
  public String toString() {
    return "Hana";
  }

  @Override
  public boolean match(String name) {
    return name.equals("hana");
  }

  @Override
  public DatabasePlatform create(String name) {
    return new HanaPlatform();
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("hdb");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    return new HanaPlatform();
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    return Platform.HANA.equals(platform);
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    return new HanaPlatform();
  }
}
