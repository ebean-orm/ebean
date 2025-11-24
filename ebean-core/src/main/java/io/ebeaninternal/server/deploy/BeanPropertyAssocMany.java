package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebean.plugin.PropertyAssocMany;
import io.ebean.text.PathProperties;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.el.ElPropertyChainBuilder;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.query.STreePropertyAssocMany;
import io.ebeaninternal.server.query.SqlBeanLoad;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Property mapped to a List Set or Map.
 */
public class BeanPropertyAssocMany<T> extends BeanPropertyAssoc<T> implements STreePropertyAssocMany, PropertyAssocMany {

  private final BeanPropertyAssocManyJsonHelp jsonHelp;
  /**
   * Join for manyToMany intersection table.
   */
  private final TableJoin intersectionJoin;
  private final String intersectionPublishTable;
  private final String intersectionDraftTable;
  private final boolean orphanRemoval;
  private IntersectionTable intersectionTable;
  /**
   * For ManyToMany this is the Inverse join used to build reference queries.
   */
  final TableJoin inverseJoin;
  /**
   * Flag to indicate that this is a unidirectional relationship.
   */
  private final boolean unidirectional;
  private final boolean o2mJoinTable;
  /**
   * Flag to indicate that the target has a order column to auto populate.
   */
  private final boolean hasOrderColumn;
  /**
   * Flag to indicate manyToMany relationship.
   */
  private final boolean manyToMany;
  private final boolean elementCollection;
  /**
   * Descriptor for the 'target' when the property maps to an element collection.
   */
  final BeanDescriptor<T> elementDescriptor;
  /**
   * Order by used when fetch joining the associated many.
   */
  private final String fetchOrderBy;
  /**
   * Order by used when lazy loading the associated many.
   */
  private String lazyFetchOrderBy;
  private final String mapKey;
  private final ManyType manyType;
  private final ModifyListenMode modifyListenMode;
  private BeanProperty mapKeyProperty;
  private BeanPropertyAssocOne<?> childMasterProperty;
  private String childMasterIdProperty;
  private boolean embeddedExportedProperties;
  private BeanCollectionHelp<T> help;
  private ImportedId importedId;
  private BeanPropertyAssocManySqlHelp<T> sqlHelp;

  /**
   * Create this property.
   */
  public BeanPropertyAssocMany(BeanDescriptor<?> descriptor, DeployBeanPropertyAssocMany<T> deploy) {
    super(descriptor, deploy);
    this.unidirectional = deploy.isUnidirectional();
    this.orphanRemoval = deploy.isOrphanRemoval();
    this.o2mJoinTable = deploy.isO2mJoinTable();
    this.hasOrderColumn = deploy.hasOrderColumn();
    this.manyToMany = deploy.isManyToMany();
    this.elementCollection = deploy.isElementCollection();
    this.elementDescriptor = deploy.getElementDescriptor();
    this.manyType = deploy.getManyType();
    this.mapKey = deploy.getMapKey();
    this.fetchOrderBy = deploy.getFetchOrderBy();
    this.intersectionJoin = deploy.createIntersectionTableJoin();
    if (intersectionJoin != null) {
      this.intersectionPublishTable = intersectionJoin.getTable();
      this.intersectionDraftTable = deploy.getIntersectionDraftTable();
    } else {
      this.intersectionPublishTable = null;
      this.intersectionDraftTable = null;
    }
    this.inverseJoin = deploy.createInverseTableJoin();
    this.modifyListenMode = deploy.getModifyListenMode();
    this.jsonHelp = descriptor.isJacksonCorePresent() ? new BeanPropertyAssocManyJsonHelp(this) : null;
  }

  @Override
  public void initialise(BeanDescriptorInitContext initContext) {
    super.initialise(initContext);
    initialiseAssocMany();
    if (elementCollection) {
      // initialise all non-id properties (we don't have an Id property)
      elementDescriptor.initialiseOther(initContext);
    }
  }

  @Override
  void initialiseTargetDescriptor(BeanDescriptorInitContext initContext) {
    if (elementCollection) {
      targetDescriptor = elementDescriptor;
    } else {
      super.initialiseTargetDescriptor(initContext);
    }
  }

