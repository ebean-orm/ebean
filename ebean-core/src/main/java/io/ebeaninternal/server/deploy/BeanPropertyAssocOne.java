package io.ebeaninternal.server.deploy;

import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.ValuePair;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarDataReader;
import io.ebean.core.type.ScalarType;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cache.CachedBeanData;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.el.ElPropertyChainBuilder;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.query.STreePropertyAssocOne;
import io.ebeaninternal.server.query.SqlBeanLoad;
import io.ebeaninternal.server.query.SqlJoinType;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Property mapped to a joined bean.
 */
public class BeanPropertyAssocOne<T> extends BeanPropertyAssoc<T> implements STreePropertyAssocOne {

  private final boolean oneToOne;
  private final boolean oneToOneExported;
  private final boolean orphanRemoval;
  private final boolean primaryKeyExport;
  private final boolean primaryKeyJoin;

  private AssocOneHelp localHelp;
  final BeanProperty[] embeddedProps;
  private final HashMap<String, BeanProperty> embeddedPropsMap;
  ImportedId importedId;
  private String deleteByParentIdSql;
  private String deleteByParentIdInSql;
  private BeanPropertyAssocMany<?> relationshipProperty;
  private boolean cacheNotifyRelationship;

  /**
   * Create based on deploy information of an EmbeddedId.
   */
  public BeanPropertyAssocOne(BeanDescriptorMap owner, DeployBeanPropertyAssocOne<T> deploy) {
    this(owner, null, deploy);
  }

  /**
   * Create the property.
   */
  public BeanPropertyAssocOne(BeanDescriptorMap owner, BeanDescriptor<?> descriptor,
                              DeployBeanPropertyAssocOne<T> deploy) {
    super(descriptor, deploy);
    primaryKeyExport = deploy.isPrimaryKeyExport();
    primaryKeyJoin = deploy.isPrimaryKeyJoin();
    oneToOne = deploy.isOneToOne();
    oneToOneExported = deploy.isOneToOneExported();
    orphanRemoval = deploy.isOrphanRemoval();
    if (embedded) {
      // Overriding of the columns and use table alias of owning BeanDescriptor
      BeanEmbeddedMeta overrideMeta = BeanEmbeddedMetaFactory.create(owner, deploy);
      embeddedProps = overrideMeta.getProperties();
      embeddedPropsMap = new HashMap<>();
      for (BeanProperty embeddedProp : embeddedProps) {
        embeddedPropsMap.put(embeddedProp.name(), embeddedProp);
      }
    } else {
      embeddedProps = null;
      embeddedPropsMap = null;
    }
  }

  @Override
  public BeanPropertyAssocOne<?> override(BeanPropertyOverride override) {
    return new BeanPropertyAssocOne<>(this, override);
  }

  protected BeanPropertyAssocOne(BeanPropertyAssocOne<T> source, BeanPropertyOverride override) {
    super(source, override);
    primaryKeyExport = source.primaryKeyExport;
    primaryKeyJoin = source.primaryKeyJoin;
    oneToOne = source.oneToOne;
    oneToOneExported = source.oneToOneExported;
    orphanRemoval = source.orphanRemoval;
    embeddedProps = null;
    embeddedPropsMap = null;
  }

  @Override
  public void initialise(BeanDescriptorInitContext initContext) {
    super.initialise(initContext);
    initialiseAssocOne(initContext.getEmbeddedPrefix());
    if (embedded) {
      // initialise ManyToOne importedId
      initContext.setEmbeddedPrefix(name);
      for (BeanProperty embeddedProp : embeddedProps) {
        embeddedProp.initialise(initContext);
      }
      initContext.setEmbeddedPrefix(null);
    }
  }

