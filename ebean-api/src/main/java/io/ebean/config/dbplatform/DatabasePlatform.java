package io.ebean.config.dbplatform;

import io.ebean.BackgroundExecutor;
import io.ebean.Query;
import io.ebean.annotation.PartitionMode;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.config.CustomDbTypeMapping;
import io.ebean.config.PlatformConfig;
import io.ebean.util.JdbcClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Database platform specific settings.
 */
public class DatabasePlatform {

  private static final Logger log = LoggerFactory.getLogger("io.ebean");

  /**
   * Behavior used when ending a query only transaction (at read committed isolation level).
   */
  public enum OnQueryOnly {

    /**
     * Rollback the transaction.
     */
    ROLLBACK,

    /**
     * Commit the transaction
     */
    COMMIT
  }


  /**
   * Set to true for MySql, no other jdbc drivers need this workaround.
   */
  protected boolean useExtraTransactionOnIterateSecondaryQueries;

  protected boolean supportsDeleteTableAlias;

  protected boolean supportsSavepointId = true;

  /**
   * The behaviour used when ending a read only transaction at read committed isolation level.
   */
  protected OnQueryOnly onQueryOnly = OnQueryOnly.COMMIT;

  /**
   * The open quote used by quoted identifiers.
   */
  protected String openQuote = "\"";

  /**
   * The close quote used by quoted identifiers.
   */
  protected String closeQuote = "\"";

  /**
   * When set to true all db column names and table names use quoted identifiers.
   */
  protected boolean allQuotedIdentifiers;

  protected boolean caseSensitiveCollation = true;

  /**
   * Set true if the Database support LIMIT clause on sql update.
   */
  protected boolean inlineSqlUpdateLimit;

  /**
   * For limit/offset, row_number etc limiting of SQL queries.
   */
  protected SqlLimiter sqlLimiter = new LimitOffsetSqlLimiter();

  /**
   * Limit/offset support for SqlQuery only.
   */
  protected BasicSqlLimiter basicSqlLimiter = new BasicSqlLimitOffset();

  /**
   * Mapping of JDBC to Database types.
   */
  protected DbPlatformTypeMapping dbTypeMap = new DbPlatformTypeMapping();

  /**
   * Default values for DB columns.
   */
  protected DbDefaultValue dbDefaultValue = new DbDefaultValue();

  /**
   * Set to true if the DB has native UUID type support.
   */
  protected boolean nativeUuidType;

  /**
   * Defines DB identity/sequence features.
   */
  protected DbIdentity dbIdentity = new DbIdentity();

  protected boolean sequenceBatchMode = true;

  protected int sequenceBatchSize = 20;

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
  protected Platform platform = Platform.GENERIC;

  protected String truncateTable = "truncate table %s";

  protected String columnAliasPrefix;

  /**
   * Use a BackTick ` at the beginning and end of table or column names that you
   * want to use quoted identifiers for. The backticks get converted to the
   * appropriate characters in convertQuotedIdentifiers
   */
  private static final char BACK_TICK = '`';

  /**
   * The non-escaped like clause (to stop slash being escaped on some platforms).
   * Used for the 'raw like' expression but not for startsWith, endsWith and contains expressions.
   */
  protected String likeClauseRaw = "like ? escape''";

  /**
   * Escaped like clause for startsWith, endsWith and contains.
   */
  protected String likeClauseEscaped = "like ? escape'|'";

  /**
   * Escape character used for startsWith, endsWith and contains.
   */
  protected char likeEscapeChar = '|';

  /**
   * Characters escaped for startsWith, endsWith and contains.
   */
  protected char[] likeSpecialCharacters = {'%', '_', '|'};

  protected DbEncrypt dbEncrypt;

  protected boolean idInExpandedForm;

  protected boolean selectCountWithAlias;
  protected boolean selectCountWithColumnAlias;

  /**
   * If set then use the FORWARD ONLY hint when creating ResultSets for
   * findIterate() and findVisit().
   */
  protected boolean forwardOnlyHintOnFindIterate;

