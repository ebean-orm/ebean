package io.ebean.config;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Configuration for DB types such as UUID, Geometry etc.
 */
public class PlatformConfig {

  private boolean allQuotedIdentifiers;

  /**
   * Set this to true for Postgres FOR UPDATE to use NO KEY option.
   */
  private boolean forUpdateNoKey;

  private DbConstraintNaming constraintNaming;

  /**
   * Flag set when a supplied constraintNaming is used.
   */
  private boolean customConstraintNaming;

  /**
   * The database boolean true value (typically either 1, T, or Y).
   */
  private String databaseBooleanTrue;

  /**
   * The database boolean false value (typically either 0, F or N).
   */
  private String databaseBooleanFalse;

  /**
   * For DB's using sequences this is the number of sequence values prefetched.
   */
  private int databaseSequenceBatchSize = 20;

  /**
   * Set for DB's that support both Sequence and Identity (and the default choice is not desired).
   */
  private IdType idType;

  /**
   * The Geometry SRID value (default 4326).
   */
  private int geometrySRID = 4326;

  /**
   * Setting to indicate if UUID should be stored as binary(16) or varchar(40) or native DB type (for H2 and Postgres).
   */
  private DbUuid dbUuid = DbUuid.AUTO_VARCHAR;

  /**
   * Set to true to force InetAddress to map to Varchar (for Postgres rather than INET)
   */
  private boolean databaseInetAddressVarchar;

  private boolean caseSensitiveCollation = true;

  /**
   * Modify the default mapping of standard types such as default precision for DECIMAL etc.
   */
  private List<CustomDbTypeMapping> customDbTypeMappings = new ArrayList<>();

  /**
   * Construct with defaults.
   */
  public PlatformConfig() {
    this.constraintNaming = new DbConstraintNaming();
  }

  /**
   * Construct based on given config - typically for DbMigration generation with many platforms.
   */
  public PlatformConfig(PlatformConfig platformConfig) {
    this.forUpdateNoKey = platformConfig.forUpdateNoKey;
    this.databaseBooleanFalse = platformConfig.databaseBooleanFalse;
    this.databaseBooleanTrue = platformConfig.databaseBooleanTrue;
    this.databaseSequenceBatchSize = platformConfig.databaseSequenceBatchSize;
    this.idType = platformConfig.idType;
    this.geometrySRID = platformConfig.geometrySRID;
    this.dbUuid = platformConfig.dbUuid;
    this.caseSensitiveCollation = platformConfig.caseSensitiveCollation;
    this.allQuotedIdentifiers = platformConfig.allQuotedIdentifiers;
    this.databaseInetAddressVarchar = platformConfig.databaseInetAddressVarchar;
    this.customDbTypeMappings = platformConfig.customDbTypeMappings;
    this.constraintNaming = new DbConstraintNaming(!allQuotedIdentifiers);
  }

  public DbConstraintNaming getConstraintNaming() {
    return constraintNaming;
  }

  /**
   * Set a custom database constraint naming convention.
   */
  public void setConstraintNaming(DbConstraintNaming constraintNaming) {
    this.customConstraintNaming = true;
    this.constraintNaming = constraintNaming;
  }

  /**
   * Return true if all DB column and table names should use quoted identifiers.
   */
  public boolean isAllQuotedIdentifiers() {
    return allQuotedIdentifiers;
  }

  /**
   * Set to true if all DB column and table names should use quoted identifiers.
   * <p>
   * For Postgres pgjdbc version 42.3.0 should be used with datasource property
   * <em>quoteReturningIdentifiers</em> set to <em>false</em> (refer #2303).
   */
  public void setAllQuotedIdentifiers(boolean allQuotedIdentifiers) {
    this.allQuotedIdentifiers = allQuotedIdentifiers;
    if (!customConstraintNaming) {
      this.constraintNaming = new DbConstraintNaming(!allQuotedIdentifiers);
    }
  }

  /**
   * Return true if the collation is case sensitive.
   */
  public boolean isCaseSensitiveCollation() {
    return caseSensitiveCollation;
  }