  private void initialiseAssocOne(String embeddedPrefix) {
    localHelp = createHelp(embedded, oneToOneExported, embeddedPrefix);
    if (!isTransient) {
      //noinspection StatementWithEmptyBody
      if (embedded || descriptor.isDocStoreOnly()) {
        // no imported or exported information
      } else if (!oneToOneExported) {
        importedId = createImportedId(this, targetDescriptor, tableJoin);
        if (importedId == null) {
          throw new PersistenceException("Cannot find imported id for " + fullName() + " from " + targetDescriptor
          + ". If using native-image, possibly missing reflect-config for the Id property.");
        }
        if (importedId.isScalar()) {
          // limit JoinColumn mapping to the @Id / primary key
          TableJoinColumn[] columns = tableJoin.columns();
          String foreignJoinColumn = columns[0].getForeignDbColumn();
          String foreignIdColumn = targetDescriptor.idProperty().dbColumn();
          if (!foreignJoinColumn.equalsIgnoreCase(foreignIdColumn)) {
            throw new PersistenceException("Mapping limitation - @JoinColumn on " + fullName() + " needs to map to a primary key as per Issue #529 "
              + " - joining to " + foreignJoinColumn + " and not " + foreignIdColumn);
          }
        }
      } else {
        exportedProperties = createExported();
        String delStmt = "delete from " + targetDescriptor.baseTable() + " where ";
        deleteByParentIdSql = delStmt + deriveWhereParentIdSql(false);
        deleteByParentIdInSql = delStmt + deriveWhereParentIdSql(true);
      }
    }
  }

  /**
   * Derive late in lifecycle cache notification on this relationship.
   */
  void initialisePostTarget() {
    this.cacheNotifyRelationship = isCacheNotifyRelationship();
  }

  /**
   * Return the property value as an entity bean.
   */
  @Override
  public EntityBean valueAsEntityBean(EntityBean owner) {
    return (EntityBean) getValue(owner);
  }

  void setRelationshipProperty(BeanPropertyAssocMany<?> relationshipProperty) {
    this.relationshipProperty = relationshipProperty;
  }

  /**
   * Return true if this relationship needs to maintain/update L2 cache.
   */
  boolean isCacheNotifyRelationship() {
    return relationshipProperty != null && targetDescriptor.isBeanCaching();
  }

  /**
   * Clear the L2 relationship cache for this property.
   */
  void cacheClear() {
    if (cacheNotifyRelationship) {
      targetDescriptor.cacheManyPropClear(relationshipProperty.name());
    }
  }

  void cacheClear(CacheChangeSet changeSet) {
    if (cacheNotifyRelationship) {
      changeSet.addManyClear(targetDescriptor, relationshipProperty.name());
    }
  }

  /**
   * Clear part of the L2 relationship cache for this property.
   */
  void cacheDelete(boolean clear, EntityBean bean, CacheChangeSet changeSet) {
    if (cacheNotifyRelationship) {
      if (clear) {
        changeSet.addManyClear(targetDescriptor, relationshipProperty.name());
      } else {
        Object assocBean = getValue(bean);
        if (assocBean != null) {
          Object parentId = targetDescriptor.id(assocBean);
          if (parentId != null) {
            final String parentKey = targetDescriptor.cacheKey(parentId);
            changeSet.addManyRemove(targetDescriptor, relationshipProperty.name(), parentKey);
          }
        }
      }
    }
  }

  @Override
  Object naturalKeyVal(Map<String, Object> values) {
    EntityBean bean = (EntityBean) values.get(name);
    if (bean == null) {
      return null;
    }
    return targetIdBinder.cacheKeyFromBean(bean);
  }

  @Override
  public ElPropertyValue buildElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {
    if (embedded) {
      BeanProperty embProp = embeddedPropsMap.get(remainder);
      if (embProp == null) {
        String msg = "Embedded Property " + remainder + " not found in " + fullName();
        throw new PersistenceException(msg);
      }
      if (chain == null) {
        chain = new ElPropertyChainBuilder(true, propName);
      }
      chain.add(this);
      chain.setEmbedded(true);
      return chain.add(embProp).build();
    }
    return createElPropertyValue(propName, remainder, chain, propertyDeploy);
  }

  @Override
  public String elPlaceholder(boolean encrypted) {
    return encrypted ? elPlaceHolderEncrypted : elPlaceHolder;
  }


  @Override
  public SqlUpdate deleteByParentIdList(List<Object> parentIds) {
    String sql = deleteByParentIdInSql + targetIdBinder.idInValueExpr(false, parentIds.size());
    DefaultSqlUpdate delete = new DefaultSqlUpdate(sql);
    bindParentIds(delete, parentIds);
    return delete;
  }

