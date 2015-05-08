package com.avaje.ebeaninternal.server.deploy.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Version;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebean.config.dbplatform.DbEncryptFunction;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.properties.BeanPropertyGetter;
import com.avaje.ebeaninternal.server.properties.BeanPropertySetter;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypeEnum;
import com.avaje.ebeaninternal.server.type.ScalarTypeWrapper;

/**
 * Description of a property of a bean. Includes its deployment information such
 * as database column mapping information.
 */
public class DeployBeanProperty {

    private static final int ID_ORDER = 1000000;
    private static final int UNIDIRECTIONAL_ORDER = 100000;
    private static final int AUDITCOLUMN_ORDER = -1000000;
    private static final int VERSIONCOLUMN_ORDER = -1000000;

    /**
     * Advanced bean deployment. To exclude this property from update where
     * clause.
     */
    public static final String EXCLUDE_FROM_UPDATE_WHERE = "EXCLUDE_FROM_UPDATE_WHERE";

    /**
     * Advanced bean deployment. To exclude this property from delete where
     * clause.
     */
    public static final String EXCLUDE_FROM_DELETE_WHERE = "EXCLUDE_FROM_DELETE_WHERE";

    /**
     * Advanced bean deployment. To exclude this property from insert.
     */
    public static final String EXCLUDE_FROM_INSERT = "EXCLUDE_FROM_INSERT";

    /**
     * Advanced bean deployment. To exclude this property from update set
     * clause.
     */
    public static final String EXCLUDE_FROM_UPDATE = "EXCLUDE_FROM_UPDATE";

    /**
     * Flag to mark this at part of the unique id.
     */
    private boolean id;

    /**
     * Flag to mark the property as embedded. This could be on
     * BeanPropertyAssocOne rather than here. Put it here for checking Id type
     * (embedded or not).
     */
    private boolean embedded;

    /**
     * Flag indicating if this the version property.
     */
    private boolean versionColumn;

    private boolean fetchEager = true;

    /**
     * Set if this property is nullable.
     */
    private boolean nullable = true;

    private boolean unique;

    private boolean discriminator;
    
    /**
     * The length or precision of the DB column.
     */
    private int dbLength;

    private int dbScale;

    private String dbColumnDefn;

    private boolean isTransient;

    private boolean localEncrypted;
    
    private boolean exposeSerialize = true;
    private boolean exposeDeserialize = true;

    private boolean dbEncrypted;
    private DbEncryptFunction dbEncryptFunction;

    private int dbEncryptedType;

    private String dbBind = "?";

    /**
     * Is this property include in database resultSet.
     */
    private boolean dbRead;

    /**
     * Include this in DB insert.
     */
    private boolean dbInsertable;

    /**
     * Include this in a DB update.
     */
    private boolean dbUpdateable;

    private DeployTableJoin secondaryTableJoin;

    private String secondaryTableJoinPrefix;

    /**
     * Set to true if this property is based on a secondary table.
     */
    private String secondaryTable;

    /**
     * The type that owns this property.
     */
    private Class<?> owningType;

    /**
     * True if the property is a Clob, Blob LongVarchar or LongVarbinary.
     */
    private boolean lob;

    private boolean naturalKey;
    
    /**
     * The logical bean property name.
     */
    private String name;

    /**
     * The reflected field.
     */
    private Field field;

    /**
     * The bean type.
     */
    private Class<?> propertyType;

    /**
     * Set for Non-JDBC types to provide logical to db type conversion.
     */
    private ScalarType<?> scalarType;

    /**
     * The database column. This can include quoted identifiers.
     */
    private String dbColumn;

    private String sqlFormulaSelect;
    private String sqlFormulaJoin;

    /**
     * The jdbc data type this maps to.
     */
    private int dbType;

    /**
     * The default value to insert if null.
     */
    private Object defaultValue;

    /**
     * Extra deployment parameters.
     */
    private HashMap<String, String> extraAttributeMap = new HashMap<String, String>();

    /**
     * The method used to read the property.
     */
    private Method readMethod;

    /**
     * The method used to write the property.
     */
    private Method writeMethod;

