package io.ebeaninternal.server.loadcontext;

import io.ebean.CacheMode;
import io.ebean.ProfileLocation;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.CallOrigin;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.ObjectGraphOrigin;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.LoadContext;
import io.ebeaninternal.api.LoadSecondaryQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuerySecondary;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of LoadContext.
 */
public final class DLoadContext implements LoadContext {

  private final SpiEbeanServer ebeanServer;
  private final BeanDescriptor<?> rootDescriptor;
  private final Map<String, DLoadBeanContext> beanMap = new HashMap<>();
  private final Map<String, DLoadManyContext> manyMap = new HashMap<>();
  private final DLoadBeanContext rootBeanContext;
  private final boolean asDraft;
  private final Timestamp asOf;
  private final Boolean readOnly;
  private final CacheMode useBeanCache;
  private final int defaultBatchSize;
  private final boolean disableLazyLoading;
  private final boolean disableReadAudit;
  private final boolean includeSoftDeletes;
  final boolean useDocStore;

  /**
   * The path relative to the root of the object graph.
   */
  private final String relativePath;
  private final ObjectGraphOrigin origin;
  private final String planLabel;
  private final ProfileLocation profileLocation;
  private final ProfilingListener profilingListener;
  private final Map<String, ObjectGraphNode> nodePathMap = new HashMap<>();
  private final PersistenceContext persistenceContext;
  private List<OrmQueryProperties> secQuery;
  private Object tenantId;

  /**
   * Construct for use with JSON marshalling (doc store).
   */
  public DLoadContext(BeanDescriptor<?> rootDescriptor, PersistenceContext persistenceContext) {
    this.useDocStore = true;
    this.rootDescriptor = rootDescriptor;
    this.ebeanServer = rootDescriptor.ebeanServer();
    this.persistenceContext = persistenceContext;
    this.origin = initOrigin();
    this.defaultBatchSize = 100;
    this.useBeanCache = CacheMode.OFF;
    this.asDraft = false;
    this.asOf = null;
    this.readOnly = false;
    this.disableLazyLoading = false;
    this.disableReadAudit = false;
    this.includeSoftDeletes = false;
    this.relativePath = null;
    this.planLabel = null;
    this.profileLocation = null;
    this.profilingListener = null;
    this.rootBeanContext = new DLoadBeanContext(this, rootDescriptor, null, null);
  }

  private ObjectGraphOrigin initOrigin() {
    CallOrigin callOrigin = ebeanServer.createCallOrigin();
    return new ObjectGraphOrigin(0, callOrigin, rootDescriptor.fullName());
  }

  public DLoadContext(OrmQueryRequest<?> request, SpiQuerySecondary secondaryQueries) {
    this.tenantId = request.tenantId();
    this.persistenceContext = request.persistenceContext();
    this.ebeanServer = request.server();
    this.defaultBatchSize = request.lazyLoadBatchSize();
    this.rootDescriptor = request.descriptor();

    SpiQuery<?> query = request.query();
    this.useDocStore = query.isUseDocStore();
    this.asOf = query.getAsOf();
    this.asDraft = query.isAsDraft();
    this.includeSoftDeletes = query.isIncludeSoftDeletes();
    this.readOnly = query.isReadOnly();
    this.disableReadAudit = query.isDisableReadAudit();
    this.disableLazyLoading = query.isDisableLazyLoading();
    this.useBeanCache = query.getUseBeanCache();
    this.profilingListener = query.getProfilingListener();
    this.planLabel = query.getPlanLabel();
    this.profileLocation = query.getProfileLocation();

    ObjectGraphNode parentNode = query.getParentNode();
    if (parentNode != null) {
      this.origin = parentNode.getOriginQueryPoint();
      this.relativePath = parentNode.getPath();
    } else {
      this.origin = null;
      this.relativePath = null;
    }

    // initialise rootBeanContext after origin and relativePath have been set
    this.rootBeanContext = new DLoadBeanContext(this, rootDescriptor, null, null);
    registerSecondaryQueries(secondaryQueries);
  }

  /**
   * Return the query plan label of the origin query.
   */
  String getPlanLabel() {
    return planLabel;
  }

