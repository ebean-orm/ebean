package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * MySQL specific platform.
 * <p>
 * <ul>
 * <li>supportsGetGeneratedKeys = true</li>
 * <li>Uses LIMIT OFFSET clause</li>
 * <li>Uses ` for quoted identifiers</li>
 * </ul>
 * </p>
 */
public class MySqlPlatform extends DatabasePlatform {

  public MySqlPlatform() {
    super();
    this.name = "mysql";
    this.selectCountWithAlias = true;
    this.dbEncrypt = new MySqlDbEncrypt();

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsIdentity(true);
    this.dbIdentity.setSupportsSequence(false);

    this.openQuote = "`";
    this.closeQuote = "`";

    this.booleanDbType = Types.BIT;

    dbTypeMap.put(Types.BIT, new DbType("tinyint(1) default 0"));
    dbTypeMap.put(Types.BOOLEAN, new DbType("tinyint(1) default 0"));
    dbTypeMap.put(Types.TIMESTAMP, new DbType("datetime"));
    dbTypeMap.put(Types.CLOB, new MySqlClob());
    dbTypeMap.put(Types.BLOB, new MySqlBlob());
    dbTypeMap.put(Types.BINARY, new DbType("binary", 255));
    dbTypeMap.put(Types.VARBINARY, new DbType("varbinary", 255));

    dbDdlSyntax.setMaxConstraintNameLength(64);
    dbDdlSyntax.setDisableReferentialIntegrity("SET FOREIGN_KEY_CHECKS=0");
    dbDdlSyntax.setEnableReferentialIntegrity("SET FOREIGN_KEY_CHECKS=1");
    dbDdlSyntax.setForeignKeySuffix("on delete restrict on update restrict");

  }

  /**
   * Return null in case there is a sequence annotation.
   */
  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be,
      DataSource ds, String seqName, int batchSize) {

    return null;
  }

  @Override
  protected String withForUpdate(String sql) {
    return sql + " for update";
  }
}
