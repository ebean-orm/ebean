package io.ebeaninternal.server.deploy;

import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;
import io.ebean.text.PathProperties;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.el.ElPropertyChainBuilder;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.query.SqlBeanLoad;
import io.ebeaninternal.server.text.json.ReadJson;
import io.ebeaninternal.server.text.json.SpiJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Property mapped to a List Set or Map.
 */
public class BeanPropertyAssocMany<T> extends BeanPropertyAssoc<T> {

  private static final Logger logger = LoggerFactory.getLogger(BeanPropertyAssocMany.class);

  private final BeanPropertyAssocManyJsonHelp jsonHelp;

  /**
   * Join for manyToMany intersection table.
   */
  private final TableJoin intersectionJoin;
  private final String intersectionPublishTable;
  private final String intersectionDraftTable;

  /**
   * For ManyToMany this is the Inverse join used to build reference queries.
   */
  private final TableJoin inverseJoin;

  /**
   * Flag to indicate that this is a unidirectional relationship.
   */
  private final boolean unidirectional;

  /**
   * Flag to indicate manyToMany relationship.
   */
  private final boolean manyToMany;

  /**
   * Order by used when fetch joining the associated many.
   */
  private final String fetchOrderBy;

  /**
   * Order by used when lazy loading the associated many.
   */
  private String lazyFetchOrderBy;

  private final String mapKey;

  /**
   * The type of the many, set, list or map.
   */
  private final ManyType manyType;

  private final ModifyListenMode modifyListenMode;

  private BeanProperty mapKeyProperty;

  private String exportedPropertyBindProto = "?";

  /**
   * Property on the 'child' bean that links back to the 'master'.
   */
  protected BeanPropertyAssocOne<?> childMasterProperty;

  private String childMasterIdProperty;

  private boolean embeddedExportedProperties;

  private BeanCollectionHelp<T> help;

  private ImportedId importedId;

  private String deleteByParentIdSql;

  private String deleteByParentIdInSql;

  /**
   * Create this property.
   */
  public BeanPropertyAssocMany(BeanDescriptor<?> descriptor, DeployBeanPropertyAssocMany<T> deploy) {
    super(descriptor, deploy);
    this.unidirectional = deploy.isUnidirectional();
    this.manyToMany = deploy.isManyToMany();
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
    this.jsonHelp = new BeanPropertyAssocManyJsonHelp(this);
  }

