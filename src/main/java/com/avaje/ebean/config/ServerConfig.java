package com.avaje.ebean.config;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.LogLevel;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Encrypted;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.GlobalProperties.PropertySource;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.event.BulkTableEventListener;
import com.avaje.ebean.event.ServerConfigStartup;
import com.avaje.ebean.event.TransactionEventListener;
import com.avaje.ebean.util.ClassUtil;

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
 * <pre class="code">
 * ServerConfig c = new ServerConfig();
 * c.setName(&quot;ordh2&quot;);
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
 * c.addPackage(&quot;app.data&quot;);
 * 
 * // add the names of Jars that contain entities
 * c.addJar(&quot;myJarContainingEntities.jar&quot;);
 * c.addJar(&quot;someOtherJarContainingEntities.jar&quot;);
 * 
 * // register as the 'Default' server
 * c.setDefaultServer(true);
 * 
 * EbeanServer server = EbeanServerFactory.create(c);
 * 
 * </pre>
 * 
 * @see EbeanServerFactory
 * 
 * @author emcgreal
 * @author rbygrave
 */
public class ServerConfig {
  /** The Constant DEFAULT_QUERY_BATCH_SIZE. Default: 100 */
  private final static int DEFAULT_QUERY_BATCH_SIZE = 100;

  /**
   * The EbeanServer name.
   */
  private String name;

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

  /** The autofetch config. */
  private AutofetchConfig autofetchConfig = new AutofetchConfig();

  /** The database platform name. */
  private String databasePlatformName;

  /** The database platform. */
  private DatabasePlatform databasePlatform;

  /**
   * For DB's using sequences this is the number of sequence values prefetched.
   */
  private int databaseSequenceBatchSize = 20;

  private boolean persistBatching;

  private int persistBatchSize = 20;

  /** The default batch size for lazy loading */
  private int lazyLoadBatchSize = 1;

  /** The query batch size. */
  private int queryBatchSize = -1;

  private boolean ddlGenerate;

  private boolean ddlRun;

  private boolean debugSql;

  private boolean debugLazyLoad;

  private boolean useJtaTransactionManager;

  /**
   * The external transaction manager (like Spring).
   */
  private ExternalTransactionManager externalTransactionManager;

  /**
   * Set to true to log using java.util.logging and otherwise uses ebean
   * transaction loggers.
   */
  private boolean loggingToJavaLogger;

  /**
   * The directory transaction logs go (when loggingToJavaLogger is false).
   */
  private String loggingDirectory = "logs";

  /**
   * The overall transaction logging level.
   */
  private LogLevel loggingLevel = LogLevel.NONE;

  /**
   * Used to unwrap PreparedStatements to perform JDBC Driver specific functions
   */
  private PstmtDelegate pstmtDelegate;

  /** The data source. */
  private DataSource dataSource;

  /** The data source config. */
  private DataSourceConfig dataSourceConfig = new DataSourceConfig();

  /** The data source jndi name. */
  private String dataSourceJndiName;

  /** The database boolean true. */
  private String databaseBooleanTrue;

  /** The database boolean false. */
  private String databaseBooleanFalse;

  /** The naming convention. */
  private NamingConvention namingConvention;

  /** The update changes only. */
  private boolean updateChangesOnly = true;

  private List<BeanPersistController> persistControllers = new ArrayList<BeanPersistController>();
  private List<BeanPersistListener<?>> persistListeners = new ArrayList<BeanPersistListener<?>>();
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

  /**
   * Set this to true when by default vanilla objects should be returned from
   * queries rather than dynamic subclasses etc. Only relevant when not using
   * enhancement (using dynamic subclasses).
   */
  private boolean vanillaMode;

  /**
   * Controls whether the {@link EbeanServer#getReference(Class, Object)} method
   * returns vanilla objects or not.
   */
  private boolean vanillaRefMode;

  /**
   * Set to false to require enhancement to be used. Defaults to true.
   */
  private boolean allowSubclassing = true;

