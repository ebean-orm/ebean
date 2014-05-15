package com.avaje.ebeaninternal.server.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.AdminAutofetch;
import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.CallableSql;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.Filter;
import com.avaje.ebean.FutureIds;
import com.avaje.ebean.FutureList;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.PagingList;
import com.avaje.ebean.Query;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.QueryResultVisitor;
import com.avaje.ebean.SqlFutureList;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxCallable;
import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.TxRunnable;
import com.avaje.ebean.TxScope;
import com.avaje.ebean.TxType;
import com.avaje.ebean.Update;
import com.avaje.ebean.ValuePair;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.bean.PersistenceContext.WithOption;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.EncryptKeyManager;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.meta.MetaBeanInfo;
import com.avaje.ebean.meta.MetaInfoManager;
import com.avaje.ebean.text.csv.CsvReader;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebeaninternal.api.LoadBeanRequest;
import com.avaje.ebeaninternal.api.LoadManyRequest;
import com.avaje.ebeaninternal.api.ScopeTrans;
import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.api.SpiEbeanPlugin;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.api.SpiQuery.Type;
import com.avaje.ebeaninternal.api.SpiSqlQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.server.autofetch.AutoFetchManager;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.deploy.BeanManager;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.DNativeQuery;
import com.avaje.ebeaninternal.server.deploy.DeployNamedQuery;
import com.avaje.ebeaninternal.server.deploy.DeployNamedUpdate;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.el.ElFilter;
import com.avaje.ebeaninternal.server.jmx.MAdminAutofetch;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;
import com.avaje.ebeaninternal.server.query.CQuery;
import com.avaje.ebeaninternal.server.query.CQueryEngine;
import com.avaje.ebeaninternal.server.query.CallableQueryIds;
import com.avaje.ebeaninternal.server.query.CallableQueryList;
import com.avaje.ebeaninternal.server.query.CallableQueryRowCount;
import com.avaje.ebeaninternal.server.query.CallableSqlQueryList;
import com.avaje.ebeaninternal.server.query.LimitOffsetPagedList;
import com.avaje.ebeaninternal.server.query.LimitOffsetPagingQuery;
import com.avaje.ebeaninternal.server.query.QueryFutureIds;
import com.avaje.ebeaninternal.server.query.QueryFutureList;
import com.avaje.ebeaninternal.server.query.QueryFutureRowCount;
import com.avaje.ebeaninternal.server.query.SqlQueryFutureList;
import com.avaje.ebeaninternal.server.querydefn.DefaultOrmQuery;
import com.avaje.ebeaninternal.server.querydefn.DefaultOrmUpdate;
import com.avaje.ebeaninternal.server.querydefn.DefaultRelationalQuery;
import com.avaje.ebeaninternal.server.text.csv.TCsvReader;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;
import com.avaje.ebeaninternal.server.transaction.TransactionScopeManager;
import com.avaje.ebeaninternal.util.ParamTypeHelper;
import com.avaje.ebeaninternal.util.ParamTypeHelper.TypeInfo;

/**
 * The default server side implementation of EbeanServer.
 */
