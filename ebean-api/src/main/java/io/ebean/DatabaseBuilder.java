package io.ebean;

import com.fasterxml.jackson.core.JsonFactory;
import io.ebean.annotation.*;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.*;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbEncrypt;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.event.*;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import jakarta.persistence.EnumType;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Builder for Database.
 *
 * <pre>{@code
 *
 *   Database db = Database.builder()
 *     .name("h2")
 *     .loadFromProperties()
 *     .register(false);
 *     .defaultDatabase(false)
 *     .build();
 *
 * }</pre>
 */
public interface DatabaseBuilder {

  /**
   * Set the name of the Database.
   */
  DatabaseBuilder name(String name);

  /**
   * Set to false if you do not want this server to be registered with the Ebean
   * singleton when it is created.
   * <p>
   * By default this is set to true.
   */
  DatabaseBuilder register(boolean register);

  /**
   * Set false if you do not want this Database to be registered as the "default" database
   * with the DB singleton.
   * <p>
   * This is only used when {@link #register(boolean)} is also true.
   */
  DatabaseBuilder defaultDatabase(boolean defaultDatabase);

  /**
   * Set a DataSource.
   */
  DatabaseBuilder dataSource(DataSource dataSource);

  /**
   * Set the read only DataSource.
   * <p>
   * Note that the DataSource is expected to use AutoCommit true mode avoiding the need
   * for explicit commit (or rollback).
   * <p>
   * This read only DataSource will be used for implicit query only transactions. It is not
   * used if the transaction is created explicitly or if the query is an update or delete query.
   */
  DatabaseBuilder readOnlyDataSource(DataSource readOnlyDataSource);

  /**
   * Set the configuration required to build a DataSource using Ebean's own
   * DataSource implementation.
   */
  DatabaseBuilder dataSourceConfig(DataSourceConfig dataSourceConfig);

  /**
   * Set to true if Ebean should create a DataSource for use with implicit read only transactions.
   */
  DatabaseBuilder autoReadOnlyDataSource(boolean autoReadOnlyDataSource);

  /**
   * Set the configuration for the read only DataSource.
   */
  DatabaseBuilder readOnlyDataSourceConfig(DataSourceConfig readOnlyDataSourceConfig);

  /**
   * Set to true to run DB migrations on startup.
   * <p>
   * This is the same as config.getMigrationConfig().setRunMigration(). We have added this method here
   * as it is often the only thing we need to configure for migrations.
   */
  DatabaseBuilder runMigration(boolean runMigration);

  /**
   * Load settings from application.properties, application.yaml and other sources.
   * <p>
   * Uses <code>avaje-config</code> to load configuration properties.
   * Goto <a href="https://avaje.io/config">avaje.io/config</a>
   * for detail on how and where properties are loaded from.
   */
  DatabaseBuilder loadFromProperties();

  /**
   * Load the settings from the given properties
   */
  DatabaseBuilder loadFromProperties(Properties properties);

  /**
   * Set the enabled L2 cache regions (comma delimited).
   */
  DatabaseBuilder enabledL2Regions(String enabledL2Regions);

  /**
   * Set to true to disable L2 caching. Typically useful in performance testing.
   */
  DatabaseBuilder disableL2Cache(boolean disableL2Cache);

  /**
   * Force the use of local only L2 cache. Effectively ignore l2 cache plugin like ebean-redis etc.
   */
  DatabaseBuilder localOnlyL2Cache(boolean localOnlyL2Cache);

  /**
   * Continue building with the query plan options.
   */
  DatabaseBuilder.WithQueryPlanOptions withQueryPlanOptions();

  /**
   * Continue building with all options.
   */
  DatabaseBuilder.WithAllOptions withAllOptions();

  /**
   * Build and return the Database.
   */
  Database build();

  /**
   * DatabaseBuilder extended with options for query plan capturing.
   */
  interface WithQueryPlanOptions extends DatabaseBuilder {

    /**
     * Set the time to live for ebean's internal query plan.
     * <p>
     * This is the plan that knows how to execute the query, read the result
     * and collects execution metrics. By default this is set to 5 mins.
     */
    DatabaseBuilder queryPlanTTLSeconds(int queryPlanTTLSeconds);

    /**
     * Set to true to enable query plan capture.
     */
    DatabaseBuilder queryPlanEnable(boolean queryPlanEnable);

    /**
     * Set the query plan collection threshold in microseconds.
     * <p>
     * Queries executing slower than this will have bind values captured such that later
     * the query plan can be captured and reported.
     */
    DatabaseBuilder queryPlanThresholdMicros(long queryPlanThresholdMicros);