  /**
   * Construct a Server Configuration for programmatically creating an
   * EbeanServer.
   */
  public ServerConfig() {

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
   * Use isPersistBatching() instead.
   * 
   * @deprecated
   */
  public boolean isUsePersistBatching() {
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
   * Return true if by default queries should return 'vanilla' objects rather
   * than dynamic subclasses.
   * <p>
   * This setting is not relevant when using enhancement (only when using
   * dynamic subclasses).
   * </p>
   */
  public boolean isVanillaMode() {
    return vanillaMode;
  }

  /**
   * Set this to true if by default queries should return 'vanilla' objects
   * rather than dynamic subclasses.
   * <p>
   * This setting is not relevant when using enhancement (only when using
   * dynamic subclasses).
   * </p>
   * <p>
   * Alternatively you can set this on a specific query via
   * {@link Query#setVanillaMode(boolean)}.
   * </p>
   * 
   * @see #setVanillaRefMode(boolean)
   * @see Query#setVanillaMode(boolean)
   */
  public void setVanillaMode(boolean vanillaMode) {
    this.vanillaMode = vanillaMode;
  }

  /**
   * Returns true if {@link EbeanServer#getReference(Class, Object)} should
   * return vanilla objects or not.
   * 
   * @see #setVanillaMode(boolean)
   * @see Query#setVanillaMode(boolean)
   */
  public boolean isVanillaRefMode() {
    return vanillaRefMode;
  }

  /**
   * Set this to true if you want
   * {@link EbeanServer#getReference(Class, Object)} to return vanilla objects.
   */
  public void setVanillaRefMode(boolean vanillaRefMode) {
    this.vanillaRefMode = vanillaRefMode;
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
   * Return true to get the generated SQL queries output to the console.
   * <p>
   * To get the SQL and bind variables for insert update delete statements you
   * should use transaction logging.
   * </p>
   */
  public boolean isDebugSql() {
    return debugSql;
  }

  /**
   * Set to true to get the generated SQL queries output to the console.
   * <p>
   * To get the SQL and bind variables for insert update delete statements you
   * should use transaction logging.
   * </p>
   */
  public void setDebugSql(boolean debugSql) {
    this.debugSql = debugSql;
  }

  /**
   * Return true if there is debug logging on lazy loading events.
   */
  public boolean isDebugLazyLoad() {
    return debugLazyLoad;
  }

  /**
   * Set to true to get debug logging on lazy loading events.
   */
  public void setDebugLazyLoad(boolean debugLazyLoad) {
    this.debugLazyLoad = debugLazyLoad;
  }

  /**
   * Return the default transaction logging level.
   * <p>
   * The logging level can be changed on a per transaction basis.
   * </p>
   */
  public LogLevel getLoggingLevel() {
    return loggingLevel;
  }

  /**
   * Set the default transaction logging level.
   * <p>
   * The logging level can be changed on a per transaction basis.
   * </p>
   */
  public void setLoggingLevel(LogLevel logLevel) {
    this.loggingLevel = logLevel;
  }

  /**
   * Return the directory where transaction logs go.
   */
  public String getLoggingDirectory() {
    return loggingDirectory;
  }

  /**
   * Return the transaction log directory substituting any expressions such as
   * ${catalina.base} etc.
   */
  public String getLoggingDirectoryWithEval() {
    return GlobalProperties.evaluateExpressions(loggingDirectory);
  }

  /**
   * Set the directory that the transaction logs go in.
   * <p>
   * This will not be used if the transaction logging is going to java util
   * logging (via {@link #setLoggingToJavaLogger(boolean)}).
   * </p>
   * <p>
   * This can contain expressions like ${catalina.base} with environment
   * variables, java system properties and entries in ebean.properties.
   * </p>
   * <p>
   * e.g. ${catalina.base}/logs/trans
   * </p>
   * 
   * @param loggingDirectory
   *          the transaction log directory
   */
  public void setLoggingDirectory(String loggingDirectory) {
    this.loggingDirectory = loggingDirectory;
  }

  /**
   * Return true if you want to use a java.util.logging.Logger to log
   * transaction statements, bind values etc.
   * <p>
   * If this is false then the default transaction logger is used which logs the
   * transaction details to separate transaction log files.
   * </p>
   */
  @Deprecated
  public boolean isLoggingToJavaLogger() {
    return loggingToJavaLogger;
  }

  /**
   * Set this to true if you want transaction logging to use a
   * java.util.logging.Logger to log the statements and bind variables etc
   * rather than the default one which creates separate transaction log files.
   */
  @Deprecated
  public void setLoggingToJavaLogger(boolean transactionLogToJavaLogger) {
    this.loggingToJavaLogger = transactionLogToJavaLogger;
  }

  /**
   * Deprecated - please use isTransactionLogToJavaLogger();
   * 
   * @deprecated
   */
  public boolean isUseJuliTransactionLogger() {
    return isLoggingToJavaLogger();
  }

  /**
   * Deprecated - please use setTransactionLogToJavaLogger();
   * 
   * @deprecated
   */
  public void setUseJuliTransactionLogger(boolean transactionLogToJavaLogger) {
    setLoggingToJavaLogger(transactionLogToJavaLogger);
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
   * Set to false to require enhancement to be used. Defaults to true.
   */
  public void setAllowSubclassing(boolean allowSubclassing) {
    this.allowSubclassing = allowSubclassing;
  }

  /**
   * Returns whether this config supports subclassed entities.
   */
  public boolean isAllowSubclassing() {
    return allowSubclassing;
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
  public void add(BeanPersistListener<?> beanPersistListener) {
    persistListeners.add(beanPersistListener);
  }

  /**
   * Return the BeanPersistListener instances.
   */
  public List<BeanPersistListener<?>> getPersistListeners() {
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
  public void setPersistListeners(List<BeanPersistListener<?>> persistListeners) {
    this.persistListeners = persistListeners;
  }

  /**
   * Load the settings from the ebean.properties file.
   */
  public void loadFromProperties() {
    ConfigPropertyMap p = new ConfigPropertyMap(name);
    loadSettings(p);
  }

  /**
   * Return a PropertySource for this server.
   */
  public PropertySource getPropertySource() {
    return GlobalProperties.getPropertySource(name);
  }

  /**
   * Return a configuration property using a default value.
   */
  public String getProperty(String propertyName, String defaultValue) {
    PropertySource p = new ConfigPropertyMap(name);
    return p.get(propertyName, defaultValue);
  }

  /**
   * Return a configuration property.
   */
  public String getProperty(String propertyName) {
    return getProperty(propertyName, null);
  }

  @SuppressWarnings("unchecked")
  private <T> T createInstance(PropertySource p, Class<T> type, String key) {

    String classname = p.get(key, null);
    if (classname == null) {
      return null;
    }

    return (T) ClassUtil.newInstance(classname);
  }

  /**
   * loads the data source settings to preserve existing behaviour. IMHO, if someone has set the datasource config already,
   * they don't want the settings to be reloaded and reset. This allows a descending class to override this behaviour and prevent it
   * from happening.
   *
   * @param p - The defined property source passed to load settings
   */
  protected void loadDataSourceSettings(PropertySource p) {
    dataSourceConfig.loadSettings(p.getServerName());
  }

  /**
   * This is broken out for the same reason as above - preserve existing behaviour but let it be overridden.
   *
   * @param p
   */
  protected void loadAutofetchConfig(PropertySource p) {
    autofetchConfig.loadSettings(p);
  }

  /**
   * Load the configuration settings from the properties file.
   */
  protected void loadSettings(PropertySource p) {

    if (autofetchConfig == null) {
      autofetchConfig = new AutofetchConfig();
    }

    loadAutofetchConfig(p);

    if (dataSourceConfig == null) {
      dataSourceConfig = new DataSourceConfig();
    }

    loadDataSourceSettings(p);

    useJtaTransactionManager = p.getBoolean("useJtaTransactionManager", false);
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

    String packagesProp = p.get("search.packages", p.get("packages", null));
    if (packages != null) {
      packages = getSearchJarsPackages(packagesProp);
    }

    allowSubclassing = p.getBoolean("allowSubclassing", true);
    vanillaMode = p.getBoolean("vanillaMode", false);
    vanillaRefMode = p.getBoolean("vanillaRefMode", false);
    updateChangesOnly = p.getBoolean("updateChangesOnly", true);

    boolean batchMode = p.getBoolean("batch.mode", false);
    persistBatching = p.getBoolean("persistBatching", batchMode);

    int batchSize = p.getInt("batch.size", 20);
    persistBatchSize = p.getInt("persistBatchSize", batchSize);

    dataSourceJndiName = p.get("dataSourceJndiName", null);
    databaseSequenceBatchSize = p.getInt("databaseSequenceBatchSize", 20);
    databaseBooleanTrue = p.get("databaseBooleanTrue", null);
    databaseBooleanFalse = p.get("databaseBooleanFalse", null);
    databasePlatformName = p.get("databasePlatformName", null);

    lazyLoadBatchSize = p.getInt("lazyLoadBatchSize", 1);
    queryBatchSize = p.getInt("queryBatchSize", DEFAULT_QUERY_BATCH_SIZE);

    ddlGenerate = p.getBoolean("ddl.generate", false);
    ddlRun = p.getBoolean("ddl.run", false);
    debugSql = p.getBoolean("debug.sql", false);
    debugLazyLoad = p.getBoolean("debug.lazyload", false);

    loggingLevel = getLogLevelValue(p);

    String s = p.get("useJuliTransactionLogger", null);
    s = p.get("loggingToJavaLogger", s);
    loggingToJavaLogger = "true".equalsIgnoreCase(s);

    s = p.get("log.directory", "logs");
    loggingDirectory = p.get("logging.directory", s);

    classes = getClasses(p);
  }

  private LogLevel getLogLevelValue(PropertySource p) {
    // logging.level preferred but others parameters will work
    String logValue = p.get("logging", "NONE");
    logValue = p.get("log.level", logValue);
    logValue = p.get("logging.level", logValue);
    if (logValue.trim().equalsIgnoreCase("ALL")) {
      logValue = "SQL";
    }
    return Enum.valueOf(LogLevel.class, logValue.toUpperCase());
  }

  private NamingConvention createNamingConvention(PropertySource p) {

    NamingConvention nc = createInstance(p, NamingConvention.class, "namingconvention");
    if (nc == null) {
      return null;
    }
    if (nc instanceof AbstractNamingConvention) {
      AbstractNamingConvention anc = (AbstractNamingConvention) nc;
      String v = p.get("namingConvention.useForeignKeyPrefix", null);
      if (v != null) {
        boolean useForeignKeyPrefix = Boolean.valueOf(v);
        anc.setUseForeignKeyPrefix(useForeignKeyPrefix);
      }

      String sequenceFormat = p.get("namingConvention.sequenceFormat", null);
      if (sequenceFormat != null) {
        anc.setSequenceFormat(sequenceFormat);
      }
    }
    return nc;
  }

  /**
   * Build the list of classes from the comma delimited string.
   * 
   * @param p
   *          the p
   * 
   * @return the classes
   */
  private ArrayList<Class<?>> getClasses(PropertySource p) {

    String classNames = p.get("classes", null);
    if (classNames == null) {

      return null;
    }

    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

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
