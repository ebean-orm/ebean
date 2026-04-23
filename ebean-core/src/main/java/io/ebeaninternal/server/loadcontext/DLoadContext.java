package io.ebeaninternal.server.loadcontext;

import io.ebean.CacheMode;
import io.ebean.ImmutableBeanCache;
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
  /**
   * Path based contexts used for +query secondary query execution.
   */
  private final Map<String, DLoadBeanContext> beanMap = new HashMap<>();
  /**
   * Type based contexts used for assoc-one lazy loading registration.
   */
  private final Map<String, DLoadBeanContext> lazyBeanMap = new HashMap<>();
  /**
   * Type based sets used when lazy loading is disabled but immutable cache population should still occur.
   */
  private final Map<String, Set<EntityBeanIntercept>> immutableRefMap = new HashMap<>();
  private final Map<Class<?>, ImmutableBeanCache<?>> immutableCaches;
  private final Map<String, DLoadManyContext> manyMap = new HashMap<>();
  private final DLoadBeanContext rootBeanContext;
  private final boolean asDraft;
  private final Timestamp asOf;
  private final boolean unmodifiable;
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
  boolean useReferences;
  private List<OrmQueryProperties> secQuery;
  private Object tenantId;
  private final Set<BeanProperty> secondaryProperties;

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
    this.unmodifiable = false;
    this.disableLazyLoading = false;
    this.disableReadAudit = false;
    this.includeSoftDeletes = false;
    this.relativePath = null;
    this.planLabel = null;
    this.profileLocation = null;
    this.profilingListener = null;
    this.immutableCaches = Collections.emptyMap();
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
    this.useDocStore = query.isUseDocStore();
    this.asOf = query.getAsOf();
    this.asDraft = query.isAsDraft();
    this.includeSoftDeletes = query.isIncludeSoftDeletes() && query.mode() == SpiQuery.Mode.NORMAL;
    this.unmodifiable = query.isUnmodifiable();
    this.disableReadAudit = query.isDisableReadAudit();
    this.disableLazyLoading = query.isDisableLazyLoading();
    this.useBeanCache = query.beanCacheMode();
    this.profilingListener = query.profilingListener();
    this.planLabel = query.planLabel();
    this.profileLocation = query.profileLocation();
    this.immutableCaches = query.immutableBeanCaches();
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

  @Override
  public void useReferences(boolean useReferences) {
    this.useReferences = useReferences;
  }

  /**
   * Setup the load context at this path with OrmQueryProperties which is
   * used to build the appropriate query for +query or +lazy loading.
   */
  private void registerSecondaryQuery(OrmQueryProperties props) {
    ElPropertyValue elGetValue = rootDescriptor.elGetValue(props.getPath());
    boolean many = elGetValue.beanProperty().containsMany();
    registerSecondaryNode(many, props);
    if (many && secondaryProperties != null) {
      secondaryProperties.add(elGetValue.beanProperty());
    }
  }

  @Override
  public boolean includeSecondary(BeanPropertyAssocMany<?> many) {
    return secondaryProperties != null && secondaryProperties.contains(many);
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
    DLoadBeanContext context = pathBeanContext(path);
    if (context != null) {
      context.register(ebi);
    } else {
      lazyBeanContext(descriptor(ebi)).register(ebi);
    }
  }

  @Override
  public void register(String path, EntityBeanIntercept ebi, BeanPropertyAssocOne<?> property) {
    DLoadBeanContext context = pathBeanContext(path);
    if (context != null) {
      context.register(ebi);
    } else {
      lazyBeanContext(property.targetDescriptor()).register(ebi);
    }
  }

  @Override
  public void register(String path, BeanPropertyAssocMany<?> many, BeanCollection<?> bc) {
    manyContext(path, many).register(bc);
  }

  @Override
  public void registerForImmutable(EntityBeanIntercept ebi) {
    if (immutableCaches.isEmpty()) {
      return;
    }
    BeanDescriptor<?> descriptor = descriptor(ebi);
    if (immutableCaches.containsKey(descriptor.type())) {
      immutableRefMap.computeIfAbsent(descriptor.fullName(), key -> new LinkedHashSet<>()).add(ebi);
    }
  }

  int batchSize(OrmQueryProperties props) {
    if (props == null) {
      return defaultBatchSize;
    }
    int batchSize = props.getBatchSize();
    return batchSize == 0 ? defaultBatchSize : batchSize;
  }

  private DLoadBeanContext pathBeanContext(String path) {
    if (path == null) {
      return rootBeanContext;
    }
    return beanMap.get(path);
  }

  private DLoadBeanContext lazyBeanContext(BeanDescriptor<?> descriptor) {
    return lazyBeanMap.computeIfAbsent(descriptor.fullName(), p -> new DLoadBeanContext(this, descriptor, descriptor.name(), null));
  }

  @Override
  public void populateFromImmutableCache() {
    if (immutableCaches.isEmpty()) {
      return;
    }
    for (Map.Entry<Class<?>, ImmutableBeanCache<?>> entry : immutableCaches.entrySet()) {
      BeanDescriptor<?> descriptor = rootDescriptor.descriptor(entry.getKey());
      if (descriptor == null) {
        continue;
      }
      DLoadBeanContext context = lazyBeanMap.get(descriptor.fullName());
      Map<Object, EntityBeanIntercept> interceptById = new LinkedHashMap<>();
      if (context != null) {
        for (EntityBeanIntercept ebi : context.bufferedBeans()) {
          Object id = descriptor.id(ebi.owner());
          if (id != null) {
            interceptById.put(id, ebi);
          }
        }
      }
      Set<EntityBeanIntercept> immutableRefs = immutableRefMap.get(descriptor.fullName());
      if (immutableRefs != null) {
        for (EntityBeanIntercept ebi : immutableRefs) {
          Object id = descriptor.id(ebi.owner());
          if (id != null) {
            interceptById.putIfAbsent(id, ebi);
          }
        }
      }
      if (!interceptById.isEmpty()) {
        Map<Object, ?> hits = entry.getValue().getAll(interceptById.keySet());
        for (Map.Entry<Object, ?> hit : hits.entrySet()) {
          EntityBeanIntercept ebi = interceptById.get(hit.getKey());
          Object bean = hit.getValue();
          if (ebi != null && bean instanceof EntityBean) {
            EntityBean cachedBean = (EntityBean) bean;
            descriptor.merge(cachedBean, ebi.owner());
            // TODO Move loaded-property propagation into BeanDescriptor.merge().
            markLoadedProperties(cachedBean, ebi);
            ebi.setLoadedFromCache(true);
            ebi.setLoadedLazy();
          }
        }
      }
    }
  }

  private void markLoadedProperties(EntityBean source, EntityBeanIntercept target) {
    Set<String> loadedProperties = source._ebean_getIntercept().loadedPropertyNames();
    if (loadedProperties == null) {
      return;
    }
    for (String property : loadedProperties) {
      target.setPropertyLoaded(property, true);
    }
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

  private BeanDescriptor<?> descriptor(EntityBeanIntercept ebi) {
    return rootDescriptor.descriptor(ebi.owner().getClass());
  }

  private BeanProperty beanProperty(BeanDescriptor<?> desc, String path) {
    return desc.findPropertyFromPath(path);
  }

  /**
   * Propagate the original query settings (draft, asOf etc) to the secondary queries.
   */
  void propagateQueryState(SpiQuery<?> query, boolean docStoreMapped) {
    if (useDocStore && docStoreMapped) {
      query.setUseDocStore(true);
    }
    query.setUnmodifiable(unmodifiable);
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
    if (!immutableCaches.isEmpty()) {
      for (ImmutableBeanCache<?> beanCache : immutableCaches.values()) {
        query.putImmutableBeanCache(beanCache);
      }
    }
    if (profilingListener != null) {
      query.setProfilingListener(profilingListener);
    }
    if (tenantId != null) {
      query.setTenantId(tenantId);
    }
  }
}
