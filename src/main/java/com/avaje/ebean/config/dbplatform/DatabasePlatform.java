package com.avaje.ebean.config.dbplatform;

import java.sql.Types;

import javax.sql.DataSource;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.Query;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database platform specific settings.
 */
public class DatabasePlatform {

  private static final Logger logger = LoggerFactory.getLogger(DatabasePlatform.class);


  /**
   * Behavior used when ending a query only transaction (at read committed isolation level).
   */
  public enum OnQueryOnly {

    /**
     * Rollback the transaction.
     */
    ROLLBACK,

    /**
     * Just close the transaction. Valid at READ_COMMITTED isolation and preferred on some Databases
     * as a performance optimisation.
     */
    CLOSE,

    /**
     * Commit the transaction
     */
    COMMIT
  }


  /**
   * Set to true for MySql, no other jdbc drivers need this workaround.
   */
  protected boolean useExtraTransactionOnIterateSecondaryQueries;

  /**
   * The behaviour used when ending a read only transaction at read committed isolation level.
   */
  protected OnQueryOnly onQueryOnly = OnQueryOnly.ROLLBACK;

  /** 
   * The open quote used by quoted identifiers. 
   */
  protected String openQuote = "\"";

  /**
   *  The close quote used by quoted identifiers. 
   */
  protected String closeQuote = "\"";

  /** 
   * For limit/offset, row_number etc limiting of SQL queries. 
   */
  protected SqlLimiter sqlLimiter = new LimitOffsetSqlLimiter();

  /** 
   * Mapping of JDBC to Database types. 
   */
  protected DbTypeMap dbTypeMap = new DbTypeMap();

  /**
   * Defines DB identity/sequence features. 
   */
  protected DbIdentity dbIdentity = new DbIdentity();

  /**
   * The history support for this database platform.
   */
  protected DbHistorySupport historySupport;

  /** 
   * The JDBC type to map booleans to (by default). 
   */
  protected int booleanDbType = Types.BOOLEAN;

  /** 
   * The JDBC type to map Blob to. 
   */
  protected int blobDbType = Types.BLOB;

  /** 
   * The JDBC type to map Clob to. 
   */
  protected int clobDbType = Types.CLOB;

  /** 
   * For Oracle treat empty strings as null. 
   */
  protected boolean treatEmptyStringsAsNull;

  /** 
   * The database platform name. 
   */
  protected String name = "generic";

  protected String columnAliasPrefix = "c";

  protected String tableAliasPlaceHolder = "${ta}";

  /**
   * Use a BackTick ` at the beginning and end of table or column names that you
   * want to use quoted identifiers for. The backticks get converted to the
   * appropriate characters in convertQuotedIdentifiers
   */
  private static final char BACK_TICK = '`';

  /**
   * The like clause. Can be overridden to disable default escape character.
   */
  protected String likeClause = "like ?";
  
  protected DbEncrypt dbEncrypt;

  protected boolean idInExpandedForm;

  protected boolean selectCountWithAlias;

  /**
   * If set then use the FORWARD ONLY hint when creating ResultSets for
   * findIterate() and findVisit().
   */
  protected boolean forwardOnlyHintOnFindIterate;

  /**
   * Flag set for SQL Server due to lack of support of getGeneratedKeys in
   * batch mode (meaning for batch inserts you should explicitly turn off
   * getGeneratedKeys - joy).
   */
  protected boolean disallowBatchOnCascade;

  protected PlatformDdl platformDdl;

  /**
   * The maximum length of table names - used specifically when derived
   * default table names for intersection tables.
   */
  protected int maxTableNameLength = 60;

  /**
   * A value of 60 is a reasonable default for all databases except
   * Oracle (limited to 30) and DB2 (limited to 18).
   */
  protected int maxConstraintNameLength = 60;

  /**
   * Instantiates a new database platform.
   */
  public DatabasePlatform() {
  }

  /**
   * Return the name of the DatabasePlatform.
   * <p>
   * "generic" is returned when no specific database platform has been set or
   * found.
   * </p>
   */
  public String getName() {
    return name;
  }

