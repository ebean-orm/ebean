package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.*;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollection.ModifyListenMode;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.el.ElPropertyChainBuilder;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SqlBeanLoad;
import com.avaje.ebeaninternal.server.text.json.WriteJson;
import com.fasterxml.jackson.core.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

  /**
   * Derived list of exported property and matching foreignKey
   */
  private ExportedProperty[] exportedProperties;

  private String exportedPropertyBindProto = "?";

  /**
   * Property on the 'child' bean that links back to the 'master'.
   */
  protected BeanPropertyAssocOne<?> childMasterProperty;

  private boolean embeddedExportedProperties;

  private BeanCollectionHelp<T> help;

  private ImportedId importedId;

  private String deleteByParentIdSql;

  private String deleteByParentIdInSql;

  /**
   * Create this property.
   */
  public BeanPropertyAssocMany(BeanDescriptorMap owner, BeanDescriptor<?> descriptor, DeployBeanPropertyAssocMany<T> deploy) {
    super(owner, descriptor, deploy);
    this.unidirectional = deploy.isUnidirectional();
    this.manyToMany = deploy.isManyToMany();
    this.manyType = deploy.getManyType();
    this.mapKey = deploy.getMapKey();
    this.fetchOrderBy = deploy.getFetchOrderBy();
    this.intersectionJoin = deploy.createIntersectionTableJoin();
    this.inverseJoin = deploy.createInverseTableJoin();
    this.modifyListenMode = deploy.getModifyListenMode();
    this.jsonHelp = new BeanPropertyAssocManyJsonHelp(this);
  }

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
   * Add the bean to the appropriate collection on the parent bean.
   */
  public void addBeanToCollectionWithCreate(EntityBean parentBean, EntityBean detailBean) {
    BeanCollection<?> bc = (BeanCollection<?>) super.getValue(parentBean);
    if (bc == null) {
      bc = help.createEmpty(parentBean);
      setValue(parentBean, bc);
    }
    help.add(bc, detailBean);
  }

  public boolean isEmptyBeanCollection(EntityBean bean) {
    Object val = getValue(bean);
    return val == null || (val instanceof BeanCollection<?>) && ((BeanCollection<?>) val).isEmptyAndUntouched();
  }

  /**
   * Reset the many properties to be empty and ready for reloading.
   * <p>
   * Used in bean refresh.
   */
  public void resetMany(EntityBean bean) {
    Object value = getValue(bean);
    if (value == null) {
      // not expecting this - set an empty reference
      createReference(bean);
    } else {
      // reset the collection back to empty
      ((BeanCollection)value).reset(bean, name);
    }
  }

  @Override
  public Object getValue(EntityBean bean) {
    return super.getValue(bean);
  }

  @Override
  public Object getValueIntercept(EntityBean bean) {
    return super.getValueIntercept(bean);
  }

  @Override
  public void setValue(EntityBean bean, Object value) {
    super.setValue(bean, value);
  }

  @Override
  public void setValueIntercept(EntityBean bean, Object value) {
    super.setValueIntercept(bean, value);
  }

  public ElPropertyValue buildElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {
    return createElPropertyValue(propName, remainder, chain, propertyDeploy);
  }

  public void buildSelectExpressionChain(String prefix, List<String> selectChain) {
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
    bindWhereParendId(sqlDelete, parentId);
    return sqlDelete;
  }

  /**
   * Find the Id's of detail beans given a parent Id or list of parent Id's.
   */
  public List<Object> findIdsByParentId(Object parentId, List<Object> parentIdist, Transaction t, ArrayList<Object> excludeDetailIds) {
    if (parentId != null) {
      return findIdsByParentId(parentId, t, excludeDetailIds);
    } else {
      return findIdsByParentIdList(parentIdist, t, excludeDetailIds);
    }
  }

  private List<Object> findIdsByParentId(Object parentId, Transaction t, ArrayList<Object> excludeDetailIds) {

    String rawWhere = deriveWhereParentIdSql(false, "");

    EbeanServer server = getBeanDescriptor().getEbeanServer();
    Query<?> q = server.find(getPropertyType())
        .where().raw(rawWhere).query();

    bindWhereParendId(1, q, parentId);

    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      Expression idIn = q.getExpressionFactory().idIn(excludeDetailIds);
      q.where().not(idIn);
    }

    return server.findIds(q, t);
  }

  /**
   * Add a where clause to the query for a given list of parent Id's.
   */
  public void addWhereParentIdIn(SpiQuery<?> query, List<Object> parentIds) {

    String tableAlias = manyToMany ? "int_." : "t0.";
    if (manyToMany) {
      query.setIncludeTableJoin(inverseJoin);
    }
    String rawWhere = deriveWhereParentIdSql(true, tableAlias);
    String expr = descriptor.getParentIdInExpr(parentIds.size(), rawWhere);

    // Flatten the bind values if needed (embeddedId)
    List<Object> bindValues = getBindParentIds(parentIds);

    query.where().raw(expr, bindValues.toArray());
  }

  private List<Object> findIdsByParentIdList(List<Object> parentIdist, Transaction t, ArrayList<Object> excludeDetailIds) {

    String rawWhere = deriveWhereParentIdSql(true, "");
    String inClause = buildInClauseBinding(parentIdist.size(), exportedPropertyBindProto);

    String expr = rawWhere + inClause;

    EbeanServer server = getBeanDescriptor().getEbeanServer();
    Query<?> q = server.find(getPropertyType()).where().raw(expr).query();

    int pos = 1;
    for (int i = 0; i < parentIdist.size(); i++) {
      pos = bindWhereParendId(pos, q, parentIdist.get(i));
    }

    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      Expression idIn = q.getExpressionFactory().idIn(excludeDetailIds);
      q.where().not(idIn);
    }

    return server.findIds(q, t);
  }

  private SqlUpdate deleteByParentIdList(List<Object> parentIdist) {

    StringBuilder sb = new StringBuilder(100);
    sb.append(deleteByParentIdInSql);

    String inClause = buildInClauseBinding(parentIdist.size(), exportedPropertyBindProto);
    sb.append(inClause);

    DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
    for (int i = 0; i < parentIdist.size(); i++) {
      bindWhereParendId(delete, parentIdist.get(i));
    }

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
  public void load(SqlBeanLoad sqlBeanLoad) throws SQLException {
    sqlBeanLoad.loadAssocMany(this);
  }

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean, Class<?> type) throws SQLException {
    return null;
  }

  @Override
  public Object read(DbReadContext ctx) throws SQLException {
    return null;
  }

  public void add(BeanCollection<?> collection, EntityBean bean) {
    help.add(collection, bean);
  }

  /**
   * Refresh the appropriate list set or map.
   */
  public void refresh(EbeanServer server, Query<?> query, Transaction t, EntityBean parentBean) {
    help.refresh(server, query, t, parentBean);
  }

  /**
   * Apply the refreshed BeanCollection to the property of the parentBean.
   */
  public void refresh(BeanCollection<?> bc, EntityBean parentBean) {
    help.refresh(bc, parentBean);
  }

  /**
   * Return the Id values from the given bean.
   */
  @Override
  public Object[] getAssocOneIdValues(EntityBean bean) {
    return targetDescriptor.getIdBinder().getIdValues(bean);
  }

  /**
   * Return the Id expression to add to where clause etc.
   */
  public String getAssocOneIdExpr(String prefix, String operator) {
    return targetDescriptor.getIdBinder().getAssocOneIdExpr(prefix, operator);
  }

  /**
   * Return the logical id value expression taking into account embedded id's.
   */
  @Override
  public String getAssocIdInValueExpr(int size) {
    return targetDescriptor.getIdBinder().getIdInValueExpr(size);
  }

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  @Override
  public String getAssocIdInExpr(String prefix) {
    return targetDescriptor.getIdBinder().getAssocIdInExpr(prefix);
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

  public List<Object> getBindParentIds(List<Object> parentIds) {
    if (exportedProperties.length == 1) {
      return parentIds;
    }
    List<Object> expandedList = new ArrayList<Object>(parentIds.size() * exportedProperties.length);
    for (int i = 0; i < parentIds.size(); i++) {
      for (int y = 0; y < exportedProperties.length; y++) {
        Object compId = parentIds.get(i);
        expandedList.add(exportedProperties[y].getValue((EntityBean) compId));
      }
    }
    return expandedList;
  }

  private void bindWhereParendId(DefaultSqlUpdate sqlUpd, Object parentId) {

    if (exportedProperties.length == 1) {
      sqlUpd.addParameter(parentId);
      return;
    }
    EntityBean parent = (EntityBean) parentId;
    for (int i = 0; i < exportedProperties.length; i++) {
      Object embVal = exportedProperties[i].getValue(parent);
      sqlUpd.addParameter(embVal);
    }
  }

  private int bindWhereParendId(int pos, Query<?> q, Object parentId) {

    if (exportedProperties.length == 1) {
      q.setParameter(pos++, parentId);

    } else {

      EntityBean parent = (EntityBean) parentId;
      for (int i = 0; i < exportedProperties.length; i++) {
        Object embVal = exportedProperties[i].getValue(parent);
        q.setParameter(pos++, embVal);
      }
    }
    return pos;
  }

  public void addSelectExported(DbSqlContext ctx, String tableAlias) {

    String alias = manyToMany ? "int_" : tableAlias;
    if (alias == null) {
      alias = "t0";
    }
    for (int i = 0; i < exportedProperties.length; i++) {
      ctx.appendColumn(alias, exportedProperties[i].getForeignDbColumn());
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

    ArrayList<ExportedProperty> list = new ArrayList<ExportedProperty>();

    if (idProp != null && idProp.isEmbedded()) {

      BeanPropertyAssocOne<?> one = (BeanPropertyAssocOne<?>) idProp;
      BeanDescriptor<?> targetDesc = one.getTargetDescriptor();
      BeanProperty[] emIds = targetDesc.propertiesBaseScalar();
      try {
        for (int i = 0; i < emIds.length; i++) {
          ExportedProperty expProp = findMatch(true, emIds[i]);
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
    for (int i = 0; i < columns.length; i++) {
      String matchTo = columns[i].getLocalDbColumn();

      if (matchColumn.equalsIgnoreCase(matchTo)) {
        String foreignCol = columns[i].getForeignDbColumn();
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
    for (int i = 0; i < ones.length; i++) {
      BeanPropertyAssocOne<?> prop = ones[i];
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

  public IntersectionRow buildManyDeleteChildren(EntityBean parentBean, ArrayList<Object> excludeDetailIds) {

    IntersectionRow row = new IntersectionRow(tableJoin.getTable());
    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      row.setExcludeIds(excludeDetailIds, getTargetDescriptor());
    }
    buildExport(row, parentBean);
    return row;
  }

  public IntersectionRow buildManyToManyDeleteChildren(EntityBean parentBean) {

    IntersectionRow row = new IntersectionRow(intersectionJoin.getTable());
    buildExport(row, parentBean);
    return row;
  }

  public IntersectionRow buildManyToManyMapBean(EntityBean parent, EntityBean other) {

    IntersectionRow row = new IntersectionRow(intersectionJoin.getTable());

    buildExport(row, parent);
    buildImport(row, other);
    return row;
  }

  private void buildExport(IntersectionRow row, EntityBean parentBean) {

    if (embeddedExportedProperties) {
      BeanProperty idProp = descriptor.getIdProperty();
      parentBean = (EntityBean) idProp.getValue(parentBean);
    }
    for (int i = 0; i < exportedProperties.length; i++) {
      Object val = exportedProperties[i].getValue(parentBean);
      String fkColumn = exportedProperties[i].getForeignDbColumn();

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

  public void jsonWrite(WriteJson ctx, EntityBean bean) throws IOException {
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
        ctx.toJson(name, (Collection<?>) value);
      }
      ctx.popParentBeanMany();
    }
  }

  public void jsonRead(JsonParser parser, EntityBean parentBean) throws IOException {
    jsonHelp.jsonRead(parser, parentBean);
  }
}