  private void initialiseAssocMany() {
    if (!isTransient) {
      this.help = BeanCollectionHelpFactory.create(this);
      if (hasJoinTable() || elementCollection) {
        importedId = createImportedId(this, targetDescriptor, tableJoin);
      } else {
        // find the property in the many that matches
        // back to the master (Order in the OrderDetail bean)
        childMasterProperty = initChildMasterProperty();
        if (childMasterProperty != null) {
          childMasterProperty.setRelationshipProperty(this);
        }
      }
      if (mapKey != null) {
        mapKeyProperty = initMapKeyProperty();
      }
      exportedProperties = createExported();
      this.sqlHelp = new BeanPropertyAssocManySqlHelp<>(this, exportedProperties);
      if (exportedProperties.length > 0) {
        embeddedExportedProperties = exportedProperties[0].isEmbedded();
        if (fetchOrderBy != null) {
          lazyFetchOrderBy = sqlHelp.lazyFetchOrderBy(fetchOrderBy);
        }
      }
    }
  }

  String targetTable() {
    return beanTable.getBaseTable();
  }

  /**
   * Initialise after the target bean descriptors have been all set.
   */
  void initialisePostTarget() {
    if (childMasterProperty != null) {
      BeanProperty masterId = childMasterProperty.targetDescriptor().idProperty();
      if (masterId != null) { // in docstore only, the master-id may be not available
        childMasterIdProperty = childMasterProperty.name() + "." + masterId.name();
      }
    }
  }

  @Override
  public BeanPropertyAssocMany<?> asMany() {
    return this;
  }

  @Override
  public boolean isManyToManyWithHistory() {
    return manyToMany && !excludedFromHistory && descriptor.isHistorySupport();
  }

  @Override
  protected void docStoreIncludeByDefault(PathProperties pathProps) {
    // by default not including "Many" properties in document store
  }

  @Override
  public void registerColumn(BeanDescriptor<?> desc, String prefix) {
    if (targetDescriptor != null) {
      desc.registerTable(targetDescriptor.baseTable(), this);
    }
  }

  /**
   * Return the underlying collection of beans.
   */
  @SuppressWarnings("rawtypes")
  public Collection rawCollection(EntityBean bean) {
    return help.underlying(getValue(bean));
  }

  /**
   * Copy collection value if existing is empty.
   */
  @Override
  public void merge(EntityBean bean, EntityBean existing) {
    Object existingCollection = getValue(existing);
    if (existingCollection instanceof BeanCollection<?>) {
      BeanCollection<?> toBC = (BeanCollection<?>) existingCollection;
      if (!toBC.isPopulated()) {
        Object fromCollection = getValue(bean);
        if (fromCollection instanceof BeanCollection<?>) {
          BeanCollection<?> fromBC = (BeanCollection<?>) fromCollection;
          if (fromBC.isPopulated()) {
            toBC.loadFrom(fromBC);
          }
        }
      }
    }
  }

  /**
   * Add the bean to the appropriate collection on the parent bean.
   */
  @Override
  public void addBeanToCollectionWithCreate(EntityBean parentBean, EntityBean detailBean, boolean withCheck) {
    BeanCollection<?> bc = beanCollection(parentBean);
    if (bc == null) {
      bc = help.createEmpty(parentBean);
      setValue(parentBean, bc);
    }
    help.add(bc, detailBean, withCheck);
  }

  private BeanCollection<?> beanCollection(EntityBean parentBean) {
    try {
      return (BeanCollection<?>) super.getValue(parentBean);
    } catch (ClassCastException e) {
      // fetching element collection, ok for now
      return null;
    }
  }

  /**
   * Return true if this is considered 'empty' from a save perspective.
   */
  public boolean isSkipSaveBeanCollection(EntityBean bean, boolean insertedParent) {
    Object val = getValue(bean);
    if (val == null) {
      return true;
    }
    if ((val instanceof BeanCollection<?>)) {
      return ((BeanCollection<?>) val).isSkipSave();
    }
    if (insertedParent) {
      // check 'vanilla' collection types
      if (val instanceof Collection<?>) {
        return ((Collection<?>) val).isEmpty();
      }
      if (val instanceof Map<?, ?>) {
        return ((Map<?, ?>) val).isEmpty();
      }
    }
    return false;
  }

  /**
   * Reset the many properties to be empty and ready for reloading.
   * <p>
   * Used in bean refresh.
   */
  public void resetMany(EntityBean bean) {
    Object value = getValue(bean);
    if (value instanceof BeanCollection) {
      // reset the collection back to empty
      ((BeanCollection<?>) value).reset(bean, name);
    } else {
      createReference(bean);
    }
  }

