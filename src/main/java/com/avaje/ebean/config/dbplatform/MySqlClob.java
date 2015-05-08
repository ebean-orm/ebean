package com.avaje.ebean.config.dbplatform;

/**
 * Support for text, mediumtext or longtext selection based on the deployment
 * length.
 * <p>
 * If no deployment length is defined longtext is used.
 * </p>
 */
public class MySqlClob extends DbType {

  private static final int POWER_2_16 = 65536;
  private static final int POWER_2_24 = 16777216;

  public MySqlClob() {
    super("text");
  }

  @Override
  public String renderType(int deployLength, int deployScale) {

    if (deployLength >= POWER_2_24) {
      return "longtext";
    }
    if (deployLength >= POWER_2_16) {
      return "mediumtext";
    }
    if (deployLength < 1) {
      // length not explicitly defined
      return "longtext";
    }
    return "text";
  }

}
