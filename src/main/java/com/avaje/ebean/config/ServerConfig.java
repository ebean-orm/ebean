package com.avaje.ebean.config;

import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.PersistenceContextScope;
import com.avaje.ebean.annotation.Encrypted;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebean.event.*;
import com.avaje.ebean.meta.MetaInfoManager;
import com.avaje.ebean.util.ClassUtil;
import com.fasterxml.jackson.core.JsonFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The configuration used for creating a EbeanServer.
 * <p>
 * Used to programmatically construct an EbeanServer and optionally register it
 * with the Ebean singleton.
 * </p>
 * <p>
 * If you just use Ebean without this programmatic configuration Ebean will read
 * the ebean.properties file and take the configuration from there. This usually
 * includes searching the class path and automatically registering any entity
 * classes and listeners etc.
 * </p>
 * 
 * <pre>{@code
 * ServerConfig c = new ServerConfig();
 * c.setName("ordh2");
 * 
 * // read the ebean.properties and load
 * // those settings into this serverConfig object
 * c.loadFromProperties();
 * 
 * // generate DDL and run it
 * c.setDdlGenerate(true);
 * c.setDdlRun(true);
 * 
 * // add any classes found in the app.data package
 * c.addPackage("app.data");
 * 
 * // add the names of Jars that contain entities
 * c.addJar("myJarContainingEntities.jar");
 * c.addJar("someOtherJarContainingEntities.jar");
 * 
 * // register as the 'Default' server
 * c.setDefaultServer(true);
 * 
 * EbeanServer server = EbeanServerFactory.create(c);
 * 
 * }</pre>
 * 
 * @see EbeanServerFactory
 * 
 * @author emcgreal
 * @author rbygrave
 */
public class ServerConfig {

  /**
   * The EbeanServer name.
   */
  private String name;

  private ContainerConfig containerConfig;

  /**
   * The resource directory.
   */
  private String resourceDirectory;

  /**
   * The enhance log level. Used with subclass generation.
   */
  private int enhanceLogLevel;

  /**
   * Set to true to register this EbeanServer with the Ebean singleton.
   */
  private boolean register = true;

  /**
   * Set to true if this is the default/primary server.
   */
  private boolean defaultServer;

  /**
   * List of interesting classes such as entities, embedded, ScalarTypes,
   * Listeners, Finders, Controllers etc.
   */
  private List<Class<?>> classes = new ArrayList<Class<?>>();

  /**
   * The packages that are searched for interesting classes. Only used when
   * classes is empty/not explicitly specified.
   */
  private List<String> packages = new ArrayList<String>();

  /**
   * The names of Jar files that are searched for entities and other interesting
   * classes. Only used when classes is empty/not explicitly specified.
   */
  private List<String> searchJars = new ArrayList<String>();

  /**
   * Class name of a classPathReader implementation.
   */
  private String classPathReaderClassName;

  /** 
   * Config controlling the autofetch behaviour.
   */
  private AutofetchConfig autofetchConfig = new AutofetchConfig();

  /**
   * The JSON format used for DateTime types. Default to millis.
   */
  private JsonConfig.DateTime jsonDateTime = JsonConfig.DateTime.MILLIS;

  /** 
   * The database platform name. Used to imply a DatabasePlatform to use.  
   */
  private String databasePlatformName;

  /** 
   * The database platform. 
   */
  private DatabasePlatform databasePlatform;

  /**
   * For DB's using sequences this is the number of sequence values prefetched.
   */
  private int databaseSequenceBatchSize = 20;

  private boolean persistBatching;

  private int persistBatchSize = 20;

  /** 
   * The default batch size for lazy loading 
   */
  private int lazyLoadBatchSize = 10;

  /** 
   * The default batch size for 'query joins'.
   */
  private int queryBatchSize = 100;

  private boolean eagerFetchLobs;

  private boolean ddlGenerate;

  private boolean ddlRun;

  private boolean useJtaTransactionManager;

  /**
   * The external transaction manager (like Spring).
   */
  private ExternalTransactionManager externalTransactionManager;

  /**
   * Used to unwrap PreparedStatements to perform JDBC Driver specific functions
   */
  private PstmtDelegate pstmtDelegate;

  /** 
   * The data source (if programmatically provided). 
   */
  private DataSource dataSource;

  /** 
   * The data source config. 
   */
  private DataSourceConfig dataSourceConfig = new DataSourceConfig();

  /**
   * Set to true if the DataSource uses autoCommit.
   * <p>
   * Indicates that Ebean should use autoCommit friendly Transactions and TransactionManager.
   */
  private boolean autoCommitMode;
  
  /** 
   * The data source JNDI name if using a JNDI DataSource. 
   */
  private String dataSourceJndiName;

  /** 
   * The database boolean true value (typically either 1, T, or Y).
   */
  private String databaseBooleanTrue;

  /** 
   * The database boolean false value (typically either 0, F or N). 
   */
  private String databaseBooleanFalse;

  /** 
   * The naming convention. 
   */
  private NamingConvention namingConvention = new UnderscoreNamingConvention();

  /** 
   * Behaviour of update to include on the change properties. 
   */
  private boolean updateChangesOnly = true;
  
  /**
   * Default behaviour for updates when cascade save on a O2M or M2M to delete any missing children.
   */
  private boolean updatesDeleteMissingChildren = true;
  
  /**
   * Setting to indicate if UUID should be stored as binary(16) or varchar(40).
   */
  private boolean uuidStoreAsBinary;


  private List<BeanPersistController> persistControllers = new ArrayList<BeanPersistController>();
  private List<BeanPersistListener> persistListeners = new ArrayList<BeanPersistListener>();
  private List<BeanQueryAdapter> queryAdapters = new ArrayList<BeanQueryAdapter>();
  private List<BulkTableEventListener> bulkTableEventListeners = new ArrayList<BulkTableEventListener>();
  private List<ServerConfigStartup> configStartupListeners = new ArrayList<ServerConfigStartup>();
  private List<TransactionEventListener> transactionEventListeners = new ArrayList<TransactionEventListener>();

  private EncryptKeyManager encryptKeyManager;

  private EncryptDeployManager encryptDeployManager;

  private Encryptor encryptor;

  private DbEncrypt dbEncrypt;

  private ServerCacheFactory serverCacheFactory;