  @Override
  public SqlUpdate deleteByParentId(Object parentId) {
    DefaultSqlUpdate delete = new DefaultSqlUpdate(deleteByParentIdSql);
    bindParentId(delete, parentId);
    return delete;
  }


  @Override
  public List<Object> findIdsByParentId(Object parentId, Transaction t, boolean includeSoftDeletes) {
    String rawWhere = deriveWhereParentIdSql(false);
    SpiEbeanServer server = server();
    SpiQuery<?> q = server.createQuery(type());
    q.usingTransaction(t);
    bindParentIdEq(rawWhere, parentId, q);
    if (includeSoftDeletes) {
      q.setIncludeSoftDeletes();
    }
    return server.findIds(q);
  }

  @Override
  public List<Object> findIdsByParentIdList(List<Object> parentIds, Transaction t, boolean includeSoftDeletes) {
    String rawWhere = deriveWhereParentIdSql(true);
    String inClause = idBinder().idInValueExpr(false, parentIds.size());
    String expr = rawWhere + inClause;
    SpiEbeanServer server = server();
    SpiQuery<?> q = server.createQuery(type());
    q.usingTransaction(t);
    bindParentIdsIn(expr, parentIds, q);
    if (includeSoftDeletes) {
      q.setIncludeSoftDeletes();
    }
    return server.findIds(q);
  }

  void addFkey() {
    if (importedId != null) {
      importedId.addFkeys(name);
    }
  }

  @Override
  public void registerColumn(BeanDescriptor<?> desc, String prefix) {
    if (embedded) {
      for (BeanProperty prop : embeddedProps) {
        prop.registerColumn(desc, SplitName.add(prefix, name));
      }
    } else {
      if (targetIdProperty != null) {
        BeanDescriptor<T> target = targetDescriptor();
        String basePath = SplitName.add(prefix, name);
        if (dbColumn != null) {
          BeanProperty idProperty = target.idProperty();
          desc.registerColumn(dbColumn, SplitName.add(basePath, idProperty.name()));
        }
        desc.registerTable(target.baseTable(), this);
      }
    }
  }

  /**
   * Return meta data for the deployment of the embedded bean specific to this
   * property.
   */
  public BeanProperty[] properties() {
    return embeddedProps;
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
    prefix = SplitName.add(prefix, name);
    if (!embedded) {
      if (targetIdBinder == null) {
        throw new IllegalStateException("No Id binding property for " + fullName()
          + ". Probably a missing @OneToOne mapping annotation on this relationship?");
      }
      targetIdBinder.buildRawSqlSelectChain(prefix, selectChain);
    } else {
      for (BeanProperty embeddedProp : embeddedProps) {
        embeddedProp.buildRawSqlSelectChain(prefix, selectChain);
      }
    }
  }

  @Override
  public boolean hasForeignKey() {
    return foreignKey == null || primaryKeyJoin || !foreignKey.isNoConstraint();
  }

  /**
   * Return true if this a OneToOne property. Otherwise assumed ManyToOne.
   */
  public boolean isOneToOne() {
    return oneToOne;
  }

  /**
   * Return true if this is the exported side of a OneToOne.
   */
  public boolean isOneToOneExported() {
    return oneToOneExported;
  }

  public boolean isOrphanRemoval() {
    return orphanRemoval;
  }

  @Override
  public void diff(String prefix, Map<String, ValuePair> map, EntityBean newBean, EntityBean oldBean) {
    Object newEmb = (newBean == null) ? null : getValue(newBean);
    Object oldEmb = (oldBean == null) ? null : getValue(oldBean);
    if (newEmb == null && oldEmb == null) {
      return;
    }

    String nextPrefix = (prefix == null) ? name : prefix + "." + name;
    if (embedded) {
      BeanDescriptor<T> targetDescriptor = targetDescriptor();
      targetDescriptor.diff(nextPrefix, map, (EntityBean) newEmb, (EntityBean) oldEmb);

    } else {
      // we are only interested in the Id value
      newBean = (EntityBean) newEmb;
      oldBean = (EntityBean) oldEmb;

      BeanDescriptor<T> targetDescriptor = targetDescriptor();
      BeanProperty idProperty = targetDescriptor.idProperty();

      Object newId = (newBean == null) ? null : idProperty.getValue(newBean);
      Object oldId = (oldBean == null) ? null : idProperty.getValue(oldBean);
      if (newId != null || oldId != null) {
        idProperty.diffVal(nextPrefix, map, newId, oldId);
      }
    }
  }