    private int propertyIndex;
    
    private BeanPropertyGetter getter;

    private BeanPropertySetter setter;

    /**
     * Generator for insert or update timestamp etc.
     */
    private GeneratedProperty generatedProperty;

    private final DeployBeanDescriptor<?> desc;

    private boolean undirectionalShadow;
    
    private int sortOrder;

    private boolean indexed;
    private String indexName;

    public DeployBeanProperty(DeployBeanDescriptor<?> desc, Class<?> propertyType, ScalarType<?> scalarType, ScalarTypeConverter<?, ?> typeConverter) {
        this.desc = desc;
        this.propertyType = propertyType;
        this.scalarType = wrapScalarType(propertyType, scalarType, typeConverter);
    }

    /**
     * Wrap the ScalarType using a ScalarTypeConverter.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ScalarType<?> wrapScalarType(Class<?> propertyType, ScalarType<?> scalarType, ScalarTypeConverter<?, ?> typeConverter) {
        if (typeConverter == null){
            return scalarType;
        }
        return new ScalarTypeWrapper(propertyType, scalarType, typeConverter);
    }
    
    public int getSortOverride() {
        if (field == null) {
            return 0;
        }
        if (field.getAnnotation(Id.class) != null) {
            return ID_ORDER;
        } else if (field.getAnnotation(EmbeddedId.class) != null) {
            return ID_ORDER;
        } else if (undirectionalShadow){
            return UNIDIRECTIONAL_ORDER;
        } else if (field.getAnnotation(CreatedTimestamp.class) != null) {
            return AUDITCOLUMN_ORDER;
        } else if (field.getAnnotation(UpdatedTimestamp.class) != null) {
            return AUDITCOLUMN_ORDER;
        } else if (field.getAnnotation(Version.class) != null) {
            return VERSIONCOLUMN_ORDER;
        }
        return 0;
    }

    /**
     * Return true is this is a simple scalar property.
     */
    public boolean isScalar() {
        return true;
    }

    public String getFullBeanName() {
        return desc.getFullName() + "." + name;
    }

    /**
     * Return true if this is a primitive type with a nullable DB column.
     * <p>
     * This should log a WARNING as primitive types can't be null.
     * </p>
     */
    public boolean isNullablePrimitive() {
        if (nullable && propertyType.isPrimitive()) {
            return true;
        }
        return false;
    }

    /**
     * Return the DB column length for character columns.
     * <p>
     * Note if there is no length explicitly defined then the scalarType is
     * checked to see if that has one (primarily to support putting a length on
     * Enum types).
     * </p>
     */
    public int getDbLength() {
        if (dbLength == 0 && scalarType != null) {
            return scalarType.getLength();
        }

        return dbLength;
    }

    public boolean isExposeSerialize() {
      return exposeSerialize;
    }

    public void setExposeSerialize(boolean exposeSerialize) {
      this.exposeSerialize = exposeSerialize;
    }

    public boolean isExposeDeserialize() {
      return exposeDeserialize;
    }

    public void setExposeDeserialize(boolean exposeDeserialize) {
      this.exposeDeserialize = exposeDeserialize;
    }

    /**
     * Return the sortOrder for the properties.
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Set the sortOrder for the properties.
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Return true if this is a placeholder property for a unidirectional relationship.
     */
    public boolean isUndirectionalShadow() {
        return undirectionalShadow;
    }

    /**
     * Mark this property as a placeholder for a unidirectional relationship.
     */
    public void setUndirectionalShadow(boolean undirectionalShadow) {
        this.undirectionalShadow = undirectionalShadow;
    }

    /**
     * Mark this property as mapping to the discriminator column.
     */
    public void setDiscriminator(boolean discriminator) {
      this.discriminator = discriminator;
    }

    /**
     * Return true if this property maps to the inheritance discriminator column.s
     */
    public boolean isDiscriminator() {
      return discriminator;
    }
    
    /**
     * Return true if the property is encrypted in java rather than in the DB.
     */
    public boolean isLocalEncrypted() {
        return localEncrypted;
    }