  private ServerCacheManager serverCacheManager;

  private boolean collectQueryStatsByNode = true;

  private boolean collectQueryOrigins = true;

  /**
   * The default PersistenceContextScope used if one is not explicitly set on a query.
   */
  private PersistenceContextScope persistenceContextScope = PersistenceContextScope.TRANSACTION;

  private JsonFactory jsonFactory;

  private boolean localTimeWithNanos;

  private boolean durationWithNanos;

  private int maxCallStack = 5;

  private boolean transactionRollbackOnChecked = true;

  private boolean registerJmxMBeans = true;

  // configuration for the background executor service (thread pool)

  private int backgroundExecutorSchedulePoolSize = 1;
  private int backgroundExecutorCorePoolSize = 1;
  private int backgroundExecutorMaxPoolSize = 8;
  private int backgroundExecutorIdleSecs = 60;
  private int backgroundExecutorShutdownSecs = 30;

  // defaults for the L2 bean caching

  private int cacheWarmingDelay = 30;
  private int cacheMaxSize = 10000;
  private int cacheMaxIdleTime = 600;
  private int cacheMaxTimeToLive = 60*60*6;

  // defaults for the L2 query caching

  private int queryCacheMaxSize = 1000;
  private int queryCacheMaxIdleTime = 600;
  private int queryCacheMaxTimeToLive = 60*60*6;

  /**
   * Construct a Server Configuration for programmatically creating an EbeanServer.
   */
  public ServerConfig() {

  }
  
