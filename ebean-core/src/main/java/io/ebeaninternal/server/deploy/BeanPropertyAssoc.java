package io.ebeaninternal.server.deploy;

import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.bean.EntityBean;
import io.ebean.core.type.DocPropertyType;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.deploy.id.ImportedIdEmbedded;
import io.ebeaninternal.server.deploy.id.ImportedIdSimple;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.el.ElPropertyChainBuilder;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.persist.MultiValueWrapper;
import io.ebeaninternal.server.query.STreePropertyAssoc;
import io.ebeaninternal.server.query.STreeType;
import io.ebeaninternal.server.query.SqlJoinType;
import io.ebeaninternal.server.querydefn.DefaultOrmQuery;
import io.ebeanservice.docstore.api.mapping.DocMappingBuilder;
import io.ebeanservice.docstore.api.mapping.DocPropertyMapping;
import io.ebeanservice.docstore.api.support.DocStructure;

import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Abstract base for properties mapped to an associated bean, list, set or map.
 */
public abstract class BeanPropertyAssoc<T> extends BeanProperty implements STreePropertyAssoc {

  /**
   * The descriptor of the target. This MUST be initialised after construction
   * so as to avoid a dependency loop between BeanDescriptors.
   */
  BeanDescriptor<T> targetDescriptor;
  IdBinder targetIdBinder;
  InheritInfo targetInheritInfo;
  String targetIdProperty;
  /**
   * Derived list of exported property and matching foreignKey
   */
  ExportedProperty[] exportedProperties;
  /**
   * Persist settings.
   */
  final BeanCascadeInfo cascadeInfo;
  /**
   * Join between the beans.
   */
  final TableJoin tableJoin;
  final PropertyForeignKey foreignKey;
  /**
   * The type of the joined bean.
   */
  private final Class<T> targetType;
  /**
   * The join table information.
   */
  final BeanTable beanTable;
  final String mappedBy;
  private final String docStoreDoc;
  private final String extraWhere;
  private final int fetchPreference;
  private boolean saveRecurseSkippable;

  /**
   * Construct the property.
   */
  BeanPropertyAssoc(BeanDescriptor<?> descriptor, DeployBeanPropertyAssoc<T> deploy) {
    super(descriptor, deploy);
    this.foreignKey = deploy.getForeignKey();
    this.extraWhere = InternString.intern(deploy.getExtraWhere());
    this.beanTable = deploy.getBeanTable();
    this.mappedBy = InternString.intern(deploy.getMappedBy());
    this.docStoreDoc = deploy.getDocStoreDoc();
    this.tableJoin = new TableJoin(deploy.getTableJoin());
    this.targetType = deploy.getTargetType();
    this.cascadeInfo = deploy.getCascadeInfo();
    this.fetchPreference = deploy.getFetchPreference();
  }

  /**
   * Copy constructor for ManyToOne inside Embeddable.
   */
  @SuppressWarnings("unchecked")
  BeanPropertyAssoc(BeanPropertyAssoc source, BeanPropertyOverride override) {
    super(source, override);
    foreignKey = source.foreignKey;
    extraWhere = source.extraWhere;
    beanTable = source.beanTable;
    mappedBy = source.mappedBy;
    docStoreDoc = source.docStoreDoc;
    targetType = source.targetType;
    cascadeInfo = source.cascadeInfo;
    fetchPreference = source.fetchPreference;
    tableJoin = source.tableJoin.withOverrideColumn(override.getDbColumn());
  }

  /**
   * Initialise post construction.
   */
  @Override
  public void initialise(BeanDescriptorInitContext initContext) {
    // this *MUST* execute after the BeanDescriptor is
    // put into the map to stop infinite recursion
    initialiseTargetDescriptor(initContext);
  }

  void initialiseTargetDescriptor(BeanDescriptorInitContext initContext) {
    targetDescriptor = descriptor.descriptor(targetType);
    if (!isTransient) {
      targetIdBinder = targetDescriptor.idBinder();
      targetInheritInfo = targetDescriptor.inheritInfo();
      saveRecurseSkippable = targetDescriptor.isSaveRecurseSkippable();
      if (!targetIdBinder.isComplexId()) {
        targetIdProperty = targetIdBinder.idSelect();
      }
    }
  }

  @Override
  public int fetchPreference() {
    return fetchPreference;
  }

  /**
   * Return the extra configuration for the foreign key.
   */
  public PropertyForeignKey foreignKey() {
    return foreignKey;
  }

  /**
   * Return true if foreign key constraint is enabled on this relationship (not disabled).
   */
  public boolean hasForeignKeyConstraint() {
    return foreignKey == null || !foreignKey.isNoConstraint();
  }

