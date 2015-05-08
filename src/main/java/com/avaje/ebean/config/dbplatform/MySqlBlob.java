package com.avaje.ebean.config.dbplatform;

/**
 * Support for blob, mediumblob or longblob selection based on the deployment
 * length.
 * <p>
 * If no deployment length is defined longblob is used.
 * </p>
 */
public class MySqlBlob extends DbType {

  private static final int POWER_2_16 = 65536;
  private static final int POWER_2_24 = 16777216;

  public MySqlBlob() {
    super("blob");
  }

  @Override
  public String renderType(int deployLength, int deployScale) {

    if (deployLength >= POWER_2_24) {
      return "longblob";
    }
    if (deployLength >= POWER_2_16) {
      return "mediumblob";
    }
    if (deployLength < 1) {
      // length not explicitly defined
      return "longblob";
    }
    return "blob";
  }

}