    /**
     * Set to true to turn on periodic capture of query plans.
     */
    DatabaseBuilder queryPlanCapture(boolean queryPlanCapture);

    /**
     * Set the frequency in seconds to capture query plans.
     */
    DatabaseBuilder queryPlanCapturePeriodSecs(long queryPlanCapturePeriodSecs);

    /**
     * Set the time after which a capture query plans request will
     * stop capturing more query plans.
     * <p>
     * Effectively this controls the amount of load/time we want to
     * allow for query plan capture.
     */
    DatabaseBuilder queryPlanCaptureMaxTimeMillis(long queryPlanCaptureMaxTimeMillis);

    /**
     * Set the max number of query plans captured per request.
     */
    DatabaseBuilder queryPlanCaptureMaxCount(int queryPlanCaptureMaxCount);

    /**
     * Set the listener used to process captured query plans.
     */
    DatabaseBuilder queryPlanListener(QueryPlanListener queryPlanListener);

  }

  /**
   * All options for the DatabaseBuilder.
   */
  interface WithAllOptions extends WithQueryPlanOptions {

    /**
     * Set this to true to run L2 cache notification in the foreground.
     * <p>
     * In general we don't want to do that as when we use a distributed cache (like Ignite, Hazelcast etc)
     * we are making network calls and we prefer to do this in background and not impact the response time
     * of the executing transaction.
     */
    WithAllOptions notifyL2CacheInForeground(boolean notifyL2CacheInForeground);

    /**
     * Set to true if metrics should be dumped when the server is shutdown.
     */
    WithAllOptions dumpMetricsOnShutdown(boolean dumpMetricsOnShutdown);

    /**
     * Include 'sql' or 'hash' in options such that they are included in the output.
     *
     * @param dumpMetricsOptions Example "sql,hash", "sql"
     */
    WithAllOptions dumpMetricsOptions(String dumpMetricsOptions);

    /**
     * Put a service object into configuration such that it can be used by ebean or a plugin.
     * <p>
     * For example, put IgniteConfiguration in to be passed to the Ignite plugin.
     */
    WithAllOptions putServiceObject(String key, Object configObject);

    /**
     * Put a service object into configuration such that it can be used by ebean or a plugin.
     * <p>
     * For example, put IgniteConfiguration in to be passed to the Ignite plugin.
     * You can also override some SPI objects that should be used for that Database. Currently, the following
     * objects are possible.
     * <ul>
     *   <li>DataSourceAlertFactory (e.g. add different alert factories for different ebean instances)</li>
     *   <li>DocStoreFactory</li>
     *   <li>SlowQueryListener (e.g. add custom query listener for a certain ebean instance)</li>
     *   <li>ServerCacheNotifyPlugin</li>
     * </ul>
     */
    <T> WithAllOptions putServiceObject(Class<T> iface, T configObject);

    /**
     * Put a service object into configuration such that it can be used by ebean or a plugin.
     *
     * <pre>{@code
     *
     *   JedisPool jedisPool = ..
     *
     *   config.putServiceObject(jedisPool);
     *
     * }</pre>
     */
    WithAllOptions putServiceObject(Object configObject);

    /**
     * Set the JDBC batch mode to use at the transaction level.
     * <p>
     * When INSERT or ALL is used then save(), delete() etc do not execute immediately but instead go into
     * a JDBC batch execute buffer that is flushed. The buffer is flushed if a query is executed, transaction ends
     * or the batch size is meet.
     */
    WithAllOptions persistBatch(PersistBatch persistBatch);

    /**
     * Set the JDBC batch mode to use per save(), delete(), insert() or update() request.
     * <p>
     * This makes sense when a save() or delete() etc cascades and executes multiple child statements. The best caase
     * for this is when saving a master/parent bean this cascade inserts many detail/child beans.
     * <p>
     * This only takes effect when the persistBatch mode at the transaction level does not take effect.
     */
    WithAllOptions persistBatchOnCascade(PersistBatch persistBatchOnCascade);

    /**
     * Set the batch size used for JDBC batching. If unset this defaults to 20.
     * <p>
     * You can also set the batch size on the transaction.
     *
     * @see Transaction#setBatchSize(int)
     */
    WithAllOptions persistBatchSize(int persistBatchSize);