public final class DefaultServer implements SpiEbeanServer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultServer.class);

  private static final int IGNORE_LEADING_ELEMENTS = 5;
  
  private static final String AVAJE_EBEAN = Ebean.class.getName().substring(0, 15);
  
  private final ServerConfig serverConfig;
  
  private final String serverName;

  private final DatabasePlatform databasePlatform;

  private final AdminAutofetch adminAutofetch;

  private final TransactionManager transactionManager;

  private final TransactionScopeManager transactionScopeManager;

  private final int maxCallStack;

  /**
   * Ebean defaults this to true but for EJB compatible behaviour set this to
   * false;
   */
  private final boolean rollbackOnChecked;
  
  /**
   * Handles the save, delete, updateSql CallableSql.
   */
  private final Persister persister;

  private final OrmQueryEngine queryEngine;

  private final RelationalQueryEngine relationalQueryEngine;

  private final ServerCacheManager serverCacheManager;

  private final BeanDescriptorManager beanDescriptorManager;

  private final DiffHelp diffHelp = new DiffHelp();

  private final AutoFetchManager autoFetchManager;

  private final CQueryEngine cqueryEngine;

  //@Deprecated
  private DdlGenerator ddlGenerator;

  private final ExpressionFactory expressionFactory;

  private final SpiBackgroundExecutor backgroundExecutor;

  private final DefaultBeanLoader beanLoader;

  private final EncryptKeyManager encryptKeyManager;

  private final JsonContext jsonContext;

  private final MetaInfoManager metaInfoManager;
  
  /**
   * The MBean name used to register Ebean.
   */
  private String mbeanName;

  /**
   * The MBeanServer Ebean is registered with.
   */
  private MBeanServer mbeanServer;

  /**
   * Flag set when the server has shutdown.
   */
  private boolean shutdown;
  
  /**
   * The default batch size for lazy loading beans or collections.
   */
  private int lazyLoadBatchSize;

  /** 
   * The query batch size 
   */
  private int queryBatchSize;

  /**
   * JDBC driver specific handling for JDBC batch execution.
   */
  private PstmtBatch pstmtBatch;

  /**
   * holds plugins (e.g. ddl generator) detected by the service loader
   */
  private List<SpiEbeanPlugin> ebeanPlugins;

  private final boolean collectQueryOrigins;
  
  private final boolean collectQueryStatsByNode;

  /**
   * Cache used to collect statistics based on ObjectGraphNode (used to highlight lazy loading origin points).
   */
  protected final ConcurrentHashMap<ObjectGraphNode, CObjectGraphNodeStatistics> objectGraphStats;

  /**
   * Create the DefaultServer.
   */
  public DefaultServer(InternalConfiguration config, ServerCacheManager cache) {

    this.serverConfig = config.getServerConfig();
    this.objectGraphStats = new ConcurrentHashMap<ObjectGraphNode, CObjectGraphNodeStatistics>();
    this.metaInfoManager = new DefaultMetaInfoManager(this);
    this.serverCacheManager = cache;
    this.pstmtBatch = config.getPstmtBatch();
    this.databasePlatform = config.getDatabasePlatform();
    this.backgroundExecutor = config.getBackgroundExecutor();
    
    this.serverName = serverConfig.getName();
    this.lazyLoadBatchSize = serverConfig.getLazyLoadBatchSize();
    this.queryBatchSize = serverConfig.getQueryBatchSize();
    this.cqueryEngine = config.getCQueryEngine();
    this.expressionFactory = config.getExpressionFactory();
    this.encryptKeyManager = serverConfig.getEncryptKeyManager();

    this.beanDescriptorManager = config.getBeanDescriptorManager();
    beanDescriptorManager.setEbeanServer(this);

    this.collectQueryOrigins = serverConfig.isCollectQueryOrigins();
    this.collectQueryStatsByNode = serverConfig.isCollectQueryStatsByNode();
    this.maxCallStack = GlobalProperties.getInt("ebean.maxCallStack", 5);

    this.rollbackOnChecked = GlobalProperties.getBoolean("ebean.transaction.rollbackOnChecked", true);
    this.transactionManager = config.getTransactionManager();
    this.transactionScopeManager = config.getTransactionScopeManager();

    this.persister = config.createPersister(this);
    this.queryEngine = config.createOrmQueryEngine();
    this.relationalQueryEngine = config.createRelationalQueryEngine();

    this.autoFetchManager = config.createAutoFetchManager(this);
    this.adminAutofetch = new MAdminAutofetch(autoFetchManager);

    this.beanLoader = new DefaultBeanLoader(this);
    this.jsonContext = config.createJsonContext(this);

    loadAndInitializePlugins(config);
    
    // Register with the JVM Shutdown hook
    ShutdownManager.registerEbeanServer(this);
  }

  protected void loadAndInitializePlugins(InternalConfiguration config) {
    
    List<SpiEbeanPlugin> spiPlugins = new ArrayList<SpiEbeanPlugin>();

    final Iterator<SpiEbeanPlugin> plugins = ServiceLoader.load(SpiEbeanPlugin.class).iterator();

    while (plugins.hasNext()) {
      SpiEbeanPlugin plugin = plugins.next();
      spiPlugins.add(plugin);
      plugin.setup(this, this.getDatabasePlatform(), config.getServerConfig());

      if (plugin instanceof DdlGenerator) {
        // backwards compatible
        ddlGenerator = (DdlGenerator)plugin;
      } 
    }

    ebeanPlugins = Collections.unmodifiableList(spiPlugins);
  }

  public List<SpiEbeanPlugin> getSpiEbeanPlugins() {
    return ebeanPlugins;
  }

  public void executePlugins(boolean online) {
    for(SpiEbeanPlugin plugin : ebeanPlugins) {
      plugin.execute(online);
    }
  }

  @Override
  public boolean isCollectQueryOrigins() {
    return collectQueryOrigins;
  }

  public int getLazyLoadBatchSize() {
    return lazyLoadBatchSize;
  }

  public PstmtBatch getPstmtBatch() {
    return pstmtBatch;
  }

  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  public DatabasePlatform getDatabasePlatform() {
    return databasePlatform;
  }
  
  @Override
  public MetaInfoManager getMetaInfoManager() {
    return metaInfoManager;
  }

  public BackgroundExecutor getBackgroundExecutor() {
    return backgroundExecutor;
  }

  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  public DdlGenerator getDdlGenerator() {
    return ddlGenerator;
  }

  public AdminAutofetch getAdminAutofetch() {
    return adminAutofetch;
  }

  public AutoFetchManager getAutoFetchManager() {
    return autoFetchManager;
  }

  /**
   * Run any initialisation required before registering with the ClusterManager.
   */
  public void initialise() {
    if (encryptKeyManager != null) {
      encryptKeyManager.initialise();
    }
    List<BeanDescriptor<?>> list = beanDescriptorManager.getBeanDescriptorList();
    for (int i = 0; i < list.size(); i++) {
      list.get(i).cacheInitialise();
    }

  }

  /**
   * Start any services after registering with the ClusterManager.
   */
  public void start() {
  }

  public void registerMBeans(MBeanServer mbeanServer, int uniqueServerId) {

    this.mbeanServer = mbeanServer;
    this.mbeanName = "Ebean:server=" + serverName + uniqueServerId;

    ObjectName autofetchName;
    try {
      autofetchName = new ObjectName(mbeanName + ",key=AutoFetch");
    } catch (Exception e) {
      String msg = "Failed to register the JMX beans for Ebean server [" + serverName + "].";
      logger.error(msg, e);
      return;
    }

    try {
      mbeanServer.registerMBean(adminAutofetch, autofetchName);

    } catch (InstanceAlreadyExistsException e) {
      // tomcat webapp reloading
      String msg = "JMX beans for Ebean server [" + serverName + "] already registered. Will try unregister/register" + e.getMessage();
      logger.warn(msg);
      try {
        mbeanServer.unregisterMBean(autofetchName);
        mbeanServer.registerMBean(adminAutofetch, autofetchName);

      } catch (Exception ae) {
        String amsg = "Unable to unregister/register the JMX beans for Ebean server [" + serverName + "].";
        logger.error(amsg, ae);
      }
    } catch (Exception e) {
      String msg = "Error registering MBean[" + mbeanName + "]";
      logger.error(msg, e);
    }
  }

  /**
   * Shutting down via JVM Shutdown hook.
   */
  public void shutdownManaged() {
    synchronized (this) {
      shutdownInternal(true, false);
    }
  }

  /**
   * Shutting down manually.
   */
  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
    synchronized (this) {
      // Unregister from JVM Shutdown hook
      ShutdownManager.unregisterEbeanServer(this);
      shutdownInternal(shutdownDataSource, deregisterDriver);
    }
  }

  /**
   * Shutdown the services like threads and DataSource.
   */
  private void shutdownInternal(boolean shutdownDataSource, boolean deregisterDriver) {

    logger.debug("Shutting down EbeanServer " + getName());
    if (shutdown) {
      // Already shutdown
      return;
    }
    try {
      if (mbeanServer != null) {
        mbeanServer.unregisterMBean(new ObjectName(mbeanName + ",key=AutoFetch"));
      }
    } catch (Exception e) {
      logger.error("Error unregistering Ebean " + mbeanName, e);
    }

    // shutdown autofetch profile collection
    autoFetchManager.shutdown();
    // shutdown background threads
    backgroundExecutor.shutdown();
    // shutdown DataSource (if its an Ebean one)
    transactionManager.shutdown(shutdownDataSource, deregisterDriver);
    shutdown = true;
  }
  
  /**
   * Return the server name.
   */
  public String getName() {
    return serverName;
  }

  public BeanState getBeanState(Object bean) {
    if (bean instanceof EntityBean) {
      return new DefaultBeanState((EntityBean) bean);
    }
    // Not an entity bean
    return null;
  }

  /**
   * Run the cache warming queries on all beans that have them defined.
   */
  public void runCacheWarming() {
    List<BeanDescriptor<?>> descList = beanDescriptorManager.getBeanDescriptorList();
    for (int i = 0; i < descList.size(); i++) {
      descList.get(i).runCacheWarming();
    }
  }

  public void runCacheWarming(Class<?> beanType) {
    BeanDescriptor<?> desc = beanDescriptorManager.getBeanDescriptor(beanType);
    if (desc == null) {
      String msg = "Is " + beanType + " an entity? Could not find a BeanDescriptor";
      throw new PersistenceException(msg);
    } else {
      desc.runCacheWarming();
    }
  }

  /**
   * Compile a query. Only valid for ORM queries.
   */
  public <T> CQuery<T> compileQuery(Query<T> query, Transaction t) {
    SpiOrmQueryRequest<T> qr = createQueryRequest(Type.SUBQUERY, query, t);
    OrmQueryRequest<T> orm = (OrmQueryRequest<T>) qr;
    return cqueryEngine.buildQuery(orm);
  }

  public CQueryEngine getQueryEngine() {
    return cqueryEngine;
  }

  public ServerCacheManager getServerCacheManager() {
    return serverCacheManager;
  }

  /**
   * Return the Profile Listener.
   */
  public AutoFetchManager getProfileListener() {
    return autoFetchManager;
  }

  /**
   * Return the Relational query engine.
   */
  public RelationalQueryEngine getRelationalQueryEngine() {
    return relationalQueryEngine;
  }

  public void refreshMany(Object parentBean, String propertyName, Transaction t) {

    beanLoader.refreshMany(checkEntityBean(parentBean), propertyName, t);
  }

  public void refreshMany(Object parentBean, String propertyName) {

    beanLoader.refreshMany(checkEntityBean(parentBean), propertyName);
  }

  public void loadMany(LoadManyRequest loadRequest) {

    beanLoader.loadMany(loadRequest);
  }

  public void loadMany(BeanCollection<?> bc, boolean onlyIds) {

    beanLoader.loadMany(bc, onlyIds);
  }

  public void refresh(Object bean) {

    beanLoader.refresh(checkEntityBean(bean));
  }

  public void loadBean(LoadBeanRequest loadRequest) {

    beanLoader.loadBean(loadRequest);
  }

  public void loadBean(EntityBeanIntercept ebi) {

    beanLoader.loadBean(ebi);
  }

  public Map<String, ValuePair> diff(Object a, Object b) {
    if (a == null) {
      return null;
    }

    BeanDescriptor<?> desc = getBeanDescriptor(a.getClass());
    return diffHelp.diff(a, b, desc);
  }

  /**
   * Process committed beans from another framework or server in another
   * cluster.
   * <p>
   * This notifies this instance of the framework that beans have been committed
   * externally to it. Either by another framework or clustered server. It needs
   * to maintain its cache and text indexes appropriately.
   * </p>
   */
  public void externalModification(TransactionEventTable tableEvent) {
    SpiTransaction t = transactionScopeManager.get();
    if (t != null) {
      t.getEvent().add(tableEvent);
    } else {
      transactionManager.externalModification(tableEvent);
    }
  }

  /**
   * Developer informing eBean that tables where modified outside of eBean.
   * Invalidate the cache etc as required.
   */
  public void externalModification(String tableName, boolean inserts, boolean updates, boolean deletes) {

    TransactionEventTable evt = new TransactionEventTable();
    evt.add(tableName, inserts, updates, deletes);

    externalModification(evt);
  }

  /**
   * Clear the query execution statistics.
   */
  public void clearQueryStatistics() {
    for (BeanDescriptor<?> desc : getBeanDescriptors()) {
      desc.clearQueryStatistics();
    }
  }

  /**
   * Create a new EntityBean bean.
   * <p>
   * This will generally return a subclass of the parameter 'type' which
   * additionally implements the EntityBean interface. That is, the returned
   * bean is typically an instance of a dynamically generated class.
   * </p>
   */
  @SuppressWarnings("unchecked")
  public <T> T createEntityBean(Class<T> type) {
    BeanDescriptor<T> desc = getBeanDescriptor(type);
    return (T) desc.createEntityBean();
  }

  /**
   * Return a Reference bean.
   * <p>
   * If a current transaction is active then this will check the Context of that
   * transaction to see if the bean is already loaded. If it is already loaded
   * then it will returned that object.
   * </p>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> T getReference(Class<T> type, Object id) {

    if (id == null) {
      throw new NullPointerException("The id is null");
    }

    BeanDescriptor desc = getBeanDescriptor(type);
    // convert the id type if necessary
    id = desc.convertId(id);

    Object ref = null;
    PersistenceContext ctx = null;

    SpiTransaction t = transactionScopeManager.get();
    if (t != null) {
      // first try the persistence context
      ctx = t.getPersistenceContext();
      ref = ctx.get(type, id);
    }

    if (ref == null) {
      InheritInfo inheritInfo = desc.getInheritInfo();
      if (inheritInfo != null) {
        // we actually need to do a query because
        // we don't know the type without the
        // discriminator value
        BeanProperty idProp = desc.getIdProperty();
        if (idProp == null) {
          throw new PersistenceException("No ID properties for this type? " + desc);          
        }
        
        // just select the id properties and
        // the discriminator column (auto added)
        Query<T> query = createQuery(type);
        query.select(idProp.getName()).setId(id);

        ref = query.findUnique();

      } else {
        // use the default reference options
        ref = desc.createReference(null, id);
      }

      if (ctx != null && (ref instanceof EntityBean)) {
        // Not putting a vanilla reference in the persistence context
        ctx.put(id, ref);
      }
    }
    return (T) ref;
  }

  /**
   * Creates a new Transaction that is NOT stored in TransactionThreadLocal. Use
   * this when you want a thread to have a second independent transaction.
   */
  public Transaction createTransaction() {

    return transactionManager.createTransaction(true, -1);
  }

  /**
   * Create a transaction additionally specify the Isolation level.
   * <p>
   * Note that this transaction is not stored in a thread local.
   * </p>
   */
  public Transaction createTransaction(TxIsolation isolation) {

    return transactionManager.createTransaction(true, isolation.getLevel());
  }

  public <T> T execute(TxCallable<T> c) {
    return execute(null, c);
  }

  public <T> T execute(TxScope scope, TxCallable<T> c) {
    ScopeTrans scopeTrans = createScopeTrans(scope);
    try {
      return c.call();

    } catch (Error e) {
      throw scopeTrans.caughtError(e);

    } catch (RuntimeException e) {
      throw scopeTrans.caughtThrowable(e);

    } finally {
      scopeTrans.onFinally();
    }
  }

  public void execute(TxRunnable r) {
    execute(null, r);
  }

  public void execute(TxScope scope, TxRunnable r) {
    ScopeTrans scopeTrans = createScopeTrans(scope);
    try {
      r.run();

    } catch (Error e) {
      throw scopeTrans.caughtError(e);

    } catch (RuntimeException e) {
      throw scopeTrans.caughtThrowable(e);

    } finally {
      scopeTrans.onFinally();
    }
  }

  /**
   * Determine whether to create a new transaction or not.
   * <p>
   * This will also potentially throw exceptions for MANDATORY and NEVER types.
   * </p>
   */
  private boolean createNewTransaction(SpiTransaction t, TxScope scope) {

    TxType type = scope.getType();
    switch (type) {
    case REQUIRED:
      return t == null;

    case REQUIRES_NEW:
      return true;

    case MANDATORY:
      if (t == null) {
        throw new PersistenceException("Transaction missing when MANDATORY");
      }
      return true;

    case NEVER:
      if (t != null) {
        throw new PersistenceException("Transaction exists for Transactional NEVER");
      }
      return false;

    case SUPPORTS:
      return false;

    case NOT_SUPPORTED:
      throw new RuntimeException("NOT_SUPPORTED should already be handled?");

    default:
      throw new RuntimeException("Should never get here?");
    }
  }

  public ScopeTrans createScopeTrans(TxScope txScope) {

    if (txScope == null) {
      // create a TxScope with default settings
      txScope = new TxScope();
    }

    SpiTransaction suspended = null;

    // get current transaction from ThreadLocal or equivalent
    SpiTransaction t = transactionScopeManager.get();

    boolean newTransaction;
    if (txScope.getType().equals(TxType.NOT_SUPPORTED)) {
      // Suspend existing transaction and
      // run without a transaction in scope
      newTransaction = false;
      suspended = t;
      t = null;

    } else {
      // create a new Transaction based on TxType and t
      newTransaction = createNewTransaction(t, txScope);

      if (newTransaction) {
        // suspend existing transaction (if there is one)
        suspended = t;

        // create a new transaction
        int isoLevel = -1;
        TxIsolation isolation = txScope.getIsolation();
        if (isolation != null) {
          isoLevel = isolation.getLevel();
        }
        t = transactionManager.createTransaction(true, isoLevel);
      }
    }

    // replace the current transaction ... ScopeTrans.onFinally()
    // has the job of restoring the suspended transaction
    transactionScopeManager.replace(t);

    return new ScopeTrans(rollbackOnChecked, newTransaction, t, txScope, suspended, transactionScopeManager);
  }

  /**
   * Returns the current transaction (or null) from the scope.
   */
  public SpiTransaction getCurrentServerTransaction() {
    return transactionScopeManager.get();
  }

  /**
   * Start a transaction.
   * <p>
   * Note that the transaction is stored in a ThreadLocal variable.
   * </p>
   */
  public Transaction beginTransaction() {
    // start an explicit transaction
    SpiTransaction t = transactionManager.createTransaction(true, -1);
    transactionScopeManager.set(t);
    return t;
  }

  /**
   * Start a transaction with a specific Isolation Level.
   * <p>
   * Note that the transaction is stored in a ThreadLocal variable.
   * </p>
   */
  public Transaction beginTransaction(TxIsolation isolation) {
    // start an explicit transaction
    SpiTransaction t = transactionManager.createTransaction(true, isolation.getLevel());
    transactionScopeManager.set(t);
    return t;
  }

  /**
   * Return the current transaction or null if there is not one currently in
   * scope.
   */
  public Transaction currentTransaction() {
    return transactionScopeManager.get();
  }

  /**
   * Commit the current transaction.
   */
  public void commitTransaction() {
    transactionScopeManager.commit();
  }

  /**
   * Rollback the current transaction.
   */
  public void rollbackTransaction() {
    transactionScopeManager.rollback();
  }

  /**
   * If the current transaction has already been committed do nothing otherwise
   * rollback the transaction.
   * <p>
   * Useful to put in a finally block to ensure the transaction is ended, rather
   * than a rollbackTransaction() in each catch block.
   * </p>
   * <p>
   * Code example:<br />
   * 
   * <pre>
   * &lt;code&gt;
   * Ebean.startTransaction();
   * try {
   * 	// do some fetching and or persisting
   * 
   * 	// commit at the end
   * 	Ebean.commitTransaction();
   * 
   * } finally {
   * 	// if commit didn't occur then rollback the transaction
   * 	Ebean.endTransaction();
   * }
   * &lt;/code&gt;
   * </pre>
   * 
   * </p>
   */
  public void endTransaction() {
    transactionScopeManager.end();
  }

  /**
   * return the next unique identity value.
   * <p>
   * Uses the BeanDescriptor deployment information to determine the sequence to
   * use.
   * </p>
   */
  public Object nextId(Class<?> beanType) {
    BeanDescriptor<?> desc = getBeanDescriptor(beanType);
    return desc.nextId(null);
  }

  @SuppressWarnings("unchecked")
  public <T> void sort(List<T> list, String sortByClause) {

    if (list == null) {
      throw new NullPointerException("list is null");
    }
    if (sortByClause == null) {
      throw new NullPointerException("sortByClause is null");
    }
    if (list.size() == 0) {
      // don't need to sort an empty list
      return;
    }
    // use first bean in the list as the correct type
    Class<T> beanType = (Class<T>) list.get(0).getClass();
    BeanDescriptor<T> beanDescriptor = getBeanDescriptor(beanType);
    if (beanDescriptor == null) {
      String m = "BeanDescriptor not found, is [" + beanType + "] an entity bean?";
      throw new PersistenceException(m);
    }
    beanDescriptor.sort(list, sortByClause);
  }

  public <T> Query<T> createQuery(Class<T> beanType) throws PersistenceException {
    return createQuery(beanType, null);
  }

  public <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery) throws PersistenceException {

    BeanDescriptor<?> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      throw new PersistenceException("Is " + beanType.getName() + " an Entity Bean? BeanDescriptor not found?");
    }
    DeployNamedQuery deployQuery = desc.getNamedQuery(namedQuery);
    if (deployQuery == null) {
      throw new PersistenceException("named query " + namedQuery + " was not found for " + desc.getFullName());
    }

    // this will parse the query
    return new DefaultOrmQuery<T>(beanType, this, expressionFactory, deployQuery);
  }

  public <T> Filter<T> filter(Class<T> beanType) {
    BeanDescriptor<T> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }
    return new ElFilter<T>(desc);
  }

  public <T> CsvReader<T> createCsvReader(Class<T> beanType) {
    BeanDescriptor<T> descriptor = getBeanDescriptor(beanType);
    if (descriptor == null) {
      throw new NullPointerException("BeanDescriptor for " + beanType.getName() + " not found");
    }
    return new TCsvReader<T>(this, descriptor);
  }

  public <T> Query<T> find(Class<T> beanType) {
    return createQuery(beanType);
  }

  public <T> Query<T> createQuery(Class<T> beanType, String query) {
    BeanDescriptor<?> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }
    switch (desc.getEntityType()) {
    case SQL:
      if (query != null) {
        throw new PersistenceException("You must used Named queries for this Entity " + desc.getFullName());
      }
      // use the "default" SqlSelect
      DeployNamedQuery defaultSqlSelect = desc.getNamedQuery("default");
      return new DefaultOrmQuery<T>(beanType, this, expressionFactory, defaultSqlSelect);

    default:
      return new DefaultOrmQuery<T>(beanType, this, expressionFactory, query);
    }
  }

  public <T> Update<T> createNamedUpdate(Class<T> beanType, String namedUpdate) {
    BeanDescriptor<?> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }

    DeployNamedUpdate deployUpdate = desc.getNamedUpdate(namedUpdate);
    if (deployUpdate == null) {
      throw new PersistenceException("named update " + namedUpdate + " was not found for " + desc.getFullName());
    }

    return new DefaultOrmUpdate<T>(beanType, this, desc.getBaseTable(), deployUpdate);
  }

  public <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate) {
    BeanDescriptor<?> desc = getBeanDescriptor(beanType);
    if (desc == null) {
      String m = beanType.getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }

    return new DefaultOrmUpdate<T>(beanType, this, desc.getBaseTable(), ormUpdate);
  }

  public SqlQuery createSqlQuery(String sql) {
    return new DefaultRelationalQuery(this, sql);
  }

  public SqlQuery createNamedSqlQuery(String namedQuery) {
    DNativeQuery nq = beanDescriptorManager.getNativeQuery(namedQuery);
    if (nq == null) {
      throw new PersistenceException("SqlQuery " + namedQuery + " not found.");
    }
    return new DefaultRelationalQuery(this, nq.getQuery());
  }

  public SqlUpdate createSqlUpdate(String sql) {
    return new DefaultSqlUpdate(this, sql);
  }

  public CallableSql createCallableSql(String sql) {
    return new DefaultCallableSql(this, sql);
  }

  public SqlUpdate createNamedSqlUpdate(String namedQuery) {
    DNativeQuery nq = beanDescriptorManager.getNativeQuery(namedQuery);
    if (nq == null) {
      throw new PersistenceException("SqlUpdate " + namedQuery + " not found.");
    }
    return new DefaultSqlUpdate(this, nq.getQuery());
  }

  public <T> T find(Class<T> beanType, Object uid) {

    return find(beanType, uid, null);
  }

  /**
   * Find a bean using its unique id.
   */
  public <T> T find(Class<T> beanType, Object id, Transaction t) {

    if (id == null) {
      throw new NullPointerException("The id is null");
    }

    Query<T> query = createQuery(beanType).setId(id);
    return findId(query, t);
  }

  private <T> SpiOrmQueryRequest<T> createQueryRequest(Type type, Query<T> query, Transaction t) {

    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    spiQuery.setType(type);

    BeanDescriptor<T> desc = beanDescriptorManager.getBeanDescriptor(spiQuery.getBeanType());
    spiQuery.setBeanDescriptor(desc);

    return createQueryRequest(desc, spiQuery, t);
  }

  public <T> SpiOrmQueryRequest<T> createQueryRequest(BeanDescriptor<T> desc, SpiQuery<T> query, Transaction t) {

    if (desc.isAutoFetchTunable() && !query.isSqlSelect()) {
      // its a tunable query
      if (autoFetchManager.tuneQuery(query)) {
        // was automatically tuned by Autofetch
      } else {
        // use deployment FetchType.LAZY/EAGER annotations
        // to define the 'default' select clause
        query.setDefaultSelectClause();
      }
    }

    if (query.selectAllForLazyLoadProperty()) {
      // we need to select all properties to ensure the lazy load property
      // was included (was not included by default or via autofetch).
      if (logger.isDebugEnabled()) {
        logger.debug("Using selectAllForLazyLoadProperty");
      }
    }

    if (true) {
      // if determine cost and no origin for Autofetch
      if (query.getParentNode() == null) {
        CallStack callStack = createCallStack();
        query.setOrigin(callStack);
      }
    }

    // determine extra joins required to support where clause
    // predicates on *ToMany properties
    if (query.initManyWhereJoins()) {
      // we need a sql distinct now
      query.setDistinct(true);
    }

    boolean allowOneManyFetch = true;
    if (Mode.LAZYLOAD_MANY.equals(query.getMode())) {
      allowOneManyFetch = false;

    } else if (query.hasMaxRowsOrFirstRow() && !query.isRawSql() && !query.isSqlSelect()) {
      // convert ALL fetch joins to Many's to be query joins
      // so that limit offset type SQL clauses work
      allowOneManyFetch = false;
    }

    query.convertManyFetchJoinsToQueryJoins(allowOneManyFetch, queryBatchSize);

    SpiTransaction serverTrans = (SpiTransaction) t;
    OrmQueryRequest<T> request = new OrmQueryRequest<T>(this, queryEngine, query, desc, serverTrans);

    BeanQueryAdapter queryAdapter = desc.getQueryAdapter();
    if (queryAdapter != null) {
      // adaption of the query probably based on the
      // current user
      queryAdapter.preQuery(request);
    }

    // the query hash after any tuning
    request.calculateQueryPlanHash();

    return request;
  }

  /**
   * Try to get the object out of the persistence context.
   */
  @SuppressWarnings("unchecked")
  private <T> T findIdCheckPersistenceContextAndCache(Transaction transaction, BeanDescriptor<T> beanDescriptor, SpiQuery<T> query) {

    SpiTransaction t = (SpiTransaction) transaction;
    if (t == null) {
      t = getCurrentServerTransaction();
    }
    PersistenceContext context = null;
    if (t != null) {
      // first look in the persistence context
      context = t.getPersistenceContext();
      if (context != null) {
        WithOption o = context.getWithOption(beanDescriptor.getBeanType(), query.getId());
        if (o != null) {
          if (o.isDeleted()) {
            // Bean was previously deleted in the same transaction / persistence context
            return null;
          }
          // Return the entity bean instance from the persistence context
          return (T) o.getBean();
        }
      }
    }

    if (!beanDescriptor.calculateUseCache(query.isUseBeanCache())) {
      // not using bean cache
      return null;
    }

    // Hit the L2 bean cache
    return beanDescriptor.cacheBeanGet(query, context);
  }
    
  @SuppressWarnings("unchecked")
  private <T> T findId(Query<T> query, Transaction t) {

    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    spiQuery.setType(Type.BEAN);

    BeanDescriptor<T> desc = beanDescriptorManager.getBeanDescriptor(spiQuery.getBeanType());
    spiQuery.setBeanDescriptor(desc);

    if (SpiQuery.Mode.NORMAL.equals(spiQuery.getMode()) && !spiQuery.isLoadBeanCache()) {
      // See if we can skip doing the fetch completely by getting the bean from the
      // persistence context or the bean cache
      T bean = findIdCheckPersistenceContextAndCache(t, desc, spiQuery);
      if (bean != null) {
        return bean;
      }
    }

    SpiOrmQueryRequest<T> request = createQueryRequest(desc, spiQuery, t);

    try {
      request.initTransIfRequired();

      T bean = (T) request.findId();
      request.endTransIfRequired();

      return bean;

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public <T> T findUnique(Query<T> query, Transaction t) {

    // actually a find by Id type of query...
    // ... perhaps with joins and cache hints?
    SpiQuery<T> q = (SpiQuery<T>) query;
    Object id = q.getId();
    if (id != null) {
      return findId(query, t);
    }

    BeanDescriptor<T> desc = beanDescriptorManager.getBeanDescriptor(q.getBeanType());

    T bean = desc.cacheNaturalKeyLookup(q, (SpiTransaction)t);
    if (bean != null) {
      return bean;
    }

    // a query that is expected to return either 0 or 1 rows
    List<T> list = findList(query, t);

    if (list.size() == 0) {
      return null;
      
    } else if (list.size() > 1) {
      throw new PersistenceException("Unique expecting 0 or 1 rows but got [" + list.size() + "]");
      
    } else {
      return list.get(0);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> Set<T> findSet(Query<T> query, Transaction t) {

    SpiOrmQueryRequest request = createQueryRequest(Type.SET, query, t);

    Object result = request.getFromQueryCache();
    if (result != null) {
      return (Set<T>) result;
    }

    try {
      request.initTransIfRequired();
      Set<T> set = (Set<T>) request.findSet();
      request.endTransIfRequired();

      return set;

    } catch (RuntimeException ex) {
      // String stackTrace = throwablePrinter.print(ex);
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> Map<?, T> findMap(Query<T> query, Transaction t) {

    SpiOrmQueryRequest request = createQueryRequest(Type.MAP, query, t);

    Object result = request.getFromQueryCache();
    if (result != null) {
      return (Map<?, T>) result;
    }

    try {
      request.initTransIfRequired();
      Map<?, T> map = (Map<?, T>) request.findMap();
      request.endTransIfRequired();

      return map;

    } catch (RuntimeException ex) {
      // String stackTrace = throwablePrinter.print(ex);
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public <T> int findRowCount(Query<T> query, Transaction t) {

    SpiQuery<T> copy = ((SpiQuery<T>) query).copy();
    return findRowCountWithCopy(copy, t);
  }

  public <T> int findRowCountWithCopy(Query<T> query, Transaction t) {

    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ROWCOUNT, query, t);
    try {
      request.initTransIfRequired();
      int rowCount = request.findRowCount();
      request.endTransIfRequired();

      return rowCount;

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public <T> List<Object> findIds(Query<T> query, Transaction t) {

    SpiQuery<T> copy = ((SpiQuery<T>) query).copy();

    return findIdsWithCopy(copy, t);
  }

  public <T> List<Object> findIdsWithCopy(Query<T> query, Transaction t) {

    SpiOrmQueryRequest<T> request = createQueryRequest(Type.ID_LIST, query, t);
    try {
      request.initTransIfRequired();
      List<Object> list = request.findIds();
      request.endTransIfRequired();

      return list;

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public <T> FutureRowCount<T> findFutureRowCount(Query<T> q, Transaction t) {

    SpiQuery<T> copy = ((SpiQuery<T>) q).copy();
    copy.setFutureFetch(true);

    Transaction newTxn = createTransaction();

    CallableQueryRowCount<T> call = new CallableQueryRowCount<T>(this, copy, newTxn);

    QueryFutureRowCount<T> queryFuture = new QueryFutureRowCount<T>(call);
    backgroundExecutor.execute(queryFuture.getFutureTask());

    return queryFuture;
  }

  public <T> FutureIds<T> findFutureIds(Query<T> query, Transaction t) {

    SpiQuery<T> copy = ((SpiQuery<T>) query).copy();
    copy.setFutureFetch(true);

    // this is the list we will put the id's in ... create it now so
    // it is available for other threads to read while the id query
    // is still executing (we don't need to wait for it to finish)
    List<Object> idList = Collections.synchronizedList(new ArrayList<Object>());
    copy.setIdList(idList);

    Transaction newTxn = createTransaction();

    CallableQueryIds<T> call = new CallableQueryIds<T>(this, copy, newTxn);
    QueryFutureIds<T> queryFuture = new QueryFutureIds<T>(call);

    backgroundExecutor.execute(queryFuture.getFutureTask());

    return queryFuture;
  }

  public <T> FutureList<T> findFutureList(Query<T> query, Transaction t) {

    SpiQuery<T> spiQuery = (SpiQuery<T>) query;
    spiQuery.setFutureFetch(true);

    // transfer the persistence content from the transaction
    if (spiQuery.getPersistenceContext() == null) {
      if (t != null) {
        spiQuery.setPersistenceContext(((SpiTransaction) t).getPersistenceContext());
      } else {
        SpiTransaction st = getCurrentServerTransaction();
        if (st != null) {
          spiQuery.setPersistenceContext(st.getPersistenceContext());
        }
      }
    }

    // Create a new transaction solely to execute the findList() at some future time
    Transaction newTxn = createTransaction();
    CallableQueryList<T> call = new CallableQueryList<T>(this, spiQuery, newTxn);
    QueryFutureList<T> queryFuture = new QueryFutureList<T>(call);
    backgroundExecutor.execute(queryFuture.getFutureTask());

    return queryFuture;
  }

  public <T> PagingList<T> findPagingList(Query<T> query, Transaction t, int pageSize) {

    SpiQuery<T> spiQuery = (SpiQuery<T>) query;

    // we want to use a single PersistenceContext to be used
    // for all the paging queries so we make sure there is a
    // PersistenceContext on the query
    PersistenceContext pc = spiQuery.getPersistenceContext();
    if (pc == null) {
      SpiTransaction currentTransaction = getCurrentServerTransaction();
      if (currentTransaction != null) {
        pc = currentTransaction.getPersistenceContext();
      }
      if (pc == null) {
        pc = new DefaultPersistenceContext();
      }
      spiQuery.setPersistenceContext(pc);
    }

    return new LimitOffsetPagingQuery<T>(this, spiQuery, pageSize);
  }

  @Override
  public <T> PagedList<T> findPagedList(Query<T> query, Transaction transaction, int pageIndex, int pageSize) {
    
    return new LimitOffsetPagedList<T>(this, (SpiQuery<T>)query, pageIndex, pageSize);
  }

  public <T> void findVisit(Query<T> query, QueryResultVisitor<T> visitor, Transaction t) {

    SpiOrmQueryRequest<T> request = createQueryRequest(Type.LIST, query, t);

    try {
      request.initTransIfRequired();
      request.findVisit(visitor);

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public <T> QueryIterator<T> findIterate(Query<T> query, Transaction t) {

    SpiOrmQueryRequest<T> request = createQueryRequest(Type.LIST, query, t);

    try {
      request.initTransIfRequired();
      return request.findIterate();
      // request.endTransIfRequired();

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> findList(Query<T> query, Transaction t) {

    SpiOrmQueryRequest<T> request = createQueryRequest(Type.LIST, query, t);

    Object result = request.getFromQueryCache();
    if (result != null) {
      return (List<T>) result;
    }

    try {
      request.initTransIfRequired();
      List<T> list = request.findList();
      request.endTransIfRequired();

      return list;

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public SqlRow findUnique(SqlQuery query, Transaction t) {

    // no findId() method for SqlQuery...
    // a query that is expected to return either 0 or 1 rows
    List<SqlRow> list = findList(query, t);

    if (list.size() == 0) {
      return null;

    } else if (list.size() > 1) {
      String m = "Unique expecting 0 or 1 rows but got [" + list.size() + "]";
      throw new PersistenceException(m);

    } else {
      return list.get(0);
    }
  }

  public SqlFutureList findFutureList(SqlQuery query, Transaction t) {

    SpiSqlQuery spiQuery = (SpiSqlQuery) query;
    spiQuery.setFutureFetch(true);

    Transaction newTxn = createTransaction();
    CallableSqlQueryList call = new CallableSqlQueryList(this, query, newTxn);

    FutureTask<List<SqlRow>> futureTask = new FutureTask<List<SqlRow>>(call);

    backgroundExecutor.execute(futureTask);

    return new SqlQueryFutureList(query, futureTask);
  }

  public List<SqlRow> findList(SqlQuery query, Transaction t) {

    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query, t);

    try {
      request.initTransIfRequired();
      List<SqlRow> list = request.findList();
      request.endTransIfRequired();

      return list;

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public Set<SqlRow> findSet(SqlQuery query, Transaction t) {

    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query, t);

    try {
      request.initTransIfRequired();
      Set<SqlRow> set = request.findSet();
      request.endTransIfRequired();

      return set;

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  public Map<?, SqlRow> findMap(SqlQuery query, Transaction t) {

    RelationalQueryRequest request = new RelationalQueryRequest(this, relationalQueryEngine, query, t);
    try {
      request.initTransIfRequired();
      Map<?, SqlRow> map = request.findMap();
      request.endTransIfRequired();

      return map;

    } catch (RuntimeException ex) {
      request.rollbackTransIfRequired();
      throw ex;
    }
  }

  /**
   * Persist the bean by either performing an insert or update.
   */
  public void save(Object bean) {
    save(bean, null);
  }

  /**
   * Save the bean with an explicit transaction.
   */
  public void save(Object bean, Transaction t) {
    persister.save(checkEntityBean(bean), t);
  }

  /**
   * Update the bean using the default 'updatesDeleteMissingChildren' setting. 
   */
  public void update(Object bean) {
    update(bean, null);
  }

  /**
   * Update the bean using the default 'updatesDeleteMissingChildren' setting. 
   */
  public void update(Object bean, Transaction t) {
    persister.update(checkEntityBean(bean), t);
  }

  /**
   * Update the bean specifying the deleteMissingChildren option.
   */
  public void update(Object bean, Transaction t, boolean deleteMissingChildren) {
    persister.update(checkEntityBean(bean), t, deleteMissingChildren);
  }

  /**
   * Update all beans in the collection.
   */
  public void update(Collection<?> beans) {
    update(beans, null);
  }
  
  /**
   * Update all beans in the collection with an explicit transaction.
   */
  public void update(Collection<?> beans, Transaction t) {

    if (beans == null || beans.isEmpty()) {
      // Nothing to update?
      return;
    }
    
    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;
      for (Object bean : beans) {
        update(checkEntityBean(bean), trans);
      }
      wrap.commitIfCreated();
      
    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }
  
  /**
   * Insert the bean.
   */
  public void insert(Object bean) {
    insert(bean, null);
  }

  /**
   * Insert the bean with a transaction.
   */
  public void insert(Object bean, Transaction t) {
    persister.insert(checkEntityBean(bean), t);
  }

  /**
   * Insert all beans in the collection.
   */
  public void insert(Collection<?> beans) {
    insert(beans, null);
  }
  
  /**
   * Insert all beans in the collection with a transaction.
   */
  public void insert(Collection<?> beans, Transaction t) {

    if (beans == null || beans.isEmpty()) {
      // Nothing to insert?
      return;
    }
    
    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;
      for (Object bean : beans) {
        persister.insert(checkEntityBean(bean), trans);
      }
      wrap.commitIfCreated();
      
    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  private EntityBean checkEntityBean(Object bean) {
    if (bean == null) {
      throw new IllegalArgumentException(Message.msg("bean.isnull"));
    }
    if (bean instanceof EntityBean == false) {
      throw new IllegalArgumentException("Was expecting an EntityBean but got a "+bean.getClass());
    }
    return (EntityBean)bean;
  }
  
  /**
   * Delete the associations (from the intersection table) of a ManyToMany given
   * the owner bean and the propertyName of the ManyToMany collection.
   * <p>
   * This returns the number of associations deleted.
   * </p>
   */
  public int deleteManyToManyAssociations(Object ownerBean, String propertyName) {
    return deleteManyToManyAssociations(ownerBean, propertyName, null);
  }

  /**
   * Delete the associations (from the intersection table) of a ManyToMany given
   * the owner bean and the propertyName of the ManyToMany collection.
   * <p>
   * This returns the number of associations deleted.
   * </p>
   */
  public int deleteManyToManyAssociations(Object ownerBean, String propertyName, Transaction t) {

    EntityBean owner = checkEntityBean(ownerBean);
    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;
      int rc = persister.deleteManyToManyAssociations(owner, propertyName, trans);
      wrap.commitIfCreated();
      return rc;

    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  /**
   * Save the associations of a ManyToMany given the owner bean and the
   * propertyName of the ManyToMany collection.
   */
  public void saveManyToManyAssociations(Object ownerBean, String propertyName) {
    saveManyToManyAssociations(ownerBean, propertyName, null);
  }

  /**
   * Save the associations of a ManyToMany given the owner bean and the
   * propertyName of the ManyToMany collection.
   */
  public void saveManyToManyAssociations(Object ownerBean, String propertyName, Transaction t) {

    EntityBean owner = checkEntityBean(ownerBean);
    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;

      persister.saveManyToManyAssociations(owner, propertyName, trans);

      wrap.commitIfCreated();

    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  public void saveAssociation(Object ownerBean, String propertyName) {
    saveAssociation(ownerBean, propertyName, null);
  }

  public void saveAssociation(Object ownerBean, String propertyName, Transaction t) {

    EntityBean owner = checkEntityBean(ownerBean);
    
    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;
      persister.saveAssociation(owner, propertyName, trans);

      wrap.commitIfCreated();

    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  /**
   * Perform an update or insert on each bean in the iterator. Returns the
   * number of beans that where saved.
   */
  public int save(Iterator<?> it) {
    return save(it, null);
  }

  /**
   * Perform an update or insert on each bean in the collection. Returns the
   * number of beans that where saved.
   */
  public int save(Collection<?> c) {
    return save(c.iterator(), null);
  }

  /**
   * Perform an update or insert on each bean in the collection. Returns the
   * number of beans that where saved.
   */
  public int save(Collection<?> c, Transaction t) {
    return save(c.iterator(), t);
  }
  
  /**
   * Save all beans in the iterator with an explicit transaction.
   */
  public int save(Iterator<?> it, Transaction t) {

    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;
      int saveCount = 0;
      while (it.hasNext()) {
        EntityBean bean = checkEntityBean(it.next());
        persister.save(bean, trans);
        saveCount++;
      }

      wrap.commitIfCreated();

      return saveCount;

    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  public int delete(Class<?> beanType, Object id) {
    return delete(beanType, id, null);
  }

  public int delete(Class<?> beanType, Object id, Transaction t) {

    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;
      int rowCount = persister.delete(beanType, id, trans);
      wrap.commitIfCreated();

      return rowCount;

    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  public void delete(Class<?> beanType, Collection<?> ids) {
    delete(beanType, ids, null);
  }

  public void delete(Class<?> beanType, Collection<?> ids, Transaction t) {

    TransWrapper wrap = initTransIfRequired(t);
    try {
      SpiTransaction trans = wrap.transaction;
      persister.deleteMany(beanType, ids, trans);
      wrap.commitIfCreated();

    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  /**
   * Delete the bean.
   */
  public void delete(Object bean) {
    delete(bean, null);
  }

  /**
   * Delete the bean with the explicit transaction.
   */
  public void delete(Object bean, Transaction t) {
    
    persister.delete(checkEntityBean(bean), t);
  }

  /**
   * Delete all the beans in the iterator.
   */
  public int delete(Iterator<?> it) {
    return delete(it, null);
  }

  /**
   * Delete all the beans in the collection.
   */
  public int delete(Collection<?> c) {
    return delete(c.iterator(), null);
  }

  /**
   * Delete all the beans in the iterator with an explicit transaction.
   */
  public int delete(Iterator<?> it, Transaction t) {

    TransWrapper wrap = initTransIfRequired(t);

    try {
      SpiTransaction trans = wrap.transaction;
      int deleteCount = 0;
      while (it.hasNext()) {
        EntityBean bean = checkEntityBean(it.next());
        persister.delete(bean, trans);
        deleteCount++;
      }

      wrap.commitIfCreated();

      return deleteCount;

    } catch (RuntimeException e) {
      wrap.rollbackIfCreated();
      throw e;
    }
  }

  /**
   * Execute the CallableSql with an explicit transaction.
   */
  public int execute(CallableSql callSql, Transaction t) {
    return persister.executeCallable(callSql, t);
  }

  /**
   * Execute the CallableSql.
   */
  public int execute(CallableSql callSql) {
    return execute(callSql, null);
  }

  /**
   * Execute the updateSql with an explicit transaction.
   */
  public int execute(SqlUpdate updSql, Transaction t) {
    return persister.executeSqlUpdate(updSql, t);
  }

  /**
   * Execute the updateSql.
   */
  public int execute(SqlUpdate updSql) {
    return execute(updSql, null);
  }

  /**
   * Execute the updateSql with an explicit transaction.
   */
  public int execute(Update<?> update, Transaction t) {
    return persister.executeOrmUpdate(update, t);
  }

  /**
   * Execute the orm update.
   */
  public int execute(Update<?> update) {
    return execute(update, null);
  }

  public <T> BeanManager<T> getBeanManager(Class<T> beanClass) {
    return beanDescriptorManager.getBeanManager(beanClass);
  }

  /**
   * Return all the BeanDescriptors.
   */
  public List<BeanDescriptor<?>> getBeanDescriptors() {
    return beanDescriptorManager.getBeanDescriptorList();
  }

  public List<MetaBeanInfo> getMetaBeanInfoList() {

    List<MetaBeanInfo> list = new ArrayList<MetaBeanInfo>();
    list.addAll(getBeanDescriptors());
    return list;
  }
  
  public void register(BeanPersistController c) {
    List<BeanDescriptor<?>> list = beanDescriptorManager.getBeanDescriptorList();
    for (int i = 0; i < list.size(); i++) {
      list.get(i).register(c);
    }
  }

  public void deregister(BeanPersistController c) {
    List<BeanDescriptor<?>> list = beanDescriptorManager.getBeanDescriptorList();
    for (int i = 0; i < list.size(); i++) {
      list.get(i).deregister(c);
    }
  }

  public boolean isSupportedType(java.lang.reflect.Type genericType) {

    TypeInfo typeInfo = ParamTypeHelper.getTypeInfo(genericType);
    if (typeInfo == null) {
      return false;
    }
    Class<?> beanType = typeInfo.getBeanType();
    if (JsonElement.class.isAssignableFrom(beanType)) {
      return true;
    }
    return getBeanDescriptor(typeInfo.getBeanType()) != null;
  }

  public Object getBeanId(Object bean) {
    EntityBean eb = checkEntityBean(bean);
    BeanDescriptor<?> desc = getBeanDescriptor(bean.getClass());
    if (desc == null) {
      String m = bean.getClass().getName() + " is NOT an Entity Bean registered with this server?";
      throw new PersistenceException(m);
    }

    return desc.getId(eb);
  }

  /**
   * Return the BeanDescriptor for a given type of bean.
   */
  public <T> BeanDescriptor<T> getBeanDescriptor(Class<T> beanClass) {
    return beanDescriptorManager.getBeanDescriptor(beanClass);
  }

  /**
   * Return the BeanDescriptor's for a given table name.
   */
  public List<BeanDescriptor<?>> getBeanDescriptors(String tableName) {
    return beanDescriptorManager.getBeanDescriptors(tableName);
  }

  /**
   * Return the BeanDescriptor using its unique id.
   */
  public BeanDescriptor<?> getBeanDescriptorById(String descriptorId) {
    return beanDescriptorManager.getBeanDescriptorById(descriptorId);
  }

  /**
   * Another server in the cluster sent this event so that we can inform local
   * BeanListeners of inserts updates and deletes that occurred remotely (on
   * another server in the cluster).
   */
  public void remoteTransactionEvent(RemoteTransactionEvent event) {
    transactionManager.remoteTransactionEvent(event);
  }

  /**
   * Create a transaction if one is not currently active in the
   * TransactionThreadLocal.
   * <p>
   * Returns a TransWrapper which contains the wasCreated flag. If this is true
   * then the transaction was created for this request in which case it will
   * need to be committed after the request has been processed.
   * </p>
   */
  TransWrapper initTransIfRequired(Transaction t) {

    if (t != null) {
      return new TransWrapper((SpiTransaction) t, false);
    }

    boolean wasCreated = false;
    SpiTransaction trans = transactionScopeManager.get();
    if (trans == null) {
      // create a transaction
      trans = transactionManager.createTransaction(false, -1);
      wasCreated = true;
    }
    return new TransWrapper(trans, wasCreated);
  }

  public SpiTransaction createServerTransaction(boolean isExplicit, int isolationLevel) {
    return transactionManager.createTransaction(isExplicit, isolationLevel);
  }

  public SpiTransaction createQueryTransaction() {
    return transactionManager.createQueryTransaction();
  }


  /**
   * Create a CallStack object.
   * <p>
   * This trims off the avaje ebean part of the stack trace so that the first
   * element in the CallStack should be application code.
   * </p>
   */
  public CallStack createCallStack() {

    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    // ignore the first 6 as they are always avaje stack elements
    int startIndex = IGNORE_LEADING_ELEMENTS;

    // find the first non-avaje stackElement
    for (; startIndex < stackTrace.length; startIndex++) {
      if (!stackTrace[startIndex].getClassName().startsWith(AVAJE_EBEAN)) {
        break;
      }
    }

    int stackLength = stackTrace.length - startIndex;
    if (stackLength > maxCallStack) {
      // maximum of maxCallStack stackTrace elements
      stackLength = maxCallStack;
    }

    // create the 'interesting' part of the stackTrace
    StackTraceElement[] finalTrace = new StackTraceElement[stackLength];
    for (int i = 0; i < stackLength; i++) {
      finalTrace[i] = stackTrace[i + startIndex];
    }

    if (stackLength < 1) {
      // this should not really happen
      throw new RuntimeException("StackTraceElement size 0?  stack: " + Arrays.toString(stackTrace));
    }

    return new CallStack(finalTrace);
  }

  public JsonContext createJsonContext() {
    // immutable thread safe so return shared instance
    return jsonContext;
  }

  
  @Override
  public void collectQueryStats(ObjectGraphNode node, long loadedBeanCount, long timeMicros) {
    
    if (collectQueryStatsByNode) {
      CObjectGraphNodeStatistics nodeStatistics = objectGraphStats.get(node);
      if (nodeStatistics == null) {
        // race condition here but I actually don't care too much if we miss a 
        // few early statistics - especially when the server is warming up etc
        nodeStatistics = new CObjectGraphNodeStatistics(node);
        objectGraphStats.put(node, nodeStatistics);
      }
      nodeStatistics.add(loadedBeanCount, timeMicros);
    }
  }
  
}
