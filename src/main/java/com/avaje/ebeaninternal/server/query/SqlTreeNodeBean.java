package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;

/**
 * Normal bean included in the query.
 */
public class SqlTreeNodeBean implements SqlTreeNode {

  private static final SqlTreeNode[] NO_CHILDREN = new SqlTreeNode[0];

  protected final BeanDescriptor<?> desc;

  protected final IdBinder idBinder;

  /**
   * The children which will be other SelectBean or SelectProxyBean.
   */
  protected final SqlTreeNode[] children;

  protected final boolean readOnlyLeaf;

  /**
   * Set to true if this is a partial object fetch.
   */
  protected final boolean partialObject;

  protected final BeanProperty[] properties;

  /**
   * Extra where clause added by Where annotation on associated many.
   */
  protected final String extraWhere;

  protected final BeanPropertyAssoc<?> nodeBeanProp;

  protected final TableJoin[] tableJoins;

  /**
   * False if report bean and has no id property.
   */
  protected final boolean readId;

  protected final boolean disableLazyLoad;

  protected final InheritInfo inheritInfo;

  protected final String prefix;

  protected final Map<String, String> pathMap;
  
  protected final BeanPropertyAssocMany<?> lazyLoadParent;
  
  public SqlTreeNodeBean(String prefix, BeanPropertyAssoc<?> beanProp, SqlTreeProperties props,
      List<SqlTreeNode> myChildren, boolean withId) {

    this(prefix, beanProp, beanProp.getTargetDescriptor(), props, myChildren, withId, null);
  }

  /**
   * Create with the appropriate node.
   */
  public SqlTreeNodeBean(String prefix, BeanPropertyAssoc<?> beanProp, BeanDescriptor<?> desc,
      SqlTreeProperties props, List<SqlTreeNode> myChildren, boolean withId,  BeanPropertyAssocMany<?> lazyLoadParent) {

    this.lazyLoadParent = lazyLoadParent;
    this.prefix = prefix;
    this.nodeBeanProp = beanProp;
    this.desc = desc;
    this.inheritInfo = desc.getInheritInfo();
    this.extraWhere = (beanProp == null) ? null : beanProp.getExtraWhere();

    this.idBinder = desc.getIdBinder();

    // the bean has an Id property and we want to use it
    this.readId = withId && (desc.getIdProperty() != null);
    this.disableLazyLoad = !readId || desc.isSqlSelectBased();

    this.tableJoins = props.getTableJoins();

    this.partialObject = props.isPartialObject();

    this.readOnlyLeaf = props.isReadOnly();

    this.properties = props.getProps();


    if (myChildren == null) {
      children = NO_CHILDREN;
    } else {
      children = myChildren.toArray(new SqlTreeNode[myChildren.size()]);
    }

    pathMap = createPathMap(prefix, desc);
  }

  private Map<String, String> createPathMap(String prefix, BeanDescriptor<?> desc) {

    BeanPropertyAssocMany<?>[] manys = desc.propertiesMany();

    HashMap<String, String> m = new HashMap<String, String>();
    for (int i = 0; i < manys.length; i++) {
      String name = manys[i].getName();
      m.put(name, getPath(prefix, name));
    }

    return m;
  }

  private String getPath(String prefix, String propertyName) {
    if (prefix == null) {
      return propertyName;
    } else {
      return prefix + "." + propertyName;
    }
  }

  protected void postLoad(DbReadContext cquery, EntityBean loadedBean, Object id, Object lazyLoadParentId) {
  }

  public void buildSelectExpressionChain(List<String> selectChain) {
    if (readId) {
      idBinder.buildSelectExpressionChain(prefix, selectChain);
    }
    for (int i = 0, x = properties.length; i < x; i++) {
      properties[i].buildSelectExpressionChain(prefix, selectChain);
    }
    // recursively continue reading...
    for (int i = 0; i < children.length; i++) {
      // read each child... and let them set their
      // values back to this localBean
      children[i].buildSelectExpressionChain(selectChain);
    }
  }