  /**
   * Set to false to indicate that the collation is case insensitive.
   */
  public void setCaseSensitiveCollation(boolean caseSensitiveCollation) {
    this.caseSensitiveCollation = caseSensitiveCollation;
  }

  /**
   * Return true if Postgres FOR UPDATE should use the NO KEY option.
   */
  public boolean isForUpdateNoKey() {
    return forUpdateNoKey;
  }

  /**
   * Set to true such that Postgres FOR UPDATE should use the NO KEY option.
   */
  public void setForUpdateNoKey(boolean forUpdateNoKey) {
    this.forUpdateNoKey = forUpdateNoKey;
  }

  /**
   * Return a value used to represent TRUE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * <p>
   * The value returned is either a Integer or a String (e.g. "1", or "T").
   */
  public String getDatabaseBooleanTrue() {
    return databaseBooleanTrue;
  }

  /**
   * Set the value to represent TRUE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * <p>
   * The value set is either a Integer or a String (e.g. "1", or "T").
   */
  public void setDatabaseBooleanTrue(String databaseBooleanTrue) {
    this.databaseBooleanTrue = databaseBooleanTrue;
  }

  /**
   * Return a value used to represent FALSE in the database.
   */
  public String getDatabaseBooleanFalse() {
    return databaseBooleanFalse;
  }

  /**
   * Set the value used to represent FALSE in the database.
   */
  public void setDatabaseBooleanFalse(String databaseBooleanFalse) {
    this.databaseBooleanFalse = databaseBooleanFalse;
  }

  /**
   * Return the number of DB sequence values that should be preallocated.
   */
  public int getDatabaseSequenceBatchSize() {
    return databaseSequenceBatchSize;
  }

  /**
   * Set the number of DB sequence values that should be preallocated.
   */
  public void setDatabaseSequenceBatchSize(int databaseSequenceBatchSize) {
    this.databaseSequenceBatchSize = databaseSequenceBatchSize;
  }

  /**
   * Return the Geometry SRID.
   */
  public int getGeometrySRID() {
    return geometrySRID;
  }

  /**
   * Set the Geometry SRID.
   */
  public void setGeometrySRID(int geometrySRID) {
    this.geometrySRID = geometrySRID;
  }

  /**
   * Return the DB type used to store UUID.
   */
  public DbUuid getDbUuid() {
    return dbUuid;
  }

  /**
   * Set the DB type used to store UUID.
   */
  public void setDbUuid(DbUuid dbUuid) {
    this.dbUuid = dbUuid;
  }

  /**
   * Return the IdType to use (or null for the default choice).
   */
  public IdType getIdType() {
    return idType;
  }

  /**
   * Set the IdType to use (when the DB supports both SEQUENCE and IDENTITY and the default is not desired).
   */
  public void setIdType(IdType idType) {
    this.idType = idType;
  }

  /**
   * Return true if InetAddress should map to varchar column (rather than Postgres INET).
   */
  public boolean isDatabaseInetAddressVarchar() {
    return databaseInetAddressVarchar;
  }

  /**
   * Set to true to force InetAddress to map to varchar column.
   */
  public void setDatabaseInetAddressVarchar(boolean databaseInetAddressVarchar) {
    this.databaseInetAddressVarchar = databaseInetAddressVarchar;
  }

  /**
   * Add a custom type mapping.
   * <pre>{@code
   *
   *   // set the default mapping for BigDecimal.class/decimal
   *   config.addCustomMapping(DbType.DECIMAL, "decimal(18,6)");
   *
   *   // set the default mapping for String.class/varchar but only for Postgres
   *   config.addCustomMapping(DbType.VARCHAR, "text", Platform.POSTGRES);
   *
   * }</pre>
   *
   * @param type             The DB type this mapping should apply to
   * @param columnDefinition The column definition that should be used
   * @param platform         Optionally specify the platform this mapping should apply to.
   */
  public void addCustomMapping(DbType type, String columnDefinition, Platform platform) {
    customDbTypeMappings.add(new CustomDbTypeMapping(type, columnDefinition, platform));
  }