    /**
     * Set the DB schema to use. This specifies to use this schema for:
     * <ul>
     * <li>Running Database migrations - Create and use the DB schema</li>
     * <li>Testing DDL - Create-all.sql DDL execution creates and uses schema</li>
     * <li>Testing Docker - Set default schema on connection URL</li>
     * </ul>
     */
    WithAllOptions dbSchema(String dbSchema);

    /**
     * Set the Geometry SRID.
     */
    WithAllOptions geometrySRID(int geometrySRID);

    /**
     * Set the time zone to use when reading/writing Timestamps via JDBC.
     */
    WithAllOptions dataTimeZone(String dataTimeZone);

    /**
     * Set to true if we are running in a JTA Transaction manager.
     */
    WithAllOptions useJtaTransactionManager(boolean useJtaTransactionManager);

    /**
     * Set the external transaction manager.
     */
    WithAllOptions externalTransactionManager(ExternalTransactionManager externalTransactionManager);

    /**
     * Set the L2 cache default max size.
     */
    WithAllOptions cacheMaxSize(int cacheMaxSize);

    /**
     * Set the L2 cache default max idle time in seconds.
     */
    WithAllOptions cacheMaxIdleTime(int cacheMaxIdleTime);

    /**
     * Set the L2 cache default max time to live in seconds.
     */
    WithAllOptions cacheMaxTimeToLive(int cacheMaxTimeToLive);

    /**
     * Set the L2 query cache default max size.
     */
    WithAllOptions queryCacheMaxSize(int queryCacheMaxSize);

    /**
     * Set the L2 query cache default max idle time in seconds.
     */
    WithAllOptions queryCacheMaxIdleTime(int queryCacheMaxIdleTime);

    /**
     * Set the L2 query cache default max time to live in seconds.
     */
    WithAllOptions queryCacheMaxTimeToLive(int queryCacheMaxTimeToLive);

    /**
     * Set to true if all DB column and table names should use quoted identifiers.
     * <p>
     * For Postgres pgjdbc version 42.3.0 should be used with datasource property
     * <em>quoteReturningIdentifiers</em> set to <em>false</em> (refer #2303).
     */
    WithAllOptions allQuotedIdentifiers(boolean allQuotedIdentifiers);

    /**
     * Set the Jackson ObjectMapper.
     * <p>
     * Note that this is not strongly typed as Jackson ObjectMapper is an optional dependency.
     */
    WithAllOptions objectMapper(Object objectMapper);

    /**
     * Set the naming convention to apply to metrics names.
     */
    WithAllOptions metricNaming(Function<String, String> metricNaming);

    /**
     * Set the container / clustering configuration.
     * <p/>
     * The container holds all the Database instances and provides clustering communication
     * services to all the Database instances.
     */
    WithAllOptions containerConfig(ContainerConfig containerConfig);

    /**
     * Sets the query batch size. This defaults to 100.
     *
     * @param queryBatchSize the new query batch size
     */
    WithAllOptions queryBatchSize(int queryBatchSize);

    /**
     * Set the default Enum type to use.
     */
    WithAllOptions defaultEnumType(EnumType defaultEnumType);

    /**
     * Set to true to disable lazy loading by default.
     * <p>
     * It can be turned on per query via {@link Query#setDisableLazyLoading(boolean)}.
     */
    WithAllOptions disableLazyLoading(boolean disableLazyLoading);

    /**
     * Set the ChangeLogPrepare.
     * <p>
     * This is used to set user context information to the ChangeSet in the
     * foreground thread prior to the logging occurring in a background thread.
     */
    WithAllOptions changeLogPrepare(ChangeLogPrepare changeLogPrepare);

    /**
     * Set the ChangeLogListener which actually performs the logging of change sets
     * in the background.
     */
    WithAllOptions changeLogListener(ChangeLogListener changeLogListener);

    /**
     * Set the ChangeLogRegister which controls which ChangeLogFilter is used for each
     * bean type and in this way provide fine grained control over which persist requests
     * are included in the change log.
     */
    WithAllOptions changeLogRegister(ChangeLogRegister changeLogRegister);

    /**
     * Set if inserts should be included in the change log by default.
     */
    WithAllOptions changeLogIncludeInserts(boolean changeLogIncludeInserts);

    /**
     * Sets if the changelog should be written async (default = true).
     */
    WithAllOptions changeLogAsync(boolean changeLogAsync);

    /**
     * Set the NamingConvention.
     * <p>
     * If none is set the default UnderscoreNamingConvention is used.
     */
    WithAllOptions namingConvention(NamingConvention namingConvention);

    /**
     * Set the configuration for DB platform (such as UUID and custom mappings).
     */
    WithAllOptions platformConfig(PlatformConfig platformConfig);