  /**
   * Return the profile location of the origin query.
   */
  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }

  /**
   * Register the +query and +lazy secondary queries with their appropriate LoadBeanContext or LoadManyContext.
   */
  private void registerSecondaryQueries(SpiQuerySecondary secondaryQueries) {
    this.secQuery = secondaryQueries.getQueryJoins();
    if (secQuery != null) {
      for (OrmQueryProperties pathProperties : secQuery) {
        registerSecondaryQuery(pathProperties);
      }
    }
    List<OrmQueryProperties> lazyJoins = secondaryQueries.getLazyJoins();
    if (lazyJoins != null) {
      for (OrmQueryProperties lazyJoin : lazyJoins) {
        registerSecondaryQuery(lazyJoin);
      }
    }
  }

  /**
   * Setup the load context at this path with OrmQueryProperties which is
   * used to build the appropriate query for +query or +lazy loading.
   */
  private void registerSecondaryQuery(OrmQueryProperties props) {
    ElPropertyValue elGetValue = rootDescriptor.elGetValue(props.getPath());
    boolean many = elGetValue.beanProperty().containsMany();
    registerSecondaryNode(many, props);
  }

  boolean isBeanCacheGet() {
    return useBeanCache.isGet();
  }

  /**
   * Return the minimum batch size when using QueryIterator with query joins.
   */
  @Override
  public int getSecondaryQueriesMinBatchSize() {
    if (secQuery == null) {
      return -1;
    }
    int maxBatch = 0;
    for (OrmQueryProperties aSecQuery : secQuery) {
      int batchSize = aSecQuery.getBatchSize();
      if (batchSize == 0) {
        batchSize = 100;
      }
      maxBatch = Math.max(maxBatch, batchSize);
    }
    return maxBatch;
  }

  /**
   * Execute all the secondary queries.
   */
  @Override
  public void executeSecondaryQueries(OrmQueryRequest<?> parentRequest, boolean forEach) {
    if (secQuery != null) {
      for (OrmQueryProperties aSecQuery : secQuery) {
        LoadSecondaryQuery load = getLoadSecondaryQuery(aSecQuery.getPath());
        load.loadSecondaryQuery(parentRequest, forEach);
      }
    }
  }

  /**
   * Return the LoadBeanContext or LoadManyContext for the given path.
   */
  private LoadSecondaryQuery getLoadSecondaryQuery(String path) {
    LoadSecondaryQuery beanLoad = beanMap.get(path);
    return beanLoad == null ? manyMap.get(path) : beanLoad;
  }

  @Override
  public ObjectGraphNode getObjectGraphNode(String path) {
    return nodePathMap.computeIfAbsent(path, this::createObjectGraphNode);
  }

  private ObjectGraphNode createObjectGraphNode(String path) {
    if (relativePath != null) {
      if (path == null) {
        path = relativePath;
      } else {
        path = relativePath + "." + path;
      }
    }
    return new ObjectGraphNode(origin, path);
  }

  String getFullPath(String path) {
    if (relativePath == null) {
      return path;
    } else {
      return relativePath + "." + path;
    }
  }

  protected SpiEbeanServer getEbeanServer() {
    return ebeanServer;
  }

  /**
   * Return the parent state which defines the sharedInstance and readOnly status
   * which needs to be propagated to other beans and collections.
   */
  protected Boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  @Override
  public void register(String path, EntityBeanIntercept ebi) {
    getBeanContext(path).register(ebi);
  }

  @Override
  public void register(String path, EntityBeanIntercept ebi, BeanPropertyAssocOne<?> property) {
    getBeanContextWithInherit(path, property).register(ebi);
  }

  @Override
  public void register(String path, BeanPropertyAssocMany<?> many, BeanCollection<?> bc) {
    getManyContext(path, many).register(bc);
  }

  int batchSize(OrmQueryProperties props) {
    if (props == null) {
      return defaultBatchSize;
    }
    int batchSize = props.getBatchSize();
    return batchSize == 0 ? defaultBatchSize : batchSize;
  }

  DLoadBeanContext getBeanContext(String path) {
    if (path == null) {
      return rootBeanContext;
    }
    return beanMap.computeIfAbsent(path, p -> createBeanContext(p, null));
  }

  DLoadBeanContext getBeanContextWithInherit(String path, BeanPropertyAssocOne<?> property) {
    String key = path + ":" + property.targetDescriptor().name();
    return beanMap.computeIfAbsent(key, p -> createBeanContext(property, path, null));
  }

  private void registerSecondaryNode(boolean many, OrmQueryProperties props) {
    String path = props.getPath();
    if (many) {
      manyMap.put(path, createManyContext(path, props));
    } else {
      beanMap.put(path, createBeanContext(path, props));
    }
  }

  DLoadManyContext getManyContext(String path, BeanPropertyAssocMany<?> many) {
    return manyMap.computeIfAbsent(path, p -> createManyContext(p, many));
  }

  private DLoadManyContext createManyContext(String path, BeanPropertyAssocMany<?> many) {
    return new DLoadManyContext(this, many, path, null);
  }

  private DLoadManyContext createManyContext(String path, OrmQueryProperties queryProps) {
    BeanPropertyAssocMany<?> p = (BeanPropertyAssocMany<?>) getBeanProperty(rootDescriptor, path);
    return new DLoadManyContext(this, p, path, queryProps);
  }

  private DLoadBeanContext createBeanContext(String path, OrmQueryProperties queryProps) {
    BeanPropertyAssoc<?> p = (BeanPropertyAssoc<?>) getBeanProperty(rootDescriptor, path);
    return new DLoadBeanContext(this, p.targetDescriptor(), path, queryProps);
  }

  private DLoadBeanContext createBeanContext(BeanPropertyAssoc<?> property, String path, OrmQueryProperties queryProps) {
    return new DLoadBeanContext(this, property.targetDescriptor(), path, queryProps);
  }

  private BeanProperty getBeanProperty(BeanDescriptor<?> desc, String path) {
    return desc.findPropertyFromPath(path);
  }

  /**
   * Propagate the original query settings (draft, asOf etc) to the secondary queries.
   */
  void propagateQueryState(SpiQuery<?> query, boolean docStoreMapped) {
    if (useDocStore && docStoreMapped) {
      query.setUseDocStore(true);
    }
    if (readOnly != null) {
      query.setReadOnly(readOnly);
    }
    query.setDisableLazyLoading(disableLazyLoading);
    query.asOf(asOf);
    if (asDraft) {
      query.asDraft();
    }
    if (includeSoftDeletes) {
      query.setIncludeSoftDeletes();
    }
    if (disableReadAudit) {
      query.setDisableReadAuditing();
    }
    if (profilingListener != null) {
      query.setProfilingListener(profilingListener);
    }
    if (tenantId != null) {
      query.setTenantId(tenantId);
    }
  }
}