  /**
   * Return the Jackson JsonFactory to use.
   * <p>
   * If not set a default implmentation will be used.
   */
  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Set the Jackson JsonFactory to use.
   * <p>
   * If not set a default implmentation will be used.
   */
  public void setJsonFactory(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  /**
   * Return the JSON format used for DateTime types.
   */
  public JsonConfig.DateTime getJsonDateTime() {
    return jsonDateTime;
  }

  /**
   * Set the JSON format to use for DateTime types.
   */
  public void setJsonDateTime(JsonConfig.DateTime jsonDateTime) {
    this.jsonDateTime = jsonDateTime;
  }

  /**
   * Return the name of the EbeanServer.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the EbeanServer.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Return the container / clustering configuration.
   * <p/>
   * The container holds all the EbeanServer instances and provides clustering communication
   * services to all the EbeanServer instances.
   */
  public ContainerConfig getContainerConfig() {
    return containerConfig;
  }

  /**
   * Set the container / clustering configuration.
   * <p/>
   * The container holds all the EbeanServer instances and provides clustering communication
   * services to all the EbeanServer instances.
   */
  public void setContainerConfig(ContainerConfig containerConfig) {
    this.containerConfig = containerConfig;
  }

  /**
   * Return true if this server should be registered with the Ebean singleton
   * when it is created.
   * <p>
   * By default this is set to true.
   * </p>
   */
  public boolean isRegister() {
    return register;
  }

  /**
   * Set to false if you do not want this server to be registered with the Ebean
   * singleton when it is created.
   * <p>
   * By default this is set to true.
   * </p>
   */
  public void setRegister(boolean register) {
    this.register = register;
  }

  /**
   * Return true if this server should be registered as the "default" server
   * with the Ebean singleton.
   * <p>
   * This is only used when {@link #setRegister(boolean)} is also true.
   * </p>
   */
  public boolean isDefaultServer() {
    return defaultServer;
  }

  /**
   * Set true if this EbeanServer should be registered as the "default" server
   * with the Ebean singleton.
   * <p>
   * This is only used when {@link #setRegister(boolean)} is also true.
   * </p>
   */
  public void setDefaultServer(boolean defaultServer) {
    this.defaultServer = defaultServer;
  }

  /**
   * Returns true if by default JDBC batching is used for persisting or deleting
   * beans.
   * <p>
   * With this Ebean will batch up persist requests and use the JDBC batch api.
   * This is a performance optimisation designed to reduce the network chatter.
   * </p>
   */
  public boolean isPersistBatching() {
    return persistBatching;
  }

  /**
   * Set to true if you what to use JDBC batching for persisting and deleting
   * beans.
   * <p>
   * With this Ebean will batch up persist requests and use the JDBC batch api.
   * This is a performance optimisation designed to reduce the network chatter.
   * </p>
   */
  public void setPersistBatching(boolean persistBatching) {
    this.persistBatching = persistBatching;
  }

  /**
   * Use setPersistBatching() instead.
   * 
   * @deprecated
   */
  public void setUsePersistBatching(boolean persistBatching) {
    this.persistBatching = persistBatching;
  }

  /**
   * Return the batch size used for JDBC batching. This defaults to 20.
   */
  public int getPersistBatchSize() {
    return persistBatchSize;
  }

  /**
   * Set the batch size used for JDBC batching. If unset this defaults to 20.
   */
  public void setPersistBatchSize(int persistBatchSize) {
    this.persistBatchSize = persistBatchSize;
  }

  /**
   * Return the default batch size for lazy loading of beans and collections.
   */
  public int getLazyLoadBatchSize() {
    return lazyLoadBatchSize;
  }

  /**
   * Gets the query batch size.
   * 
   * @return the query batch size
   */
  public int getQueryBatchSize() {
    return queryBatchSize;
  }

  /**
   * Sets the query batch size.
   * 
   * @param queryBatchSize
   *          the new query batch size
   */
  public void setQueryBatchSize(int queryBatchSize) {
    this.queryBatchSize = queryBatchSize;
  }

  /**
   * Set the default batch size for lazy loading.
   * <p>
   * This is the number of beans or collections loaded when lazy loading is
   * invoked by default.
   * </p>
   * <p>
   * The default value is for this is 1 (load 1 bean or collection).
   * </p>
   * <p>
   * You can explicitly control the lazy loading batch size for a given join on
   * a query using +lazy(batchSize) or JoinConfig.
   * </p>
   */
  public void setLazyLoadBatchSize(int lazyLoadBatchSize) {
    this.lazyLoadBatchSize = lazyLoadBatchSize;
  }

  /**
   * Set the number of sequences to fetch/preallocate when using DB sequences.
   * <p>
   * This is a performance optimisation to reduce the number times Ebean
   * requests a sequence to be used as an Id for a bean (aka reduce network
   * chatter).
   * </p>
   */
  public void setDatabaseSequenceBatchSize(int databaseSequenceBatchSize) {
    this.databaseSequenceBatchSize = databaseSequenceBatchSize;
  }

  /**
   * Return true if we are running in a JTA Transaction manager.
   */
  public boolean isUseJtaTransactionManager() {
    return useJtaTransactionManager;
  }

  /**
   * Set to true if we are running in a JTA Transaction manager.
   */
  public void setUseJtaTransactionManager(boolean useJtaTransactionManager) {
    this.useJtaTransactionManager = useJtaTransactionManager;
  }

  /**
   * Return the external transaction manager.
   */
  public ExternalTransactionManager getExternalTransactionManager() {
    return externalTransactionManager;
  }

  /**
   * Set the external transaction manager.
   */
  public void setExternalTransactionManager(ExternalTransactionManager externalTransactionManager) {
    this.externalTransactionManager = externalTransactionManager;
  }

  /**
   * Return the ServerCacheFactory.
   */
  public ServerCacheFactory getServerCacheFactory() {
    return serverCacheFactory;
  }

  /**
   * Set the ServerCacheFactory to use.
   */
  public void setServerCacheFactory(ServerCacheFactory serverCacheFactory) {
    this.serverCacheFactory = serverCacheFactory;
  }

  /**
   * Return the ServerCacheManager.
   */
  public ServerCacheManager getServerCacheManager() {
    return serverCacheManager;
  }

  /**
   * Set the ServerCacheManager to use.
   */
  public void setServerCacheManager(ServerCacheManager serverCacheManager) {
    this.serverCacheManager = serverCacheManager;
  }

  /**
   * Return the log level used for "subclassing" enhancement.
   */
  public int getEnhanceLogLevel() {
    return enhanceLogLevel;
  }

  /**
   * Set the log level used for "subclassing" enhancement.
   */
  public void setEnhanceLogLevel(int enhanceLogLevel) {
    this.enhanceLogLevel = enhanceLogLevel;
  }

  /**
   * Return true if LOB's should default to fetch eager.
   * By default this is set to false and LOB's must be explicitly fetched.
   */
  public boolean isEagerFetchLobs() {
    return eagerFetchLobs;
  }

  /**
   * Set to true if you want LOB's to be fetch eager by default.
   * By default this is set to false and LOB's must be explicitly fetched.
   */
  public void setEagerFetchLobs(boolean eagerFetchLobs) {
    this.eagerFetchLobs = eagerFetchLobs;
  }

  /**
   * Return the max call stack to use for origin location.
   */
  public int getMaxCallStack() {
    return maxCallStack;
  }

  /**
   * Set the max call stack to use for origin location.
   */
  public void setMaxCallStack(int maxCallStack) {
    this.maxCallStack = maxCallStack;
  }

  /**
   * Return true if transactions should rollback on checked exceptions.
   */
  public boolean isTransactionRollbackOnChecked() {
    return transactionRollbackOnChecked;
  }

  /**
   * Set to true if transactions should by default rollback on checked exceptions.
   */
  public void setTransactionRollbackOnChecked(boolean transactionRollbackOnChecked) {
    this.transactionRollbackOnChecked = transactionRollbackOnChecked;
  }

  /**
   * Return true if the server should register JMX MBeans.
   */
  public boolean isRegisterJmxMBeans() {
    return registerJmxMBeans;
  }

  /**
   * Set if the server should register JMX MBeans.
   */
  public void setRegisterJmxMBeans(boolean registerJmxMBeans) {
    this.registerJmxMBeans = registerJmxMBeans;
  }

  /**
   * Return the Background executor schedule pool size. Defaults to 1.
   */
  public int getBackgroundExecutorSchedulePoolSize() {
    return backgroundExecutorSchedulePoolSize;
  }

  /**
   * Set the Background executor schedule pool size.
   */
  public void setBackgroundExecutorSchedulePoolSize(int backgroundExecutorSchedulePoolSize) {
    this.backgroundExecutorSchedulePoolSize = backgroundExecutorSchedulePoolSize;
  }

  /**
   * Return the Background executor core pool size.
   */
  public int getBackgroundExecutorCorePoolSize() {
    return backgroundExecutorCorePoolSize;
  }

  /**
   * Set the Background executor core pool size.
   */
  public void setBackgroundExecutorCorePoolSize(int backgroundExecutorCorePoolSize) {
    this.backgroundExecutorCorePoolSize = backgroundExecutorCorePoolSize;
  }

  /**
   * Return the Background executor max pool size.
   */
  public int getBackgroundExecutorMaxPoolSize() {
    return backgroundExecutorMaxPoolSize;
  }

  /**
   * Set the Background executor max pool size.
   */
  public void setBackgroundExecutorMaxPoolSize(int backgroundExecutorMaxPoolSize) {
    this.backgroundExecutorMaxPoolSize = backgroundExecutorMaxPoolSize;
  }

  /**
   * Return the Background executor idle seconds.
   */
  public int getBackgroundExecutorIdleSecs() {
    return backgroundExecutorIdleSecs;
  }

  /**
   * Set the Background executor idle seconds.
   */
  public void setBackgroundExecutorIdleSecs(int backgroundExecutorIdleSecs) {
    this.backgroundExecutorIdleSecs = backgroundExecutorIdleSecs;
  }

  /**
   * Return the Background executor shutdown seconds. This is the time allowed for the pool to shutdown nicely
   * before it is forced shutdown.
   */
  public int getBackgroundExecutorShutdownSecs() {
    return backgroundExecutorShutdownSecs;
  }

  /**
   * Set the Background executor shutdown seconds. This is the time allowed for the pool to shutdown nicely
   * before it is forced shutdown.
   */
  public void setBackgroundExecutorShutdownSecs(int backgroundExecutorShutdownSecs) {
    this.backgroundExecutorShutdownSecs = backgroundExecutorShutdownSecs;
  }

  /**
   * Return the cache warming delay in seconds.
   */
  public int getCacheWarmingDelay() {
    return cacheWarmingDelay;
  }

  /**
   * Set the cache warming delay in seconds.
   */
  public void setCacheWarmingDelay(int cacheWarmingDelay) {
    this.cacheWarmingDelay = cacheWarmingDelay;
  }

  /**
   * Return the L2 cache default max size.
   */
  public int getCacheMaxSize() {
    return cacheMaxSize;
  }

  /**
   * Set the L2 cache default max size.
   */
  public void setCacheMaxSize(int cacheMaxSize) {
    this.cacheMaxSize = cacheMaxSize;
  }

  /**
   * Return the L2 cache default max idle time in seconds.
   */
  public int getCacheMaxIdleTime() {
    return cacheMaxIdleTime;
  }

  /**
   * Set the L2 cache default max idle time in seconds.
   */
  public void setCacheMaxIdleTime(int cacheMaxIdleTime) {
    this.cacheMaxIdleTime = cacheMaxIdleTime;
  }

  /**
   * Return the L2 cache default max time to live in seconds.
   */
  public int getCacheMaxTimeToLive() {
    return cacheMaxTimeToLive;
  }

  /**
   * Set the L2 cache default max time to live in seconds.
   */
  public void setCacheMaxTimeToLive(int cacheMaxTimeToLive) {
    this.cacheMaxTimeToLive = cacheMaxTimeToLive;
  }

  /**
   * Return the L2 query cache default max size.
   */
  public int getQueryCacheMaxSize() {
    return queryCacheMaxSize;
  }

  /**
   * Set the L2 query cache default max size.
   */
  public void setQueryCacheMaxSize(int queryCacheMaxSize) {
    this.queryCacheMaxSize = queryCacheMaxSize;
  }

  /**
   * Return the L2 query cache default max idle time in seconds.
   */
  public int getQueryCacheMaxIdleTime() {
    return queryCacheMaxIdleTime;
  }

  /**
   * Set the L2 query cache default max idle time in seconds.
   */
  public void setQueryCacheMaxIdleTime(int queryCacheMaxIdleTime) {
    this.queryCacheMaxIdleTime = queryCacheMaxIdleTime;
  }

  /**
   * Return the L2 query cache default max time to live in seconds.
   */
  public int getQueryCacheMaxTimeToLive() {
    return queryCacheMaxTimeToLive;
  }

  /**
   * Set the L2 query cache default max time to live in seconds.
   */
  public void setQueryCacheMaxTimeToLive(int queryCacheMaxTimeToLive) {
    this.queryCacheMaxTimeToLive = queryCacheMaxTimeToLive;
  }

  /**
   * Return the NamingConvention.
   * <p>
   * If none has been set the default UnderscoreNamingConvention is used.
   * </p>
   */
  public NamingConvention getNamingConvention() {
    return namingConvention;
  }

  /**
   * Set the NamingConvention.
   * <p>
   * If none is set the default UnderscoreNamingConvention is used.
   * </p>
   */
  public void setNamingConvention(NamingConvention namingConvention) {
    this.namingConvention = namingConvention;
  }

  /**
   * Return the configuration for the Autofetch feature.
   */
  public AutofetchConfig getAutofetchConfig() {
    return autofetchConfig;
  }

  /**
   * Set the configuration for the Autofetch feature.
   */
  public void setAutofetchConfig(AutofetchConfig autofetchConfig) {
    this.autofetchConfig = autofetchConfig;
  }

  /**
   * Return the PreparedStatementDelegate.
   */
  public PstmtDelegate getPstmtDelegate() {
    return pstmtDelegate;
  }

  /**
   * Set the PstmtDelegate which can be used to support JDBC driver specific
   * features.
   * <p>
   * Typically this means Oracle JDBC driver specific workarounds.
   * </p>
   */
  public void setPstmtDelegate(PstmtDelegate pstmtDelegate) {
    this.pstmtDelegate = pstmtDelegate;
  }

  /**
   * Return the DataSource.
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Set a DataSource.
   */
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Return the configuration to build a DataSource using Ebean's own DataSource
   * implementation.
   */
  public DataSourceConfig getDataSourceConfig() {
    return dataSourceConfig;
  }

  /**
   * Set the configuration required to build a DataSource using Ebean's own
   * DataSource implementation.
   */
  public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
    this.dataSourceConfig = dataSourceConfig;
  }