  /**
   * read the properties from the resultSet.
   */
  public void load(DbReadContext ctx, EntityBean parentBean) throws SQLException {

    Object lazyLoadParentId = null;
    if (lazyLoadParent != null) {
      lazyLoadParentId = lazyLoadParent.getBeanDescriptor().getIdBinder().read(ctx);
    }
    
    // bean already existing in the persistence context
    EntityBean contextBean = null;

    Class<?> localType;
    BeanDescriptor<?> localDesc;
    IdBinder localIdBinder;
    EntityBean localBean;

    if (inheritInfo != null) {
      InheritInfo localInfo = inheritInfo.readType(ctx);
      if (localInfo == null) {
        // the bean must be null
        localIdBinder = idBinder;
        localBean = null;
        localType = null;
        localDesc = desc;
      } else {
        localBean = localInfo.createEntityBean();
        localType = localInfo.getType();
        localIdBinder = localInfo.getIdBinder();
        localDesc = localInfo.getBeanDescriptor();
      }

    } else {
      localType = null;
      localDesc = desc;
      localBean = desc.createEntityBean();
      localIdBinder = idBinder;
    }

    Mode queryMode = ctx.getQueryMode();

    PersistenceContext persistenceContext = ctx.getPersistenceContext();

    Object id = null;
    if (!readId) {
      // report type bean... or perhaps excluding the id for SqlSelect?

    } else {
      id = localIdBinder.readSet(ctx, localBean);
      if (id == null) {
        // bean must be null...
        localBean = null;
      } else {
        // check the PersistenceContext to see if the bean already exists
        contextBean = (EntityBean)persistenceContext.putIfAbsent(id, localBean);
        if (contextBean == null) {
          // bean just added to the persistenceContext
          contextBean = localBean;
        } else {
          // bean already exists in persistenceContext
          if (isLoadContextBeanNeeded(queryMode, contextBean)){ 
            // refresh it anyway (lazy loading for example)
            localBean = contextBean;
          } else {
            // ignore the DB data...
            localBean = null;
          }
        }
      }
    }

    ctx.setCurrentPrefix(prefix, pathMap);

    ctx.propagateState(localBean);

    SqlBeanLoad sqlBeanLoad = new SqlBeanLoad(ctx, localType, localBean, queryMode);

    if (inheritInfo == null) {
      // normal behavior with no inheritance
      for (int i = 0, x = properties.length; i < x; i++) {
        properties[i].load(sqlBeanLoad);
      }

    } else {
      // take account of inheritance and due to subclassing approach
      // need to get a 'local' version of the property
      for (int i = 0, x = properties.length; i < x; i++) {
        // get a local version of the BeanProperty
        BeanProperty p = localDesc.getBeanProperty(properties[i].getName());
        if (p != null) {
          p.load(sqlBeanLoad);
        } else {
          properties[i].loadIgnore(ctx);
        }
      }
    }

    for (int i = 0, x = tableJoins.length; i < x; i++) {
      tableJoins[i].load(sqlBeanLoad);
    }

    boolean lazyLoadMany = false;
    if (localBean == null && queryMode.equals(Mode.LAZYLOAD_MANY)) {
      // batch lazy load many into existing contextBean
      localBean = contextBean;
      lazyLoadMany = true;
    }

    // recursively continue reading...
    for (int i = 0; i < children.length; i++) {
      // read each child... and let them set their
      // values back to this localBean
      children[i].load(ctx, localBean);
    }

    if (lazyLoadMany) {
      // special case where we load children

    } else if (localBean != null) {

      ctx.setCurrentPrefix(prefix, pathMap);
      createListProxies(localDesc, ctx, localBean);

      localDesc.postLoad(localBean, null);

      if (localBean instanceof EntityBean) {
        EntityBeanIntercept ebi = ((EntityBean) localBean)._ebean_getIntercept();
        ebi.setPersistenceContext(persistenceContext);
        if (Mode.LAZYLOAD_BEAN.equals(queryMode)) {
          // Lazy Load does not reset the dirty state
          ebi.setLoadedLazy();
        } else {
          // normal bean loading
          ebi.setLoaded();
        }

        if (partialObject) {
          ctx.register(null, ebi);
        } else {
          ebi.setFullyLoadedBean(true);
        }

        if (disableLazyLoad) {
          // bean does not have an Id or is SqlSelect based
          ebi.setDisableLazyLoad(true);
        }
        if (ctx.isAutoFetchProfiling()) {
          // collect autofetch profiling for this bean...
          ctx.profileBean(ebi, prefix);
        }
      }

    }
    if (parentBean != null && contextBean != null) {
      // set this back to the parentBean
      nodeBeanProp.setValue(parentBean, contextBean);
    }

    if (!readId) {
      // a bean with no Id (never found in context)
      postLoad(ctx, localBean, id, null);

    } else {
      // return the contextBean which is either the localBean
      // read from the resultSet and put into the context OR
      // the 'matching' bean that already existed in the context
      postLoad(ctx, contextBean, id, lazyLoadParentId);
    }
  }

  /**
   * Create lazy loading proxies for the Many's except for the one that is
   * included in the actual query.
   */
  private void createListProxies(BeanDescriptor<?> localDesc, DbReadContext ctx, EntityBean localBean) {

    BeanPropertyAssocMany<?> fetchedMany = ctx.getManyProperty();

    // load the List/Set/Map proxy objects (deferred fetching of lists)
    BeanPropertyAssocMany<?>[] manys = localDesc.propertiesMany();
    for (int i = 0; i < manys.length; i++) {

      if (fetchedMany != null && fetchedMany.equals(manys[i])) {
        // this many property is included in the query...
        // it is being loaded with real row data (result[1])
      } else {
        // create a proxy for the many (deferred fetching)
        BeanCollection<?> ref = manys[i].createReferenceIfNull(localBean);
        if (ref != null) {
          ctx.register(manys[i].getName(), ref);
        }
      }
    }
  }