  @Override
  public void initialise() {
    super.initialise();

    if (!isTransient) {
      this.help = BeanCollectionHelpFactory.create(this);

      if (manyToMany) {
        // only manyToMany's have imported properties
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
      if (exportedProperties.length > 0) {
        embeddedExportedProperties = exportedProperties[0].isEmbedded();
        exportedPropertyBindProto = deriveExportedPropertyBindProto();

        if (fetchOrderBy != null) {
          // derive lazyFetchOrderBy
          StringBuilder sb = new StringBuilder(50);
          for (int i = 0; i < exportedProperties.length; i++) {
            if (i > 0) {
              sb.append(", ");
            }
            // these fk columns are either on the intersection (int_) or base table (t0)
            String fkTableAlias = isManyToMany() ? "int_" : "t0";
            sb.append(fkTableAlias).append(".").append(exportedProperties[i].getForeignDbColumn());
          }
          sb.append(", ").append(fetchOrderBy);
          lazyFetchOrderBy = sb.toString().trim();
        }
      }

      String delStmt;
      if (manyToMany) {
        delStmt = "delete from " + inverseJoin.getTable() + " where ";
      } else {
        delStmt = "delete from " + targetDescriptor.getBaseTable() + " where ";
      }
      deleteByParentIdSql = delStmt + deriveWhereParentIdSql(false, "");
      deleteByParentIdInSql = delStmt + deriveWhereParentIdSql(true, "");
    }
  }

  /**
   * Initialise after the target bean descriptors have been all set.
   */
  public void initialisePostTarget() {
    if (childMasterProperty != null) {
      BeanProperty masterId = childMasterProperty.getTargetDescriptor().getIdProperty();
      childMasterIdProperty = childMasterProperty.getName() + "." + masterId.getName();
    }
  }

  @Override
  protected void docStoreIncludeByDefault(PathProperties pathProps) {
    // by default not including "Many" properties in document store
  }

  /**
   * Copy collection value if existing is empty.
   */
  @Override
  public void merge(EntityBean bean, EntityBean existing) {

    Object existingCollection = getVal(existing);
    if (existingCollection instanceof BeanCollection<?>) {
      BeanCollection<?> toBC = (BeanCollection<?>) existingCollection;
      if (!toBC.isPopulated()) {
        Object fromCollection = getVal(bean);
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
  public void addBeanToCollectionWithCreate(EntityBean parentBean, EntityBean detailBean, boolean withCheck) {
    BeanCollection<?> bc = (BeanCollection<?>) super.getValue(parentBean);
    if (bc == null) {
      bc = help.createEmpty(parentBean);
      setValue(parentBean, bc);
    }
    help.add(bc, detailBean, withCheck);
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

  public SqlUpdate deleteByParentId(Object parentId, List<Object> parentIdist) {
    if (parentId != null) {
      return deleteByParentId(parentId);
    } else {
      return deleteByParentIdList(parentIdist);
    }
  }

  private SqlUpdate deleteByParentId(Object parentId) {
    DefaultSqlUpdate sqlDelete = new DefaultSqlUpdate(deleteByParentIdSql);
    bindParentId(sqlDelete, parentId);
    return sqlDelete;
  }

  /**
   * Find the Id's of detail beans given a parent Id or list of parent Id's.
   */
  public List<Object> findIdsByParentId(Object parentId, List<Object> parentIdList, Transaction t, List<Object> excludeDetailIds) {
    if (parentId != null) {
      return findIdsByParentId(parentId, t, excludeDetailIds);
    } else {
      return findIdsByParentIdList(parentIdList, t, excludeDetailIds);
    }
  }

  private List<Object> findIdsByParentId(Object parentId, Transaction t, List<Object> excludeDetailIds) {

    String rawWhere = deriveWhereParentIdSql(false, "");

    EbeanServer server = server();
    Query<?> q = server.find(getPropertyType());
    bindParentIdEq(rawWhere, parentId, q);

    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      q.where().not(q.getExpressionFactory().idIn(excludeDetailIds));
    }

    return server.findIds(q, t);
  }

  /**
   * Exclude many properties from bean cache data.
   */
  @Override
  public boolean isCacheDataInclude() {
    // this would change for DB Array type support
    return false;
  }

  public void addWhereParentIdIn(SpiQuery<?> query, List<Object> parentIds, boolean useDocStore) {
    if (useDocStore) {
      // assumes the ManyToOne property is included
      query.where().in(childMasterIdProperty, parentIds);
    } else {
      addWhereParentIdIn(query, parentIds);
    }
  }

  /**
   * Add a where clause to the query for a given list of parent Id's.
   */
  private void addWhereParentIdIn(SpiQuery<?> query, List<Object> parentIds) {

    String tableAlias = manyToMany ? "int_." : "t0.";
    if (manyToMany) {
      query.setM2MIncludeJoin(inverseJoin);
    }
    String rawWhere = deriveWhereParentIdSql(true, tableAlias);
    String expr = descriptor.getParentIdInExpr(parentIds.size(), rawWhere);

    bindParentIdsIn(expr, parentIds, query);
  }

  private List<Object> findIdsByParentIdList(List<Object> parentIds, Transaction t, List<Object> excludeDetailIds) {

    String rawWhere = deriveWhereParentIdSql(true, "");
    String inClause = buildInClauseBinding(parentIds.size(), exportedPropertyBindProto);

    String expr = rawWhere + inClause;

    EbeanServer server = descriptor.getEbeanServer();
    Query<?> q = server.find(propertyType);
    bindParentIdsIn(expr, parentIds, q);

    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      q.where().not(q.getExpressionFactory().idIn(excludeDetailIds));
    }

    return server.findIds(q, t);
  }

  private SqlUpdate deleteByParentIdList(List<Object> parentIds) {

    StringBuilder sb = new StringBuilder(100);
    sb.append(deleteByParentIdInSql);
    sb.append(buildInClauseBinding(parentIds.size(), exportedPropertyBindProto));

    DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
    bindParentIds(delete, parentIds);
    return delete;
  }

  private String deriveExportedPropertyBindProto() {
    if (exportedProperties.length == 1) {
      return "?";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("?");
    }
    sb.append(")");
    return sb.toString();
  }

  private String buildInClauseBinding(int size, String bindProto) {

    if (descriptor.isSimpleId()) {
      return descriptor.getIdBinder().getIdInValueExpr(false, size);
    }
    StringBuilder sb = new StringBuilder(10 + (size * (bindProto.length() + 1)));
    sb.append(" in");
    sb.append(" (");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(bindProto);
    }
    sb.append(") ");
    return sb.toString();
  }

  /**
   * Set the lazy load server to help create reference collections (that lazy
   * load on demand).
   */
  public void setLoader(BeanCollectionLoader loader) {
    if (help != null) {
      help.setLoader(loader);
    }
  }

  /**
   * Return the mode for listening to modifications to collections for this
   * association.
   */
  public ModifyListenMode getModifyListenMode() {
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
  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    return null;
  }

  @Override
  public Object read(DbReadContext ctx) throws SQLException {
    return null;
  }

  public void add(BeanCollection<?> collection, EntityBean bean) {
    help.add(collection, bean, false);
  }

  @Override
  public String getAssocIsEmpty(SpiExpressionRequest request, String path) {

    boolean softDelete = targetDescriptor.isSoftDelete();

    StringBuilder sb = new StringBuilder(50);
    SpiQuery<?> query = request.getQueryRequest().getQuery();
    if (manyToMany) {
      sb.append(query.isAsDraft() ? intersectionDraftTable : intersectionPublishTable);
    } else {
      sb.append(targetDescriptor.getBaseTable(query.getTemporalMode()));
    }
    if (softDelete && manyToMany) {
      sb.append(" x join ");
      sb.append(targetDescriptor.getBaseTable(query.getTemporalMode()));
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
    if (softDelete) {
      String alias = (manyToMany) ? "x2" : "x";
      sb.append(" and ").append(targetDescriptor.getSoftDeletePredicate(alias));
    }
    return sb.toString();
  }

  /**
   * Return the Id values from the given bean.
   */
  @Override
  public Object[] getAssocIdValues(EntityBean bean) {
    return targetDescriptor.getIdBinder().getIdValues(bean);
  }

  /**
   * Return the Id expression to add to where clause etc.
   */
  @Override
  public String getAssocIdExpression(String prefix, String operator) {
    return targetDescriptor.getIdBinder().getAssocOneIdExpr(prefix, operator);
  }

  /**
   * Return the logical id value expression taking into account embedded id's.
   */
  @Override
  public String getAssocIdInValueExpr(boolean not, int size) {
    return targetDescriptor.getIdBinder().getIdInValueExpr(not, size);
  }

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  @Override
  public String getAssocIdInExpr(String prefix) {
    return targetDescriptor.getIdBinder().getAssocIdInExpr(prefix);
  }

  @Override
  public boolean isMany() {
    return true;
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
  public ManyType getManyType() {
    return manyType;
  }

  /**
   * Return true if this is many to many.
   */
  public boolean isManyToMany() {
    return manyToMany;
  }

  /**
   * ManyToMany only, join from local table to intersection table.
   */
  public TableJoin getIntersectionTableJoin() {
    return intersectionJoin;
  }

  /**
   * Set the join properties from the parent bean to the child bean.
   * This is only valid for OneToMany and NOT valid for ManyToMany.
   */
  public void setJoinValuesToChild(EntityBean parent, EntityBean child, Object mapKeyValue) {

    if (mapKeyProperty != null) {
      mapKeyProperty.setValue(child, mapKeyValue);
    }

    if (!manyToMany && childMasterProperty != null) {
      // bidirectional in the sense that the 'master' property
      // exists on the 'detail' bean
      childMasterProperty.setValue(child, parent);
    }
  }

  /**
   * Return the order by clause used to order the fetching of the data for
   * this list, set or map.
   */
  public String getFetchOrderBy() {
    return fetchOrderBy;
  }

  /**
   * Return the order by for use when lazy loading the associated collection.
   */
  public String getLazyFetchOrderBy() {
    return lazyFetchOrderBy;
  }

  /**
   * Return the default mapKey when returning a Map.
   */
  public String getMapKey() {
    return mapKey;
  }

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

  public BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {
    return help.getBeanCollectionAdd(bc, mapKey);
  }

  public Object getParentId(EntityBean parentBean) {
    return descriptor.getId(parentBean);
  }

  public void addSelectExported(DbSqlContext ctx, String tableAlias) {

    String alias = manyToMany ? "int_" : tableAlias;
    if (alias == null) {
      alias = "t0";
    }
    for (ExportedProperty exportedProperty : exportedProperties) {
      ctx.appendColumn(alias, exportedProperty.getForeignDbColumn());
    }
  }

  private String deriveWhereParentIdSql(boolean inClause, String tableAlias) {

    StringBuilder sb = new StringBuilder();

    if (inClause) {
      sb.append("(");
    }
    for (int i = 0; i < exportedProperties.length; i++) {
      String fkColumn = exportedProperties[i].getForeignDbColumn();
      if (i > 0) {
        String s = inClause ? "," : " and ";
        sb.append(s);
      }
      sb.append(tableAlias).append(fkColumn);
      if (!inClause) {
        sb.append("=? ");
      }
    }
    if (inClause) {
      sb.append(")");
    }
    return sb.toString();
  }

//	public void setPredicates(SpiQuery<?> query, EntityBean parentBean) {
//
//		if (manyToMany){
//			// for ManyToMany lazy loading we need to include a
//			// join to the intersection table. The predicate column
//			// is not on the 'destination many table'.
//			query.setIncludeTableJoin(inverseJoin);
//		}
//
//		if (embeddedExportedProperties) {
//			// use the EmbeddedId object instead of the parentBean
//			BeanProperty idProp = descriptor.getIdProperty();
//			parentBean = (EntityBean)idProp.getValue(parentBean);
//		}
//
//		for (int i = 0; i < exportedProperties.length; i++) {
//			Object val = exportedProperties[i].getValue(parentBean);
//			String fkColumn = exportedProperties[i].getForeignDbColumn();
//			if (!manyToMany){
//				fkColumn = targetDescriptor.getBaseTableAlias()+"."+fkColumn;
//			} else {
//				// use hard coded alias for intersection table
//				fkColumn = "int_."+fkColumn;
//			}
//			query.where().eq(fkColumn, val);
//		}
//
//		if (extraWhere != null){
//			// replace the table alias place holder
//			String ta = targetDescriptor.getBaseTableAlias();
//			String where = StringHelper.replaceString(extraWhere, "${ta}", ta);
//			query.where().raw(where);
//		}
//
//		if (fetchOrderBy != null){
//			query.order(fetchOrderBy);
//		}
//	}

  /**
   * Create the array of ExportedProperty used to build reference objects.
   */
  private ExportedProperty[] createExported() {

    BeanProperty idProp = descriptor.getIdProperty();

    ArrayList<ExportedProperty> list = new ArrayList<>();

    if (idProp != null && idProp.isEmbedded()) {

      BeanPropertyAssocOne<?> one = (BeanPropertyAssocOne<?>) idProp;
      BeanDescriptor<?> targetDesc = one.getTargetDescriptor();
      BeanProperty[] emIds = targetDesc.propertiesBaseScalar();
      try {
        for (BeanProperty emId : emIds) {
          ExportedProperty expProp = findMatch(true, emId);
          list.add(expProp);
        }
      } catch (PersistenceException e) {
        // not found as individual scalar properties
        logger.error("Could not find a exported property?", e);
      }

    } else {
      if (idProp != null) {
        ExportedProperty expProp = findMatch(false, idProp);
        list.add(expProp);
      }
    }

    return list.toArray(new ExportedProperty[list.size()]);
  }

  /**
   * Find the matching foreignDbColumn for a given local property.
   */
  private ExportedProperty findMatch(boolean embedded, BeanProperty prop) {

    String matchColumn = prop.getDbColumn();

    String searchTable;
    TableJoinColumn[] columns;
    if (manyToMany) {
      // look for column going to intersection
      columns = intersectionJoin.columns();
      searchTable = intersectionJoin.getTable();

    } else {
      columns = tableJoin.columns();
      searchTable = tableJoin.getTable();
    }
    for (TableJoinColumn column : columns) {
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

  /**
   * Return the child property that links back to the master bean.
   * <p>
   * Note that childMasterProperty will be null if a field is used instead of
   * a ManyToOne bean association.
   * </p>
   */
  private BeanPropertyAssocOne<?> initChildMasterProperty() {

    if (unidirectional) {
      return null;
    }

    // search for the property, to see if it exists
    Class<?> beanType = descriptor.getBeanType();
    BeanDescriptor<?> targetDesc = getTargetDescriptor();

    BeanPropertyAssocOne<?>[] ones = targetDesc.propertiesOne();
    for (BeanPropertyAssocOne<?> prop : ones) {
      if (mappedBy != null) {
        // match using mappedBy as property name
        if (mappedBy.equalsIgnoreCase(prop.getName())) {
          return prop;
        }
      } else {
        // assume only one property that matches parent object type
        if (prop.getTargetType().equals(beanType)) {
          // found it, stop search
          return prop;
        }
      }
    }

    throw new RuntimeException("Can not find Master [" + beanType + "] in Child[" + targetDesc + "]");
  }

  /**
   * Search for and return the mapKey property.
   */
  private BeanProperty initMapKeyProperty() {

    // search for the property
    BeanDescriptor<?> targetDesc = getTargetDescriptor();
    for (BeanProperty prop : targetDesc.propertiesAll()) {
      if (mapKey.equalsIgnoreCase(prop.getName())) {
        return prop;
      }
    }

    String from = descriptor.getFullName();
    String to = targetDesc.getFullName();
    throw new PersistenceException(from + ": Could not find mapKey property [" + mapKey + "] on [" + to + "]");
  }

  public IntersectionRow buildManyDeleteChildren(EntityBean parentBean, List<Object> excludeDetailIds) {

    IntersectionRow row = new IntersectionRow(tableJoin.getTable(), targetDescriptor);
    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      row.setExcludeIds(excludeDetailIds, getTargetDescriptor());
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
  public void registerDraftIntersectionTable(Map<String, String> draftTableMap) {
    if (hasDraftIntersection()) {
      draftTableMap.put(intersectionPublishTable, intersectionDraftTable);
    }
  }

  /**
   * Return true if the relationship is a ManyToMany with the intersection having an associated draft table.
   */
  private boolean hasDraftIntersection() {
    return intersectionDraftTable != null && !intersectionDraftTable.equals(intersectionPublishTable);
  }

  private void buildExport(IntersectionRow row, EntityBean parentBean) {

    if (embeddedExportedProperties) {
      BeanProperty idProp = descriptor.getIdProperty();
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
  public void jsonWriteValue(SpiJsonWriter writeJson, Object value) throws IOException {
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
        help.jsonWrite(ctx, name, value, include != null);
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
  public void jsonRead(ReadJson readJson, EntityBean parentBean) throws IOException {
    jsonHelp.jsonRead(readJson, parentBean);
  }

  @SuppressWarnings("unchecked")
  public void publishMany(EntityBean draft, EntityBean live) {

    // collections will not be null due to enhancement
    BeanCollection<T> draftVal = (BeanCollection<T>) getValueIntercept(draft);
    BeanCollection<T> liveVal = (BeanCollection<T>) getValueIntercept(live);

    // Organise the existing live beans into map keyed by id
    Map<Object, T> liveBeansAsMap = liveBeansAsMap(liveVal);

    // publish from each draft to live bean creating new live beans as required
    draftVal.size();
    Collection<T> actualDetails = draftVal.getActualDetails();
    for (T bean : actualDetails) {
      Object id = targetDescriptor.getId((EntityBean) bean);
      T liveBean = liveBeansAsMap.remove(id);

      if (isManyToMany()) {
        if (liveBean == null) {
          // add new relationship (Map not allowed here)
          liveVal.addBean(targetDescriptor.createReference(Boolean.FALSE, false, id, null));
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
    Collection<?> liveBeans = liveVal.getActualDetails();
    Map<Object, T> liveMap = new LinkedHashMap<>();

    for (Object liveBean : liveBeans) {
      Object id = targetDescriptor.getId((EntityBean) liveBean);
      liveMap.put(id, (T) liveBean);
    }
    return liveMap;
  }

}