  @Override
  public ElPropertyValue buildElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {
    return createElPropertyValue(propName, remainder, chain, propertyDeploy);
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
    // do not add to the selectChain at the top level of the Many bean
  }

  @Override
  public SpiSqlUpdate deleteByParentId(Object parentId) {
    return sqlHelp.deleteByParentId(parentId);
  }

  @Override
  public SpiSqlUpdate deleteByParentIdList(List<Object> parentIdist) {
    return sqlHelp.deleteByParentIdList(parentIdist);
  }


  /**
   * Find the Id's of detail beans given a parent Id
   */
  @Override
  public List<Object> findIdsByParentId(Object parentId, Transaction t, boolean includeSoftDeletes) {
    return sqlHelp.findIdsByParentId(parentId, t, includeSoftDeletes, null);
  }

  /**
   * Find the Id's of detail beans given a parent Id and optionally exclude detail IDs
   */
  public List<Object> findIdsByParentId(Object parentId, Transaction t, boolean includeSoftDeletes, Set<Object> excludeDetailIds) {
    return sqlHelp.findIdsByParentId(parentId, t, includeSoftDeletes, excludeDetailIds);
  }

  /**
   * Find the Id's of detail beans given a list of parent Id's.
   */
  @Override
  public List<Object> findIdsByParentIdList(List<Object> parentIdList, Transaction t, boolean includeSoftDeletes) {
    return sqlHelp.findIdsByParentIdList(parentIdList, t, includeSoftDeletes);
  }

  /**
   * Exclude many properties from bean cache data.
   */
  @Override
  public boolean isCacheDataInclude() {
    // this would change for DB Array type support
    return false;
  }

  /**
   * Add the loaded current bean to its associated parent.
   * <p>
   * Helper method used by Elastic integration when loading with a persistence context.
   */
  @Override
  public void lazyLoadMany(EntityBean current) {
    EntityBean parentBean = childMasterProperty.valueAsEntityBean(current);
    if (parentBean != null) {
      addBeanToCollectionWithCreate(parentBean, current, true);
    }
  }

  public void addWhereParentIdIn(SpiQuery<?> query, List<Object> parentIds, boolean useDocStore) {
    if (useDocStore) {
      // assumes the ManyToOne property is included
      query.where().in(childMasterIdProperty, parentIds);
    } else {
      sqlHelp.addWhereParentIdIn(query, parentIds);
    }
  }

  /**
   * Set the lazy load server to help create reference collections (that lazy
   * load on demand).
   */
  public void setEbeanServer(SpiEbeanServer server) {
    if (help != null) {
      help.setLoader(server);
    }
    if (manyToMany) {
      intersectionTable = initIntersectionTable();
    }
  }

  private IntersectionTable initIntersectionTable() {
    IntersectionBuilder row = new IntersectionBuilder(intersectionPublishTable, intersectionDraftTable);
    for (ExportedProperty exportedProperty : exportedProperties) {
      row.addColumn(exportedProperty.getForeignDbColumn());
    }
    if (importedId == null) {
      throw new PersistenceException("Missing @Id property on bean related to ManyToMany " + fullName());
    }
    importedId.buildImport(row);
    return row.build();
  }

  /**
   * Return the intersection table helper.
   */
  public IntersectionTable intersectionTable() {
    return intersectionTable;
  }

  /**
   * Return the mode for listening to modifications to collections for this association.
   */
  public ModifyListenMode modifyListenMode() {
    return modifyListenMode;
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
    // nothing to ignore for Many
  }