  /**
   * Return true if foreign key index is enabled on this relationship (not disabled).
   */
  public boolean hasForeignKeyIndex() {
    return foreignKey == null || !foreignKey.isNoIndex();
  }

  /**
   * Create a ElPropertyValue for a *ToOne or *ToMany.
   */
  ElPropertyValue createElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {
    // associated or embedded bean
    BeanDescriptor<?> embDesc = targetDescriptor();
    chain.add(this);
    return embDesc.buildElGetValue(remainder, chain, propertyDeploy);
  }

  /**
   * Add table join with table alias based on prefix.
   */
  @Override
  public SqlJoinType addJoin(SqlJoinType joinType, String prefix, DbSqlContext ctx) {
    return tableJoin.addJoin(joinType, prefix, ctx);
  }

  /**
   * Add table join with explicit table alias.
   */
  @Override
  public SqlJoinType addJoin(SqlJoinType joinType, String a1, String a2, DbSqlContext ctx) {
    return tableJoin.addJoin(joinType, a1, a2, ctx);
  }

  /**
   * Return false.
   */
  @Override
  public boolean isScalar() {
    return false;
  }

  /**
   * Return the mappedBy property.
   * This will be null on the owning side.
   */
  public String mappedBy() {
    return mappedBy;
  }

  /**
   * Return the Id property of the target entity type.
   * <p>
   * This will return null for multiple Id properties.
   * </p>
   */
  public String targetIdProperty() {
    return targetIdProperty;
  }

  /**
   * Return the BeanDescriptor of the target.
   */
  public BeanDescriptor<T> targetDescriptor() {
    return targetDescriptor;
  }

  SpiEbeanServer server() {
    return descriptor.ebeanServer();
  }

  /**
   * Create a new query for the target type.
   * <p>
   * We use target descriptor rather than target property type to support ElementCollection.
   */
  public SpiQuery<T> newQuery(SpiEbeanServer server) {
    return new DefaultOrmQuery<>(targetDescriptor, server, server.expressionFactory());
  }

  @Override
  public IdBinder idBinder() {
    return descriptor.idBinder();
  }

  @Override
  public STreeType target() {
    return targetDescriptor;
  }

  /**
   * Return true if the target side has soft delete.
   */
  @Override
  public boolean isTargetSoftDelete() {
    return targetDescriptor.isSoftDelete();
  }

  @Override
  public String softDeletePredicate(String tableAlias) {
    return targetDescriptor.softDeletePredicate(tableAlias);
  }

  /**
   * Return true if REFRESH should cascade.
   */
  boolean isCascadeRefresh() {
    return cascadeInfo.isRefresh();
  }

  public boolean isSaveRecurseSkippable(Object bean) {
    return saveRecurseSkippable && bean instanceof EntityBean && !((EntityBean) bean)._ebean_getIntercept().isNewOrDirty();
  }

  /**
   * Return true if save can be skipped for unmodified bean(s) of this
   * property.
   * <p>
   * That is, if a bean of this property is unmodified we don't need to
   * saveRecurse because none of its associated beans have cascade save set to
   * true.
   * </p>
   */
  public boolean isSaveRecurseSkippable() {
    return saveRecurseSkippable;
  }

  /**
   * Return true if the unique id properties are all not null for this bean.
   */
  public boolean hasId(EntityBean bean) {
    BeanDescriptor<?> targetDesc = targetDescriptor();
    BeanProperty idProp = targetDesc.idProperty();
    // all the unique properties are non-null
    return idProp == null || idProp.getValue(bean) != null;
  }

  /**
   * Return the type of the target.
   * <p>
   * This is the class of the associated bean, or beans contained in a list,
   * set or map.
   * </p>
   */
  public Class<?> targetType() {
    return targetType;
  }

  /**
   * Return an extra clause to add to the query for loading or joining
   * to this bean type.
   */
  @Override
  public String extraWhere() {
    return extraWhere;
  }

  /**
   * Return the elastic search doc for this embedded property.
   */
  private String docStoreDoc() {
    return docStoreDoc;
  }

  /**
   * Determine if and how the associated bean is included in the doc store document.
   */
  @Override
  public void docStoreInclude(boolean includeByDefault, DocStructure docStructure) {
    String embeddedDoc = docStoreDoc();
    if (embeddedDoc == null) {
      // not annotated so use include by default
      // which is *ToOne included and *ToMany excluded
      if (includeByDefault) {
        docStoreIncludeByDefault(docStructure.doc());
      }
    } else {
      // explicitly annotated to be included
      if (embeddedDoc.isEmpty()) {
        embeddedDoc = "*";
      }
      // add in a nested way
      PathProperties embDoc = PathProperties.parse(embeddedDoc);
      docStructure.addNested(name, embDoc);
    }
  }