  /**
   * Add a custom type mapping that applies to all platforms.
   * <pre>{@code
   *
   *   // set the default mapping for BigDecimal/decimal
   *   config.addCustomMapping(DbType.DECIMAL, "decimal(18,6)");
   *
   *   // set the default mapping for String/varchar
   *   config.addCustomMapping(DbType.VARCHAR, "text");
   *
   * }</pre>
   *
   * @param type             The DB type this mapping should apply to
   * @param columnDefinition The column definition that should be used
   */
  public void addCustomMapping(DbType type, String columnDefinition) {
    customDbTypeMappings.add(new CustomDbTypeMapping(type, columnDefinition));
  }

  /**
   * Return the list of custom type mappings.
   */
  public List<CustomDbTypeMapping> getCustomTypeMappings() {
    return customDbTypeMappings;
  }

  public void loadSettings(PropertiesWrapper p) {

    idType = p.getEnum(IdType.class, "idType", idType);
    forUpdateNoKey = p.getBoolean("forUpdateNoKey", forUpdateNoKey);
    databaseSequenceBatchSize = p.getInt("databaseSequenceBatchSize", databaseSequenceBatchSize);
    databaseBooleanTrue = p.get("databaseBooleanTrue", databaseBooleanTrue);
    databaseBooleanFalse = p.get("databaseBooleanFalse", databaseBooleanFalse);
    databaseInetAddressVarchar = p.getBoolean("databaseInetAddressVarchar", databaseInetAddressVarchar);
    caseSensitiveCollation = p.getBoolean("caseSensitiveCollation", caseSensitiveCollation);

    DbUuid dbUuid = p.getEnum(DbUuid.class, "dbuuid", null);
    if (dbUuid != null) {
      setDbUuid(dbUuid);
    }
    if (p.getBoolean("uuidStoreAsBinary", false)) {
      setDbUuid(DbUuid.BINARY);
    }

    int srid = p.getInt("geometrySRID", 0);
    if (srid > 0) {
      setGeometrySRID(srid);
    }

    // Mapping is specified in the form: BOOLEAN=int(1);BIT=int(1);
    String mapping = p.get("mapping");
    if (mapping != null && !mapping.isEmpty()) {
      Map<String, String> map = StringHelper.delimitedToMap(mapping, ";", "=");
      for (Entry<String, String> entry : map.entrySet()) {
        addCustomMapping(DbType.valueOf(entry.getKey()), entry.getValue());
      }
    }

    boolean quotedIdentifiers = p.getBoolean("allQuotedIdentifiers", allQuotedIdentifiers);
    if (quotedIdentifiers != allQuotedIdentifiers) {
      // potentially also set to use matching naming convention
      setAllQuotedIdentifiers(quotedIdentifiers);
    }
  }

  /**
   * Specify how UUID is stored.
   */
  public enum DbUuid {

    /**
     * Store using native UUID in H2 and Postgres and otherwise fallback to VARCHAR(40).
     */
    AUTO_VARCHAR(true, false, false),

    /**
     * Store using native UUID in H2 and Postgres and otherwise fallback to BINARY(16).
     */
    AUTO_BINARY(true, true, false),

    /**
     * Store using native UUID in H2 and Postgres and otherwise fallback to BINARY(16) with optimized packing.
     */
    AUTO_BINARY_OPTIMIZED(true, true, true),

    /**
     * Store using DB VARCHAR(40).
     */
    VARCHAR(false, false, false),

    /**
     * Store using DB BINARY(16).
     */
    BINARY(false, true, false),

    /**
     * Store using DB BINARY(16) with optimized packing.
     */
    BINARY_OPTIMIZED(false, true, true);

    boolean nativeType;
    boolean binary;
    boolean binaryOptimized;

    DbUuid(boolean nativeType, boolean binary, boolean binaryOptimized) {
      this.nativeType = nativeType;
      this.binary = binary;
      this.binaryOptimized = binaryOptimized;
    }

    /**
     * Return true if native UUID type is preferred.
     */
    public boolean useNativeType() {
      return nativeType;
    }

    /**
     * Return true if BINARY(16) storage is preferred over VARCHAR(40).
     */
    public boolean useBinary() {
      return binary;
    }

    /**
     * Return true, if optimized packing should be used.
     */
    public boolean useBinaryOptimized() {
      return binaryOptimized;
    }
  }
}