  @Override
  public void load(SqlBeanLoad sqlBeanLoad) {
    // do nothing, as a lazy loading BeanCollection 'reference'
    // is created and registered with the loading context
    // in SqlTreeNodeBean.createListProxies()
  }

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean) {
    return null;
  }

  @Override
  public Object read(DbReadContext ctx) {
    return null;
  }

  public void add(BeanCollection<?> collection, EntityBean bean) {
    help.add(collection, bean, false);
  }

  @Override
  public String assocIsEmpty(SpiExpressionRequest request, String path) {
    boolean softDelete = targetDescriptor.isSoftDelete();
    boolean needsX2Table = softDelete || extraWhere() != null;
    StringBuilder sb = new StringBuilder(50).append("from "); // use from to stop parsing on table name
    SpiQuery<?> query = request.queryRequest().query();
    if (hasJoinTable()) {
      sb.append(query.isAsDraft() ? intersectionDraftTable : intersectionPublishTable);
    } else {
      sb.append(targetDescriptor.baseTable(query.temporalMode()));
    }
    if (needsX2Table && hasJoinTable()) {
      sb.append(" x join ");
      sb.append(targetDescriptor.baseTable(query.temporalMode()));
      sb.append(" x2 on ");
      inverseJoin.addJoin("x2", "x", sb);
    } else {
      sb.append(" x");
    }
    sb.append(" where ");
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      exportedProperties[i].appendWhere(sb, "x.", path);
    }
    if (extraWhere() != null) {
      sb.append(" and ");
      if (hasJoinTable()) {
        sb.append(extraWhere().replace("${ta}", "x2").replace("${mta}", "x"));
      } else {
        sb.append(extraWhere().replace("${ta}", "x"));
      }
    }
    if (softDelete) {
      String alias = hasJoinTable() ? "x2" : "x";
      sb.append(" and ").append(targetDescriptor.softDeletePredicate(alias));
    }
    return sb.toString();
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
  public boolean isMany() {
    return true;
  }

  public boolean hasOrderColumn() {
    return hasOrderColumn;
  }

  public boolean isOrphanRemoval() {
    return orphanRemoval;
  }

  @Override
  public boolean isAssocMany() {
    return true;
  }

  @Override
  public boolean isAssocId() {
    return true;
  }

  @Override
  public boolean isAssocProperty() {
    return true;
  }

  /**
   * Returns true.
   */
  @Override
  public boolean containsMany() {
    return true;
  }

  /**
   * Return the many type.
   */
  public ManyType manyType() {
    return manyType;
  }

  /**
   * Return true if this is many to many.
   */
  @Override
  public boolean hasJoinTable() {
    return manyToMany || o2mJoinTable;
  }

  /**
   * Return true if this is a one to many with a join table.
   */
  public boolean isO2mJoinTable() {
    return o2mJoinTable;
  }

  /**
   * Return true if this is many to many.
   */
  public boolean isManyToMany() {
    return manyToMany;
  }

  public boolean isElementCollection() {
    return elementCollection;
  }

  /**
   * Return the element bean descriptor (for an element collection only).
   */
  public BeanDescriptor<T> elementDescriptor() {
    return elementDescriptor;
  }

  /**
   * ManyToMany only, join from local table to intersection table.
   */
  @Override
  public TableJoin intersectionTableJoin() {
    return intersectionJoin;
  }

  /**
   * Set the parent bean to the child (update the relationship).
   */
  public void setParentToChild(EntityBean parent, EntityBean child, Object mapKeyValue) {
    if (mapKeyProperty != null) {
      mapKeyProperty.setValue(child, mapKeyValue);
    }
    if (!manyToMany && childMasterProperty != null) {
      // bidirectional in the sense that the 'master' property
      // exists on the 'detail' bean
      childMasterProperty.setValueIntercept(child, parent);
    }
  }

  /**
   * Return true if the parent bean has been set to the child (updated the relationship).
   */
  public boolean setParentToChild(EntityBean parent, EntityBean child, Object mapKeyValue, BeanDescriptor<?> parentDesc) {
    if (manyToMany
      || childMasterProperty == null
      || !child._ebean_getIntercept().isLoadedProperty(childMasterProperty.propertyIndex())) {
      return false;
    }

    Object currentParent = childMasterProperty.getValue(child);
    if (currentParent != null) {
      Object newId = parentDesc.getId(parent);
      Object oldId = parentDesc.id(currentParent);
      if (Objects.equals(newId, oldId)) {
        return false;
      }
    }
    childMasterProperty.setValueIntercept(child, parent);
    if (mapKeyProperty != null) {
      mapKeyProperty.setValue(child, mapKeyValue);
    }
    return true;
  }

  /**
   * Return the order by clause used to order the fetching of the data for
   * this list, set or map.
   */
  public String fetchOrderBy() {
    return fetchOrderBy;
  }

  /**
   * Return the order by for use when lazy loading the associated collection.
   */
  public String lazyFetchOrderBy() {
    return lazyFetchOrderBy;
  }

  /**
   * Return the default mapKey when returning a Map.
   */
  public String mapKey() {
    return mapKey;
  }

  @Override
  public void createEmptyReference(EntityBean localBean) {
    setValue(localBean, help.createEmptyReference());
  }

  @Override
  public BeanCollection<?> createReference(EntityBean localBean, boolean forceNewReference) {
    return forceNewReference ? createReference(localBean) : createReferenceIfNull(localBean);
  }

  @Override
  public BeanCollection<?> createReferenceIfNull(EntityBean parentBean) {
    Object v = getValue(parentBean);
    if (v instanceof BeanCollection<?>) {
      BeanCollection<?> bc = (BeanCollection<?>) v;
      return bc.isReference() ? bc : null;
    } else if (v != null) {
      return null;
    } else {
      return createReference(parentBean);
    }
  }

  public BeanCollection<?> createReference(EntityBean parentBean) {
    BeanCollection<?> ref = help.createReference(parentBean);
    setValue(parentBean, ref);
    return ref;
  }

  public BeanCollection<T> createEmpty(EntityBean parentBean) {
    return help.createEmpty(parentBean);
  }

  private BeanCollectionAdd beanCollectionAdd(Object bc) {
    return help.getBeanCollectionAdd(bc, null);
  }

  public Object parentId(EntityBean parentBean) {
    return descriptor.getId(parentBean);
  }

  @Override
  public void addSelectExported(DbSqlContext ctx, String tableAlias) {
    String alias = hasJoinTable() ? "int_" : tableAlias;
    if (alias == null) {
      alias = "t0";
    }
    for (ExportedProperty exportedProperty : exportedProperties) {
      ctx.appendColumn(alias, exportedProperty.getForeignDbColumn());
    }
  }

  /**
   * Create the array of ExportedProperty used to build reference objects.
   */
  private ExportedProperty[] createExported() {
    BeanProperty idProp = descriptor.idProperty();
    ArrayList<ExportedProperty> list = new ArrayList<>();
    if (idProp != null && idProp.isEmbedded()) {
      BeanPropertyAssocOne<?> one = (BeanPropertyAssocOne<?>) idProp;
      try {
        for (BeanProperty emId : one.targetDescriptor().propertiesBaseScalar()) {
          list.add(findMatch(true, emId));
        }
      } catch (PersistenceException e) {
        // not found as individual scalar properties
        CoreLog.log.log(ERROR, "Could not find a exported property?", e);
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
  private ExportedProperty findMatch(boolean embedded, BeanProperty prop) {
    if (hasJoinTable()) {
      // look for column going to intersection
      return findMatch(embedded, prop, prop.dbColumn(), intersectionJoin);
    } else {
      return findMatch(embedded, prop, prop.dbColumn(), tableJoin);
    }
  }

  /**
   * Return the child property that links back to the master bean.
   * <p>
   * Note that childMasterProperty will be null if a field is used instead of
   * a ManyToOne bean association.
   */
  private BeanPropertyAssocOne<?> initChildMasterProperty() {
    if (unidirectional) {
      return null;
    }
    // search for the property, to see if it exists
    Class<?> beanType = descriptor.type();
    BeanDescriptor<?> targetDesc = targetDescriptor();
    for (BeanPropertyAssocOne<?> prop : targetDesc.propertiesOne()) {
      if (mappedBy != null) {
        // match using mappedBy as property name
        if (mappedBy.equalsIgnoreCase(prop.name())) {
          return prop;
        }
      } else {
        // assume only one property that matches parent object type
        if (prop.targetType().equals(beanType)) {
          // found it, stop search
          return prop;
        }
      }
    }
    throw new RuntimeException("Can not find Master " + beanType + " in Child " + targetDesc);
  }

  /**
   * Search for and return the mapKey property.
   */
  private BeanProperty initMapKeyProperty() {
    // search for the property
    BeanDescriptor<?> targetDesc = targetDescriptor();
    for (BeanProperty prop : targetDesc.propertiesAll()) {
      if (mapKey.equalsIgnoreCase(prop.name())) {
        return prop;
      }
    }
    String from = descriptor.fullName();
    String to = targetDesc.fullName();
    throw new PersistenceException(from + ": Could not find mapKey property " + mapKey + " on " + to);
  }

  public IntersectionRow buildManyDeleteChildren(EntityBean parentBean, Set<Object> excludeDetailIds) {
    IntersectionRow row = new IntersectionRow(tableJoin.getTable(), targetDescriptor);
    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      row.setExcludeIds(excludeDetailIds, targetDescriptor());
    }
    buildExport(row, parentBean);
    return row;
  }

  public IntersectionRow buildManyToManyDeleteChildren(EntityBean parentBean, boolean publish) {
    String tableName = publish ? intersectionPublishTable : intersectionDraftTable;
    IntersectionRow row = new IntersectionRow(tableName);
    buildExport(row, parentBean);
    return row;
  }

  public IntersectionRow buildManyToManyMapBean(EntityBean parent, EntityBean other, boolean publish) {
    String tableName = publish ? intersectionPublishTable : intersectionDraftTable;
    IntersectionRow row = new IntersectionRow(tableName);
    buildExport(row, parent);
    buildImport(row, other);
    return row;
  }

  /**
   * Register the mapping of intersection table to associated draft table.
   */
  void registerDraftIntersectionTable(BeanDescriptorInitContext initContext) {
    if (hasDraftIntersection()) {
      initContext.addDraftIntersection(intersectionPublishTable, intersectionDraftTable);
    }
  }

  /**
   * Return true if the relationship is a ManyToMany with the intersection having an associated draft table.
   */
  private boolean hasDraftIntersection() {
    return intersectionDraftTable != null && !intersectionDraftTable.equals(intersectionPublishTable);
  }

  /**
   * Bind the two side of a many to many to the given SqlUpdate.
   */
  public void intersectionBind(SqlUpdate sql, EntityBean parentBean, EntityBean other) {
    if (embeddedExportedProperties) {
      BeanProperty idProp = descriptor.idProperty();
      parentBean = (EntityBean) idProp.getValue(parentBean);
    }
    for (ExportedProperty exportedProperty : exportedProperties) {
      sql.setParameter(exportedProperty.getValue(parentBean));
    }
    importedId.bindImport(sql, other);
  }

  private void buildExport(IntersectionRow row, EntityBean parentBean) {
    if (embeddedExportedProperties) {
      BeanProperty idProp = descriptor.idProperty();
      parentBean = (EntityBean) idProp.getValue(parentBean);
    }
    for (ExportedProperty exportedProperty : exportedProperties) {
      Object val = exportedProperty.getValue(parentBean);
      String fkColumn = exportedProperty.getForeignDbColumn();

      row.put(fkColumn, val);
    }
  }

  /**
   * Set the predicates for lazy loading of the association.
   * Handles predicates for both OneToMany and ManyToMany.
   */
  private void buildImport(IntersectionRow row, EntityBean otherBean) {
    importedId.buildImport(row, otherBean);
  }

  /**
   * Return true if the otherBean has an Id value.
   */
  public boolean hasImportedId(EntityBean otherBean) {
    return null != targetDescriptor.getId(otherBean);
  }

  /**
   * Skip JSON write value for ToMany property.
   */
  @Override
  public void jsonWriteValue(SpiJsonWriter writeJson, Object value) {
    // do nothing, exclude ToMany properties
  }

  @Override
  public void jsonWrite(SpiJsonWriter ctx, EntityBean bean) throws IOException {
    if (!this.jsonSerialize) {
      return;
    }
    Boolean include = ctx.includeMany(name);
    if (Boolean.FALSE.equals(include)) {
      return;
    }

    Object value = getValueIntercept(bean);
    if (value != null) {
      ctx.pushParentBeanMany(bean);
      if (help != null) {
        jsonWriteCollection(ctx, name, value, include != null);
      } else {
        if (isTransient && targetDescriptor == null) {
          ctx.writeValueUsingObjectMapper(name, value);
        } else {
          Collection<?> collection = (Collection<?>) value;
          if (!collection.isEmpty() || ctx.isIncludeEmpty()) {
            ctx.toJson(name, collection);
          }
        }
      }
      ctx.popParentBeanMany();
    }
  }

  @Override
  public void jsonRead(SpiJsonReader readJson, EntityBean parentBean) throws IOException {
    jsonHelp.jsonRead(readJson, parentBean);
  }

  @SuppressWarnings("unchecked")
  void publishMany(EntityBean draft, EntityBean live) {
    // collections will not be null due to enhancement
    BeanCollection<T> draftVal = (BeanCollection<T>) getValueIntercept(draft);
    BeanCollection<T> liveVal = (BeanCollection<T>) getValueIntercept(live);

    // Organise the existing live beans into map keyed by id
    Map<Object, T> liveBeansAsMap = liveBeansAsMap(liveVal);

    // publish from each draft to live bean creating new live beans as required
    draftVal.size();
    Collection<T> actualDetails = draftVal.actualDetails();
    for (T bean : actualDetails) {
      Object id = targetDescriptor.id(bean);
      T liveBean = liveBeansAsMap.remove(id);

      if (isManyToMany()) {
        if (liveBean == null) {
          // add new relationship (Map not allowed here)
          liveVal.addBean(targetDescriptor.createReference(id, null));
        }
      } else {
        // recursively publish the OneToMany child bean
        T newLive = targetDescriptor.publish(bean, liveBean);
        if (liveBean == null) {
          // Map not allowed here
          liveVal.addBean(newLive);
        }
      }
    }
    // anything remaining should be deleted (so remove from modify aware collection)
    Collection<T> values = liveBeansAsMap.values();
    for (T value : values) {
      liveVal.removeBean(value);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<Object, T> liveBeansAsMap(BeanCollection<?> liveVal) {
    liveVal.size();
    Collection<?> liveBeans = liveVal.actualDetails();
    Map<Object, T> liveMap = new LinkedHashMap<>();
    for (Object liveBean : liveBeans) {
      Object id = targetDescriptor.id(liveBean);
      liveMap.put(id, (T) liveBean);
    }
    return liveMap;
  }

  public boolean isIncludeCascadeSave() {
    // Note ManyToMany always included as we always 'save'
    // the relationship via insert/delete of intersection table
    // REMOVALS means including PrivateOwned relationships
    return cascadeInfo.isSave() || hasJoinTable() || ModifyListenMode.REMOVALS == modifyListenMode;
  }

  public boolean isIncludeCascadeDelete() {
    return cascadeInfo.isDelete() || hasJoinTable() || ModifyListenMode.REMOVALS == modifyListenMode;
  }

  boolean isCascadeDeleteEscalate() {
    return !elementCollection && cascadeInfo.isDelete();
  }

  public SpiSqlUpdate insertElementCollection() {
    return sqlHelp.insertElementCollection();
  }

  public boolean isTargetDocStoreMapped() {
    return targetDescriptor.isDocStoreMapped();
  }

  void jsonWriteMapEntry(SpiJsonWriter ctx, Map.Entry<?, ?> entry) throws IOException {
    if (elementDescriptor != null) {
      elementDescriptor.jsonWriteMapEntry(ctx, entry);
    } else {
      targetDescriptor.jsonWrite(ctx, (EntityBean) entry.getValue());
    }
  }

  void jsonWriteElementValue(SpiJsonWriter ctx, Object element) {
    elementDescriptor.jsonWriteElement(ctx, element);
  }

  /**
   * Only cache Many Ids if the target bean is also cached.
   */
  public boolean isUseCache() {
    return targetDescriptor.isBeanCaching();
  }

  /**
   * A Many (element collection) property in bean cache to held as JSON.
   */
  @Override
  public void setCacheDataValue(EntityBean bean, Object cacheData, PersistenceContext context) {
    try {
      String asJson = (String) cacheData;
      if (asJson != null && !asJson.isEmpty()) {
        Object collection = jsonReadCollection(asJson);
        setValue(bean, collection);
      }
    } catch (Exception e) {
      CoreLog.log.log(ERROR, "Error setting value from L2 cache", e);
    }
  }

  @Override
  public Object getCacheDataValue(EntityBean bean) {
    try {
      Object collection = getValue(bean);
      if (collection == null) {
        return null;
      }
      return jsonWriteCollection(collection);
    } catch (Exception e) {
      CoreLog.log.log(ERROR, "Error building value element collection json for L2 cache", e);
      return null;
    }
  }

  /**
   * Write the collection to JSON.
   */
  public String jsonWriteCollection(Object value) throws IOException {
    StringWriter writer = new StringWriter(300);
    SpiJsonWriter ctx = descriptor.createJsonWriter(writer);
    help.jsonWrite(ctx, null, value, true);
    ctx.flush();
    return writer.toString();
  }

  /**
   * Read the collection as JSON.
   */
  private Object jsonReadCollection(String json) throws IOException {
    SpiJsonReader ctx = descriptor.createJsonReader(json);
    JsonParser parser = ctx.parser();
    JsonToken event = parser.nextToken();
    if (JsonToken.VALUE_NULL == event) {
      return null;
    }
    return jsonReadCollection(ctx, null, null);
  }

  /**
   * Write the collection to JSON.
   */
  private void jsonWriteCollection(SpiJsonWriter ctx, String name, Object value, boolean explicitInclude) throws IOException {
    help.jsonWrite(ctx, name, value, explicitInclude);
  }

  /**
   * Read the collection (JSON Array) containing entity beans.
   */
  public Object jsonReadCollection(SpiJsonReader readJson, EntityBean parentBean, Object targets) throws IOException {
    if (elementDescriptor != null && elementDescriptor.isJsonReadCollection()) {
      return elementDescriptor.jsonReadCollection(readJson, parentBean);
    }
    BeanCollection<?> collection = createEmpty(parentBean);
    BeanCollectionAdd add = beanCollectionAdd(collection);
    Map<Object, T> existingBeans = extractBeans(targets);
    do {

      EntityBean detailBean = getDetailBean(readJson, existingBeans);
      if (detailBean == null) {
        // read the entire array
        break;
      }
      add.addEntityBean(detailBean);

      if (parentBean != null && childMasterProperty != null) {
        // bind detail bean back to master via mappedBy property
        childMasterProperty.setValue(detailBean, parentBean);
      }
    } while (true);
    return collection;
  }

  /**
   * Find bean in the target collection and reuse it for JSON update.
   */
  private EntityBean getDetailBean(SpiJsonReader readJson, Map<Object, T> targets) throws IOException {
    BeanProperty idProperty = targetDescriptor.idProperty();
    if (targets == null || idProperty == null) {
      return (EntityBean) targetDescriptor.jsonRead(readJson, name, null);
    } else {
      JsonToken token = readJson.parser().nextToken();
      if (JsonToken.VALUE_NULL == token || JsonToken.END_ARRAY == token) {
        return null;
      }
      // extract the id. We have to buffer the JSON;
      ObjectNode node = readJson.mapper().readTree(readJson.parser());
      SpiJsonReader jsonReader = readJson.forJson(node.traverse());
      JsonNode idNode = node.get(idProperty.name());
      Object id = idNode == null ? null : idProperty.jsonRead(readJson.forJson(idNode.traverse()));
      return (EntityBean) targetDescriptor.jsonRead(jsonReader, name, targets.get(id));
    }
  }

  /**
   * Extract beans, that are currently in the target collection. (targets can be a List/Set/Map)
   */
  private Map<Object, T> extractBeans(Object targets) {
    Collection<T> beans;

    if (targets == null) {
      return null;
    } else if (targets instanceof Map) {
      if (((Map<?, T>) targets).isEmpty()) {
        return null;
      }
      beans = ((Map<?, T>) targets).values();
    } else if (targets instanceof Collection) {
      if (((Collection<?>) targets).isEmpty()) {
        return null;
      }
      beans = (Collection<T>) targets;
    } else {
      CoreLog.log.log(WARNING, "Found non collection value " + targets.getClass().getSimpleName());
      return null;
    }

    BeanProperty idProp = targetDescriptor.idProperty();
    Map<Object, T> ret = new HashMap<>();
    for (T bean : beans) {
      if (bean instanceof EntityBean) {
        Object id = idProp.getValue((EntityBean) bean);
        if (id != null) {
          ret.put(id, bean);
        }
      }
    }
    return ret.isEmpty() ? null : ret;
  }

  /**
   * Bind all the property values to the SqlUpdate.
   */
  public void bindElementValue(SqlUpdate insert, Object value) {
    targetDescriptor.bindElementValue(insert, value);
  }

  /**
   * Returns true, if we must create a m2m join table.
   */
  public boolean createJoinTable() {
    if (hasJoinTable() && mappedBy() == null) {
      // only create on other 'owning' side
      return !descriptor.isTableManaged(intersectionJoin.getTable());
    } else {
      return false;
    }
  }

  @Override
  public void freeze(EntityBean entityBean) {
    Object value = getValue(entityBean);
    if (value instanceof BeanCollection) {
      BeanCollection<?> beanCollection = (BeanCollection<?>) value;
      Collection<?> entities = beanCollection.actualDetails();
      if (entities != null) {
        for (Object actualEntry : entities) {
          targetDescriptor.freeze((EntityBean) actualEntry);
        }
      }
      setValue(entityBean, beanCollection.freeze());
    } else if (value == null) {
      // make it an error to access the collection (no lazy loading allowed)
      entityBean._ebean_getIntercept().setPropertyUnloaded(propertyIndex);
    }
  }
}