  /**
   * If set then use the CONCUR_UPDATABLE hint when creating ResultSets.
   * <p>
   * This is {@code false} for HANA
   */
  protected boolean supportsResultSetConcurrencyModeUpdatable = true;


  /**
   * By default we use JDBC batch when cascading (except for SQL Server and HANA).
   */
  protected PersistBatch persistBatchOnCascade = PersistBatch.ALL;

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

  protected boolean supportsNativeIlike;

  protected SqlExceptionTranslator exceptionTranslator = new SqlCodeTranslator();

  /**
   * Instantiates a new database platform.
   */
  public DatabasePlatform() {
  }

  /**
   * Translate the SQLException into a specific persistence exception if possible.
   */
  public PersistenceException translate(String message, SQLException e) {
    return exceptionTranslator.translate(message, e);
  }

  /**
   * Configure the platform given the server configuration.
   */
  public void configure(PlatformConfig config) {
    this.sequenceBatchSize = config.getDatabaseSequenceBatchSize();
    this.caseSensitiveCollation = config.isCaseSensitiveCollation();
    configureIdType(config.getIdType());
    configure(config, config.isAllQuotedIdentifiers());
  }

  /**
   * Configure UUID Storage etc based on DatabaseConfig settings.
   */
  protected void configure(PlatformConfig config, boolean allQuotedIdentifiers) {
    this.allQuotedIdentifiers = allQuotedIdentifiers;
    addGeoTypes(config.getGeometrySRID());
    configureIdType(config.getIdType());
    dbTypeMap.config(nativeUuidType, config.getDbUuid());
    for (CustomDbTypeMapping mapping : config.getCustomTypeMappings()) {
      if (platformMatch(mapping.getPlatform())) {
        dbTypeMap.put(mapping.getType(), parse(mapping.getColumnDefinition()));
      }
    }
  }

  protected void configureIdType(IdType idType) {
    if (idType != null) {
      this.dbIdentity.setIdType(idType);
    }
  }

  protected void addGeoTypes(int srid) {
    // default has no geo type support
  }

  private DbPlatformType parse(String columnDefinition) {
    return DbPlatformType.parse(columnDefinition);
  }

  private boolean platformMatch(Platform platform) {
    return platform == null || isPlatform(platform);
  }

  /**
   * Return true if this matches the given platform.
   */
  public boolean isPlatform(Platform platform) {
    return this.platform.base() == platform;
  }

  /**
   * Return the platform key.
   */
  public Platform getPlatform() {
    return platform;
  }

  /**
   * Return the name of the underlying Platform in lowercase.
   * <p>
   * "generic" is returned when no specific database platform has been set or found.
   * </p>
   */
  public String getName() {
    return platform.name().toLowerCase();
  }

  /**
   * Return true if we are using Sequence batch mode rather than STEP.
   */
  public boolean isSequenceBatchMode() {
    return sequenceBatchMode;
  }

  /**
   * Set to false to not use sequence batch mode but instead STEP mode.
   */
  public void setSequenceBatchMode(boolean sequenceBatchMode) {
    this.sequenceBatchMode = sequenceBatchMode;
  }

  /**
   * Return true if this database platform supports native ILIKE expression.
   */
  public boolean isSupportsNativeIlike() {
    return supportsNativeIlike;
  }

  /**
   * Return true if the platform supports delete statements with table alias.
   */
  public boolean isSupportsDeleteTableAlias() {
    return supportsDeleteTableAlias;
  }

  /**
   * Return true if the collation is case sensitive.
   * <p>
   * This is expected to be used for testing only.
   * </p>
   */
  public boolean isCaseSensitiveCollation() {
    return caseSensitiveCollation;
  }

  /**
   * Return true if the platform supports SavepointId values.
   */
  public boolean isSupportsSavepointId() {
    return supportsSavepointId;
  }