    /**
     * Programmatically add classes (typically entities) that this server should use.
     * <p>
     * The class can be an Entity, Embedded type, ScalarType, BeanPersistListener,
     * BeanFinder or BeanPersistController.
     * <p>
     * If no classes are specified then the classes are found automatically via
     * searching the class path.
     *
     * @param cls the entity type (or other type) that should be registered by this database.
     */
    WithAllOptions addClass(Class<?> cls);

    /**
     * Register all the classes (typically entity classes).
     */
    WithAllOptions addAll(Collection<Class<?>> classList);

    /**
     * Explicitly set the database platform name
     * <p>
     * If none is set then the platform is determined automatically via the JDBC
     * driver information.
     * <p>
     * This can be used when the Database Platform can not be automatically
     * detected from the JDBC driver (possibly 3rd party JDBC driver). It is also
     * useful when you want to do offline DDL generation for a database platform
     * that you don't have access to.
     * <p>
     * Values are oracle, h2, postgres, mysql, sqlserver16, sqlserver17.
     */
    WithAllOptions databasePlatformName(String databasePlatformName);

    /**
     * Explicitly set the database platform to use.
     * <p>
     * If none is set then the platform is determined via the databasePlatformName
     * or automatically via the JDBC driver information.
     */
    WithAllOptions databasePlatform(DatabasePlatform databasePlatform);

    /**
     * Set the preferred DB platform IdType.
     */
    WithAllOptions idType(IdType idType);

    /**
     * Set to true to generate the "create all" DDL on startup.
     * <p>
     * Typically we want this on when we are running tests locally (and often using H2)
     * and we want to create the full DB schema from scratch to run tests.
     */
    WithAllOptions ddlGenerate(boolean ddlGenerate);

    /**
     * Set to true to run the generated "create all DDL" on startup.
     * <p>
     * Typically we want this on when we are running tests locally (and often using H2)
     * and we want to create the full DB schema from scratch to run tests.
     */
    WithAllOptions ddlRun(boolean ddlRun);

    /**
     * Set to false if you not want to run the extra-ddl.xml scripts. (default = true)
     * <p>
     * Typically we want this on when we are running tests.
     */
    WithAllOptions ddlExtra(boolean ddlExtra);

    /**
     * Set to true if the "drop all ddl" should be skipped.
     * <p>
     * Typically we want to do this when using H2 (in memory) as our test database and the drop statements
     * are not required so skipping the drop table statements etc makes it faster with less noise in the logs.
     */
    WithAllOptions ddlCreateOnly(boolean ddlCreateOnly);

    /**
     * Set a SQL script to execute after the "create all" DDL has been run.
     * <p>
     * Typically this is a sql script that inserts test seed data when running tests.
     * Place a sql script in src/test/resources that inserts test seed data.
     */
    WithAllOptions ddlSeedSql(String ddlSeedSql);

    /**
     * Set a SQL script to execute before the "create all" DDL has been run.
     */
    WithAllOptions ddlInitSql(String ddlInitSql);

    /**
     * Set the header to use with DDL generation.
     */
    WithAllOptions ddlHeader(String ddlHeader);

    /**
     * Set to false to turn off strict mode allowing non-null columns to not have a default value.
     */
    WithAllOptions ddlStrictMode(boolean ddlStrictMode);

    /**
     * Set a comma and equals delimited placeholders that are substituted in DDL scripts.
     */
    WithAllOptions ddlPlaceholders(String ddlPlaceholders);

    /**
     * Set a map of placeholder values that are substituted in DDL scripts.
     */
    WithAllOptions ddlPlaceholderMap(Map<String, String> ddlPlaceholderMap);

    /**
     * Set the clock used for setting the timestamps (e.g. @UpdatedTimestamp) on objects.
     */
    WithAllOptions clock(final Clock clock);

    /**
     * Set the slow query time in millis.
     */
    WithAllOptions slowQueryMillis(long slowQueryMillis);

    /**
     * Set the slow query event listener.
     */
    WithAllOptions slowQueryListener(SlowQueryListener slowQueryListener);

    /**
     * Set the Jackson JsonFactory to use.
     * <p>
     * If not set a default implementation will be used.
     */
    WithAllOptions jsonFactory(JsonFactory jsonFactory);

    /**
     * Set the JSON format to use for DateTime types.
     */
    WithAllOptions jsonDateTime(JsonConfig.DateTime jsonDateTime);

    /**
     * Set the JSON format to use for Date types.
     */
    WithAllOptions jsonDate(JsonConfig.Date jsonDate);

