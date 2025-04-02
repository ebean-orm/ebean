package io.ebeaninternal.server.loadcontext;

import io.ebean.CacheMode;
import io.ebean.ProfileLocation;
import io.ebean.bean.*;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.sql.Timestamp;
import java.util.*;

/**
 * Default implementation of LoadContext.
 */
public final class DLoadContext implements LoadContext {

  private final SpiEbeanServer ebeanServer;
  private final BeanDescriptor<?> rootDescriptor;
  private final Map<String, DLoadBeanContext> beanMap = new HashMap<>();
  private final Map<String, DLoadManyContext> manyMap = new HashMap<>();
  private final DLoadBeanContext rootBeanContext;
  private final Timestamp asOf;
  private final boolean unmodifiable;
  private final CacheMode useBeanCache;
  private final int defaultBatchSize;
  private final boolean disableLazyLoading;
  private final boolean includeSoftDeletes;

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
  private final Set<BeanProperty> secondaryProperties;

  /**
   * Construct for use with JSON marshalling (doc store).
   */
  public DLoadContext(BeanDescriptor<?> rootDescriptor, PersistenceContext persistenceContext) {
    this.rootDescriptor = rootDescriptor;
    this.ebeanServer = rootDescriptor.ebeanServer();
    this.persistenceContext = persistenceContext;
    this.origin = initOrigin();
    this.defaultBatchSize = 100;
    this.useBeanCache = CacheMode.OFF;
    this.asOf = null;
    this.unmodifiable = false;
    this.disableLazyLoading = false;
    this.includeSoftDeletes = false;
    this.relativePath = null;
    this.planLabel = null;
    this.profileLocation = null;
    this.profilingListener = null;
    this.rootBeanContext = new DLoadBeanContext(this, rootDescriptor, null, null);
    this.secondaryProperties = null;
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
    this.asOf = query.getAsOf();
    this.includeSoftDeletes = query.isIncludeSoftDeletes() && query.mode() == SpiQuery.Mode.NORMAL;
    this.unmodifiable = query.isUnmodifiable();
    this.disableLazyLoading = query.isDisableLazyLoading();
    this.useBeanCache = query.beanCacheMode();
    this.profilingListener = query.profilingListener();
    this.planLabel = query.planLabel();
    this.profileLocation = query.profileLocation();
    this.secondaryProperties = query.isUnmodifiable() ? new HashSet<>() : null;

    ObjectGraphNode parentNode = query.parentNode();
    if (parentNode != null) {
      this.origin = parentNode.origin();
      this.relativePath = parentNode.path();
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
  String planLabel() {
    return planLabel;
  }

  /**
   * Return the profile location of the origin query.
   */
  public ProfileLocation profileLocation() {
    return profileLocation;
  }

  /**
   * Register the +query and +lazy secondary queries with their appropriate LoadBeanContext or LoadManyContext.
   */
  private void registerSecondaryQueries(SpiQuerySecondary secondaryQueries) {
    this.secQuery = secondaryQueries.queryJoins();
    if (secQuery != null) {
      for (OrmQueryProperties pathProperties : secQuery) {
        registerSecondaryQuery(pathProperties);
      }
    }
    List<OrmQueryProperties> lazyJoins = secondaryQueries.lazyJoins();
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
  public int secondaryQueriesMinBatchSize() {
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
        LoadSecondaryQuery load = loadSecondaryQuery(aSecQuery.getPath());
        load.loadSecondaryQuery(parentRequest, forEach);
      }
    }
  }

  /**
   * Return the LoadBeanContext or LoadManyContext for the given path.
   */
  private LoadSecondaryQuery loadSecondaryQuery(String path) {
    LoadSecondaryQuery beanLoad = beanMap.get(path);
    return beanLoad == null ? manyMap.get(path) : beanLoad;
  }

  @Override
  public ObjectGraphNode objectGraphNode(String path) {
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

  String fullPath(String path) {
    if (relativePath == null) {
      return path;
    } else {
      return relativePath + "." + path;
    }
  }

  SpiEbeanServer server() {
    return ebeanServer;
  }

  @Override
  public PersistenceContext persistenceContext() {
    return persistenceContext;
  }

  @Override
  public void register(String path, EntityBeanIntercept ebi) {
    beanContext(path).register(ebi);
  }

  @Override
  public void register(String path, EntityBeanIntercept ebi, BeanPropertyAssocOne<?> property) {
    beanContextWithInherit(path, property).register(ebi);
  }

  @Override
  public void register(String path, BeanPropertyAssocMany<?> many, BeanCollection<?> bc) {
    manyContext(path, many).register(bc);
  }

  int batchSize(OrmQueryProperties props) {
    if (props == null) {
      return defaultBatchSize;
    }
    int batchSize = props.getBatchSize();
    return batchSize == 0 ? defaultBatchSize : batchSize;
  }

  DLoadBeanContext beanContext(String path) {
    if (path == null) {
      return rootBeanContext;
    }
    return beanMap.computeIfAbsent(path, p -> createBeanContext(p, null));
  }

  DLoadBeanContext beanContextWithInherit(String path, BeanPropertyAssocOne<?> property) {
    String key = path + ":" + property.targetDescriptor().name();
    return beanMap.computeIfAbsent(key, p -> createBeanContext(property, path));
  }

  private void registerSecondaryNode(boolean many, OrmQueryProperties props) {
    String path = props.getPath();
    if (many) {
      manyMap.put(path, createManyContext(path, props));
    } else {
      beanMap.put(path, createBeanContext(path, props));
    }
  }

  DLoadManyContext manyContext(String path, BeanPropertyAssocMany<?> many) {
    return manyMap.computeIfAbsent(path, p -> createManyContext(p, many));
  }

  private DLoadManyContext createManyContext(String path, BeanPropertyAssocMany<?> many) {
    return new DLoadManyContext(this, many, path, null);
  }

  private DLoadManyContext createManyContext(String path, OrmQueryProperties queryProps) {
    BeanPropertyAssocMany<?> p = (BeanPropertyAssocMany<?>) beanProperty(rootDescriptor, path);
    return new DLoadManyContext(this, p, path, queryProps);
  }

  private DLoadBeanContext createBeanContext(String path, OrmQueryProperties queryProps) {
    BeanPropertyAssoc<?> p = (BeanPropertyAssoc<?>) beanProperty(rootDescriptor, path);
    return new DLoadBeanContext(this, p.targetDescriptor(), path, queryProps);
  }

  private DLoadBeanContext createBeanContext(BeanPropertyAssoc<?> property, String path) {
    return new DLoadBeanContext(this, property.targetDescriptor(), path, null);
  }

  private BeanProperty beanProperty(BeanDescriptor<?> desc, String path) {
    return desc.findPropertyFromPath(path);
  }

  /**
   * Propagate the original query settings (draft, asOf etc) to the secondary queries.
   */
  void propagateQueryState(SpiQuery<?> query) {
    query.setUnmodifiable(unmodifiable);
    query.setDisableLazyLoading(disableLazyLoading);
    query.asOf(asOf);
    if (includeSoftDeletes) {
      query.setIncludeSoftDeletes();
    }
    if (profilingListener != null) {
      query.setProfilingListener(profilingListener);
    }
    if (tenantId != null) {
      query.setTenantId(tenantId);
    }
  }
}
