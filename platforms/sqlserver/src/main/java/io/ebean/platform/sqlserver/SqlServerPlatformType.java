package io.ebean.platform.sqlserver;

import io.ebean.config.dbplatform.DbPlatformType;

/**
 * SqlServer supports various max lengths for string and binary data types.
 * See: https://learn.microsoft.com/en-us/sql/t-sql/data-types/binary-and-varbinary-transact-sql?view=sql-server-ver15
 * @author Roland Praml, FOCONIS AG
 */
public class SqlServerPlatformType extends DbPlatformType {
  private final int maxLength;

  public SqlServerPlatformType(String name, int defaultLength, int maxLength) {
    super(name, defaultLength);
    this.maxLength = maxLength;
  }

  @Override
  protected void renderLengthScale(int deployLength, int deployScale, StringBuilder sb) {
    int len = deployLength != 0 ? deployLength : getDefaultLength();
    if (len > maxLength) {
      sb.append("(max)");
    } else {
      super.renderLengthScale(deployLength, deployScale, sb);
    }
  }
}
