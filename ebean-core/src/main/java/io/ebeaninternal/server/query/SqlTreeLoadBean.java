package io.ebeaninternal.server.query;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.core.type.ScalarDataReader;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.id.IdBinder;

import java.sql.SQLException;
import java.util.Map;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Normal bean included in the query.
 */
class SqlTreeLoadBean implements SqlTreeLoad {

  final STreeType desc;
  final IdBinder idBinder;
  final SqlTreeLoad[] children;
  private final boolean partialObject;
  private final STreeProperty[] properties;
  private final STreePropertyAssoc nodeBeanProp;
  final boolean readId;
  private final boolean readIdNormal;
  private final boolean disableLazyLoad;
  private final boolean unmodifiable;
  private final InheritInfo inheritInfo;
  final String prefix;
  private final Map<String, String> pathMap;
  final STreePropertyAssocMany lazyLoadParent;
  private final SpiQuery.TemporalMode temporalMode;
  private final boolean temporalVersions;
  final IdBinder lazyLoadParentIdBinder;
  private final STreePropertyAssocMany loadingChildProperty;

  SqlTreeLoadBean(SqlTreeNodeBean node) {
    this.lazyLoadParent = node.lazyLoadParent;
    this.lazyLoadParentIdBinder = node.lazyLoadParentIdBinder;
    this.prefix = node.prefix;
    this.desc = node.desc;
    this.inheritInfo = desc.inheritInfo();
    this.idBinder = desc.idBinder();
    this.temporalMode = node.temporalMode;
    this.temporalVersions = node.temporalVersions;
    this.nodeBeanProp = node.nodeBeanProp;
    this.readId = node.readId;
    this.readIdNormal = readId && !temporalVersions;
    this.disableLazyLoad = node.disableLazyLoad;
    this.unmodifiable = node.unmodifiable;
    this.partialObject = node.partialObject;
    this.properties = node.properties;
    this.pathMap = node.pathMap;
    this.children =  node.createLoadChildren();
    this.loadingChildProperty = loadingChildProperty();
  }

  private STreePropertyAssocMany loadingChildProperty() {
    for (SqlTreeLoad child : children) {
      if (child instanceof SqlTreeLoadManyRoot) {
        return ((SqlTreeLoadManyRoot) child).manyProp();
      }
    }
    return null;
  }

  boolean isRoot() {
    return false;
  }

  @Override
  public final ScalarDataReader<?> singleAttributeReader() {
    if (properties == null || properties.length == 0) {
      // if we have no property ask first children (in a distinct select with join)
      if (children.length == 0) {
        // expected to be a findIds query
        return desc.idBinder().beanProperty();
      }
      return children[0].singleAttributeReader();
    }
    if (properties[0] instanceof STreePropertyAssocOne) {
      STreePropertyAssocOne assocOne = (STreePropertyAssocOne)properties[0];
      if (assocOne.isAssocId()) {
        return assocOne.idReader();
      }
    }
    return properties[0];
  }

  /**
   * Load that takes into account inheritance.
   */
  private final class LoadInherit extends Load {

    private LoadInherit(DbReadContext ctx, EntityBean parentBean) {
      super(ctx, parentBean);
    }

    @Override
    void initBeanType() throws SQLException {
      InheritInfo localInfo = readId ? inheritInfo.readType(ctx) : desc.inheritInfo();
      if (localInfo == null) {
        // the bean must be null
        localIdBinder = idBinder;
        localDesc = desc;
      } else {
        localBean = localInfo.createEntityBean(unmodifiable);
        localType = localInfo.getType();
        localIdBinder = localInfo.getIdBinder();
        localDesc = localInfo.desc();
      }
    }

    @Override
    void loadProperties() {
      // take account of inheritance
      for (STreeProperty property : properties) {
        localDesc.inheritanceLoad(sqlBeanLoad, property, ctx);
      }
    }
  }

  /**
   * Load a bean instance.
   */
  class Load {