    /**
     * Set to true when the property is encrypted in java rather than in the DB.
     */
    public void setLocalEncrypted(boolean localEncrypted) {
        this.localEncrypted = localEncrypted;
    }

    /**
     * Set the DB column length for character columns.
     */
    public void setDbLength(int dbLength) {
        this.dbLength = dbLength;
    }

    /**
     * Return the Db scale for numeric columns.
     */
    public int getDbScale() {
        return dbScale;
    }

    /**
     * Set the Db scale for numeric columns.
     */
    public void setDbScale(int dbScale) {
        this.dbScale = dbScale;
    }

    /**
     * Return the DB column definition if defined.
     */
    public String getDbColumnDefn() {
        return dbColumnDefn;
    }

    /**
     * Set a specific DB column definition.
     */
    public void setDbColumnDefn(String dbColumnDefn) {
        if (dbColumnDefn == null || dbColumnDefn.trim().length() == 0) {
            this.dbColumnDefn = null;
        } else {
            this.dbColumnDefn = InternString.intern(dbColumnDefn);
        }
    }

    public String getDbConstraintExpression() {
        if (scalarType instanceof ScalarTypeEnum) {
            // create a check constraint for the enum
            ScalarTypeEnum etype = (ScalarTypeEnum) scalarType;

            // check dbColName IN ('A', 'I', 'D')
            return "check (" + dbColumn + " in " + etype.getConstraintInValues() + ")";
        }
        return null;
    }

    /**
     * Return the scalarType. This returns null for native JDBC types, otherwise
     * it is used to convert between logical types and jdbc types.
     */
    public ScalarType<?> getScalarType() {
        return scalarType;
    }

    public void setScalarType(ScalarType<?> scalarType) {
        this.scalarType = scalarType;
    }

    public int getPropertyIndex() {
      return propertyIndex;
    }

    public void setPropertyIndex(int propertyIndex) {
      this.propertyIndex = propertyIndex;
    }

    public BeanPropertyGetter getGetter() {
        return getter;
    }

    public BeanPropertySetter getSetter() {
        return setter;
    }

    /**
     * Return the getter method.
     */
    public Method getReadMethod() {
        return readMethod;
    }

    /**
     * Return the setter method.
     */
    public Method getWriteMethod() {
        return writeMethod;
    }

    /**
     * Set to the owning type form a Inheritance heirarchy.
     */
    public void setOwningType(Class<?> owningType) {
        this.owningType = owningType;
    }

    public Class<?> getOwningType() {
        return owningType;
    }

    /**
     * Return true if this is local to this type - aka not from a super type.
     */
    public boolean isLocal() {
        return owningType == null || owningType.equals(desc.getBeanType());
    }

    /**
     * Set the getter used to read the property value from a bean.
     */
    public void setGetter(BeanPropertyGetter getter) {
        this.getter = getter;
    }

    /**
     * Set the setter used to set the property value to a bean.
     */
    public void setSetter(BeanPropertySetter setter) {
        this.setter = setter;
    }

    /**
     * Return the name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the property.
     */
    public void setName(String name) {
        this.name = InternString.intern(name);
    }

    /**
     * Return the bean Field associated with this property.
     */
    public Field getField() {
        return field;
    }

    /**
     * Set the bean Field associated with this property.
     */
    public void setField(Field field) {
        this.field = field;
    }

    public boolean isNaturalKey() {
    	return naturalKey;
    }

	public void setNaturalKey(boolean naturalKey) {
    	this.naturalKey = naturalKey;
    }

	/**
     * Return true if this is a generated property like update timestamp and
     * create timestamp.
     */
    public boolean isGenerated() {
        return generatedProperty != null;
    }

    /**
     * Return the GeneratedValue. Used to generate update timestamp etc.
     */
    public GeneratedProperty getGeneratedProperty() {
        return generatedProperty;
    }

    /**
     * Set the GeneratedValue. Used to generate update timestamp etc.
     */
    public void setGeneratedProperty(GeneratedProperty generatedValue) {
        this.generatedProperty = generatedValue;
    }

