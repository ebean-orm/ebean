package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.*;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.EncryptDeploy;
import io.ebean.config.EncryptDeployManager;
import io.ebean.config.EncryptKeyManager;
import io.ebean.config.Encryptor;
import io.ebean.config.NamingConvention;
import io.ebean.config.TableName;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.type.DataEncryptSupport;
import io.ebeaninternal.server.type.ScalarTypeArray;
import io.ebeaninternal.server.type.ScalarTypeWrapper;
import io.ebeaninternal.server.type.SimpleAesEncryptor;
import io.ebeaninternal.server.type.TypeManager;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PersistenceException;
import java.sql.Types;

/**
 * Utility object to help processing deployment information.
 */
public final class DeployUtil {

  /**
   * Assumes CLOB rather than LONGVARCHAR.
   */
  private static final int dbCLOBType = Types.CLOB;

  /**
   * Assumes BLOB rather than LONGVARBINARY. This should probably be
   * configurable.
   */
  private static final int dbBLOBType = Types.BLOB;

  private static final int DEFAULT_JSON_VARCHAR_LENGTH = 3000;

  private final NamingConvention namingConvention;
  private final TypeManager typeManager;
  private final DatabasePlatform dbPlatform;
  private final EncryptDeployManager encryptDeployManager;
  private final EncryptKeyManager encryptKeyManager;
  private final Encryptor bytesEncryptor;
  private final boolean useValidationNotNull;

  public DeployUtil(TypeManager typeMgr, DatabaseConfig config) {
    this.typeManager = typeMgr;
    this.namingConvention = config.getNamingConvention();
    this.dbPlatform = config.getDatabasePlatform();
    this.encryptDeployManager = config.getEncryptDeployManager();
    this.encryptKeyManager = config.getEncryptKeyManager();
    Encryptor be = config.getEncryptor();
    this.bytesEncryptor = be != null ? be : new SimpleAesEncryptor();
    this.useValidationNotNull = config.isUseValidationNotNull();
  }

  public TypeManager getTypeManager() {
    return typeManager;
  }

  public DatabasePlatform getDbPlatform() {
    return dbPlatform;
  }

  public NamingConvention getNamingConvention() {
    return namingConvention;
  }

  /**
   * Check that the EncryptKeyManager has been defined.
   */
  void checkEncryptKeyManagerDefined(String fullPropName) {
    if (encryptKeyManager == null) {
      String msg = "Using encryption on " + fullPropName + " but no EncryptKeyManager defined!";
      throw new PersistenceException(msg);
    }
  }

  EncryptDeploy getEncryptDeploy(TableName table, String column) {
    if (encryptDeployManager == null) {
      return EncryptDeploy.ANNOTATION;
    }
    return encryptDeployManager.getEncryptDeploy(table, column);
  }

  DataEncryptSupport createDataEncryptSupport(String table, String column) {
    return new DataEncryptSupport(encryptKeyManager, bytesEncryptor, table, column);
  }