  /**
   * Return the JNDI name of the DataSource to use.
   */
  public String getDataSourceJndiName() {
    return dataSourceJndiName;
  }

  /**
   * Set the JNDI name of the DataSource to use.
   * <p>
   * By default a prefix of "java:comp/env/jdbc/" is used to lookup the
   * DataSource. This prefix is not used if dataSourceJndiName starts with
   * "java:".
   * </p>
   */
  public void setDataSourceJndiName(String dataSourceJndiName) {
    this.dataSourceJndiName = dataSourceJndiName;
  }
  
  /**
   * Return true if autoCommit mode is on. This indicates to Ebean to use autoCommit friendly Transactions and TransactionManager.
   */
  public boolean isAutoCommitMode() {
    return autoCommitMode;
  }

  /**
   * Set to true if autoCommit mode is on and Ebean should use autoCommit friendly Transactions and TransactionManager.
   */
  public void setAutoCommitMode(boolean autoCommitMode) {
    this.autoCommitMode = autoCommitMode;
  }

  /**
   * Return a value used to represent TRUE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * </p>
   * <p>
   * The value returned is either a Integer or a String (e.g. "1", or "T").
   * </p>
   */
  public String getDatabaseBooleanTrue() {
    return databaseBooleanTrue;
  }

  /**
   * Set the value to represent TRUE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * </p>
   * <p>
   * The value set is either a Integer or a String (e.g. "1", or "T").
   * </p>
   */
  public void setDatabaseBooleanTrue(String databaseTrue) {
    this.databaseBooleanTrue = databaseTrue;
  }

  /**
   * Return a value used to represent FALSE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * </p>
   * <p>
   * The value returned is either a Integer or a String (e.g. "0", or "F").
   * </p>
   */
  public String getDatabaseBooleanFalse() {
    return databaseBooleanFalse;
  }

  /**
   * Set the value to represent FALSE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * </p>
   * <p>
   * The value set is either a Integer or a String (e.g. "0", or "F").
   * </p>
   */
  public void setDatabaseBooleanFalse(String databaseFalse) {
    this.databaseBooleanFalse = databaseFalse;
  }

  /**
   * Return the number of DB sequence values that should be preallocated.
   */
  public int getDatabaseSequenceBatchSize() {
    return databaseSequenceBatchSize;
  }

