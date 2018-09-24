package io.ebeaninternal.server.deploy;

import io.ebean.Query;
import io.ebean.bean.EntityBean;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;
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
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import io.ebeanservice.docstore.api.support.DocStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for properties mapped to an associated bean, list, set or map.
 */
public abstract class BeanPropertyAssoc<T> extends BeanProperty implements STreePropertyAssoc {

  private static final Logger logger = LoggerFactory.getLogger(BeanPropertyAssoc.class);

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
  protected ExportedProperty[] exportedProperties;

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
  final Class<T> targetType;

  /**
   * The join table information.
   */
  final BeanTable beanTable;

  final String mappedBy;

  final String docStoreDoc;

  final String extraWhere;

  final int fetchPreference;

  boolean saveRecurseSkippable;

  /**
   * Construct the property.
   */
  public BeanPropertyAssoc(BeanDescriptor<?> descriptor, DeployBeanPropertyAssoc<T> deploy) {
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
   * Initialise post construction.
   */
  @Override
  public void initialise(BeanDescriptorInitContext initContext) {
    // this *MUST* execute after the BeanDescriptor is
    // put into the map to stop infinite recursion
    initialiseTargetDescriptor(initContext);
  }

  void initialiseTargetDescriptor(BeanDescriptorInitContext initContext) {
    targetDescriptor = descriptor.getBeanDescriptor(targetType);
    if (!isTransient) {
      targetIdBinder = targetDescriptor.getIdBinder();
      targetInheritInfo = targetDescriptor.getInheritInfo();
      saveRecurseSkippable = targetDescriptor.isSaveRecurseSkippable();
      if (!targetIdBinder.isComplexId()) {
        targetIdProperty = targetIdBinder.getIdProperty();
      }
    }
  }

  @Override
  public int getFetchPreference() {
    return fetchPreference;
  }

  /**
   * Return the extra configuration for the foreign key.
   */
  public PropertyForeignKey getForeignKey() {
    return foreignKey;
  }

  /**
   * Create a ElPropertyValue for a *ToOne or *ToMany.
   */
  protected ElPropertyValue createElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {

    // associated or embedded bean
    BeanDescriptor<?> embDesc = getTargetDescriptor();

    if (chain == null) {
      chain = new ElPropertyChainBuilder(isEmbedded(), propName);
    }
    chain.add(this);
    if (containsMany()) {
      chain.setContainsMany();
    }
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
  public String getMappedBy() {
    return mappedBy;
  }

  /**
   * Return the Id property of the target entity type.
   * <p>
   * This will return null for multiple Id properties.
   * </p>
   */
  public String getTargetIdProperty() {
    return targetIdProperty;
  }

  /**
   * Return the BeanDescriptor of the target.
   */
  public BeanDescriptor<T> getTargetDescriptor() {
    return targetDescriptor;
  }

  SpiEbeanServer server() {
    return descriptor.getEbeanServer();
  }

  /**
   * Create a new query for the target type.
   *
   * We use target descriptor rather than target property type to support ElementCollection.
   */
  public SpiQuery<T> newQuery(SpiEbeanServer server) {
    return new DefaultOrmQuery<>(targetDescriptor, server, server.getExpressionFactory());
  }

  @Override
  public IdBinder getIdBinder() {
    return descriptor.getIdBinder();
  }

  @Override
  public STreeType target() {
    return targetDescriptor;
  }

  /**
   * Return true if the target side has soft delete.
   */
  public boolean isTargetSoftDelete() {
    return targetDescriptor.isSoftDelete();
  }

  /**
   * Return true if REFRESH should cascade.
   */
  public boolean isCascadeRefresh() {
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

    BeanDescriptor<?> targetDesc = getTargetDescriptor();
    BeanProperty idProp = targetDesc.getIdProperty();
    if (idProp != null) {
      Object value = idProp.getValue(bean);
      if (value == null) {
        return false;
      }
    }
    // all the unique properties are non-null
    return true;
  }

  /**
   * Return the type of the target.
   * <p>
   * This is the class of the associated bean, or beans contained in a list,
   * set or map.
   * </p>
   */
  public Class<?> getTargetType() {
    return targetType;
  }

  /**
   * Return an extra clause to add to the query for loading or joining
   * to this bean type.
   */
  public String getExtraWhere() {
    return extraWhere;
  }

  /**
   * Return the elastic search doc for this embedded property.
   */
  public String getDocStoreDoc() {
    return docStoreDoc;
  }

  /**
   * Determine if and how the associated bean is included in the doc store document.
   */
  @Override
  public void docStoreInclude(boolean includeByDefault, DocStructure docStructure) {

    String embeddedDoc = getDocStoreDoc();
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
  protected void docStoreIncludeByDefault(PathProperties pathProps) {
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

      if (!nested.getChildren().isEmpty()) {
        mapping.add(nested);
      }
    }
  }

  /**
   * Return true if this association is updateable.
   */
  public boolean isUpdateable() {
    TableJoinColumn[] columns = tableJoin.columns();
    if (columns.length <= 0) {
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
    if (columns.length <= 0) {
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
  public BeanTable getBeanTable() {
    return beanTable;
  }

  /**
   * return the join to use for the bean.
   */
  public TableJoin getTableJoin() {
    return tableJoin;
  }

  /**
   * Get the persist info.
   */
  public BeanCascadeInfo getCascadeInfo() {
    return cascadeInfo;
  }

  /**
   * Build the list of imported property. Matches BeanProperty from the target
   * descriptor back to local database columns in the TableJoin.
   */
  protected ImportedId createImportedId(BeanPropertyAssoc<?> owner, BeanDescriptor<?> target, TableJoin join) {

    BeanProperty idProp = target.getIdProperty();
    BeanProperty[] others = target.propertiesBaseScalar();

    if (descriptor.isRawSqlBased()) {
      String dbColumn = owner.getDbColumn();
      return new ImportedIdSimple(owner, dbColumn, null, idProp, 0);
    }

    TableJoinColumn[] cols = join.columns();

    if (idProp == null) {
      return null;
    }
    if (!idProp.isEmbedded()) {
      // simple single scalar id
      if (cols.length != 1) {
        String msg = "No Imported Id column for [" + idProp + "] in table [" + join.getTable() + "]";
        logger.error(msg);
        return null;
      } else {
        BeanProperty[] idProps = {idProp};
        return createImportedScalar(owner, cols[0], idProps, others);
      }
    } else {
      // embedded id
      BeanPropertyAssocOne<?> embProp = (BeanPropertyAssocOne<?>) idProp;
      BeanProperty[] embBaseProps = embProp.getTargetDescriptor().propertiesBaseScalar();
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
      if (props[j].getDbColumn().equalsIgnoreCase(matchColumn)) {
        return new ImportedIdSimple(owner, localColumn, localSqlFormula, props[j], j, insertable, updateable);
      }
    }

    for (int j = 0; j < others.length; j++) {
      if (others[j].getDbColumn().equalsIgnoreCase(matchColumn)) {
        return new ImportedIdSimple(owner, localColumn, localSqlFormula, others[j], j + props.length, insertable, updateable);
      }
    }

    String msg = "Error with the Join on [" + getFullBeanName()
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
      delete.setNextParameter(new MultiValueWrapper(parentIds));
    } else {
      // embedded ids etc
      List<Object> bindValues = flattenParentIds(parentIds);
      for (Object bindValue : bindValues) {
        delete.setNextParameter(bindValue);
      }
    }
  }

  void bindParentId(DefaultSqlUpdate sqlUpd, Object parentId) {

    if (isExportedSimple()) {
      sqlUpd.setNextParameter(parentId);
      return;
    }
    EntityBean parent = (EntityBean) parentId;
    for (ExportedProperty exportedProperty : exportedProperties) {
      sqlUpd.setNextParameter(exportedProperty.getValue(parent));
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

    String msg = "Error with the Join on [" + getFullBeanName()
      + "]. Could not find the matching foreign key for [" + matchColumn + "] in table[" + searchTable + "]?"
      + " Perhaps using a @JoinColumn with the name/referencedColumnName attributes swapped?";
    throw new PersistenceException(msg);
  }
}
