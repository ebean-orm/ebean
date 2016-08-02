package com.avaje.ebeaninternal.server.loadcontext;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.LoadContext;
import com.avaje.ebeaninternal.api.LoadSecondaryQuery;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuerySecondary;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of LoadContext.
 */
public class DLoadContext implements LoadContext {

  private final SpiEbeanServer ebeanServer;

  private final BeanDescriptor<?> rootDescriptor;

  private final Map<String, DLoadBeanContext> beanMap = new HashMap<String, DLoadBeanContext>();
  private final Map<String, DLoadManyContext> manyMap = new HashMap<String, DLoadManyContext>();

  private final DLoadBeanContext rootBeanContext;

  private final boolean asDraft;
  private final Timestamp asOf;
  private final Boolean readOnly;
  private final boolean excludeBeanCache;
  private final int defaultBatchSize;
  private final boolean disableLazyLoading;
  private final boolean disableReadAudit;
  private final boolean includeSoftDeletes;
  protected final boolean useDocStore;

  /**
   * The path relative to the root of the object graph.
   */
  private final String relativePath;
  private final ObjectGraphOrigin origin;
  private final boolean useProfiling;

  private final Map<String, ObjectGraphNode> nodePathMap = new HashMap<String, ObjectGraphNode>();

  private PersistenceContext persistenceContext;

  private List<OrmQueryProperties> secQuery;

  /**
   * Construct for use with JSON marshalling (doc store).
   */
  public DLoadContext(BeanDescriptor<?> rootDescriptor, PersistenceContext persistenceContext) {

    this.useDocStore = true;
    this.rootDescriptor = rootDescriptor;
    this.ebeanServer = rootDescriptor.getEbeanServer();
    this.persistenceContext = persistenceContext;
    this.origin = initOrigin();
    this.defaultBatchSize = 100;
    this.excludeBeanCache = false;
    this.asDraft = false;
    this.asOf = null;
    this.readOnly = false;
    this.disableLazyLoading = false;
    this.disableReadAudit = false;
    this.includeSoftDeletes = false;
    this.relativePath = null;
    this.useProfiling = false;
    this.rootBeanContext = new DLoadBeanContext(this, rootDescriptor, null, defaultBatchSize, null);
  }

  private ObjectGraphOrigin initOrigin() {
    CallStack callStack = ebeanServer.createCallStack();
    return new ObjectGraphOrigin(0, callStack, rootDescriptor.getFullName());
  }

  public DLoadContext(OrmQueryRequest<?> request, SpiQuerySecondary secondaryQueries) {

    this.persistenceContext = request.getPersistenceContext();
    this.ebeanServer = request.getServer();
    this.defaultBatchSize = request.getLazyLoadBatchSize();
    this.rootDescriptor = request.getBeanDescriptor();

    SpiQuery<?> query = request.getQuery();
    this.useDocStore = query.isUseDocStore();
    this.asOf = query.getAsOf();
    this.asDraft = query.isAsDraft();
    this.includeSoftDeletes = query.isIncludeSoftDeletes();
    this.readOnly = query.isReadOnly();
    this.disableReadAudit = query.isDisableReadAudit();
    this.disableLazyLoading = query.isDisableLazyLoading();
    this.excludeBeanCache = query.isExcludeBeanCache();
    this.useProfiling = query.getProfilingListener() != null;

    ObjectGraphNode parentNode = query.getParentNode();
    if (parentNode != null) {
      this.origin = parentNode.getOriginQueryPoint();
      this.relativePath = parentNode.getPath();
    } else {
      this.origin = null;
      this.relativePath = null;
    }

    // initialise rootBeanContext after origin and relativePath have been set
    this.rootBeanContext = new DLoadBeanContext(this, rootDescriptor, null, defaultBatchSize, null);

    registerSecondaryQueries(secondaryQueries);
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

    ElPropertyValue elGetValue = rootDescriptor.getElGetValue(props.getPath());

    boolean many = elGetValue.getBeanProperty().containsMany();
    registerSecondaryNode(many, props);
  }

  protected boolean isExcludeBeanCache() {
    return excludeBeanCache;
  }