  @SuppressWarnings("unchecked")
  void setEnumScalarType(Enumerated enumerated, DeployBeanProperty prop) {
    Class<?> enumType = prop.getPropertyType();
    if (!enumType.isEnum()) {
      throw new IllegalArgumentException("Class [" + enumType + "] is Not a Enum?");
    }
    try {
      Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) enumType;
      EnumType type = enumerated != null ? enumerated.value() : null;
      ScalarType<?> scalarType = typeManager.createEnumScalarType(enumClass, type);
      prop.setScalarType(scalarType);
      prop.setDbType(scalarType.getJdbcType());
    } catch (IllegalStateException e) {
      throw new PersistenceException("Error mapping property " + prop.getFullBeanName() + " - " + e.getMessage());
    }
  }

  /**
   * Find the ScalarType for this property.
   * <p>
   * This determines if there is a conversion required from the logical (bean)
   * type to a DB (jdbc) type. This is the case for java.util.Date etc.
   * </p>
   */
  public void setScalarType(DeployBeanProperty property) {
    if (property.getScalarType() != null) {
      // already has a ScalarType assigned.
      // this will be an Enum type...
      return;
    }
    ScalarType<?> scalarType = getScalarType(property);
    if (scalarType != null) {
      // set the jdbc type this maps to
      property.setDbType(scalarType.getJdbcType());
      property.setScalarType(scalarType);
      property.checkPrimitiveBoolean();
    }
  }

  private ScalarType<?> getScalarType(DeployBeanProperty property) {
    // Note that Temporal types already have dbType
    // set via annotations
    Class<?> propType = property.getPropertyType();
    try {
      ScalarType<?> scalarType = typeManager.getScalarType(propType, property.getDbType());
      if (scalarType != null || property.isTransient()) {
        return scalarType;
      }
      throw new PersistenceException(property.getFullBeanName() + " has no ScalarType - type[" + propType.getName() + "]");
    } catch (IllegalArgumentException e) {
      if (property.isTransient()) {
        // expected for transient properties with unknown/non-mapped types
        return null;
      }
      throw e;
    }
  }

  /**
   * Map to Postgres HSTORE type (with fallback to JSON storage in VARCHAR).
   */
  void setDbMap(DeployBeanProperty prop, DbMap dbMap) {
    ScalarType<?> scalarType = typeManager.getDbMapScalarType();
    int dbType = scalarType.getJdbcType();
    prop.setDbType(dbType);
    prop.setScalarType(scalarType);
    if (dbType == Types.VARCHAR) {
      // this is actually the fallback of JSON storage into VARCHAR
      int dbLength = dbMap.length();
      int columnLength = (dbLength > 0) ? dbLength : DEFAULT_JSON_VARCHAR_LENGTH;
      prop.setDbLength(columnLength);
    }
  }

  /**
   * Set the DbArray type (effectively Postgres only).
   */
  void setDbArray(DeployBeanProperty prop, DbArray dbArray) {
    Class<?> type = prop.getPropertyType();
    ScalarType<?> scalarType = typeManager.getArrayScalarType(type, prop.getGenericType(), prop.isNullable());
    if (scalarType == null) {
      throw new RuntimeException("No ScalarType for @DbArray type for [" + prop.getFullBeanName() + "]");
    }
    int dbType = scalarType.getJdbcType();
    prop.setDbType(dbType);
    prop.setScalarType(scalarType);
    if (scalarType instanceof ScalarTypeArray) {
      String columnDefn = ((ScalarTypeArray) scalarType).getDbColumnDefn();
      if (dbArray.length() > 0) {
        // fallback varchar column length when ARRAY not support by DB
        columnDefn += "(" + dbArray.length() + ")";
      }
      prop.setDbLength(dbArray.length());
      prop.setDbColumnDefn(columnDefn);
    } else {
      throw new RuntimeException("Not mapped to ScalarTypeArray? " + scalarType.getClass());
    }
  }

  void setDbJsonType(DeployBeanProperty prop, DbJson dbJsonType) {
    int dbType = getDbJsonStorage(dbJsonType.storage());
    setDbJsonType(prop, dbType, dbJsonType.length(), dbJsonType.mutationDetection());
  }

  void setDbJsonBType(DeployBeanProperty prop, DbJsonB dbJsonB) {
    setDbJsonType(prop, DbPlatformType.JSONB, dbJsonB.length(), dbJsonB.mutationDetection());
  }

  private void setDbJsonType(DeployBeanProperty prop, int dbType, int dbLength, MutationDetection mutationDetection) {
    prop.setDbType(dbType);
    prop.setMutationDetection(mutationDetection);
    ScalarType<?> scalarType = typeManager.getJsonScalarType(prop, dbType, dbLength);
    if (scalarType == null) {
      throw new RuntimeException("No ScalarType for JSON property [" + prop + "] [" + dbType + "]");
    }
    prop.setScalarType(scalarType);
    if (dbType == Types.VARCHAR || dbLength > 0) {
      // determine the db column size
      int columnLength = (dbLength > 0) ? dbLength : DEFAULT_JSON_VARCHAR_LENGTH;
      prop.setDbLength(columnLength);
    }
  }

  /**
   * Return the JDBC type for the JSON storage type.
   */
  private int getDbJsonStorage(DbJsonType dbJsonType) {
    switch (dbJsonType) {
      case JSONB:
        return DbPlatformType.JSONB;
      case VARCHAR:
        return Types.VARCHAR;
      case CLOB:
        return Types.CLOB;
      case BLOB:
        return Types.BLOB;
      default:
        return DbPlatformType.JSON;
    }
  }

  /**
   * This property is marked as a Lob object.
   */
  void setLobType(DeployBeanProperty prop) {
    ScalarType<?> scalarType = prop.getScalarType();
    if (scalarType instanceof ScalarTypeWrapper) {
      int lobType = scalarType.getJdbcType() == Types.VARCHAR ? dbCLOBType : dbBLOBType;
      prop.setDbType(lobType);
    } else {
      // is String or byte[] ? used to determine if its a CLOB or BLOB
      Class<?> type = prop.getPropertyType();
      // this also sets the lob flag on DeployBeanProperty
      int lobType = isClobType(type) ? dbCLOBType : dbBLOBType;

      scalarType = typeManager.getScalarType(type, lobType);
      if (scalarType == null) {
        // this should never occur actually
        throw new RuntimeException("No ScalarType for LOB type [" + type + "] [" + lobType + "]");
      }
      prop.setDbType(lobType);
      prop.setScalarType(scalarType);
    }
  }

  private boolean isClobType(Class<?> type) {
    return type.equals(String.class);
  }

  boolean isUseValidationNotNull() {
    return useValidationNotNull;
  }

  /**
   * Convert quoted identifiers if necessary (including all quoted).
   */
  public String convertQuotes(String name) {
    return dbPlatform.convertQuotedIdentifiers(name);
  }
}