  /**
   * Include the property in the document store by default.
   */
  void docStoreIncludeByDefault(PathProperties pathProps) {
    pathProps.addToPath(null, name);
  }

  @Override
  public void docStoreMapping(DocMappingBuilder mapping, String prefix) {
    if (mapping.includesPath(prefix, name)) {
      String fullName = SplitName.add(prefix, name);

      DocPropertyType type = isMany() ? DocPropertyType.LIST : DocPropertyType.OBJECT;
      DocPropertyMapping nested = new DocPropertyMapping(name, type);
      mapping.push(nested);
      targetDescriptor.docStoreMapping(mapping, fullName);
      mapping.pop();
      if (!nested.children().isEmpty()) {
        mapping.add(nested);
      }
    }
  }

  /**
   * Return true if this association is updateable.
   */
  public boolean isUpdateable() {
    TableJoinColumn[] columns = tableJoin.columns();
    if (columns.length == 0) {
      return true;
    }
    for (TableJoinColumn column : columns) {
      if (column.isUpdateable()) {
        // at least 1 is updatable
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if this association is insertable.
   */
  public boolean isInsertable() {
    TableJoinColumn[] columns = tableJoin.columns();
    if (columns.length == 0) {
      return true;
    }
    for (TableJoinColumn column : columns) {
      if (column.isInsertable()) {
        // at least 1 is insertable
        return true;
      }
    }
    return false;
  }

  /**
   * Return the underlying BeanTable for this property.
   */
  public BeanTable beanTable() {
    return beanTable;
  }

  /**
   * return the join to use for the bean.
   */
  public TableJoin tableJoin() {
    return tableJoin;
  }

  /**
   * Get the persist info.
   */
  public BeanCascadeInfo cascadeInfo() {
    return cascadeInfo;
  }

  /**
   * Build the list of imported property. Matches BeanProperty from the target
   * descriptor back to local database columns in the TableJoin.
   */
  ImportedId createImportedId(BeanPropertyAssoc<?> owner, BeanDescriptor<?> target, TableJoin join) {
    BeanProperty idProp = target.idProperty();
    BeanProperty[] others = target.propertiesBaseScalar();
    if (descriptor.isRawSqlBased()) {
      String dbColumn = owner.dbColumn();
      if (dbColumn != null) {
        return new ImportedIdSimple(owner, dbColumn, null, idProp, 0);
      }
    }
    if (idProp == null) {
      return null;
    }
    TableJoinColumn[] cols = join.columns();
    if (!idProp.isEmbedded()) {
      // simple single scalar id, match on the foreign column, allow extra TableJoinColumn for #3664
      String matchColumn = idProp.dbColumn();
      for (TableJoinColumn col : cols) {
        if (matchColumn.equals(col.getForeignDbColumn())) {
          return createImportedScalar(owner, col, new BeanProperty[]{idProp}, others);
        }
      }
      CoreLog.log.log(ERROR, "No Imported Id column for {0} in table {1}", idProp, join.getTable());
      return null;
    } else {
      // embedded id
      BeanPropertyAssocOne<?> embProp = (BeanPropertyAssocOne<?>) idProp;
      BeanProperty[] embBaseProps = embProp.targetDescriptor().propertiesBaseScalar();
      ImportedIdSimple[] scalars = createImportedList(owner, cols, embBaseProps, others);
      return new ImportedIdEmbedded(owner, embProp, scalars);
    }
  }

  private ImportedIdSimple[] createImportedList(BeanPropertyAssoc<?> owner, TableJoinColumn[] cols, BeanProperty[] props, BeanProperty[] others) {
    ArrayList<ImportedIdSimple> list = new ArrayList<>(cols.length);
    for (TableJoinColumn col : cols) {
      list.add(createImportedScalar(owner, col, props, others));
    }
    return ImportedIdSimple.sort(list);
  }

  private ImportedIdSimple createImportedScalar(BeanPropertyAssoc<?> owner, TableJoinColumn col, BeanProperty[] props, BeanProperty[] others) {
    String matchColumn = col.getForeignDbColumn();
    String localColumn = col.getLocalDbColumn();
    String localSqlFormula = col.getLocalSqlFormula();
    boolean insertable = col.isInsertable();
    boolean updateable = col.isUpdateable();
    for (int j = 0; j < props.length; j++) {
      if (props[j].dbColumn().equalsIgnoreCase(matchColumn)) {
        return new ImportedIdSimple(owner, localColumn, localSqlFormula, props[j], j, insertable, updateable);
      }
    }
    for (int j = 0; j < others.length; j++) {
      if (others[j].dbColumn().equalsIgnoreCase(matchColumn)) {
        return new ImportedIdSimple(owner, localColumn, localSqlFormula, others[j], j + props.length, insertable, updateable);
      }
    }
    String msg = "Error with the Join on [" + fullName()
      + "]. Could not find the local match for [" + matchColumn + "] "//in table["+searchTable+"]?"
      + " Perhaps an error in a @JoinColumn";
    throw new PersistenceException(msg);
  }

  private List<Object> flattenParentIds(List<Object> parentIds) {
    List<Object> bindValues = new ArrayList<>(parentIds.size() * 3);
    for (Object parentId : parentIds) {
      flatten(bindValues, parentId);
    }
    return bindValues;
  }

  private List<Object> flattenParentId(Object parentId) {
    List<Object> bindValues = new ArrayList<>();
    flatten(bindValues, parentId);
    return bindValues;
  }

  private void flatten(List<Object> bindValues, Object parentId) {
    if (isExportedSimple()) {
      bindValues.add(parentId);
    } else {
      EntityBean parent = (EntityBean) parentId;
      for (ExportedProperty exportedProperty : exportedProperties) {
        bindValues.add(exportedProperty.getValue(parent));
      }
    }
  }

  void bindParentIds(DefaultSqlUpdate delete, List<Object> parentIds) {
    if (isExportedSimple()) {
      delete.setParameter(new MultiValueWrapper(parentIds));
    } else {
      // embedded ids etc
      List<Object> bindValues = flattenParentIds(parentIds);
      for (Object bindValue : bindValues) {
        delete.setParameter(bindValue);
      }
    }
  }

  void bindParentId(DefaultSqlUpdate sqlUpd, Object parentId) {
    if (isExportedSimple()) {
      sqlUpd.setParameter(parentId);
      return;
    }
    EntityBean parent = (EntityBean) parentId;
    for (ExportedProperty exportedProperty : exportedProperties) {
      sqlUpd.setParameter(exportedProperty.getValue(parent));
    }
  }

  void bindParentIdEq(String expr, Object parentId, Query<?> q) {
    if (isExportedSimple()) {
      q.where().raw(expr, parentId);
    } else {
      // embedded ids etc
      List<Object> bindValues = flattenParentId(parentId);
      q.where().raw(expr, bindValues.toArray());
    }
  }

  void bindParentIdsIn(String expr, List<Object> parentIds, Query<?> q) {
    if (isExportedSimple()) {
      q.where().raw(expr, new MultiValueWrapper(parentIds));
    } else {
      // embedded ids etc
      List<Object> bindValues = flattenParentIds(parentIds);
      q.where().raw(expr, bindValues.toArray());
    }
  }

  private boolean isExportedSimple() {
    return exportedProperties.length == 1;
  }

  /**
   * Find and return the exported property matching to this property.
   */
  ExportedProperty findMatch(boolean embedded, BeanProperty prop, String matchColumn, TableJoin tableJoin) {
    String searchTable = tableJoin.getTable();
    for (TableJoinColumn column : tableJoin.columns()) {
      String matchTo = column.getLocalDbColumn();
      if (matchColumn.equalsIgnoreCase(matchTo)) {
        String foreignCol = column.getForeignDbColumn();
        return new ExportedProperty(embedded, foreignCol, prop);
      }
    }

    String msg = "Error with the Join on [" + fullName()
      + "]. Could not find the matching foreign key for [" + matchColumn + "] in table[" + searchTable + "]?"
      + " Perhaps using a @JoinColumn with the name/referencedColumnName attributes swapped? "
      + " or a @JoinColumn needs an explicit referencedColumnName specified?";
    throw new PersistenceException(msg);
  }

  /**
   * Create SqlUpdate statement to delete all child beans of the parent <code>id</code>.
   */
  public abstract SqlUpdate deleteByParentId(Object id);

  /**
   * Create SqlUpdate statement to delete all child beans of the parent ids in <code>idList</code>.
   */
  public abstract SqlUpdate deleteByParentIdList(List<Object> idList);

  /**
   * Find child beans of the parent <code>id</code>.
   */
  public abstract List<Object> findIdsByParentId(Object id, Transaction transaction, boolean includeSoftDeletes);

  /**
   * Find child beans of the parent ids in <code>idList</code>.
   */
  public abstract List<Object> findIdsByParentIdList(List<Object> idList, Transaction transaction, boolean includeSoftDeletes);

}