  /**
   * Return the minimum batch size when using QueryIterator with query joins.
   */
  public int getSecondaryQueriesMinBatchSize(int defaultQueryBatch) {

    if (secQuery == null) {
      return -1;
    }

    int maxBatch = 0;
    for (int i = 0; i < secQuery.size(); i++) {
      int batchSize = secQuery.get(i).getQueryFetchBatch();
      if (batchSize == 0) {
        batchSize = defaultQueryBatch;
      }
      maxBatch = Math.max(maxBatch, batchSize);
    }
    return maxBatch;
  }

  /**
   * Execute all the secondary queries.
   */
  public void executeSecondaryQueries(OrmQueryRequest<?> parentRequest, boolean forEach) {

    if (secQuery != null) {
      for (int i = 0; i < secQuery.size(); i++) {
        LoadSecondaryQuery load = getLoadSecondaryQuery(secQuery.get(i).getPath());
        load.loadSecondaryQuery(parentRequest, forEach);
      }
    }
  }

  /**
   * Return the LoadBeanContext or LoadManyContext for the given path.
   */
  private LoadSecondaryQuery getLoadSecondaryQuery(String path) {
    LoadSecondaryQuery beanLoad = beanMap.get(path);
    if (beanLoad == null) {
      beanLoad = manyMap.get(path);
    }
    return beanLoad;
  }

  public ObjectGraphNode getObjectGraphNode(String path) {

    ObjectGraphNode node = nodePathMap.get(path);
    if (node == null) {
      node = createObjectGraphNode(path);
      nodePathMap.put(path, node);
    }

    return node;
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

  protected String getFullPath(String path) {
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

  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  public void resetPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
    // clear the load contexts for beans and beanCollections
    for (DLoadBeanContext beanContext : beanMap.values()) {
      beanContext.clear();
    }
    for (DLoadManyContext manyContext : manyMap.values()) {
      manyContext.clear();
    }
    this.rootBeanContext.clear();
  }

  public void register(String path, EntityBeanIntercept ebi) {
    getBeanContext(path).register(ebi);
  }

  public void register(String path, BeanCollection<?> bc) {
    getManyContext(path).register(bc);
  }

  protected DLoadBeanContext getBeanContext(String path) {
    if (path == null) {
      return rootBeanContext;
    }
    DLoadBeanContext beanContext = beanMap.get(path);
    if (beanContext == null) {
      beanContext = createBeanContext(path, defaultBatchSize, null);
      beanMap.put(path, beanContext);
    }
    return beanContext;
  }

  private void registerSecondaryNode(boolean many, OrmQueryProperties props) {

    int batchSize;
    if (props.isQueryFetch()) {
      batchSize = 100;
    } else {
      int lazyJoinBatch = props.getLazyFetchBatch();
      batchSize = lazyJoinBatch > 0 ? lazyJoinBatch : defaultBatchSize;
    }

    String path = props.getPath();
    if (many) {
      manyMap.put(path, createManyContext(path, batchSize, props));
    } else {
      beanMap.put(path, createBeanContext(path, batchSize, props));
    }
  }

  protected DLoadManyContext getManyContext(String path) {
    if (path == null) {
      throw new RuntimeException("path is null?");
    }
    DLoadManyContext ctx = manyMap.get(path);
    if (ctx == null) {
      ctx = createManyContext(path, defaultBatchSize, null);
      manyMap.put(path, ctx);
    }
    return ctx;
  }

  private DLoadManyContext createManyContext(String path, int batchSize, OrmQueryProperties queryProps) {

    BeanPropertyAssocMany<?> p = (BeanPropertyAssocMany<?>) getBeanProperty(rootDescriptor, path);

    return new DLoadManyContext(this, p, path, batchSize, queryProps);
  }

  private DLoadBeanContext createBeanContext(String path, int batchSize, OrmQueryProperties queryProps) {

    BeanPropertyAssoc<?> p = (BeanPropertyAssoc<?>) getBeanProperty(rootDescriptor, path);
    BeanDescriptor<?> targetDescriptor = p.getTargetDescriptor();

    return new DLoadBeanContext(this, targetDescriptor, path, batchSize, queryProps);
  }

  private BeanProperty getBeanProperty(BeanDescriptor<?> desc, String path) {
    return desc.getBeanPropertyFromPath(path);
  }

  /**
   * Propagate the original query settings (draft, asOf etc) to the secondary queries.
   */
  public void propagateQueryState(SpiQuery<?> query, boolean docStoreMapped) {
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
    if (useProfiling) {
      query.setAutoTune(true);
    }
  }
}
