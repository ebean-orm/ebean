package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * Postgres v9 specific platform.
 * <p>
 * Uses serial types and getGeneratedKeys.
 * </p>
 */
public class PostgresPlatform extends DatabasePlatform {

  /**
   * Unique jdbc type id defined for hstore type.
   */
  public static final int TYPE_HSTORE = 4001;
  
  public PostgresPlatform() {
    super();
    this.name = "postgres";
    this.likeClause = "like ? escape''";
    
    this.dbDdlSyntax = new PostgresDdlSyntax();
    
    this.selectCountWithAlias = true;
    this.blobDbType = Types.LONGVARBINARY;
    this.clobDbType = Types.VARCHAR;

    this.dbEncrypt = new PostgresDbEncrypt();

    // Use Identity and getGeneratedKeys
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);

    this.columnAliasPrefix = "as c";

    this.openQuote = "\"";
    this.closeQuote = "\"";

    dbTypeMap.put(TYPE_HSTORE, new DbType("hstore"));
    
    dbTypeMap.put(Types.INTEGER, new DbType("integer", false));
    dbTypeMap.put(Types.DOUBLE, new DbType("float"));
    dbTypeMap.put(Types.TINYINT, new DbType("smallint"));
    dbTypeMap.put(Types.DECIMAL, new DbType("decimal", 38));

    dbTypeMap.put(Types.BINARY, new DbType("bytea", false));
    dbTypeMap.put(Types.VARBINARY, new DbType("bytea", false));

    dbTypeMap.put(Types.BLOB, new DbType("bytea", false));
    dbTypeMap.put(Types.CLOB, new DbType("text"));
    dbTypeMap.put(Types.LONGVARBINARY, new DbType("bytea", false));
    dbTypeMap.put(Types.LONGVARCHAR, new DbType("text"));

    dbDdlSyntax.setDropTableCascade("cascade");
    dbDdlSyntax.setDropIfExists("if exists");

  }

  /**
   * Create a Postgres specific sequence IdGenerator.
   */
  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds,
      String seqName, int batchSize) {

    return new PostgresSequenceIdGenerator(be, ds, seqName, batchSize);
  }

  @Override
  protected String withForUpdate(String sql) {
    return sql + " for update";
  }
}