    /**
     * Return true if this property is mandatory.
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * Set the not nullable of this property.
     */
    public void setNullable(boolean isNullable) {
        this.nullable = isNullable;
    }

    /**
     * Return true if the DB column is unique.
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Set to true if the DB column is unique.
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * Return true if this is a version column used for concurrency checking.
     */
    public boolean isVersionColumn() {
        return versionColumn;
    }

    /**
     * Set if this is a version column used for concurrency checking.
     */
    public void setVersionColumn(boolean isVersionColumn) {
        this.versionColumn = isVersionColumn;
    }

    /**
     * Return true if this should be eager fetched by default.
     */
    public boolean isFetchEager() {
        return fetchEager;
    }

    /**
     * Set the default fetch type for this property.
     */
    public void setFetchType(FetchType fetchType) {
        this.fetchEager = FetchType.EAGER.equals(fetchType);
    }

    /**
     * Return the formula this property is based on.
     */
    public String getSqlFormulaSelect() {
        return sqlFormulaSelect;
    }

    public String getSqlFormulaJoin() {
        return sqlFormulaJoin;
    }

    /**
     * The property is based on a formula.
     */
    public void setSqlFormula(String formulaSelect, String formulaJoin) {
        this.sqlFormulaSelect = formulaSelect;
        this.sqlFormulaJoin = formulaJoin.equals("") ? null : formulaJoin;
        this.dbRead = true;
        this.dbInsertable = false;
        this.dbUpdateable = false;
    }

    public String getElPlaceHolder(EntityType et) {
        if (sqlFormulaSelect != null) {
            return sqlFormulaSelect;
        } else {
            if (secondaryTableJoinPrefix != null){
                return "${"+secondaryTableJoinPrefix+"}"+getDbColumn();
            }
            // prepend table alias placeholder
            return ElPropertyValue.ROOT_ELPREFIX + getDbColumn();
        }
    }

    /**
     * The database column name this is mapped to.
     */
    public String getDbColumn() {
        if (sqlFormulaSelect != null) {
            return sqlFormulaSelect;
        }
        return dbColumn;
    }

    /**
     * Set the database column name this is mapped to.
     */
    public void setDbColumn(String dbColumn) {
        this.dbColumn = InternString.intern(dbColumn);
    }

    /**
     * Return the database jdbc data type this is mapped to.
     */
    public int getDbType() {
        return dbType;
    }

    /**
     * Set the database jdbc data type this is mapped to.
     */
    public void setDbType(int dbType) {
        this.dbType = dbType;
        this.lob = isLobType(dbType);
    }

    /**
     * Return true if this is mapped to a Clob Blob LongVarchar or
     * LongVarbinary.
     */
    public boolean isLob() {
        return lob;
    }

    private boolean isLobType(int type) {
        switch (type) {
        case Types.CLOB:
            return true;
        case Types.BLOB:
            return true;
        case Types.LONGVARBINARY:
            return true;
        case Types.LONGVARCHAR:
            return true;

        default:
            return false;
        }
    }

    /**
     * Return true if this property is based on a secondary table.
     */
    public boolean isSecondaryTable() {
        return secondaryTable != null;
    }

    /**
     * Return the secondary table this property is associated with.
     */
    public String getSecondaryTable() {
        return secondaryTable;
    }

    /**
     * Set to true if this property is included in persisting.
     */
    public void setSecondaryTable(String secondaryTable) {
        this.secondaryTable = secondaryTable;
        this.dbInsertable = false;
        this.dbUpdateable = false;
    }

    /**
     * 
     */
    public String getSecondaryTableJoinPrefix() {
        return secondaryTableJoinPrefix;
    }

    public DeployTableJoin getSecondaryTableJoin() {
        return secondaryTableJoin;
    }

    public void setSecondaryTableJoin(DeployTableJoin secondaryTableJoin, String prefix) {
        this.secondaryTableJoin = secondaryTableJoin;
        this.secondaryTableJoinPrefix = prefix;
    }

    /**
     * Return the DB Bind parameter. Typically is "?" but can be different for
     * encrypted bind.
     */
    public String getDbBind() {
        return dbBind;
    }

