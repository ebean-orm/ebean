package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.ValuePair;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.dbplatform.DbEncryptFunction;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.plugin.Property;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedWhenCreated;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedWhenModified;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.el.ElPropertyChainBuilder;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.properties.BeanPropertyGetter;
import com.avaje.ebeaninternal.server.properties.BeanPropertySetter;
import com.avaje.ebeaninternal.server.query.SqlBeanLoad;
import com.avaje.ebeaninternal.server.query.SqlJoinType;
import com.avaje.ebeaninternal.server.text.json.ReadJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypeBoolean;
import com.avaje.ebeaninternal.server.type.ScalarTypeEnum;
import com.avaje.ebeaninternal.util.ValueUtil;
import com.avaje.ebeanservice.docstore.api.mapping.DocMappingBuilder;
import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyMapping;
import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyOptions;
import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.avaje.ebeanservice.docstore.api.support.DocStructure;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description of a property of a bean. Includes its deployment information such
 * as database column mapping information.
 */
public class BeanProperty implements ElPropertyValue, Property {

  private static final Logger logger = LoggerFactory.getLogger(BeanProperty.class);

  /**
   * Flag to mark this at part of the unique id.
   */
  final boolean id;

  /**
   * Flag to make this as a dummy property for unidirecitonal relationships.
   */
  final boolean unidirectionalShadow;

  /**
   * Flag set if this maps to the inheritance discriminator column
   */
  final boolean discriminator;

  /**
   * Flag to mark the property as embedded. This could be on
   * BeanPropertyAssocOne rather than here. Put it here for checking Id type
   * (embedded or not).
   */
  final boolean embedded;

  /**
   * Flag indicating if this the version property.
   */
  final boolean version;

  final boolean naturalKey;

  /**
   * Set if this property is nullable.
   */
  final boolean nullable;

  final boolean unique;

  /**
   * Is this property include in database resultSet.
   */
  final boolean dbRead;

  /**
   * Include in DB insert.
   */
  final boolean dbInsertable;

  /**
   * Include in DB update.
   */
  final boolean dbUpdatable;

  /**
   * True if the property is based on a SECONDARY table.
   */
  final boolean secondaryTable;

  final TableJoin secondaryTableJoin;
  final String secondaryTableJoinPrefix;

  /**
   * The property is inherited from a super class.
   */
  final boolean inherited;

  final Class<?> owningType;

  final boolean local;

  /**
   * True if the property is a Clob, Blob LongVarchar or LongVarbinary.
   */
  final boolean lob;

  final boolean fetchEager;

  final boolean isTransient;

  /**
   * The logical bean property name.
   */
  final String name;

  final int propertyIndex;

  /**
   * The reflected field.
   */
  final Field field;

  /**
   * The bean type.
   */
  final Class<?> propertyType;

  final String dbBind;

  /**
   * The database column. This can include quoted identifiers.
   */
  final String dbColumn;

  final String elPlaceHolder;
  final String elPlaceHolderEncrypted;

  /**
   * Select part of a SQL Formula used to populate this property.
   */
  final String sqlFormulaSelect;

  /**
   * Join part of a SQL Formula.
   */
  final String sqlFormulaJoin;

  final boolean formula;

  /**
   * Set to true if stored encrypted.
   */
  final boolean dbEncrypted;

  final boolean localEncrypted;

  final int dbEncryptedType;

  /**
   * The jdbc data type this maps to.
   */
  final int dbType;

  final boolean excludedFromHistory;

  /**
   * Generator for insert or update timestamp etc.
   */
  final GeneratedProperty generatedProperty;

  final BeanPropertyGetter getter;

  final BeanPropertySetter setter;

  final BeanDescriptor<?> descriptor;

  /**
   * Used for non-jdbc native types (java.util.Date Enums etc). Converts from
   * logical to jdbc types.
   */
  @SuppressWarnings("rawtypes")
  final ScalarType scalarType;

  final DocPropertyOptions docOptions;

  /**
   * The length or precision for DB column.
   */
  final int dbLength;

  /**
   * The scale for DB column (decimal).
   */
  final int dbScale;

  /**
   * Deployment defined DB column definition.
   */
  final String dbColumnDefn;

  /**
   * DB Column default value for DDL definition (FALSE, NOW etc).
   */
  final String dbColumnDefault;

  /**
   * Database DDL column comment.
   */
  final String dbComment;

  final DbEncryptFunction dbEncryptFunction;

  int deployOrder;