  /**
   * Append the property columns to the buffer.
   */
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {

    ctx.pushJoin(prefix);
    ctx.pushTableAlias(prefix);
    
    if (lazyLoadParent != null) {
      lazyLoadParent.addSelectExported(ctx, prefix);
    }

    if (!subQuery && inheritInfo != null) {
      ctx.appendColumn(inheritInfo.getDiscriminatorColumn());
    }

    if (readId) {
      appendSelect(ctx, false, idBinder.getBeanProperty());
    }
    appendSelect(ctx, subQuery, properties);
    appendSelectTableJoins(ctx);

    for (int i = 0; i < children.length; i++) {
      // read each child... and let them set their
      // values back to this localBean
      children[i].appendSelect(ctx, subQuery);
    }

    ctx.popTableAlias();
    ctx.popJoin();
  }

  private void appendSelectTableJoins(DbSqlContext ctx) {

    String baseAlias = ctx.getTableAlias(prefix);

    for (int i = 0; i < tableJoins.length; i++) {
      TableJoin join = tableJoins[i];

      String alias = baseAlias + i;

      ctx.pushSecondaryTableAlias(alias);
      join.appendSelect(ctx, false);
      ctx.popTableAlias();
    }
  }

  /**
   * Append the properties to the buffer.
   */
  private void appendSelect(DbSqlContext ctx, boolean subQuery, BeanProperty[] props) {

    for (int i = 0; i < props.length; i++) {
      props[i].appendSelect(ctx, subQuery);
    }
  }

  private void appendSelect(DbSqlContext ctx, boolean subQuery, BeanProperty prop) {

    if (prop != null) {
      prop.appendSelect(ctx, subQuery);
    }
  }

  public void appendWhere(DbSqlContext ctx) {

	// Only apply inheritance to root node as any join will alreay have the inheritance join include - see TableJoin
	if (inheritInfo != null && nodeBeanProp == null) {
      if (inheritInfo.isRoot()) {
        // at root of hierarchy so don't bother
        // adding a where clause because we want
        // all the types...
      } else {
        // restrict to this type and
        // sub types of this type.
        if (ctx.length() > 0) {
          ctx.append(" and");
        }
        ctx.append(" ").append(ctx.getTableAlias(prefix)).append(".");// tableAlias
        ctx.append(inheritInfo.getWhere()).append(" ");
      }
    }
    if (extraWhere != null) {
      if (ctx.length() > 0) {
        ctx.append(" and");
      }
      String ta = ctx.getTableAlias(prefix);
      String ew = StringHelper.replaceString(extraWhere, "${ta}", ta);
      ctx.append(" ").append(ew).append(" ");
    }

    for (int i = 0; i < children.length; i++) {
      // recursively add to the where clause any
      // fixed predicates (extraWhere etc)
      children[i].appendWhere(ctx);
    }
  }

  /**
   * Append to the FROM clause for this node.
   */
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {

    ctx.pushJoin(prefix);
    ctx.pushTableAlias(prefix);

    // join and return SqlJoinType to use for child joins
    joinType = appendFromBaseTable(ctx, joinType);

    for (int i = 0; i < properties.length; i++) {
      // usually nothing... except for 1-1 Exported
      properties[i].appendFrom(ctx, joinType);
    }

    for (int i = 0; i < children.length; i++) {
      children[i].appendFrom(ctx, joinType);
    }

    ctx.popTableAlias();
    ctx.popJoin();
  }

  /**
   * Join to base table for this node. This includes a join to the intersection
   * table if this is a ManyToMany node.
   */
  public SqlJoinType appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {

    if (nodeBeanProp instanceof BeanPropertyAssocMany<?>) {
      BeanPropertyAssocMany<?> manyProp = (BeanPropertyAssocMany<?>) nodeBeanProp;
      if (manyProp.isManyToMany()) {

        String alias = ctx.getTableAlias(prefix);
        String[] split = SplitName.split(prefix);
        String parentAlias = ctx.getTableAlias(split[0]);
        String alias2 = alias + "z_";

        TableJoin manyToManyJoin = manyProp.getIntersectionTableJoin();
        manyToManyJoin.addJoin(joinType, parentAlias, alias2, ctx);

        return nodeBeanProp.addJoin(joinType, alias2, alias, ctx);
      }

    }

    return nodeBeanProp.addJoin(joinType, prefix, ctx);
  }

  /**
   * Summary description.
   */
  public String toString() {
    return "SqlTreeNodeBean: " + desc;
  }
  
  private boolean isLoadContextBeanNeeded(Mode queryMode, EntityBean contextBean) {
    // if explicitly set loadContextBean to true, then reload
    if (queryMode.isLoadContextBean()) {
      return true;
    }

    if (contextBean._ebean_getIntercept().isFullyLoadedBean()) {
      // reload if contextBean is partial object
      return false;
    }

    // return true by default
    return true;
  }
   
}