  /**
   * Set the number of DB sequence values that should be preallocated and cached
   * by Ebean.
   * <p>
   * This is only used for DB's that use sequences and is a performance
   * optimisation. This reduces the number of times Ebean needs to get a
   * sequence value from the Database reducing network chatter.
   * </p>
   * <p>
   * By default this value is 10 so when we need another Id (and don't have one
   * in our cache) Ebean will fetch 10 id's from the database. Note that when
   * the cache drops to have full (which is 5 by default) Ebean will fetch
   * another batch of Id's in a background thread.
   * </p>
   */
  public void setDatabaseSequenceBatch(int databaseSequenceBatchSize) {
    this.databaseSequenceBatchSize = databaseSequenceBatchSize;
  }

  /**
   * Return the database platform name (can be null).
   * <p>
   * If null then the platform is determined automatically via the JDBC driver
   * information.
   * </p>
   */
  public String getDatabasePlatformName() {
    return databasePlatformName;
  }

  /**
   * Explicitly set the database platform name
   * <p>
   * If none is set then the platform is determined automatically via the JDBC
   * driver information.
   * </p>
   * <p>
   * This can be used when the Database Platform can not be automatically
   * detected from the JDBC driver (possibly 3rd party JDBC driver). It is also
   * useful when you want to do offline DDL generation for a database platform
   * that you don't have access to.
   * </p>
   * <p>
   * Values are oracle, h2, postgres, mysql, mssqlserver2005.
   * </p>
   * 
   * @see DataSourceConfig#setOffline(boolean)
   */
  public void setDatabasePlatformName(String databasePlatformName) {
    this.databasePlatformName = databasePlatformName;
  }

  /**
   * Return the database platform to use for this server.
   */
  public DatabasePlatform getDatabasePlatform() {
    return databasePlatform;
  }

  /**
   * Explicitly set the database platform to use.
   * <p>
   * If none is set then the platform is determined via the databasePlatformName
   * or automatically via the JDBC driver information.
   * </p>
   */
  public void setDatabasePlatform(DatabasePlatform databasePlatform) {
    this.databasePlatform = databasePlatform;
  }

  /**
   * Return the EncryptKeyManager.
   */
  public EncryptKeyManager getEncryptKeyManager() {
    return encryptKeyManager;
  }

  /**
   * Set the EncryptKeyManager.
   * <p>
   * This is required when you want to use encrypted properties.
   * </p>
   * <p>
   * You can also set this in ebean.proprerties:
   * </p>
   * 
   * <pre class="code">
   * # set via ebean.properties
   * 
   * ebean.encryptKeyManager=com.avaje.tests.basic.encrypt.BasicEncyptKeyManager
   * </pre>
   */
  public void setEncryptKeyManager(EncryptKeyManager encryptKeyManager) {
    this.encryptKeyManager = encryptKeyManager;
  }

  /**
   * Return the EncryptDeployManager.
   * <p>
   * This is optionally used to programmatically define which columns are
   * encrypted instead of using the {@link Encrypted} Annotation.
   * </p>
   */
  public EncryptDeployManager getEncryptDeployManager() {
    return encryptDeployManager;
  }

  /**
   * Set the EncryptDeployManager.
   * <p>
   * This is optionally used to programmatically define which columns are
   * encrypted instead of using the {@link Encrypted} Annotation.
   * </p>
   */
  public void setEncryptDeployManager(EncryptDeployManager encryptDeployManager) {
    this.encryptDeployManager = encryptDeployManager;
  }

  /**
   * Return the Encryptor used to encrypt data on the java client side (as
   * opposed to DB encryption functions).
   */
  public Encryptor getEncryptor() {
    return encryptor;
  }

  /**
   * Set the Encryptor used to encrypt data on the java client side (as opposed
   * to DB encryption functions).
   * <p>
   * Ebean has a default implementation that it will use if you do not set your
   * own Encryptor implementation.
   * </p>
   */
  public void setEncryptor(Encryptor encryptor) {
    this.encryptor = encryptor;
  }

  /**
   * Return the DbEncrypt used to encrypt and decrypt properties.
   * <p>
   * Note that if this is not set then the DbPlatform may already have a
   * DbEncrypt set and that will be used.
   * </p>
   */
  public DbEncrypt getDbEncrypt() {
    return dbEncrypt;
  }

  /**
   * Set the DbEncrypt used to encrypt and decrypt properties.
   * <p>
   * Note that if this is not set then the DbPlatform may already have a
   * DbEncrypt set (H2, MySql, Postgres and Oracle platforms have a DbEncrypt)
   * </p>
   */
  public void setDbEncrypt(DbEncrypt dbEncrypt) {
    this.dbEncrypt = dbEncrypt;
  }

  
  /**
   * Return true if UUID should be stored as binary(16) (as opposed to varchar(40)).
   */
  public boolean isUuidStoreAsBinary() {
    return uuidStoreAsBinary;
  }

  /**
   * Set to true if UUID should be stored as binary(16) (as opposed to varchar(40)).
   */
  public void setUuidStoreAsBinary(boolean uuidStoreAsBinary) {
    this.uuidStoreAsBinary = uuidStoreAsBinary;
  }

  /**
   * Return true if LocalTime should be persisted with nanos precision.
   */
  public boolean isLocalTimeWithNanos() {
    return localTimeWithNanos;
  }

  /**
   * Set to true if LocalTime should be persisted with nanos precision.
   * <p>
   * Otherwise it is persisted using java.sql.Time which is seconds precision.
   * </p>
   */
  public void setLocalTimeWithNanos(boolean localTimeWithNanos) {
    this.localTimeWithNanos = localTimeWithNanos;
  }

  /**
   * Return true if Duration should be persisted with nanos precision (SQL DECIMAL).
   * <p>
   * Otherwise it is persisted with second precision (SQL INTEGER).
   * </p>
   */
  public boolean isDurationWithNanos() {
    return durationWithNanos;
  }

  /**
   * Set to true if Duration should be persisted with nanos precision (SQL DECIMAL).
   * <p>
   * Otherwise it is persisted with second precision (SQL INTEGER).
   * </p>
   */
  public void setDurationWithNanos(boolean durationWithNanos) {
    this.durationWithNanos = durationWithNanos;
  }