    final DbReadContext ctx;
    final EntityBean parentBean;

    Object lazyLoadParentId;
    Class<?> localType;
    STreeType localDesc;
    IdBinder localIdBinder;
    EntityBean localBean;

    Mode queryMode;
    PersistenceContext persistenceContext;
    Object id;
    EntityBean contextBean;
    SqlBeanLoad sqlBeanLoad;
    boolean lazyLoadMany;
    boolean usingContextBean;

    private Load(DbReadContext ctx, EntityBean parentBean) {
      this.ctx = ctx;
      this.parentBean = parentBean;
    }

    private void initLazyParent() throws SQLException {
      if (lazyLoadParentIdBinder != null) {
        lazyLoadParentId = lazyLoadParentIdBinder.read(ctx);
      }
    }

    void initBeanType() throws SQLException {
      localDesc = desc;
      localBean = desc.createEntityBean2(unmodifiable);
      localIdBinder = idBinder;
    }

    private void initPersistenceContext() {
      queryMode = ctx.queryMode();
      persistenceContext = (!readIdNormal) ? null : ctx.persistenceContext();
    }

    private void readId() throws SQLException {
      if (readId) {
        id = localIdBinder.readSet(ctx, localBean);
        if (id == null) {
          readIdNullBean();
        } else if (!temporalVersions) {
          readIdBean();
        }
      }
    }

    private void readIdBean() {
      // check the PersistenceContext to see if the bean already exists
      contextBean = (EntityBean) localDesc.contextPutIfAbsent(persistenceContext, id, localBean);
      if (contextBean == null) {
        // bean just added to the persistenceContext
        contextBean = localBean;
      } else {
        // bean already exists in persistenceContext
        usingContextBean = true;
        if (ctx.isLoadContextBean()) {
          // if explicitly set loadContextBean to true, then reload
          localBean = contextBean;
        } else if (!contextBean._ebean_getIntercept().isFullyLoadedBean()) {
          // reload if contextBean is partial object
          localBean = contextBean;
          // and switch to lazyLoad query mode in order not to overwrite
          // existing properties in SqlBeanLoad::load
          queryMode = Mode.LAZYLOAD_BEAN;
        } else {
          // ignore the DB data...
          localBean = null;
        }
      }
    }

    private void readIdNullBean() {
      // bean must be null...
      localBean = null;
      // ... but there may exist as reference bean in parent which has to be marked as deleted.
      if (parentBean != null && nodeBeanProp instanceof STreePropertyAssocOne) {
        contextBean = ((STreePropertyAssocOne)nodeBeanProp).valueAsEntityBean(parentBean);
        if (contextBean != null) {
          desc.markAsDeleted(contextBean);
          if (CoreLog.markedAsDeleted.isLoggable(DEBUG)) {
            CoreLog.markedAsDeleted.log(DEBUG, contextBean + " contextBean markedAsDeleted", new RuntimeException(contextBean + " contextBean markedAsDeleted"));
          }
        }
      }
    }

    private void initSqlLoadBean() {
      ctx.setCurrentPrefix(prefix, pathMap);
      ctx.propagateState(localBean);
      sqlBeanLoad = new SqlBeanLoad(ctx, localType, localBean, queryMode);
    }

    void loadProperties() {
      for (STreeProperty property : properties) {
        property.load(sqlBeanLoad);
      }
    }

    private void loadChildren() throws SQLException {
      if (localBean == null && queryMode == Mode.LAZYLOAD_MANY) {
        // batch lazy load many into existing contextBean
        localBean = contextBean;
        lazyLoadMany = true;
      }
      for (SqlTreeLoad child : children) {
        child.load(ctx, localBean, contextBean);
      }
    }

    private boolean isLazyLoadManyRoot() {
      return queryMode == Mode.LAZYLOAD_MANY && isRoot();
    }

    private EntityBean contextBean() {
      return contextBean;
    }