  /**
   * Same as getPropertyType(). Return the type of the bean this property
   * represents.
   */
  @Override
  public Class<?> targetType() {
    return type();
  }

  /**
   * Return the bean cache value for this property using original values.
   */
  @Override
  public Object getCacheDataValueOrig(EntityBeanIntercept ebi) {
    return cacheDataConvert(ebi.origValue(propertyIndex));
  }

  @Override
  public Object getCacheDataValue(EntityBean bean) {
    return cacheDataConvert(getValue(bean));
  }

  private Object cacheDataConvert(Object ap) {
    if (ap == null) {
      return null;
    }
    if (embedded) {
      return targetDescriptor.cacheEmbeddedBeanExtract((EntityBean) ap);
    } else {
      return targetDescriptor.idProperty().getCacheDataValue((EntityBean) ap);
    }
  }

  @Override
  public String format(Object value) {
    return targetDescriptor.idBinder().cacheKey(value);
  }

  @Override
  public void setCacheDataValue(EntityBean bean, Object cacheData, PersistenceContext context) {
    if (cacheData == null) {
      setValue(bean, null);
    } else {
      if (embedded) {
        setValue(bean, targetDescriptor.cacheEmbeddedBeanLoad((CachedBeanData) cacheData, context));
      } else {
        setValue(bean, refBean(targetDescriptor, cacheData, context));
      }
    }
  }

  private Object refBean(BeanDescriptor<?> desc, Object id, PersistenceContext context) {
    if (id instanceof String) {
      id = desc.idProperty().scalarType.parse((String) id);
    }
    Object bean = desc.contextGet(context, id);
    if (bean == null) {
      bean = desc.createRef(id, context);
    }
    return bean;
  }

  @Override
  public ScalarDataReader<?> idReader() {
    return targetDescriptor.idProperty();
  }

  ScalarType<?> idScalarType() {
    return targetDescriptor.idProperty().scalarType;
  }

  /**
   * Return the Id values from the given bean.
   */
  @Override
  public Object[] assocIdValues(EntityBean bean) {
    return targetDescriptor.idBinder().values(bean);
  }

  /**
   * Return the Id expression to add to where clause etc.
   */
  @Override
  public String assocIdExpression(String prefix, String operator) {
    return targetDescriptor.idBinder().assocExpr(prefix, operator);
  }

  /**
   * Return the logical id value expression taking into account embedded id's.
   */
  @Override
  public String assocIdInValueExpr(boolean not, int size) {
    return targetDescriptor.idBinder().idInValueExpr(not, size);
  }

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  @Override
  public String assocIdInExpr(String prefix) {
    return targetDescriptor.idBinder().assocInExpr(prefix);
  }

  @Override
  public boolean isAssocId() {
    return !embedded;
  }

  @Override
  public boolean isAssocProperty() {
    return !embedded;
  }

  /**
   * Create a bean of the target type to be used as an embeddedId
   * value.
   */
  public Object createEmbeddedId() {
    return targetDescriptor().createEntityBean();
  }

  @Override
  public Object pathGetNested(Object bean) {
    Object value = getValueIntercept((EntityBean) bean);
    if (value == null) {
      value = targetDescriptor.createEntityBean();
      setValueIntercept((EntityBean) bean, value);
    }
    return value;
  }

  public ImportedId importedId() {
    return importedId;
  }

  /**
   * Find and return a matching imported id property.
   */
  public BeanProperty findMatchImport(String dbCol) {
    return importedId.findMatchImport(dbCol);
  }