  final boolean jsonSerialize;

  final boolean jsonDeserialize;

  final boolean draft;

  final boolean draftOnly;

  final boolean draftDirty;

  final boolean draftReset;

  final boolean softDelete;

  final String softDeleteDbSet;

  final String softDeleteDbPredicate;

  final boolean indexed;

  final String indexName;

  public BeanProperty(DeployBeanProperty deploy) {
    this(null, deploy);
  }

  public BeanProperty(BeanDescriptor<?> descriptor, DeployBeanProperty deploy) {

    this.descriptor = descriptor;
    this.name = InternString.intern(deploy.getName());
    this.propertyIndex = deploy.getPropertyIndex();
    this.indexed = deploy.isIndexed();
    this.indexName = deploy.getIndexName();
    this.unidirectionalShadow = deploy.isUndirectionalShadow();
    this.discriminator = deploy.isDiscriminator();
    this.localEncrypted = deploy.isLocalEncrypted();
    this.dbEncrypted = deploy.isDbEncrypted();
    this.dbEncryptedType = deploy.getDbEncryptedType();
    this.dbEncryptFunction = deploy.getDbEncryptFunction();
    this.dbBind = deploy.getDbBind();
    this.dbRead = deploy.isDbRead();
    this.dbInsertable = deploy.isDbInsertable();
    this.dbUpdatable = deploy.isDbUpdateable();
    this.excludedFromHistory = deploy.isExcludedFromHistory();
    this.draft = deploy.isDraft();
    this.draftDirty = deploy.isDraftDirty();
    this.draftOnly = deploy.isDraftOnly();
    this.draftReset = deploy.isDraftReset();
    this.secondaryTable = deploy.isSecondaryTable();
    if (secondaryTable) {
      this.secondaryTableJoin = new TableJoin(deploy.getSecondaryTableJoin());
      this.secondaryTableJoinPrefix = deploy.getSecondaryTableJoinPrefix();
    } else {
      this.secondaryTableJoin = null;
      this.secondaryTableJoinPrefix = null;
    }
    this.fetchEager = deploy.isFetchEager();
    this.isTransient = deploy.isTransient();
    this.nullable = deploy.isNullable();
    this.unique = deploy.isUnique();
    this.naturalKey = deploy.isNaturalKey();
    this.dbLength = deploy.getDbLength();
    this.dbScale = deploy.getDbScale();
    this.dbColumnDefn = InternString.intern(deploy.getDbColumnDefn());
    this.dbColumnDefault = deploy.getDbColumnDefault();

    this.inherited = false;// deploy.isInherited();
    this.owningType = deploy.getOwningType();
    this.local = deploy.isLocal();

    this.version = deploy.isVersionColumn();
    this.embedded = deploy.isEmbedded();
    this.id = deploy.isId();
    this.generatedProperty = deploy.getGeneratedProperty();
    this.getter = deploy.getGetter();
    this.setter = deploy.getSetter();

    this.dbColumn = tableAliasIntern(descriptor, deploy.getDbColumn(), false, null);
    this.dbComment = deploy.getDbComment();
    this.sqlFormulaJoin = InternString.intern(deploy.getSqlFormulaJoin());
    this.sqlFormulaSelect = InternString.intern(deploy.getSqlFormulaSelect());
    this.formula = sqlFormulaSelect != null;

    this.dbType = deploy.getDbType();
    this.scalarType = deploy.getScalarType();
    this.lob = isLobType(dbType);
    this.propertyType = deploy.getPropertyType();
    this.field = deploy.getField();
    this.docOptions = deploy.getDocPropertyOptions();

    this.elPlaceHolder = tableAliasIntern(descriptor, deploy.getElPlaceHolder(), false, null);
    this.elPlaceHolderEncrypted = tableAliasIntern(descriptor, deploy.getElPlaceHolder(), dbEncrypted, dbColumn);

    this.softDelete = deploy.isSoftDelete();
    if (softDelete) {
      ScalarTypeBoolean.BooleanBase boolType = (ScalarTypeBoolean.BooleanBase)scalarType;
      this.softDeleteDbSet = dbColumn+"="+boolType.getDbTrueLiteral();
      this.softDeleteDbPredicate = "."+dbColumn+","+boolType.getDbFalseLiteral()+")="+boolType.getDbFalseLiteral();
    } else {
      this.softDeleteDbSet = null;
      this.softDeleteDbPredicate = null;
    }

    this.jsonSerialize = deploy.isJsonSerialize();
    this.jsonDeserialize = deploy.isJsonDeserialize();
  }