  /**
   * Set to true to run the DDL generation on startup.
   */
  public void setDdlGenerate(boolean ddlGenerate) {
    this.ddlGenerate = ddlGenerate;
  }

  /**
   * Set to true to run the generated DDL on startup.
   */
  public void setDdlRun(boolean ddlRun) {
    this.ddlRun = ddlRun;
  }

  /**
   * Return true if the DDL should be generated.
   */
  public boolean isDdlGenerate() {
    return ddlGenerate;
  }

  /**
   * Return true if the DDL should be run.
   */
  public boolean isDdlRun() {
    return ddlRun;
  }

  /**
   * Programmatically add classes (typically entities) that this server should
   * use.
   * <p>
   * The class can be an Entity, Embedded type, ScalarType, BeanPersistListener,
   * BeanFinder or BeanPersistController.
   * </p>
   * <p>
   * If no classes are specified then the classes are found automatically via
   * searching the class path.
   * </p>
   * <p>
   * Alternatively the classes can be added via {@link #setClasses(List)}.
   * </p>
   * 
   * @param cls
   *          the entity type (or other type) that should be registered by this
   *          server.
   */
  public void addClass(Class<?> cls) {
    if (classes == null) {
      classes = new ArrayList<Class<?>>();
    }
    classes.add(cls);
  }

  /**
   * Add a package to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   * </p>
   */
  public void addPackage(String packageName) {
    if (packages == null) {
      packages = new ArrayList<String>();
    }
    packages.add(packageName);
  }

  /**
   * Return packages to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   * </p>
   */
  public List<String> getPackages() {
    return packages;
  }

  /**
   * Set packages to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   * </p>
   */
  public void setPackages(List<String> packages) {
    this.packages = packages;
  }

  /**
   * Add the name of a Jar to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   * </p>
   * <p>
   * If you are using ebean.properties you can specify jars to search by setting
   * a ebean.search.jars property.
   * </p>
   * 
   * <pre class="code">
   * # EBean will search through classes for entities, but will not search jar files 
   * # unless you tell it to do so, for performance reasons.  Set this value to a 
   * # comma-delimited list of jar files you want ebean to search.
   * ebean.search.jars=example.jar
   * </pre>
   */
  public void addJar(String jarName) {
    if (searchJars == null) {
      searchJars = new ArrayList<String>();
    }
    searchJars.add(jarName);
  }

  /**
   * Return packages to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   * </p>
   */
  public List<String> getJars() {
    return searchJars;
  }

  /**
   * Set the names of Jars to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   * </p>
   */
  public void setJars(List<String> searchJars) {
    this.searchJars = searchJars;
  }

  /**
   * Return the class name of a classPathReader implementation.
   */
  public String getClassPathReaderClassName() {
    return classPathReaderClassName;
  }

  /**
   * Set the class name of a classPathReader implementation.
   *
   * Refer to server.util.ClassPathReader, this should really by a plugin but doing this for now
   * to be relatively compatible with current implementation.
   */
  public void setClassPathReaderClassName(String classPathReaderClassName) {
    this.classPathReaderClassName = classPathReaderClassName;
  }

  /**
   * Set the list of classes (entities, listeners, scalarTypes etc) that should
   * be used for this server.
   * <p>
   * If no classes are specified then the classes are found automatically via
   * searching the class path.
   * </p>
   * <p>
   * Alternatively the classes can contain added via {@link #addClass(Class)}.
   * </p>
   */
  public void setClasses(List<Class<?>> classes) {
    this.classes = classes;
  }

  /**
   * Return the classes registered for this server. Typically this includes
   * entities and perhaps listeners.
   */
  public List<Class<?>> getClasses() {
    return classes;
  }

  /**
   * Return true to only update changed properties.
   */
  public boolean isUpdateChangesOnly() {
    return updateChangesOnly;
  }

  /**
   * Set to true to only update changed properties.
   */
  public void setUpdateChangesOnly(boolean updateChangesOnly) {
    this.updateChangesOnly = updateChangesOnly;
  }
    
  /**
   * Return true if updates by default delete missing children when cascading save to a OneToMany or
   * ManyToMany. When not set this defaults to true.
   */
  public boolean isUpdatesDeleteMissingChildren() {
    return updatesDeleteMissingChildren;
  }

  /**
   * Set if updates by default delete missing children when cascading save to a OneToMany or
   * ManyToMany. When not set this defaults to true.
   */
  public void setUpdatesDeleteMissingChildren(boolean updatesDeleteMissingChildren) {
    this.updatesDeleteMissingChildren = updatesDeleteMissingChildren;
  }

  /**
   * Return true if the ebeanServer should collection query statistics by ObjectGraphNode.
   */
  public boolean isCollectQueryStatsByNode() {
    return collectQueryStatsByNode;
  }

  /**
   * Set to true to collection query execution statistics by ObjectGraphNode.
   * <p>
   * These statistics can be used to highlight code/query 'origin points' that result in lots of lazy loading.
   * </p>
   * <p>
   * It is considered safe/fine to have this set to true for production.
   * </p>
   * <p>
   * This information can be later retrieved via {@link MetaInfoManager}.
   * </p>
   * @see MetaInfoManager
   */
  public void setCollectQueryStatsByNode(boolean collectQueryStatsByNode) {
    this.collectQueryStatsByNode = collectQueryStatsByNode;
  }

  /**
   * Return true if query plans should also collect their 'origins'. This means for a given query plan you
   * can identify the code/origin points where this query resulted from including lazy loading origins.
   */
  public boolean isCollectQueryOrigins() {
    return collectQueryOrigins;
  }

  /**
   * Set to true if query plans should collect their 'origin' points. This means for a given query plan you
   * can identify the code/origin points where this query resulted from including lazy loading origins.
   * <p>
   * This information can be later retrieved via {@link MetaInfoManager}.
   * </p>
   * @see MetaInfoManager
   */
  public void setCollectQueryOrigins(boolean collectQueryOrigins) {
    this.collectQueryOrigins = collectQueryOrigins;
  }

  /**
   * Returns the resource directory.
   */
  public String getResourceDirectory() {
    return resourceDirectory;
  }

  /**
   * Sets the resource directory.
   */
  public void setResourceDirectory(String resourceDirectory) {
    this.resourceDirectory = resourceDirectory;
  }