    /**
     * Set the DB bind parameter (if different from "?").
     */
    public void setDbBind(String dbBind) {
        this.dbBind = dbBind;
    }

    /**
     * Return true if this property is encrypted in the DB.
     */
    public boolean isDbEncrypted() {
        return dbEncrypted;
    }

//    /**
//     * Set true if this property should be encrypted in the DB.
//     */
//    public void setDbEncrypted(boolean dbEncrypted) {
//        this.dbEncrypted = dbEncrypted;
//    }
    
    public DbEncryptFunction getDbEncryptFunction() {
        return dbEncryptFunction;
    }

    public void setDbEncryptFunction(DbEncryptFunction dbEncryptFunction, DbEncrypt dbEncrypt, int dbLen) {
        this.dbEncryptFunction = dbEncryptFunction;
        this.dbEncrypted = true;
        this.dbBind = dbEncryptFunction.getEncryptBindSql();
        
        this.dbEncryptedType = isLob() ? Types.BLOB : dbEncrypt.getEncryptDbType();
        if (dbLen > 0){
            setDbLength(dbLen);
        }
    }

    /**
     * Return the DB type for the encrypted property. This can differ from the
     * logical type (String encrypted and stored in a VARBINARY)
     */
    public int getDbEncryptedType() {
        return dbEncryptedType;
    }

    /**
     * Set the DB type used to store the encrypted value.
     */
    public void setDbEncryptedType(int dbEncryptedType) {
        this.dbEncryptedType = dbEncryptedType;
    }

    /**
     * Return true if this property is included in database queries.
     */
    public boolean isDbRead() {
        return dbRead;
    }

    /**
     * Set to true if this property is included in database queries.
     */
    public void setDbRead(boolean isDBRead) {
        this.dbRead = isDBRead;
    }

    public boolean isDbInsertable() {
        return dbInsertable;
    }

    public void setDbInsertable(boolean insertable) {
        this.dbInsertable = insertable;
    }

    public boolean isDbUpdateable() {
        return dbUpdateable;
    }

    public void setDbUpdateable(boolean updateable) {
        this.dbUpdateable = updateable;
    }

    /**
     * Return true if the property is transient.
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Mark the property explicitly as a transient property.
     */
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    /**
     * Set the bean read method.
     * <p>
     * NB: That a BeanReflectGetter is used to actually perform the getting of
     * property values from a bean. This is due to performance considerations.
     * </p>
     */
    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    /**
     * Set the bean write method.
     * <p>
     * NB: That a BeanReflectSetter is used to actually perform the setting of
     * property values to a bean. This is due to performance considerations.
     * </p>
     */
    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    /**
     * Return the property type.
     */
    public Class<?> getPropertyType() {
        return propertyType;
    }

    /**
     * Return true if this is included in the unique id.
     */
    public boolean isId() {
        return id;
    }

    /**
     * Set to true if this is included in the unique id.
     */
    public void setId(boolean id) {
        this.id = id;
    }

    /**
     * Return true if this is an Embedded property. In this case it shares the
     * table and pk of its owner object.
     */
    public boolean isEmbedded() {
        return embedded;
    }

    /**
     * Set to true if this is an embedded property.
     */
    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public Map<String, String> getExtraAttributeMap() {
        return extraAttributeMap;
    }

    /**
     * Return an extra attribute set on this property.
     */
    public String getExtraAttribute(String key) {
        return (String) extraAttributeMap.get(key);
    }

    /**
     * Set an extra attribute set on this property.
     */
    public void setExtraAttribute(String key, String value) {
        extraAttributeMap.put(key, value);
    }

    /**
     * Return the default value.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value. Inserted if the value is null.
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String toString() {
        return desc.getFullName() + "." + name;
    }

    public boolean isIndexed() {
      return indexed;
    }

    public void setIndexed(boolean indexed) {
      this.indexed = indexed;
    }

    public String getIndexName() {
      return indexName;
    }

    public void setIndexName(String indexName) {
      this.indexName = indexName;
    }
}