  private String tableAliasIntern(BeanDescriptor<?> descriptor, String s, boolean dbEncrypted, String dbColumn) {
    if (descriptor != null) {
      s = StringHelper.replaceString(s, "${ta}.", "${}");
      s = StringHelper.replaceString(s, "${ta}", "${}");

      if (dbEncrypted) {
        s = dbEncryptFunction.getDecryptSql(s);
        String namedParam = ":encryptkey_" + descriptor.getBaseTable() + "___" + dbColumn;
        s = StringHelper.replaceString(s, "?", namedParam);
      }
    }
    return InternString.intern(s);
  }

  /**
   * Create a Matching BeanProperty with some attributes overridden.
   * <p>
   * Primarily for supporting Embedded beans with overridden dbColumn
   * mappings.
   * </p>
   */
  public BeanProperty(BeanProperty source, BeanPropertyOverride override) {

    this.descriptor = source.descriptor;
    this.name = InternString.intern(source.getName());
    this.propertyIndex = source.propertyIndex;

    this.indexed = source.isIndexed();
    this.indexName = source.getIndexName();
    this.dbColumn = InternString.intern(override.getDbColumn());
    // override with sqlFormula not currently supported
    this.sqlFormulaJoin = null;
    this.sqlFormulaSelect = null;
    this.formula = false;

    this.excludedFromHistory = source.excludedFromHistory;
    this.draft = source.draft;
    this.draftDirty = source.draftDirty;
    this.draftOnly = source.draftOnly;
    this.draftReset = source.draftReset;
    this.softDelete = source.softDelete;
    this.softDeleteDbSet = source.softDeleteDbSet;
    this.softDeleteDbPredicate = source.softDeleteDbPredicate;
    this.fetchEager = source.fetchEager;
    this.unidirectionalShadow = source.unidirectionalShadow;
    this.discriminator = source.discriminator;
    this.localEncrypted = source.isLocalEncrypted();
    this.isTransient = source.isTransient();
    this.secondaryTable = source.isSecondaryTable();
    this.secondaryTableJoin = source.secondaryTableJoin;
    this.secondaryTableJoinPrefix = source.secondaryTableJoinPrefix;

    this.dbComment = source.dbComment;
    this.dbBind = source.getDbBind();
    this.dbEncrypted = source.isDbEncrypted();
    this.dbEncryptedType = source.getDbEncryptedType();
    this.dbEncryptFunction = source.dbEncryptFunction;
    this.dbRead = source.isDbRead();
    this.dbInsertable = source.isDbInsertable();
    this.dbUpdatable = source.isDbUpdatable();
    this.nullable = source.isNullable();
    this.unique = source.isUnique();
    this.naturalKey = source.isNaturalKey();
    this.dbLength = source.getDbLength();
    this.dbScale = source.getDbScale();
    this.dbColumnDefn = InternString.intern(source.getDbColumnDefn());
    this.dbColumnDefault = source.dbColumnDefault;

    this.inherited = source.isInherited();
    this.owningType = source.owningType;
    this.local = owningType.equals(descriptor.getBeanType());

    this.version = source.isVersion();
    this.embedded = source.isEmbedded();
    this.id = source.isId();
    this.generatedProperty = source.getGeneratedProperty();
    this.getter = source.getter;
    this.setter = source.setter;
    this.dbType = source.getDbType();
    this.scalarType = source.scalarType;
    this.lob = isLobType(dbType);
    this.propertyType = source.getPropertyType();
    this.field = source.getField();
    this.docOptions = source.docOptions;

    this.elPlaceHolder = override.replace(source.elPlaceHolder, source.dbColumn);
    this.elPlaceHolderEncrypted = override.replace(source.elPlaceHolderEncrypted, source.dbColumn);

    this.jsonSerialize = source.jsonSerialize;
    this.jsonDeserialize = source.jsonDeserialize;
  }

  /**
   * Initialise the property before returning to client code. Used to
   * initialise variables that can't be done in construction due to recursive
   * issues.
   */
  public void initialise() {
    // do nothing for normal BeanProperty
    if (!isTransient && scalarType == null) {
      String msg = "No ScalarType assigned to " + descriptor.getFullName() + "." + getName();
      throw new RuntimeException(msg);
    }
  }

  /**
   * Return the order this property appears in the bean.
   */
  public int getDeployOrder() {
    return deployOrder;
  }