  /**
   * Register a BeanQueryAdapter instance.
   * <p>
   * Note alternatively you can use {@link #setQueryAdapters(List)} to set all
   * the BeanQueryAdapter instances.
   * </p>
   */
  public void add(BeanQueryAdapter beanQueryAdapter) {
    queryAdapters.add(beanQueryAdapter);
  }

  /**
   * Return the BeanQueryAdapter instances.
   */
  public List<BeanQueryAdapter> getQueryAdapters() {
    return queryAdapters;
  }

  /**
   * Register all the BeanQueryAdapter instances.
   * <p>
   * Note alternatively you can use {@link #add(BeanQueryAdapter)} to add
   * BeanQueryAdapter instances one at a time.
   * </p>
   */
  public void setQueryAdapters(List<BeanQueryAdapter> queryAdapters) {
    this.queryAdapters = queryAdapters;
  }

  /**
   * Register a BeanPersistController instance.
   * <p>
   * Note alternatively you can use {@link #setPersistControllers(List)} to set
   * all the BeanPersistController instances.
   * </p>
   */
  public void add(BeanPersistController beanPersistController) {
    persistControllers.add(beanPersistController);
  }

  /**
   * Return the BeanPersistController instances.
   */
  public List<BeanPersistController> getPersistControllers() {
    return persistControllers;
  }

  /**
   * Register all the BeanPersistController instances.
   * <p>
   * Note alternatively you can use {@link #add(BeanPersistController)} to add
   * BeanPersistController instances one at a time.
   * </p>
   */
  public void setPersistControllers(List<BeanPersistController> persistControllers) {
    this.persistControllers = persistControllers;
  }

  /**
   * Register a TransactionEventListener instance
   * <p>
   * Note alternatively you can use {@link #setTransactionEventListeners(List)}
   * to set all the TransactionEventListener instances.
   * </p>
   */
  public void add(TransactionEventListener listener) {
    transactionEventListeners.add(listener);
  }

  /**
   * Return the TransactionEventListener instances.
   */
  public List<TransactionEventListener> getTransactionEventListeners() {
    return transactionEventListeners;
  }

  /**
   * Register all the TransactionEventListener instances.
   * <p>
   * Note alternatively you can use {@link #add(TransactionEventListener)} to
   * add TransactionEventListener instances one at a time.
   * </p>
   */
  public void setTransactionEventListeners(List<TransactionEventListener> transactionEventListeners) {
    this.transactionEventListeners = transactionEventListeners;
  }

  /**
   * Register a BeanPersistListener instance.
   * <p>
   * Note alternatively you can use {@link #setPersistListeners(List)} to set
   * all the BeanPersistListener instances.
   * </p>
   */
  public void add(BeanPersistListener beanPersistListener) {
    persistListeners.add(beanPersistListener);
  }

  /**
   * Return the BeanPersistListener instances.
   */
  public List<BeanPersistListener> getPersistListeners() {
    return persistListeners;
  }

  /**
   * Add a BulkTableEventListener
   */
  public void add(BulkTableEventListener bulkTableEventListener) {
    bulkTableEventListeners.add(bulkTableEventListener);
  }

  /**
   * Return the list of BulkTableEventListener instances.
   */
  public List<BulkTableEventListener> getBulkTableEventListeners() {
    return bulkTableEventListeners;
  }

  /**
   * Add a ServerConfigStartup.
   */
  public void addServerConfigStartup(ServerConfigStartup configStartupListener) {
    configStartupListeners.add(configStartupListener);
  }

  /**
   * Return the list of ServerConfigStartup instances.
   */
  public List<ServerConfigStartup> getServerConfigStartupListeners() {
    return configStartupListeners;
  }

  /**
   * Register all the BeanPersistListener instances.
   * <p>
   * Note alternatively you can use {@link #add(BeanPersistListener)} to add
   * BeanPersistListener instances one at a time.
   * </p>
   */
  public void setPersistListeners(List<BeanPersistListener> persistListeners) {
    this.persistListeners = persistListeners;
  }

  /**
   * Return the default PersistenceContextScope to be used if one is not explicitly set on a query.
   * <p/>
   * The PersistenceContextScope can specified on each query via {@link com.avaje.ebean
   * .Query#setPersistenceContextScope(com.avaje.ebean.PersistenceContextScope)}. If it
   * is not set on the query this default scope is used.
   *
   * @see com.avaje.ebean.Query#setPersistenceContextScope(com.avaje.ebean.PersistenceContextScope)
   */
  public PersistenceContextScope getPersistenceContextScope() {
    // if somehow null return TRANSACTION scope
    return persistenceContextScope == null ? PersistenceContextScope.TRANSACTION : persistenceContextScope;
  }

  /**
   * Set the PersistenceContext scope to be used if one is not explicitly set on a query.
   * <p/>
   * This defaults to {@link PersistenceContextScope#TRANSACTION}.
   * <p/>
   * The PersistenceContextScope can specified on each query via {@link com.avaje.ebean
   * .Query#setPersistenceContextScope(com.avaje.ebean.PersistenceContextScope)}. If it
   * is not set on the query this scope is used.
   *
   * @see com.avaje.ebean.Query#setPersistenceContextScope(com.avaje.ebean.PersistenceContextScope)
   */
  public void setPersistenceContextScope(PersistenceContextScope persistenceContextScope) {
    this.persistenceContextScope = persistenceContextScope;
  }

  /**
   * Load settings from ebean.properties.
   */
  public void loadFromProperties() {
    loadFromProperties(PropertyMap.defaultProperties());
  }

  /**
   * Load the settings from the given properties
   */
  public void loadFromProperties(Properties properties) {
    PropertiesWrapper p = new PropertiesWrapper("ebean", name, properties);
    loadSettings(p);
  }


  @SuppressWarnings("unchecked")
  private <T> T createInstance(PropertiesWrapper p, Class<T> pluginType, String key) {

    String classname = p.get(key, null);
    return classname == null ? null : (T) ClassUtil.newInstance(classname);
  }

  /**
   * loads the data source settings to preserve existing behaviour. IMHO, if someone has set the datasource config already,
   * they don't want the settings to be reloaded and reset. This allows a descending class to override this behaviour and prevent it
   * from happening.
   *
   * @param p - The defined property source passed to load settings
   */
  protected void loadDataSourceSettings(PropertiesWrapper p) {
    dataSourceConfig.loadSettings(p.withPrefix("datasource"));
  }