  /**
   * Return true if the platform supports LIMIT with sql update.
   */
  public boolean isInlineSqlUpdateLimit() {
    return inlineSqlUpdateLimit;
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
   * @param be       the BackgroundExecutor that can be used to load the sequence if
   *                 desired
   * @param ds       the DataSource
   * @param stepSize the sequence allocation size as defined by mapping (defaults to 50)
   * @param seqName  the name of the sequence
   */
  public PlatformIdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds, int stepSize, String seqName) {
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
   * So no except for Postgres and CockroachDB.
   */
  public boolean isNativeArrayType() {
    return false;
  }

  /**
   * Return true if the DB supports native UUID.
   */
  public boolean isNativeUuidType() {
    return nativeUuidType;
  }

  /**
   * Return the mapping of JDBC to DB types.
   *
   * @return the db type map
   */
  public DbPlatformTypeMapping getDbTypeMap() {
    return dbTypeMap;
  }

  /**
   * Return the mapping for DB column default values.
   */
  public DbDefaultValue getDbDefaultValue() {
    return dbDefaultValue;
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
   * Return the close quote for quoted identifiers.
   */
  public String getCloseQuote() {
    return closeQuote;
  }

  /**
   * Return the open quote for quoted identifiers.
   */
  public String getOpenQuote() {
    return openQuote;
  }

  /**
   * Return the JDBC type used to store booleans.
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
   * Return true if the ResultSet CONCUR_UPDATABLE Hint should be used on
   * createNativeSqlTree() PreparedStatements.
   * <p>
   * This specifically is required for Hana which doesn't support CONCUR_UPDATABLE
   * </p>
   */
  public boolean isSupportsResultSetConcurrencyModeUpdatable() {
    return supportsResultSetConcurrencyModeUpdatable;
  }

  /**
   * Set to true if the ResultSet CONCUR_UPDATABLE Hint should be used by default on createNativeSqlTree() PreparedStatements.
   */
  public void setSupportsResultSetConcurrencyModeUpdatable(boolean supportsResultSetConcurrencyModeUpdatable) {
    this.supportsResultSetConcurrencyModeUpdatable = supportsResultSetConcurrencyModeUpdatable;
  }

  /**
   * Normally not needed - overridden in CockroachPlatform.
   */
  public boolean isDdlAutoCommit() {
    return false;
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
   * Return the BasicSqlLimiter for limit/offset of SqlQuery queries.
   */
  public BasicSqlLimiter getBasicSqlLimiter() {
    return basicSqlLimiter;
  }

  /**
   * Set the DB TRUE literal (from the registered boolean ScalarType)
   */
  public void setDbTrueLiteral(String dbTrueLiteral) {
    this.dbDefaultValue.setTrue(dbTrueLiteral);
  }

  /**
   * Set the DB FALSE literal (from the registered boolean ScalarType)
   */
  public void setDbFalseLiteral(String dbFalseLiteral) {
    this.dbDefaultValue.setFalse(dbFalseLiteral);
  }

  /**
   * Convert backticks to the platform specific open quote and close quote
   * <p>
   * Specific plugins may implement this method to cater for platform specific
   * naming rules.
   * </p>
   *
   * @param dbName the db table or column name
   * @return the db table or column name with potentially platform specific quoted identifiers
   */
  public String convertQuotedIdentifiers(String dbName) {
    // Ignore null values e.g. schema name or catalog
    if (dbName != null && !dbName.isEmpty()) {
      if (dbName.charAt(0) == BACK_TICK) {
        if (dbName.charAt(dbName.length() - 1) == BACK_TICK) {
          return openQuote + dbName.substring(1, dbName.length() - 1) + closeQuote;
        } else {
          log.error("Missing backquote on [" + dbName + "]");
        }
      } else if (allQuotedIdentifiers) {
        return openQuote + dbName + closeQuote;
      }
    }
    return dbName;
  }

  /**
   * Remove quoted identifier quotes from the table or column name if present.
   */
  public String unQuote(String dbName) {
    if (dbName != null && !dbName.isEmpty()) {
      if (dbName.startsWith(openQuote)) {
        // trim off the open and close quotes
        return dbName.substring(1, dbName.length() - 1);
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

  /**
   * Return true if select count with subquery needs column alias (SQL Server).
   */
  public boolean isSelectCountWithColumnAlias() {
    return selectCountWithColumnAlias;
  }


  public String completeSql(String sql, Query<?> query) {
    if (query.isForUpdate()) {
      sql = withForUpdate(sql, query.getForUpdateLockWait(), query.getForUpdateLockType());
    }
    return sql;
  }

  /**
   * For update hint on the FROM clause (SQL server only).
   */
  public String fromForUpdate(Query.LockWait lockWait) {
    // return null except for sql server
    return null;
  }

  protected String withForUpdate(String sql, Query.LockWait lockWait, Query.LockType lockType) {
    // silently assume the database does not support the "for update" clause.
    log.info("it seems your database does not support the 'for update' clause");
    return sql;
  }

  /**
   * Returns the like clause used by this database platform.
   * <p>
   * This may include an escape clause to disable a default escape character.
   */
  public String getLikeClause(boolean rawLikeExpression) {
    return rawLikeExpression ? likeClauseRaw : likeClauseEscaped;
  }

  /**
   * Return the platform default JDBC batch mode for persist cascade.
   */
  public PersistBatch getPersistBatchOnCascade() {
    return persistBatchOnCascade;
  }

  /**
   * Return a statement to truncate a table.
   */
  public String truncateStatement(String table) {
    return String.format(truncateTable, table);
  }

  /**
   * Create the DB schema if it does not exist.
   */
  public void createSchemaIfNotExists(String dbSchema, Connection connection) throws SQLException {
    if (!schemaExists(dbSchema, connection)) {
      Statement query = connection.createStatement();
      try {
        log.debug("create schema:{}", dbSchema);
        query.executeUpdate("create schema " + dbSchema);
      } finally {
        JdbcClose.close(query);
      }
    }
  }

  /**
   * Return true if the schema exists.
   */
  public boolean schemaExists(String dbSchema, Connection connection) throws SQLException {
    ResultSet schemas = connection.getMetaData().getSchemas();
    try {
      while (schemas.next()) {
        String schema = schemas.getString(1);
        if (schema.equalsIgnoreCase(dbSchema)) {
          return true;
        }
      }
    } finally {
      JdbcClose.close(schemas);
    }
    return false;
  }

  /**
   * Return true if the table exists.
   */
  public boolean tableExists(Connection connection, String catalog, String schema, String table) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet tables = metaData.getTables(catalog, schema, table, null);
    try {
      return tables.next();
    } finally {
      JdbcClose.close(tables);
    }
  }

  /**
   * Return true if partitions exist for the given table.
   */
  public boolean tablePartitionsExist(Connection connection, String table) throws SQLException {
    return true;
  }

  /**
   * Return the SQL to create an initial partition for the given table.
   */
  public String tablePartitionInit(String tableName, PartitionMode mode, String property, String singlePrimaryKey) {
    return null;
  }

  /**
   * Escapes the like string for this DB-Platform
   */
  public String escapeLikeString(String value) {
    StringBuilder sb = null;
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      boolean escaped = false;
      for (char escapeChar : likeSpecialCharacters) {
        if (ch == escapeChar) {
          if (sb == null) {
            sb = new StringBuilder(value.substring(0, i));
          }
          escapeLikeCharacter(escapeChar, sb);
          escaped = true;
          break;
        }
      }
      if (!escaped && sb != null) {
        sb.append(ch);
      }
    }
    if (sb == null) {
      return value;
    } else {
      return sb.toString();
    }
  }

  protected void escapeLikeCharacter(char ch, StringBuilder sb) {
    sb.append(likeEscapeChar).append(ch);
  }
}