  /**
   * Set the order this property appears in the bean.
   */
  public void setDeployOrder(int deployOrder) {
    this.deployOrder = deployOrder;
  }

  public ElPropertyValue buildElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain,
                                              boolean propertyDeploy) {
    return null;
  }

  /**
   * Return the BeanDescriptor that owns this property.
   */
  public BeanDescriptor<?> getBeanDescriptor() {
    return descriptor;
  }

  /**
   * Return true is this is a simple scalar property.
   */
  public boolean isScalar() {
    return true;
  }

  /**
   * Return true if this property is based on a formula.
   */
  public boolean isFormula() {
    return formula;
  }

  /**
   * Return true if this property maps to the inheritance discriminator column.
   */
  public boolean isDiscriminator() {
    return discriminator;
  }

  /**
   * Return true if the underlying type is mutable.
   */
  public boolean isMutableScalarType() {
    return scalarType != null && scalarType.isMutable();
  }

  /**
   * Return the encrypt key for the column matching this property.
   */
  public EncryptKey getEncryptKey() {
    return descriptor.getEncryptKey(this);
  }

  public String getDecryptProperty(String propertyName) {
    return dbEncryptFunction.getDecryptSql(propertyName);
  }

  public String getDecryptSql() {
    return dbEncryptFunction.getDecryptSql(this.getDbColumn());
  }

  public String getDecryptSql(String tableAlias) {
    return dbEncryptFunction.getDecryptSql(tableAlias + "." + this.getDbColumn());
  }

  /**
   * Add any extra joins required to support this property. Generally a no
   * operation except for a OneToOne exported.
   */
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    if (formula && sqlFormulaJoin != null) {
      ctx.appendFormulaJoin(sqlFormulaJoin, joinType);

    } else if (secondaryTableJoin != null) {

      String relativePrefix = ctx.getRelativePrefix(secondaryTableJoinPrefix);
      secondaryTableJoin.addJoin(joinType, relativePrefix, ctx);
    }
  }

  /**
   * Returns null unless this property is using a secondary table. In that
   * case this returns the logical property prefix.
   */
  public String getSecondaryTableJoinPrefix() {
    return secondaryTableJoinPrefix;
  }

  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    if (formula) {
      ctx.appendFormulaSelect(sqlFormulaSelect);

    } else if (!isTransient && !ignoreDraftOnlyProperty(ctx.isDraftQuery())) {

      if (secondaryTableJoin != null) {
        String relativePrefix = ctx.getRelativePrefix(secondaryTableJoinPrefix);
        ctx.pushTableAlias(relativePrefix);
      }

      if (dbEncrypted) {
        String decryptSql = getDecryptSql(ctx.peekTableAlias());
        ctx.appendRawColumn(decryptSql);
        ctx.addEncryptedProp(this);

      } else {
        ctx.appendColumn(dbColumn);
      }

      if (secondaryTableJoin != null) {
        ctx.popTableAlias();
      }
    }
  }

  @Override
  public boolean isMany() {
    return false;
  }

  public boolean isAssignableFrom(Class<?> type) {
    return owningType.isAssignableFrom(type);
  }

  public void loadIgnore(DbReadContext ctx) {
    scalarType.loadIgnore(ctx.getDataReader());
  }

  public void load(SqlBeanLoad sqlBeanLoad) {
    sqlBeanLoad.load(this);
  }

  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
    if (prefix == null) {
      selectChain.add(name);
    } else {
      selectChain.add(prefix + "." + name);
    }
  }

  public Object read(DbReadContext ctx) throws SQLException {
    return scalarType.read(ctx.getDataReader());
  }

  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {

    try {
      Object value = scalarType.read(ctx.getDataReader());
      if (bean != null) {
        setValue(bean, value);
      }
      return value;
    } catch (Exception e) {
      throw new PersistenceException("Error readSet on " + descriptor + "." + name, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void bind(DataBind b, Object value) throws SQLException {
    scalarType.bind(b, value);
  }

  @SuppressWarnings(value = "unchecked")
  public void writeData(DataOutput dataOutput, Object value) throws IOException {
    scalarType.writeData(dataOutput, value);
  }

  public Object readData(DataInput dataInput) throws IOException {
    return scalarType.readData(dataInput);
  }

  public BeanProperty getBeanProperty() {
    return this;
  }

  public boolean isIndexed() {
    return indexed;
  }

  public String getIndexName() {
    return indexName;
  }

  /**
   * Return true if this object is part of an inheritance hierarchy.
   */
  public boolean isInherited() {
    return inherited;
  }

  /**
   * Return true is this type is not from a super type.
   */
  public boolean isLocal() {
    return local;
  }

  /**
   * Copy/set the property value from the draft bean to the live bean.
   */
  public void publish(EntityBean draftBean, EntityBean liveBean) {

    if (!version && !draftOnly) {
      // set property value from draft to live
      Object value = getValueIntercept(draftBean);
      setValueIntercept(liveBean, value);
    }
  }

  /**
   * Return the DB literal expression to set the deleted state to true.
   */
  public String getSoftDeleteDbSet() {
    return softDeleteDbSet;
  }

  /**
   * Return the DB literal predicate used to filter out soft deleted rows from a query.
   */
  public String getSoftDeleteDbPredicate(String tableAlias) {
    // use coalesce to handle null values from optional relationships
    return "coalesce(" + tableAlias + softDeleteDbPredicate;
  }

  /**
   * Set the soft delete property value on the bean without invoking lazy loading.
   */
  public void setSoftDeleteValue(EntityBean bean) {
    // assumes boolean deleted true being set which is ok limitation for now
    setValue(bean, true);
    bean._ebean_getIntercept().setChangedProperty(propertyIndex);
  }

  /**
   * Set the changed value without invoking interception (lazy loading etc).
   * Typically used to set generated values on update.
   */
  public void setValueChanged(EntityBean bean, Object value) {
    setValue(bean, value);
    bean._ebean_getIntercept().setChangedProperty(propertyIndex);
  }

  /**
   * Set the value of the property without interception or
   * PropertyChangeSupport.
   */
  public void setValue(EntityBean bean, Object value) {
    try {
      setter.set(bean, value);
    } catch (Exception ex) {
      throw new RuntimeException(setterErrorMsg(bean, value, "set "), ex);
    }
  }

  /**
   * Set the value of the property.
   */
  public void setValueIntercept(EntityBean bean, Object value) {
    try {
      setter.setIntercept(bean, value);
    } catch (Exception ex) {
      throw new RuntimeException(setterErrorMsg(bean, value, "setIntercept "), ex);
    }
  }

  /**
   * Return an error message when calling a setter.
   */
  private String setterErrorMsg(EntityBean bean, Object value, String prefix) {
    String beanType = bean == null ? "null" : bean.getClass().getName();
    return prefix + name + " on [" + descriptor + "] arg[" + value + "] type[" + beanType + "] threw error";
  }

  /**
   * Return true if this property should be included in the cache bean data.
   */
  public boolean isCacheDataInclude() {
    return true;
  }

  /**
   * Return the value for this property which we hold in the L2 cache entry.
   * <p>
   * This uses format() where possible to store the value as a string and this
   * is done to make any resulting Java object serialisation content smaller as
   * strings get special treatment.
   * </p>
   */
  public Object getCacheDataValue(EntityBean bean) {
    Object value = getValue(bean);
    if (value == null || scalarType.isBinaryType()) {
      return value;
    } else {
      // convert to string as an optimisation for java object serialisation
      return scalarType.format(value);
    }
  }

  /**
   * Read the value for this property from L2 cache entry and set it to the bean.
   * <p>
   * This uses parse() as per the comment in getCacheDataValue().
   * </p>
   */
  public void setCacheDataValue(EntityBean bean, Object cacheData, PersistenceContext context) {
    if (cacheData instanceof String) {
      // parse back from string to support optimisation of java object serialisation
      cacheData = scalarType.parse((String)cacheData);
    }
    setValue(bean, cacheData);
  }

  /**
   * Get the property value from a compound value type.
   */
  public Object getValueObject(Object bean) {
    throw new RuntimeException("Expected to be called only on BeanPropertyCompoundScalar");
  }

  public Object getVal(Object bean) {
    return getValue((EntityBean)bean);
  }

  /**
   * Return the value of the property method.
   */
  public Object getValue(EntityBean bean) {
    try {
      return getter.get(bean);
    } catch (Exception ex) {
      String beanType = bean == null ? "null" : bean.getClass().getName();
      String msg = "get " + name + " on [" + descriptor + "] type[" + beanType + "] threw error.";
      throw new RuntimeException(msg, ex);
    }
  }

  public Object getValueIntercept(EntityBean bean) {
    try {
      return getter.getIntercept(bean);
    } catch (Exception ex) {
      String beanType = bean == null ? "null" : bean.getClass().getName();
      String msg = "getIntercept " + name + " on [" + descriptor + "] type[" + beanType + "] threw error.";
      throw new RuntimeException(msg, ex);
    }
  }

  public Object convert(Object value) {
    if (value == null) {
      return null;
    }
    return convertToLogicalType(value);
  }

  @Override
  public void pathSet(Object bean, Object value) {

    if (bean != null) {
      Object logicalVal = convertToLogicalType(value);
      setValue((EntityBean)bean, logicalVal);
    }
  }

  @Override
  public Object pathGet(Object bean) {
    if (bean == null) {
      return null;
    }
    return getValueIntercept((EntityBean)bean);
  }

  @Override
  public Object pathGetNested(Object bean) {
    throw new RuntimeException("Not expected to call this");
  }

  /**
   * Return the name of the property.
   */
  public String getName() {
    return name;
  }

  /**
   * Return the position of this property in the enhanced bean.
   */
  public int getPropertyIndex() {
    return propertyIndex;
  }

  public String getElName() {
    return name;
  }

  @Override
  public boolean containsFormulaWithJoin() {
    return formula && sqlFormulaJoin != null;
  }

  public boolean containsManySince(String sinceProperty) {
    return containsMany();
  }

  public boolean containsMany() {
    return false;
  }

  @Override
  public String getAssocIsEmpty(SpiExpressionRequest request, String path) {
    // overridden in BanePropertyAssocMany
    throw new RuntimeException("Not Supported or Expected");
  }

  public Object[] getAssocIdValues(EntityBean bean) {
    // Returns null as not an AssocOne.
    return null;
  }

  public String getAssocIdExpression(String prefix, String operator) {
    // Returns null as not an AssocOne.
    return null;
  }

  public String getAssocIdInExpr(String prefix) {
    // Returns null as not an AssocOne.
    return null;
  }

  public String getAssocIdInValueExpr(int size) {
    // Returns null as not an AssocOne.
    return null;
  }

  public boolean isAssocId() {
    // Returns false - override in BeanPropertyAssocOne.
    return false;
  }

  public boolean isAssocProperty() {
    // Returns false - override in BeanPropertyAssocOne.
    return false;
  }

  public String getElPlaceholder(boolean encrypted) {
    return encrypted ? elPlaceHolderEncrypted : elPlaceHolder;
  }

  public String getElPrefix() {
    return secondaryTableJoinPrefix;
  }

  /**
   * Return the full name of this property.
   */
  public String getFullBeanName() {
    return descriptor.getFullName() + "." + name;
  }

  /**
   * Return true if the mutable value is considered dirty.
   * This is only used for 'mutable' scalar types like hstore etc.
   */
  public boolean isDirtyValue(Object value) {
    return scalarType.isDirty(value);
  }

  /**
   * Return the scalarType.
   */
  @SuppressWarnings(value = "unchecked")
  public ScalarType<Object> getScalarType() {
    return scalarType;
  }

  public StringParser getStringParser() {
    return scalarType;
  }

  public boolean isDateTimeCapable() {
    return scalarType != null && scalarType.isDateTimeCapable();
  }

  public int getJdbcType() {
    return scalarType == null ? 0 : scalarType.getJdbcType();
  }

  public Object parseDateTime(long systemTimeMillis) {
    return scalarType.convertFromMillis(systemTimeMillis);
  }

  /**
   * Return the DB max length (varchar) or precision (decimal).
   */
  public int getDbLength() {
    return dbLength;
  }

  /**
   * Return the DB scale for numeric columns.
   */
  public int getDbScale() {
    return dbScale;
  }

  /**
   * Return a specific column DDL definition if specified (otherwise null).
   */
  public String getDbColumnDefn() {
    return dbColumnDefn;
  }

  /**
   * Return the DB constraint expression (can be null).
   * <p>
   * For an Enum returns IN expression for the set of Enum values.
   * </p>
   */
  public Set<String> getDbCheckConstraintValues() {
    if (scalarType instanceof ScalarTypeEnum) {
      return ((ScalarTypeEnum) scalarType).getDbCheckConstraintValues();
    }
    return null;
  }

  /**
   * Return the DB column type definition.
   */
  public String renderDbType(DbType dbType, boolean strict) {
    if (dbColumnDefn != null) {
      return dbColumnDefn;
    }
    return dbType.renderType(dbLength, dbScale, strict);
  }

  /**
   * Return the DB column default to use for DDL.
   */
  public String getDbColumnDefault() {
    return dbColumnDefn != null ? null : dbColumnDefault;
  }

  /**
   * Return the bean Field associated with this property.
   */
  public Field getField() {
    return field;
  }

  /**
   * Return the GeneratedValue. Used to generate update timestamp etc.
   */
  public GeneratedProperty getGeneratedProperty() {
    return generatedProperty;
  }

  /**
   * Return true if this is the natural key property.
   */
  public boolean isNaturalKey() {
    return naturalKey;
  }

  /**
   * Return true if this property is mandatory.
   */
  public boolean isNullable() {
    return nullable;
  }

  /**
   * Return true if DDL Not NULL constraint should be defined for this column
   * based on it being a version column or having a generated property.
   */
  public boolean isDDLNotNull() {
    return isVersion() || (generatedProperty != null && generatedProperty.isDDLNotNullable());
  }

  /**
   * Return true if this is a generated property mapping to @WhenCreated or @CreatedTimestamp.
   */
  public boolean isGeneratedWhenCreated() {
    return generatedProperty instanceof GeneratedWhenCreated;
  }

  /**
   * Return true if this is a generated property mapping to @WhenModified or @UpdatedTimestamp.
   */
  public boolean isGeneratedWhenModified() {
    return generatedProperty instanceof GeneratedWhenModified;
  }

  /**
   * Return true if the DB column should be unique.
   */
  public boolean isUnique() {
    return unique;
  }

  /**
   * Return true if the property is transient.
   */
  public boolean isTransient() {
    return isTransient;
  }

  /**
   * Return true if this property is loadable from a resultSet.
   */
  public boolean isLoadProperty(boolean draftQuery) {
    return !ignoreDraftOnlyProperty(draftQuery) && (!isTransient || formula);
  }

  /**
   * Return true if this is a draftOnly property on a non-asDraft query and as such this
   * property should not be included in a sql query.
   */
  protected boolean ignoreDraftOnlyProperty(boolean draftQuery) {
    return draftOnly && !draftQuery;
  }

  /**
   * Return true if this is a version column used for concurrency checking.
   */
  public boolean isVersion() {
    return version;
  }

  /**
   * The database column name this is mapped to.
   */
  public String getDbColumn() {
    return dbColumn;
  }

  /**
   * Return the comment for the associated DB column.
   */
  public String getDbComment() {
    return dbComment;
  }

  /**
   * Return the database jdbc data type this is mapped to.
   */
  public int getDbType() {
    return dbType;
  }

  /**
   * Perform DB to Logical type conversion (if necessary).
   */
  public Object convertToLogicalType(Object value) {
    if (scalarType != null) {
      return scalarType.toBeanType(value);
    }
    return value;
  }

  /**
   * Return true if by default this property is set to fetch eager.
   * Lob's usually default to fetch lazy.
   */
  public boolean isFetchEager() {
    return fetchEager;
  }

  /**
   * Return true if this is mapped to a Clob Blob LongVarchar or
   * LongVarbinary.
   */
  public boolean isLob() {
    return lob;
  }

  public static boolean isLobType(int type) {
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
   * Return the DB bind parameter. Typically is "?" but different for
   * encrypted bind.
   */
  public String getDbBind() {
    return dbBind;
  }

  /**
   * Returns true if DB encrypted.
   */
  public boolean isLocalEncrypted() {
    return localEncrypted;
  }

  /**
   * Return true if this property is stored encrypted.
   */
  public boolean isDbEncrypted() {
    return dbEncrypted;
  }

  public int getDbEncryptedType() {
    return dbEncryptedType;
  }

  /**
   * Return true if this property is excluded from history.
   */
  public boolean isExcludedFromHistory() {
    return excludedFromHistory;
  }

  /**
   * Return true if this property only exists on the draft table.
   */
  public boolean isDraftOnly() {
    return draftOnly;
  }

  /**
   * Return true if this property is a boolean flag on a draftable bean
   * indicating if the instance is a draft or live bean.
   */
  public boolean isDraft() {
    return draft;
  }

  /**
   * Return true if this property is a boolean flag only on the draft table
   * indicating that when the draft is different from the published row.
   */
  public boolean isDraftDirty() {
    return draftDirty;
  }

  /**
   * Return true if this property is reset/cleared on publish (on the draft bean).
   */
  public boolean isDraftReset() {
    return draftReset;
  }

  /**
   * Return true if this property is the soft delete property.
   */
  public boolean isSoftDelete() {
    return softDelete;
  }

  /**
   * Return true if this property should be included in an Insert.
   */
  public boolean isDbInsertable() {
    return dbInsertable;
  }

  /**
   * Return true if this property should be included in an Update.
   */
  public boolean isDbUpdatable() {
    return dbUpdatable;
  }

  /**
   * Return true if this property is included in database queries.
   */
  public boolean isDbRead() {
    return dbRead;
  }

  /**
   * Return true if this property is based on a secondary table (not the base
   * table).
   */
  public boolean isSecondaryTable() {
    return secondaryTable;
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
   * Return true if this is an Embedded property. In this case it shares the
   * table and primary key of its owner object.
   */
  public boolean isEmbedded() {
    return embedded;
  }

  public String toString() {
    return name;
  }

  /**
   * Append this property to the document store based on includeByDefault setting.
   */
  public void docStoreInclude(boolean includeByDefault, DocStructure docStructure) {
    if (includeByDefault) {
      docStructure.addProperty(name);
    }
  }

  @SuppressWarnings(value = "unchecked")
  public void jsonWrite(WriteJson writeJson, EntityBean bean) throws IOException {
    if (!jsonSerialize) {
      return;
    }
    Object value = getValueIntercept(bean);
    if (value == null) {
      writeJson.writeNullField(name);
    } else {
      if (scalarType != null) {
        writeJson.writeFieldName(name);
        scalarType.jsonWrite(writeJson.gen(), value);
      } else {
        writeJson.writeValueUsingObjectMapper(name, value);
      }
    }
  }

  public void jsonRead(ReadJson ctx, EntityBean bean) throws IOException {

    JsonToken event = ctx.nextToken();
    if (JsonToken.VALUE_NULL == event) {
      if (jsonDeserialize) {
        setValue(bean, null);
      }
    } else {
      // expect to read non-null json value
      Object objValue;
      if (scalarType != null) {
        objValue = scalarType.jsonRead(ctx.getParser());
      } else {
        try {
          objValue = ctx.readValueUsingObjectMapper(propertyType);
        } catch (IOException e) {
          // change in behavior for #318
          objValue = null;
          String msg = "Error trying to use Jackson ObjectMapper to read transient property "
              + getFullBeanName() +" - consider marking this property with @JsonIgnore";
          logger.error(msg, e);
        }
      }
      if (jsonDeserialize) {
        setValue(bean, objValue);
      }
    }
  }

  /**
   * Populate diff map for insert if the property is not null.
   */
  public void diffForInsert(String prefix, Map<String, ValuePair> map, EntityBean newBean) {
    Object newVal = (newBean == null) ? null : getValue(newBean);
    if (newVal != null) {
      String propName = (prefix == null) ? name : prefix + "." + name;
      map.put(propName, new ValuePair(newVal, null));
    }
  }

  /**
   * Populate diff map comparing the property values between the beans.
   */
  public void diff(String prefix, Map<String, ValuePair> map, EntityBean newBean, EntityBean oldBean) {
    Object newVal = (newBean == null) ? null : getValue(newBean);
    Object oldVal = (oldBean == null) ? null : getValue(oldBean);
    diffVal(prefix, map, newVal, oldVal);
  }

  /**
   * Populate diff map comparing the property values.
   */
  public void diffVal(String prefix, Map<String, ValuePair> map, Object newVal, Object oldVal) {
    if (!ValueUtil.areEqual(newVal, oldVal)) {
      String propName = (prefix == null) ? name : prefix + "." + name;
      map.put(propName, new ValuePair(newVal, oldVal));
    }
  }

  /**
   * Add to the document mapping if this property is included for this index.
   */
  public void docStoreMapping(DocMappingBuilder mapping, String prefix) {

    if (mapping.includesProperty(prefix, name)) {

      DocPropertyType type = scalarType.getDocType();
      DocPropertyOptions options = docOptions.copy();
      if (DocPropertyType.UUID == type || DocPropertyType.ENUM == type ||isStringId(type)) {
        options.setCode(true);
      }

      mapping.add(new DocPropertyMapping(name, type, options));
    }
  }

  /**
   * Return true if this is a String Id property and should be treated as a code by the document store.
   */
  private boolean isStringId(DocPropertyType type) {
    return DocPropertyType.STRING == type && (id || discriminator);
  }

  public void merge(EntityBean bean, EntityBean existing) {
    // do nothing unless Many property
  }
}