  /**
   * This is broken out for the same reason as above - preserve existing behaviour but let it be overridden.
   */
  protected void loadAutofetchSettings(PropertiesWrapper p) {
    autofetchConfig.loadSettings(p);
  }

  /**
   * Load the configuration settings from the properties file.
   */
  protected void loadSettings(PropertiesWrapper p) {

    if (namingConvention != null) {
      namingConvention.loadFromProperties(p);
    }
    if (autofetchConfig == null) {
      autofetchConfig = new AutofetchConfig();
    }
    loadAutofetchSettings(p);

    if (dataSourceConfig == null) {
      dataSourceConfig = new DataSourceConfig();
    }
    loadDataSourceSettings(p);

    autoCommitMode = p.getBoolean("autoCommitMode", autoCommitMode);
    useJtaTransactionManager = p.getBoolean("useJtaTransactionManager", useJtaTransactionManager);
    namingConvention = createNamingConvention(p);
    databasePlatform = createInstance(p, DatabasePlatform.class, "databasePlatform");
    encryptKeyManager = createInstance(p, EncryptKeyManager.class, "encryptKeyManager");
    encryptDeployManager = createInstance(p, EncryptDeployManager.class, "encryptDeployManager");
    encryptor = createInstance(p, Encryptor.class, "encryptor");
    dbEncrypt = createInstance(p, DbEncrypt.class, "dbEncrypt");
    serverCacheFactory = createInstance(p, ServerCacheFactory.class, "serverCacheFactory");
    serverCacheManager = createInstance(p, ServerCacheManager.class, "serverCacheManager");

    String jarsProp = p.get("search.jars", p.get("jars", null));
    if (jarsProp != null) {
      searchJars = getSearchJarsPackages(jarsProp);
    }

    if (packages != null) {
      String packagesProp = p.get("search.packages", p.get("packages", null));
      packages = getSearchJarsPackages(packagesProp);
    }

    collectQueryStatsByNode = p.getBoolean("collectQueryStatsByNode", collectQueryStatsByNode);
    collectQueryOrigins = p.getBoolean("collectQueryOrigins", collectQueryOrigins);

    updateChangesOnly = p.getBoolean("updateChangesOnly", updateChangesOnly);
    
    boolean defaultDeleteMissingChildren = p.getBoolean("defaultDeleteMissingChildren", updatesDeleteMissingChildren);
    updatesDeleteMissingChildren = p.getBoolean("updatesDeleteMissingChildren", defaultDeleteMissingChildren);

    boolean batchMode = p.getBoolean("batch.mode", persistBatching);
    persistBatching = p.getBoolean("persistBatching", batchMode);

    int batchSize = p.getInt("batch.size", persistBatchSize);
    persistBatchSize = p.getInt("persistBatchSize", batchSize);

    persistenceContextScope = PersistenceContextScope.valueOf(p.get("persistenceContextScope","TRANSACTION"));

    dataSourceJndiName = p.get("dataSourceJndiName", dataSourceJndiName);
    databaseSequenceBatchSize = p.getInt("databaseSequenceBatchSize", databaseSequenceBatchSize);
    databaseBooleanTrue = p.get("databaseBooleanTrue", databaseBooleanTrue);
    databaseBooleanFalse = p.get("databaseBooleanFalse", databaseBooleanFalse);
    databasePlatformName = p.get("databasePlatformName", databasePlatformName);
    uuidStoreAsBinary = p.getBoolean("uuidStoreAsBinary", uuidStoreAsBinary);
    localTimeWithNanos = p.getBoolean("localTimeWithNanos", localTimeWithNanos);

    lazyLoadBatchSize = p.getInt("lazyLoadBatchSize", lazyLoadBatchSize);
    queryBatchSize = p.getInt("queryBatchSize", queryBatchSize);

    String jsonDateTimeFormat = p.get("jsonDateTime", null);
    if (jsonDateTimeFormat != null) {
      jsonDateTime = JsonConfig.DateTime.valueOf(jsonDateTimeFormat);
    } else {
      jsonDateTime = JsonConfig.DateTime.MILLIS;
    }

    ddlGenerate = p.getBoolean("ddl.generate", ddlGenerate);
    ddlRun = p.getBoolean("ddl.run", ddlRun);

    classes = getClasses(p);
  }

  private NamingConvention createNamingConvention(PropertiesWrapper properties) {

    NamingConvention nc = createInstance(properties, NamingConvention.class, "namingconvention");
    if (nc == null) {
      return null;
    }
    if (nc instanceof AbstractNamingConvention) {
      AbstractNamingConvention anc = (AbstractNamingConvention) nc;
      String v = properties.get("namingConvention.useForeignKeyPrefix", null);
      if (v != null) {
        boolean useForeignKeyPrefix = Boolean.valueOf(v);
        anc.setUseForeignKeyPrefix(useForeignKeyPrefix);
      }

      String sequenceFormat = properties.get("namingConvention.sequenceFormat", null);
      if (sequenceFormat != null) {
        anc.setSequenceFormat(sequenceFormat);
      }
    }
    return nc;
  }

  /**
   * Build the list of classes from the comma delimited string.
   * 
   * @param properties
   *          the properties
   * 
   * @return the classes
   */
  private List<Class<?>> getClasses(PropertiesWrapper properties) {

    String classNames = properties.get("classes", null);
    if (classNames == null) {

      return null;
    }

    List<Class<?>> classes = new ArrayList<Class<?>>();

    String[] split = classNames.split("[ ,;]");
    for (int i = 0; i < split.length; i++) {
      String cn = split[i].trim();
      if (cn.length() > 0 && !"class".equalsIgnoreCase(cn)) {
        try {
          classes.add(Class.forName(cn));
        } catch (ClassNotFoundException e) {
          String msg = "Error registering class [" + cn + "] from [" + classNames + "]";
          throw new RuntimeException(msg, e);
        }
      }
    }
    return classes;
  }

  private List<String> getSearchJarsPackages(String searchPackages) {

    List<String> hitList = new ArrayList<String>();

    if (searchPackages != null) {

      String[] entries = searchPackages.split("[ ,;]");
      for (int i = 0; i < entries.length; i++) {
        hitList.add(entries[i].trim());
      }
    }
    return hitList;
  }
}