    /**
     * Set the JSON include mode used when writing JSON.
     * <p>
     * Set to NON_NULL or NON_EMPTY to suppress nulls or null and empty collections respectively.
     */
    WithAllOptions jsonInclude(JsonConfig.Include jsonInclude);

    /**
     * Set the default MutableDetection to use with {@code @DbJson} using Jackson.
     *
     * @see DbJson#mutationDetection()
     */
    WithAllOptions jsonMutationDetection(MutationDetection jsonMutationDetection);

    /**
     * Set the CurrentUserProvider. This is used to populate @WhoCreated, @WhoModified and
     * support other audit features (who executed a query etc).
     */
    WithAllOptions currentUserProvider(CurrentUserProvider currentUserProvider);

    /**
     * Set the tenancy mode to use.
     */
    WithAllOptions tenantMode(TenantMode tenantMode);

    /**
     * Set the column name used for TenantMode.PARTITION.
     */
    WithAllOptions tenantPartitionColumn(String tenantPartitionColumn);

    /**
     * Set the current tenant provider.
     */
    WithAllOptions currentTenantProvider(CurrentTenantProvider currentTenantProvider);

    /**
     * Set the tenancy datasource provider.
     */
    WithAllOptions tenantDataSourceProvider(TenantDataSourceProvider tenantDataSourceProvider);

    /**
     * Set the tenancy schema provider.
     */
    WithAllOptions tenantSchemaProvider(TenantSchemaProvider tenantSchemaProvider);

    /**
     * Set the tenancy catalog provider.
     */
    WithAllOptions tenantCatalogProvider(TenantCatalogProvider tenantCatalogProvider);

    /**
     * Set to true if dirty beans are automatically persisted.
     */
    WithAllOptions autoPersistUpdates(boolean autoPersistUpdates);

    /**
     * Set the default batch size for lazy loading.
     * <p>
     * This is the number of beans or collections loaded when lazy loading is
     * invoked by default.
     * <p>
     * The default value is for this is 10 (load 10 beans or collections).
     * <p>
     * You can explicitly control the lazy loading batch size for a given join on
     * a query using +lazy(batchSize) or JoinConfig.
     */
    WithAllOptions lazyLoadBatchSize(int lazyLoadBatchSize);

    /**
     * Set the number of sequences to fetch/preallocate when using DB sequences.
     * <p>
     * This is a performance optimisation to reduce the number times Ebean
     * requests a sequence to be used as an Id for a bean (aka reduce network
     * chatter).
     */
    WithAllOptions databaseSequenceBatchSize(int databaseSequenceBatchSize);

    /**
     * Set the default JDBC fetchSize hint for findList queries.
     */
    WithAllOptions jdbcFetchSizeFindList(int jdbcFetchSizeFindList);

    /**
     * Set the default JDBC fetchSize hint for findEach/findEachWhile queries.
     */
    WithAllOptions jdbcFetchSizeFindEach(int jdbcFetchSizeFindEach);

    /**
     * Set the ReadAuditLogger to use. If not set the default implementation is used
     * which logs the read events in JSON format to a standard named SLF4J logger
     * (which can be configured in say logback to log to a separate log file).
     */
    WithAllOptions readAuditLogger(ReadAuditLogger readAuditLogger);

    /**
     * Set the ReadAuditPrepare to use.
     * <p>
     * It is expected that an implementation is used that read user context information
     * (user id, user ip address etc) and sets it on the ReadEvent bean before it is sent
     * to the ReadAuditLogger.
     */
    WithAllOptions readAuditPrepare(ReadAuditPrepare readAuditPrepare);

    /**
     * Set the configuration for profiling.
     */
    WithAllOptions profilingConfig(ProfilingConfig profilingConfig);

    /**
     * Set the suffix appended to the base table to derive the view that contains the union
     * of the base table and the history table in order to support asOf queries.
     */
    WithAllOptions asOfViewSuffix(String asOfViewSuffix);

    /**
     * Set the database column used to support history and 'As of' queries. This column is a timestamp range
     * or equivalent.
     */
    WithAllOptions asOfSysPeriod(String asOfSysPeriod);

    /**
     * Set the history table suffix.
     */
    WithAllOptions historyTableSuffix(String historyTableSuffix);

    /**
     * Set the ServerCachePlugin to use.
     */
    WithAllOptions serverCachePlugin(ServerCachePlugin serverCachePlugin);