  private String deriveWhereParentIdSql(boolean inClause) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < exportedProperties.length; i++) {
      String fkColumn = exportedProperties[i].getForeignDbColumn();
      if (i > 0) {
        String s = inClause ? "," : " and ";
        sb.append(s);
      }
      sb.append(fkColumn);
      if (!inClause) {
        sb.append("=? ");
      }
    }
    return sb.toString();
  }

  /**
   * Create the array of ExportedProperty used to build reference objects.
   */
  private ExportedProperty[] createExported() {
    BeanProperty idProp = descriptor.idProperty();
    ArrayList<ExportedProperty> list = new ArrayList<>();
    if (idProp != null && idProp.isEmbedded()) {
      BeanPropertyAssocOne<?> one = (BeanPropertyAssocOne<?>) idProp;
      BeanDescriptor<?> targetDesc = one.targetDescriptor();
      BeanProperty[] emIds = targetDesc.propertiesBaseScalar();
      try {
        for (BeanProperty emId : emIds) {
          list.add(findMatch(true, emId));
        }
      } catch (PersistenceException e) {
        // not found as individual scalar properties
        e.printStackTrace();
      }
    } else {
      if (idProp != null) {
        list.add(findMatch(false, idProp));
      }
    }
    return list.toArray(new ExportedProperty[0]);
  }

  /**
   * Find the matching foreignDbColumn for a given local property.
   */
  private ExportedProperty findMatch(boolean embeddedProp, BeanProperty prop) {
    return findMatch(embeddedProp, prop, prop.dbColumn(), tableJoin);
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    if (!isTransient) {
      if (primaryKeyExport) {
        descriptor.idProperty().appendSelect(ctx, subQuery);
      } else {
        localHelp.appendSelect(ctx, subQuery);
      }
    }
  }

  /**
   * Add table join with explicit table alias.
   */
  @Override
  public SqlJoinType addJoin(SqlJoinType joinType, String a1, String a2, DbSqlContext ctx) {
    if (sqlFormulaJoin != null) {
      ctx.appendFormulaJoin(sqlFormulaJoin, joinType, a1);
    }
    return super.addJoin(joinType, a1, a2, ctx);
  }

  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType, String manyWhere) {
    if (!isTransient && !primaryKeyExport) {
      localHelp.appendFrom(ctx, joinType);
      if (sqlFormulaJoin != null) {
        String alias = ctx.tableAliasManyWhere(manyWhere);
        ctx.appendFormulaJoin(sqlFormulaJoin, joinType, alias);
      }
    }
  }

  @Override
  public Object readSet(DataReader reader, EntityBean bean) throws SQLException {
    return localHelp.readSet(reader, bean);
  }

  @Override
  public Object read(DataReader reader) throws SQLException {
    return localHelp.read(reader);
  }

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    return localHelp.readSet(ctx, bean);
  }

  @Override
  public Object read(DbReadContext ctx) throws SQLException {
    return localHelp.read(ctx);
  }

  @Override
  public void addTenant(SpiQuery<?> query, Object tenantId) {
    query.where().eq(name, targetDescriptor.createRef(tenantId, null));
  }

  @Override
  public void setTenantValue(EntityBean entityBean, Object tenantId) {
    setValue(entityBean, targetDescriptor.createRef(tenantId, null));
  }

  @Override
  public void setValue(EntityBean bean, Object value) {
    super.setValue(bean, value);
    if (!id && embedded && value instanceof EntityBean) {
      setEmbeddedOwner(bean, value);
    }
  }

  @Override
  public void setValueIntercept(EntityBean bean, Object value) {
    super.setValueIntercept(bean, value);
    if (!id && embedded && value instanceof EntityBean) {
      setEmbeddedOwner(bean, value);
    }
  }

  /**
   * For embedded bean set the owner and all properties to be loaded (recursively).
   */
  void setAllLoadedEmbedded(EntityBean owner) {
    Object emb = getValue(owner);
    if (emb != null) {
      EntityBean embeddedBean = (EntityBean) emb;
      targetDescriptor.setAllLoaded(embeddedBean);
      if (!id) {
        embeddedBean._ebean_getIntercept().setEmbeddedOwner(owner, propertyIndex);
      }
    }
  }

  private void setEmbeddedOwner(EntityBean bean, Object value) {
    ((EntityBean) value)._ebean_getIntercept().setEmbeddedOwner(bean, propertyIndex);
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
    localHelp.loadIgnore(ctx);
  }

  @Override
  public void load(SqlBeanLoad sqlBeanLoad) {
    Object dbVal = sqlBeanLoad.load(this);
    if (embedded && sqlBeanLoad.isLazyLoad()) {
      if (dbVal instanceof EntityBean) {
        ((EntityBean) dbVal)._ebean_getIntercept().setLoaded();
      }
    }
  }

  private AssocOneHelp createHelp(boolean embedded, boolean oneToOneExported, String embeddedPrefix) {
    if (embedded) {
      return new AssocOneHelpEmbedded(this);
    } else if (oneToOneExported) {
      return new AssocOneHelpRefExported(this);
    } else {
      return new AssocOneHelpRefSimple(this, embeddedPrefix);
    }
  }

  /**
   * JSON write property (non-recursive to other beans).
   */
  @Override
  public void jsonWriteForInsert(SpiJsonWriter writeJson, EntityBean bean) throws IOException {
    if (!jsonSerialize) {
      return;
    }
    jsonWriteBean(writeJson, getValue(bean));
  }

  /**
   * JSON write property value (non-recursive to other beans).
   */
  @Override
  public void jsonWriteValue(SpiJsonWriter writeJson, Object value) throws IOException {
    if (!jsonSerialize) {
      return;
    }
    jsonWriteBean(writeJson, value);
  }

  private void jsonWriteBean(SpiJsonWriter writeJson, Object value) throws IOException {
    if (value instanceof EntityBean) {
      if (embedded) {
        writeJson.writeFieldName(name);
        BeanDescriptor<?> refDesc = descriptor.descriptor(value.getClass());
        refDesc.jsonWriteForInsert(writeJson, (EntityBean) value);
      } else {
        jsonWriteTargetId(writeJson, (EntityBean) value);
      }
    }
  }

  /**
   * Just write the Id property of the ToOne property.
   */
  private void jsonWriteTargetId(SpiJsonWriter writeJson, EntityBean childBean) throws IOException {
    BeanProperty idProperty = targetDescriptor.idProperty();
    if (idProperty != null) {
      writeJson.writeStartObject(name);
      idProperty.jsonWriteForInsert(writeJson, childBean);
      writeJson.writeEndObject();
    }
  }

  @Override
  public void jsonWrite(SpiJsonWriter writeJson, EntityBean bean) throws IOException {
    if (!jsonSerialize) {
      return;
    }
    Object value = getValueIntercept(bean);
    if (value == null) {
      writeJson.writeNullField(name);
    } else {
      if (!writeJson.parentBean(value)) {
        // Hmmm, not writing complex non-entity bean
        if (value instanceof EntityBean) {
          writeJson.beginAssocOne(name, bean);
          BeanDescriptor<?> refDesc = descriptor.descriptor(value.getClass());
          refDesc.jsonWrite(writeJson, (EntityBean) value, name);
          writeJson.endAssocOne();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void jsonRead(SpiJsonReader readJson, EntityBean bean) throws IOException {
    if (jsonDeserialize && targetDescriptor != null) {
      // CHECKME: may we skip reading the object from the json stream?
      T target = readJson.update() ? (T) getValue(bean) : null;
      T assocBean = targetDescriptor.jsonRead(readJson, name, target);
      if (readJson.update()) {
        setValueIntercept(bean, assocBean);
      } else {
        setValue(bean, assocBean);
      }
    }
  }

  @Override
  public Object jsonRead(SpiJsonReader readJson) throws IOException {
    return targetDescriptor.jsonRead(readJson, name, null);
  }

  public boolean isReference(Object detailBean) {
    EntityBean eb = (EntityBean) detailBean;
    return targetDescriptor.isReference(eb._ebean_getIntercept());
  }

  /**
   * Set the parent bean to the child bean if it has not already been set.
   */
  public void setParentBeanToChild(EntityBean parent, EntityBean child) {
    if (primaryKeyExport) {
      Object parentId = descriptor.getId(parent);
      targetDescriptor.convertSetId(parentId, child);
    }
    if (mappedBy != null) {
      BeanProperty beanProperty = targetDescriptor.beanProperty(mappedBy);
      if (beanProperty != null && beanProperty.getValue(child) == null) {
        // set the 'parent' bean to the 'child' bean
        beanProperty.setValue(child, parent);
      }
    }
  }

  public boolean hasCircularImportedId(BeanDescriptor<?> sourceDesc) {
    return targetDescriptor.hasCircularImportedIdTo(sourceDesc);
  }
}