    private void postLoad() {
      if (!lazyLoadMany && localBean != null) {
        ctx.setCurrentPrefix(prefix, pathMap);
        if (readIdNormal) {
          createListProxies();
        }
        if (temporalMode == SpiQuery.TemporalMode.DRAFT) {
          localDesc.setDraft(localBean);
        }
        localDesc.postLoad(localBean);

        EntityBeanIntercept ebi = localBean._ebean_getIntercept();
        ebi.setPersistenceContext(persistenceContext);
        if (Mode.LAZYLOAD_BEAN == queryMode) {
          // Lazy Load does not reset the dirty state
          ebi.setLoadedLazy();
        } else if (readId) {
          // normal bean loading
          ebi.setLoaded();
        }

        if (disableLazyLoad) {
          // bean does not have an Id or is SqlSelect based
          ebi.setDisableLazyLoad(true);
          if (!partialObject) {
            ebi.setFullyLoadedBean(true);
          }
        } else if (!partialObject) {
          ebi.setFullyLoadedBean(true);
        } else if (readId && !usingContextBean) {
          // register for lazy loading if bean is new
          ctx.register(null, ebi);
        }

        if (ctx.isAutoTuneProfiling() && !disableLazyLoad) {
          // collect autoTune profiling for this bean...
          ctx.profileBean(ebi, prefix);
        }
      }
    }

    /**
     * Create lazy loading proxies for the Many's except for the one that is
     * included in the actual query.
     */
    private void createListProxies() {
      boolean forceNewReference = queryMode == Mode.REFRESH_BEAN;
      for (STreePropertyAssocMany many : localDesc.propsMany()) {
        if (many != loadingChildProperty) {
          if (!unmodifiable || ctx.includeSecondary(many.asMany())) {
            // create a proxy for the many (deferred fetching)
            BeanCollection<?> ref = many.createReference(localBean, forceNewReference);
            if (ref != null) {
              if (disableLazyLoad) {
                ref.setDisableLazyLoad(true);
              }
              if (!ref.isRegisteredWithLoadContext()) {
                ctx.register(many.asMany(), ref);
              }
            }
          }
        }
      }
    }

    private void setBeanToParent() {
      if (parentBean != null) {
        // set this back to the parentBean
        nodeBeanProp.setValue(parentBean, contextBean);
      }
    }

    private EntityBean complete() {
      if (!readIdNormal) {
        // a bean with no Id (never found in context)
        if (lazyLoadParentId != null) {
          ctx.setLazyLoadedChildBean(localBean, lazyLoadParentId);
        }
        return localBean;
      } else {
        if (lazyLoadParentId != null) {
          ctx.setLazyLoadedChildBean(contextBean, lazyLoadParentId);
        }
        return contextBean;
      }
    }

    private void initialise() throws SQLException {
      initLazyParent();
      initBeanType();
      initPersistenceContext();
      readId();
      initSqlLoadBean();
      loadProperties();
      loadChildren();
    }

    /**
     * Perform the load returning the loaded bean.
     */
    final EntityBean perform() throws SQLException {
      initialise();
      if (isLazyLoadManyRoot()) {
        return contextBean();
      }
      postLoad();
      setBeanToParent();
      return complete();
    }

    /**
     * Return true if this bean was already in the context. If already in the
     * context we need to check if it is already contained in the collection.
     */
    final boolean isContextBean() {
      return usingContextBean;
    }
  }

  /**
   * read the properties from the resultSet.
   */
  @Override
  public EntityBean load(DbReadContext ctx, EntityBean parentBean, EntityBean contextParent) throws SQLException {
    return createLoad(ctx, parentBean).perform();
  }

  /**
   * Create the loader with or without inheritance.
   */
  final Load createLoad(DbReadContext ctx, EntityBean parentBean) {
    return (inheritInfo != null) ? new LoadInherit(ctx, parentBean) : new Load(ctx, parentBean);
  }

  @Override
  public String toString() {
    return "SqlTreeLoadBean: " + desc;
  }

}