    /**
     * Set to true if you want LOB's to be fetch eager by default.
     * By default this is set to false and LOB's must be explicitly fetched.
     */
    WithAllOptions eagerFetchLobs(boolean eagerFetchLobs);

    /**
     * Set the max call stack to use for origin location.
     */
    WithAllOptions maxCallStack(int maxCallStack);

    /**
     * Set to true if transactions should by default rollback on checked exceptions.
     */
    WithAllOptions transactionRollbackOnChecked(boolean transactionRollbackOnChecked);

    /**
     * Set the Background executor schedule pool size.
     */
    WithAllOptions backgroundExecutorSchedulePoolSize(int backgroundExecutorSchedulePoolSize);

    /**
     * Set the Background executor shutdown seconds. This is the time allowed for the pool to shutdown nicely
     * before it is forced shutdown.
     */
    WithAllOptions backgroundExecutorShutdownSecs(int backgroundExecutorShutdownSecs);

    /**
     * Sets the background executor wrapper. The wrapper is used when a task is sent to background and should copy the thread-locals.
     */
    WithAllOptions backgroundExecutorWrapper(BackgroundExecutorWrapper backgroundExecutorWrapper);

    /**
     * Set the constraint naming convention used in DDL generation.
     */
    WithAllOptions constraintNaming(DbConstraintNaming constraintNaming);

    /**
     * Set the configuration for AutoTune.
     */
    WithAllOptions autoTuneConfig(AutoTuneConfig autoTuneConfig);

    /**
     * Set to true to skip the startup DataSource check.
     */
    WithAllOptions skipDataSourceCheck(boolean skipDataSourceCheck);

    /**
     * Set the value to represent TRUE in the database.
     * <p>
     * This is used for databases that do not support boolean natively.
     * <p>
     * The value set is either a Integer or a String (e.g. "1", or "T").
     */
    WithAllOptions databaseBooleanTrue(String databaseTrue);

    /**
     * Set the value to represent FALSE in the database.
     * <p>
     * This is used for databases that do not support boolean natively.
     * <p>
     * The value set is either a Integer or a String (e.g. "0", or "F").
     */
    WithAllOptions databaseBooleanFalse(String databaseFalse);

    /**
     * Set the number of DB sequence values that should be preallocated and cached
     * by Ebean.
     * <p>
     * This is only used for DB's that use sequences and is a performance
     * optimisation. This reduces the number of times Ebean needs to get a
     * sequence value from the Database reducing network chatter.
     * <p>
     * By default this value is 10 so when we need another Id (and don't have one
     * in our cache) Ebean will fetch 10 id's from the database. Note that when
     * the cache drops to have full (which is 5 by default) Ebean will fetch
     * another batch of Id's in a background thread.
     */
    WithAllOptions databaseSequenceBatch(int databaseSequenceBatchSize);

    /**
     * Set the EncryptKeyManager.
     * <p>
     * This is required when you want to use encrypted properties.
     * <p>
     * You can also set this in ebean.proprerties:
     * <p>
     * <pre>{@code
     * # set via ebean.properties
     * ebean.encryptKeyManager=org.avaje.tests.basic.encrypt.BasicEncyptKeyManager
     * }</pre>
     */
    WithAllOptions encryptKeyManager(EncryptKeyManager encryptKeyManager);

    /**
     * Set the EncryptDeployManager.
     * <p>
     * This is optionally used to programmatically define which columns are
     * encrypted instead of using the {@link Encrypted} Annotation.
     */
    WithAllOptions encryptDeployManager(EncryptDeployManager encryptDeployManager);

    /**
     * Set the Encryptor used to encrypt data on the java client side (as opposed
     * to DB encryption functions).
     * <p>
     * Ebean has a default implementation that it will use if you do not set your
     * own Encryptor implementation.
     */
    WithAllOptions encryptor(Encryptor encryptor);

    /**
     * Set to true if the Database instance should be created in offline mode.
     * <p>
     * Typically used to create an Database instance for DDL Migration generation
     * without requiring a real DataSource / Database to connect to.
     */
    WithAllOptions dbOffline(boolean dbOffline);

    /**
     * Set the DbEncrypt used to encrypt and decrypt properties.
     * <p>
     * Note that if this is not set then the DbPlatform may already have a
     * DbEncrypt set (H2, MySql, Postgres and Oracle platforms have a DbEncrypt)
     */
    WithAllOptions dbEncrypt(DbEncrypt dbEncrypt);

    /**
     * Set the DB type used to store UUID.
     */
    WithAllOptions dbUuid(PlatformConfig.DbUuid dbUuid);

