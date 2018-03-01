/*
 * Licensed Materials - Property of FOCONIS AG
 * (C) Copyright FOCONIS AG.
 */

package io.ebean.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.util.StringHelper;

/**
 * Configuration for Platforms such as UUID, Geometry etc.
 */
public class PlatformConfig {

  /**
   * For DB's using sequences this is the number of sequence values prefetched.
   */
  private int databaseSequenceBatchSize = 20;

  /**
   * The preferred IdType.
   */
  private IdType idType;

  private boolean allQuotedIdentifiers;

//  /**
//   * The naming convention.
//   */
//  private NamingConvention namingConvention = new UnderscoreNamingConvention();


  /**
   * Setting to indicate if UUID should be stored as binary(16) or varchar(40) or native DB type (for H2 and Postgres).
   */
  private PlatformConfig.DbUuid dbUuid = PlatformConfig.DbUuid.AUTO_VARCHAR;


  /**
   * The Geometry SRID value (default 4326).
   */
  private int geometrySRID = 4326;


  /**
   * Modify the default mapping of standard types such as default precision for DECIMAL etc.
   */
  private List<CustomDbTypeMapping> customDbTypeMappings = new ArrayList<>();

  public PlatformConfig() {}

  /**
   * Copy-Constructor.
   */
  public PlatformConfig(PlatformConfig source) {
    this.allQuotedIdentifiers = source.allQuotedIdentifiers;
    this.customDbTypeMappings.addAll(source.customDbTypeMappings);
    this.databaseSequenceBatchSize = source.databaseSequenceBatchSize;
    this.dbUuid = source.dbUuid;
    this.geometrySRID = source.geometrySRID;
    this.idType = source.idType;
  }

  /**
   * Set the number of sequences to fetch/preallocate when using DB sequences.
   * <p>
   * This is a performance optimisation to reduce the number times Ebean
   * requests a sequence to be used as an Id for a bean (aka reduce network
   * chatter).
   * </p>
   */
  public void setDatabaseSequenceBatchSize(int databaseSequenceBatchSize) {
    this.databaseSequenceBatchSize = databaseSequenceBatchSize;
  }

  /**
   * Return the number of DB sequence values that should be preallocated.
   */
  public int getDatabaseSequenceBatchSize() {
    return databaseSequenceBatchSize;
  }

  /**
   * Return the preferred DB platform IdType.
   */
  public IdType getIdType() {
    return idType;
  }

  /**
   * Set the preferred DB platform IdType.
   */
  public void setIdType(IdType idType) {
    this.idType = idType;
  }

  /**
   * Protected. Use {@link ServerConfig#setAllQuotedIdentifiers(boolean)}
   */
  public void setAllQuotedIdentifiers(boolean allQuotedIdentifiers) {
    this.allQuotedIdentifiers = allQuotedIdentifiers;
  }

  /**
   * Return true if all DB column and table names should use quoted identifiers.
   */
  public boolean isAllQuotedIdentifiers() {
    return allQuotedIdentifiers;
  }

  /**
   * Return the DB type used to store UUID.
   */
  public PlatformConfig.DbUuid getDbUuid() {
    return dbUuid;
  }

  /**
   * Set the DB type used to store UUID.
   */
  public void setDbUuid(PlatformConfig.DbUuid dbUuid) {
    this.dbUuid = dbUuid;
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
   * Add a custom type mapping.
   * <p>
   * <pre>{@code
   *
   *   // set the default mapping for BigDecimal.class/decimal
   *   serverConfig.addCustomMapping(DbType.DECIMAL, "decimal(18,6)");
   *
   *   // set the default mapping for String.class/varchar but only for Postgres
   *   serverConfig.addCustomMapping(DbType.VARCHAR, "text", Platform.POSTGRES);
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
   * <p>
   * <pre>{@code
   *
   *   // set the default mapping for BigDecimal/decimal
   *   serverConfig.addCustomMapping(DbType.DECIMAL, "decimal(18,6)");
   *
   *   // set the default mapping for String/varchar
   *   serverConfig.addCustomMapping(DbType.VARCHAR, "text");
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

  /**
   * @param p
   * @param object
   */
  public void loadSettings(PropertiesWrapper p) {
    databaseSequenceBatchSize = p.getInt("databaseSequenceBatchSize", databaseSequenceBatchSize);
    idType = p.getEnum(IdType.class, "idType", idType);

    int srid = p.getInt("geometrySRID", 0);
    if (srid > 0) {
      setGeometrySRID(srid);
    }

    PlatformConfig.DbUuid dbUuid = p.getEnum(PlatformConfig.DbUuid.class, "dbuuid", null);
    if (dbUuid != null) {
      setDbUuid(dbUuid);
    }
    if (p.getBoolean("uuidStoreAsBinary", false)) {
      setDbUuid(PlatformConfig.DbUuid.BINARY);
    }

    // Mapping is specified in the form: BOOLEAN=int(1);BIT=int(1);
    String mapping = p.get("mapping");
    if (mapping != null && !mapping.isEmpty()) {
      Map<String, String> map = StringHelper.delimitedToMap(mapping, ";", "=");
      for (Entry<String, String> entry : map.entrySet()) {
        addCustomMapping(DbType.valueOf(entry.getKey()), entry.getValue());
      }
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
     * Store using DB BINARY(16).
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
