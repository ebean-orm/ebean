package io.ebean.config;

import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.annotation.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for DB types such as UUID, Geometry etc.
 */
public class DbTypeConfig {

  /**
   * The Geometry SRID value (default 4326).
   */
  private int geometrySRID = 4326;

  /**
   * Set for DB's that support both Sequence and Identity (and the default choice is not desired).
   */
  private IdType idType;

  /**
   * Setting to indicate if UUID should be stored as binary(16) or varchar(40) or native DB type (for H2 and Postgres).
   */
  private ServerConfig.DbUuid dbUuid = ServerConfig.DbUuid.AUTO_VARCHAR;

  /**
   * Modify the default mapping of standard types such as default precision for DECIMAL etc.
   */
  private List<CustomDbTypeMapping> customDbTypeMappings = new ArrayList<>();

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
  public ServerConfig.DbUuid getDbUuid() {
    return dbUuid;
  }

  /**
   * Set the DB type used to store UUID.
   */
  public void setDbUuid(ServerConfig.DbUuid dbUuid) {
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
}