  /**
   * Return the maximum table name length.
   * <p>
   * This is used when deriving names of intersection tables.
   * </p>
   */
  public int getMaxTableNameLength() {
    return maxTableNameLength;
  }

  /**
   * Return the maximum constraint name allowed for the platform.
   */
  public int getMaxConstraintNameLength() {
    return maxConstraintNameLength;
  }

  /**
   * Return the platform specific DDL.
   */
  public PlatformDdl getPlatformDdl() {
    return platformDdl;
  }

  /**
   * Create and return a DDL handler for generating DDL scripts.
   */
  public DdlHandler createDdlHandler(ServerConfig serverConfig) {
    return platformDdl.createDdlHandler(serverConfig);
  }

  /**
   * Return true if the JDBC driver does not allow additional queries to execute
   * when a resultSet is being 'streamed' as is the case with findEach() etc.
   * <p>
   * Honestly, this is a workaround for a stupid MySql JDBC driver limitation.
   * </p>
   */
  public boolean useExtraTransactionOnIterateSecondaryQueries() {
    return useExtraTransactionOnIterateSecondaryQueries;
  }

  /**
   * Return a DB Sequence based IdGenerator.
   * 
   * @param be
   *          the BackgroundExecutor that can be used to load the sequence if
   *          desired
   * @param ds
   *          the DataSource
   * @param seqName
   *          the name of the sequence
   * @param batchSize
   *          the number of sequences that should be loaded
   */
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    return null;
  }

  /**
   * Return the behaviour to use when ending a read only transaction.
   */
  public OnQueryOnly getOnQueryOnly() {
    return onQueryOnly;
  }

  /**
   * Set the behaviour to use when ending a read only transaction.
   */
  public void setOnQueryOnly(OnQueryOnly onQueryOnly) {
    this.onQueryOnly = onQueryOnly;
  }

  /**
   * Return the DbEncrypt handler for this DB platform.
   */
  public DbEncrypt getDbEncrypt() {
    return dbEncrypt;
  }

  /**
   * Set the DbEncrypt handler for this DB platform.
   */
  public void setDbEncrypt(DbEncrypt dbEncrypt) {
    this.dbEncrypt = dbEncrypt;
  }

  /**
   * Return the history support for this database platform.
   */
  public DbHistorySupport getHistorySupport() {
    return historySupport;
  }

  /**
   * Set the history support for this database platform.
   */
  public void setHistorySupport(DbHistorySupport historySupport) {
    this.historySupport = historySupport;
  }

  /**
   * Return the mapping of JDBC to DB types.
   * 
   * @return the db type map
   */
  public DbTypeMap getDbTypeMap() {
    return dbTypeMap;
  }

  /**
   * Return the column alias prefix.
   */
  public String getColumnAliasPrefix() {
    return columnAliasPrefix;
  }

  /**
   * Set the column alias prefix.
   */
  public void setColumnAliasPrefix(String columnAliasPrefix) {
    this.columnAliasPrefix = columnAliasPrefix;
  }

  /**
   * Return the table alias placeholder.
   */
  public String getTableAliasPlaceHolder() {
    return tableAliasPlaceHolder;
  }

  /**
   * Set the table alias placeholder.
   */
  public void setTableAliasPlaceHolder(String tableAliasPlaceHolder) {
    this.tableAliasPlaceHolder = tableAliasPlaceHolder;
  }

  /**
   * Return the close quote for quoted identifiers.
   * 
   * @return the close quote
   */
  public String getCloseQuote() {
    return closeQuote;
  }

  /**
   * Return the open quote for quoted identifiers.
   * 
   * @return the open quote
   */
  public String getOpenQuote() {
    return openQuote;
  }

  /**
   * Return the JDBC type used to store booleans.
   * 
   * @return the boolean db type
   */
  public int getBooleanDbType() {
    return booleanDbType;
  }

  /**
   * Return the data type that should be used for Blob.
   * <p>
   * This is typically Types.BLOB but for Postgres is Types.LONGVARBINARY for
   * example.
   * </p>
   */
  public int getBlobDbType() {
    return blobDbType;
  }

  /**
   * Return the data type that should be used for Clob.
   * <p>
   * This is typically Types.CLOB but for Postgres is Types.VARCHAR.
   * </p>
   */
  public int getClobDbType() {
    return clobDbType;
  }

  /**
   * Return true if empty strings should be treated as null.
   * 
   * @return true, if checks if is treat empty strings as null
   */
  public boolean isTreatEmptyStringsAsNull() {
    return treatEmptyStringsAsNull;
  }

  /**
   * Return true if a compound ID in (...) type expression needs to be in
   * expanded form of (a=? and b=?) or (a=? and b=?) or ... rather than (a,b) in
   * ((?,?),(?,?),...);
   */
  public boolean isIdInExpandedForm() {
    return idInExpandedForm;
  }

  /**
   * Return true if the ResultSet TYPE_FORWARD_ONLY Hint should be used on
   * findIterate() and findVisit() PreparedStatements.
   * <p>
   * This specifically is required for MySql when processing large results.
   * </p>
   */
  public boolean isForwardOnlyHintOnFindIterate() {
    return forwardOnlyHintOnFindIterate;
  }

  /**
   * Set to true if the ResultSet TYPE_FORWARD_ONLY Hint should be used by default on findIterate PreparedStatements.
   */
  public void setForwardOnlyHintOnFindIterate(boolean forwardOnlyHintOnFindIterate) {
    this.forwardOnlyHintOnFindIterate = forwardOnlyHintOnFindIterate;
  }

  /**
   * Return the DB identity/sequence features for this platform.
   * 
   * @return the db identity
   */
  public DbIdentity getDbIdentity() {
    return dbIdentity;
  }

  /**
   * Return the SqlLimiter used to apply additional sql around a query to limit
   * its results.
   * <p>
   * Basically add the clauses for limit/offset, rownum, row_number().
   * </p>
   * 
   * @return the sql limiter
   */
  public SqlLimiter getSqlLimiter() {
    return sqlLimiter;
  }

  /**
   * Convert backticks to the platform specific open quote and close quote
   * 
   * <p>
   * Specific plugins may implement this method to cater for platform specific
   * naming rules.
   * </p>
   * 
   * @param dbName
   *          the db name
   * 
   * @return the string
   */
  public String convertQuotedIdentifiers(String dbName) {
    // Ignore null values e.g. schema name or catalog
    if (dbName != null && dbName.length() > 0) {
      if (dbName.charAt(0) == BACK_TICK) {
        if (dbName.charAt(dbName.length() - 1) == BACK_TICK) {

          String quotedName = getOpenQuote();
          quotedName += dbName.substring(1, dbName.length() - 1);
          quotedName += getCloseQuote();

          return quotedName;

        } else {
          logger.error("Missing backquote on [" + dbName + "]");
        }
      }
    }
    return dbName;
  }

  /**
   * Set to true if select count against anonymous view requires an alias.
   */
  public boolean isSelectCountWithAlias() {
    return selectCountWithAlias;
  }

  public String completeSql(String sql, Query<?> query) {
    if (Boolean.TRUE.equals(query.isForUpdate())) {
      sql = withForUpdate(sql);
    }

    return sql;
  }

  protected String withForUpdate(String sql) {
    // silently assume the database does not support the "for update" clause.
    logger.info("it seems your database does not support the 'for update' clause");

    return sql;
  }

  /**
   * Returns the like clause used by this database platform.
   * <p>
   * This may include an escape clause to disable a default escape character.
   */
  public String getLikeClause() {
    return likeClause;
  }

  /**
   * Return true if the persistBatchOnCascade setting should be ignored.
   * <p>
   * This is primarily for SQL Server which does not support getGeneratedKeys with jdbc batch mode
   * so can't really be transparently used.
   * </p>
   */
  public boolean isDisallowBatchOnCascade() {
    return disallowBatchOnCascade;
  }

}
