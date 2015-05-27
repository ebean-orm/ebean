package com.avaje.ebeaninternal.server.deploy.parse;

import java.sql.Types;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PersistenceException;

import com.avaje.ebean.annotation.DbJson;
import com.avaje.ebean.annotation.DbJsonType;
import com.avaje.ebean.config.EncryptDeploy;
import com.avaje.ebean.config.EncryptDeployManager;
import com.avaje.ebean.config.EncryptKeyManager;
import com.avaje.ebean.config.Encryptor;
import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.TableName;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyCompound;
import com.avaje.ebeaninternal.server.type.DataEncryptSupport;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypeEnumStandard;
import com.avaje.ebeaninternal.server.type.SimpleAesEncryptor;
import com.avaje.ebeaninternal.server.type.TypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility object to help processing deployment information.
 */
public class DeployUtil {

  private static final Logger logger = LoggerFactory.getLogger(DeployUtil.class);

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

  public DeployUtil(TypeManager typeMgr, ServerConfig serverConfig) {

    this.typeManager = typeMgr;
    this.namingConvention = serverConfig.getNamingConvention();
    this.dbPlatform = serverConfig.getDatabasePlatform();
    this.encryptDeployManager = serverConfig.getEncryptDeployManager();
    this.encryptKeyManager = serverConfig.getEncryptKeyManager();

    Encryptor be = serverConfig.getEncryptor();
    this.bytesEncryptor = be != null ? be : new SimpleAesEncryptor();
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
  public void checkEncryptKeyManagerDefined(String fullPropName) {
    if (encryptKeyManager == null) {
      String msg = "Using encryption on " + fullPropName + " but no EncryptKeyManager defined!";
      throw new PersistenceException(msg);
    }
  }

  public EncryptDeploy getEncryptDeploy(TableName table, String column) {
    if (encryptDeployManager == null) {
      return EncryptDeploy.ANNOTATION;
    }
    return encryptDeployManager.getEncryptDeploy(table, column);
  }

  public DataEncryptSupport createDataEncryptSupport(String table, String column) {
    return new DataEncryptSupport(encryptKeyManager, bytesEncryptor, table, column);
  }

  public ScalarType<?> setEnumScalarType(Enumerated enumerated, DeployBeanProperty prop) {

    Class<?> enumType = prop.getPropertyType();
    if (!enumType.isEnum()) {
      throw new IllegalArgumentException("Class [" + enumType + "] is Not a Enum?");
    }
    ScalarType<?> scalarType = typeManager.getScalarType(enumType);
    if (scalarType == null) {
      // see if it has a Mapping in avaje.properties
      scalarType = typeManager.createEnumScalarType(enumType);
      if (scalarType == null) {
        // use JPA normal Enum type (without mapping)
        EnumType type = enumerated != null ? enumerated.value() : null;
        scalarType = createEnumScalarTypePerSpec(enumType, type, prop.getDbType());
      }

      typeManager.add(scalarType);
    }
    prop.setScalarType(scalarType);
    prop.setDbType(scalarType.getJdbcType());
    return scalarType;
  }

  private ScalarType<?> createEnumScalarTypePerSpec(Class<?> enumType, EnumType type, int dbType) {

    if (type == null) {
      // default as per spec is ORDINAL
      return new ScalarTypeEnumStandard.OrdinalEnum(enumType);

    } else if (type == EnumType.ORDINAL) {
      return new ScalarTypeEnumStandard.OrdinalEnum(enumType);

    } else {
      return new ScalarTypeEnumStandard.StringEnum(enumType);
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
    if (property instanceof DeployBeanPropertyCompound) {
      // compound properties have a CvoInternalType instead
      return;
    }

    ScalarType<?> scalarType = getScalarType(property);
    if (scalarType != null) {
      // set the jdbc type this maps to

      property.setDbType(scalarType.getJdbcType());
      property.setScalarType(scalarType);
    }
  }

  private ScalarType<?> getScalarType(DeployBeanProperty property) {

    // Note that Temporal types already have dbType
    // set via annotations
    Class<?> propType = property.getPropertyType();
    ScalarType<?> scalarType = typeManager.getScalarType(propType, property.getDbType());
    if (scalarType != null) {
      return scalarType;
    }

    String msg = property.getFullBeanName() + " has no ScalarType - type[" + propType.getName() + "]";
    if (!property.isTransient()) {
      throw new PersistenceException(msg);

    } else {
      // this is ok...
      logger.trace("... transient property " + msg);
      return null;
    }
  }

  /**
   * Map to Postgres HSTORE type.
   */
  public void setDbHstore(DeployBeanProperty prop) {

    ScalarType<?> scalarType = typeManager.getScalarType(DbType.HSTORE);
    if (scalarType == null) {
      // this should never occur actually
      throw new RuntimeException("No ScalarType found for HSTORE on [" + prop.getFullBeanName() + "]");
    }
    prop.setDbType(DbType.HSTORE);
    prop.setScalarType(scalarType);
  }

  /**
   * This property is marked as a Lob object.
   */
  public void setDbJsonType(DeployBeanProperty prop, DbJson dbJsonType) {

    int dbType = getDbJsonStorage(dbJsonType.storage());
    setDbJsonType(prop, dbType, dbJsonType.length());
  }

  public void setDbJsonBType(DeployBeanProperty prop) {
    setDbJsonType(prop, DbType.JSONB, 0);
  }

  private void setDbJsonType(DeployBeanProperty prop, int dbType, int dbLength) {

    Class<?> type = prop.getPropertyType();

    ScalarType<?> scalarType = typeManager.getJsonScalarType(type, dbType);
    if (scalarType == null) {
      // this should never occur actually
      throw new RuntimeException("No ScalarType for JSON type [" + type + "] [" + dbType + "]");
    }
    prop.setDbType(dbType);
    prop.setScalarType(scalarType);
    if (dbType == Types.VARCHAR) {
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
      case JSON:
        return DbType.JSON;
      case JSONB:
        return DbType.JSONB;
      case VARCHAR:
        return Types.VARCHAR;
      case CLOB:
        return Types.CLOB;
      case BLOB:
        return Types.BLOB;
      default:
        return DbType.JSON;
    }
  }

  /**
   * This property is marked as a Lob object.
   */
  public void setLobType(DeployBeanProperty prop) {

    // is String or byte[] ? used to determine if its a CLOB or BLOB
    Class<?> type = prop.getPropertyType();

    // this also sets the lob flag on DeployBeanProperty
    int lobType = isClobType(type) ? dbCLOBType : dbBLOBType;

    ScalarType<?> scalarType = typeManager.getScalarType(type, lobType);
    if (scalarType == null) {
      // this should never occur actually
      throw new RuntimeException("No ScalarType for LOB type [" + type + "] [" + lobType + "]");
    }
    prop.setDbType(lobType);
    prop.setScalarType(scalarType);
  }

  public boolean isClobType(Class<?> type) {
    return type.equals(String.class);
  }

}