    /**
     * Sets the UUID version mode.
     */
    WithAllOptions uuidVersion(DatabaseConfig.UuidVersion uuidVersion);

    /**
     * Set the UUID state file.
     */
    WithAllOptions uuidStateFile(String uuidStateFile);

    /**
     * Sets the V1-UUID-NodeId.
     */
    WithAllOptions uuidNodeId(String uuidNodeId);

    /**
     * Set to true if LocalTime should be persisted with nanos precision.
     * <p>
     * Otherwise it is persisted using java.sql.Time which is seconds precision.
     */
    WithAllOptions localTimeWithNanos(boolean localTimeWithNanos);

    /**
     * Set to true if Duration should be persisted with nanos precision (SQL DECIMAL).
     * <p>
     * Otherwise it is persisted with second precision (SQL INTEGER).
     */
    WithAllOptions durationWithNanos(boolean durationWithNanos);

    /**
     * Set to true to disable the class path search even for the case where no entity bean classes
     * have been registered. This can be used to start an Database instance just to use the
     * SQL functions such as SqlQuery, SqlUpdate etc.
     */
    WithAllOptions disableClasspathSearch(boolean disableClasspathSearch);

    /**
     * Set the mode to use for Joda LocalTime support 'normal' or 'utc'.
     */
    WithAllOptions jodaLocalTimeMode(String jodaLocalTimeMode);

    /**
     * Set to false when we still want to hit the cache after a write has occurred on a transaction.
     */
    WithAllOptions skipCacheAfterWrite(boolean skipCacheAfterWrite);

    /**
     * Set to false if by default updates in JDBC batch should not include all properties.
     * <p>
     * This mode can be explicitly set per transaction.
     *
     * @see Transaction#setUpdateAllLoadedProperties(boolean)
     */
    WithAllOptions updateAllPropertiesInBatch(boolean updateAllPropertiesInBatch);

    /**
     * Sets the resource directory.
     */
    WithAllOptions resourceDirectory(String resourceDirectory);

    /**
     * Add a custom type mapping.
     * <p>
     * <pre>{@code
     *
     *   // set the default mapping for BigDecimal.class/decimal
     *   config.addCustomMapping(DbType.DECIMAL, "decimal(18,6)");
     *
     *   // set the default mapping for String.class/varchar but only for Postgres
     *   config.addCustomMapping(DbType.VARCHAR, "text", Platform.POSTGRES);
     *
     * }</pre>
     *
     * @param type             The DB type this mapping should apply to
     * @param columnDefinition The column definition that should be used
     * @param platform         Optionally specify the platform this mapping should apply to.
     */
    WithAllOptions addCustomMapping(DbType type, String columnDefinition, Platform platform);

    /**
     * Add a custom type mapping that applies to all platforms.
     * <p>
     * <pre>{@code
     *
     *   // set the default mapping for BigDecimal/decimal
     *   config.addCustomMapping(DbType.DECIMAL, "decimal(18,6)");
     *
     *   // set the default mapping for String/varchar
     *   config.addCustomMapping(DbType.VARCHAR, "text");
     *
     * }</pre>
     *
     * @param type             The DB type this mapping should apply to
     * @param columnDefinition The column definition that should be used
     */
    WithAllOptions addCustomMapping(DbType type, String columnDefinition);

    /**
     * Register a BeanQueryAdapter instance.
     * <p>
     * Note alternatively you can use {@link #queryAdapters(List)} to set all
     * the BeanQueryAdapter instances.
     */
    WithAllOptions add(BeanQueryAdapter beanQueryAdapter);

    /**
     * Register all the BeanQueryAdapter instances.
     * <p>
     * Note alternatively you can use {@link #add(BeanQueryAdapter)} to add
     * BeanQueryAdapter instances one at a time.
     */
    WithAllOptions queryAdapters(List<BeanQueryAdapter> queryAdapters);

    /**
     * Set the custom IdGenerator instances.
     */
    WithAllOptions idGenerators(List<IdGenerator> idGenerators);

    /**
     * Register a customer IdGenerator instance.
     */
    WithAllOptions add(IdGenerator idGenerator);

    /**
     * Register a BeanPersistController instance.
     * <p>
     * Note alternatively you can use {@link #persistControllers(List)} to set
     * all the BeanPersistController instances.
     */
    WithAllOptions add(BeanPersistController beanPersistController);

    /**
     * Register a BeanPostLoad instance.
     * <p>
     * Note alternatively you can use {@link #postLoaders(List)} to set
     * all the BeanPostLoad instances.
     */
    WithAllOptions add(BeanPostLoad postLoad);

    /**
     * Register a BeanPostConstructListener instance.
     * <p>
     * Note alternatively you can use {@link #postConstructListeners(List)} to set
     * all the BeanPostConstructListener instances.
     */
    WithAllOptions add(BeanPostConstructListener listener);

    /**
     * Set the list of BeanFindController instances.
     */
    WithAllOptions findControllers(List<BeanFindController> findControllers);

    /**
     * Set the list of BeanPostLoader instances.
     */
    WithAllOptions postLoaders(List<BeanPostLoad> postLoaders);

    /**
     * Set the list of BeanPostLoader instances.
     */
    WithAllOptions postConstructListeners(List<BeanPostConstructListener> listeners);

    /**
     * Register all the BeanPersistController instances.
     * <p>
     * Note alternatively you can use {@link #add(BeanPersistController)} to add
     * BeanPersistController instances one at a time.
     */
    WithAllOptions persistControllers(List<BeanPersistController> persistControllers);

    /**
     * Register a BeanPersistListener instance.
     * <p>
     * Note alternatively you can use {@link #persistListeners(List)} to set
     * all the BeanPersistListener instances.
     */
    WithAllOptions add(BeanPersistListener beanPersistListener);

    /**
     * Add a BulkTableEventListener
     */
    WithAllOptions add(BulkTableEventListener bulkTableEventListener);

    /**
     * Add a ServerConfigStartup.
     */
    WithAllOptions addServerConfigStartup(ServerConfigStartup configStartupListener);

    /**
     * Register all the BeanPersistListener instances.
     * <p>
     * Note alternatively you can use {@link #add(BeanPersistListener)} to add
     * BeanPersistListener instances one at a time.
     */
    WithAllOptions persistListeners(List<BeanPersistListener> persistListeners);

    /**
     * Set the PersistenceContext scope to be used if one is not explicitly set on a query.
     * <p/>
     * This defaults to {@link PersistenceContextScope#TRANSACTION}.
     * <p/>
     * The PersistenceContextScope can specified on each query via {@link io.ebean
     * .Query#setPersistenceContextScope(io.ebean.PersistenceContextScope)}. If it
     * is not set on the query this scope is used.
     *
     * @see Query#setPersistenceContextScope(PersistenceContextScope)
     */
    WithAllOptions persistenceContextScope(PersistenceContextScope persistenceContextScope);

    /**
     * Set the ClassLoadConfig which is used to detect Joda, Java8 types etc and also
     * create new instances of plugins given a className.
     */
    WithAllOptions classLoadConfig(ClassLoadConfig classLoadConfig);

    /**
     * Set to true if you want eq("someProperty", null) to generate "1=1" rather than "is null" sql expression.
     * <p>
     * Setting this to true has the effect that eq(propertyName, value), ieq(propertyName, value) and
     * ne(propertyName, value) have no effect when the value is null. The expression factory adds a NoopExpression
     * which will add "1=1" into the SQL rather than "is null".
     */
    WithAllOptions expressionEqualsWithNullAsNoop(boolean expressionEqualsWithNullAsNoop);

    /**
     * Set to true to use native ILIKE expression if supported by the database platform (e.g. Postgres).
     */
    WithAllOptions expressionNativeIlike(boolean expressionNativeIlike);

    /**
     * Controls if Ebean should ignore <code>&x64;javax.validation.contstraints.NotNull</code> or
     * <code>&x64;jakarta.validation.contstraints.NotNull</code>
     * with respect to generating a <code>NOT NULL</code> column.
     * <p>
     * Normally when Ebean sees javax NotNull annotation it means that column is defined as NOT NULL.
     * Set this to <code>false</code> and the javax NotNull annotation is effectively ignored (and
     * we instead use Ebean's own NotNull annotation or JPA Column(nullable=false) annotation.
     */
    WithAllOptions useValidationNotNull(boolean useValidationNotNull);

    /**
     * Set to false such that Id properties require explicit <code>@GeneratedValue</code>
     * mapping before they are assigned Identity or Sequence generation based on platform.
     */
    WithAllOptions idGeneratorAutomatic(boolean idGeneratorAutomatic);

    /**
     * Set false to turn off automatic registration of entity beans.
     * <p>
     * When using query beans that also generates a module info class that
     * can register the entity bean classes (to avoid classpath scanning).
     * This is on by default and setting this to false turns it off.
     */
    WithAllOptions loadModuleInfo(boolean loadModuleInfo);

  }
}
