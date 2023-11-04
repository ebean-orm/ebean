package io.ebean;

import com.fasterxml.jackson.core.JsonFactory;
import io.ebean.annotation.*;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.*;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbEncrypt;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.datasource.DataSourceBuilder;
import io.ebean.event.*;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import jakarta.persistence.EnumType;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.*;
import java.util.function.Function;

/**
 * Build a Database instance.
 *
 * <pre>{@code
 *
 *   // build the 'default' database using configuration
 *   // from application.properties / application.yaml
 *
 *   Database db = Database.builder()
 *     .loadFromProperties()
 *     .build();
 *
 * }</pre>
 *
 * Create a non-default database and not register it with {@link DB}. When
 * not registered the database can not by obtained via {@link DB#byName(String)}.
 *
 * <pre>{@code
 *
 *   Database database = Database.builder()
 *     .setName("other"
 *     .loadFromProperties()
 *     .setRegister(false)
 *     .setDefaultServer(false)
 *     .addClass(EBasic.class)
 *     .build();
 *
 * }</pre>
 */
public interface DatabaseBuilder {

  /**
   * Build and return the Database instance.
   */
  Database build();

  /**
   * Return the settings to read the configuration that has been set. This
   * provides the getters/accessors to read the configuration properties.
   */
  Settings settings();

  /**
   * Set the name of the Database.
   */
  DatabaseBuilder setName(String name);

  /**
   * Set to false if you do not want this server to be registered with the Ebean
   * singleton when it is created.
   * <p>
   * By default, this is set to true.
   */
  DatabaseBuilder setRegister(boolean register);

  /**
   * Set false if you do not want this Database to be registered as the "default" database
   * with the DB singleton.
   * <p>
   * This is only used when {@link #setRegister(boolean)} is also true.
   */
  DatabaseBuilder setDefaultServer(boolean defaultServer);

  /**
   * Set the DB schema to use. This specifies to use this schema for:
   * <ul>
   * <li>Running Database migrations - Create and use the DB schema</li>
   * <li>Testing DDL - Create-all.sql DDL execution creates and uses schema</li>
   * <li>Testing Docker - Set default schema on connection URL</li>
   * </ul>
   */
  DatabaseBuilder setDbSchema(String dbSchema);

  /**
   * Set the Geometry SRID.
   */
  DatabaseBuilder setGeometrySRID(int geometrySRID);

  /**
   * Set the time zone to use when reading/writing Timestamps via JDBC.
   */
  DatabaseBuilder setDataTimeZone(String dataTimeZone);

  /**
   * Set the JDBC batch mode to use at the transaction level.
   * <p>
   * When INSERT or ALL is used then save(), delete() etc do not execute immediately but instead go into
   * a JDBC batch execute buffer that is flushed. The buffer is flushed if a query is executed, transaction ends
   * or the batch size is meet.
   */
  DatabaseBuilder setPersistBatch(PersistBatch persistBatch);

  /**
   * Set the JDBC batch mode to use per save(), delete(), insert() or update() request.
   * <p>
   * This makes sense when a save() or delete() etc cascades and executes multiple child statements. The best caase
   * for this is when saving a master/parent bean this cascade inserts many detail/child beans.
   * <p>
   * This only takes effect when the persistBatch mode at the transaction level does not take effect.
   */
  DatabaseBuilder setPersistBatchOnCascade(PersistBatch persistBatchOnCascade);

  /**
   * Deprecated, please migrate to using setPersistBatch().
   * <p>
   * Set to true if you what to use JDBC batching for persisting and deleting beans.
   * <p>
   * With this Ebean will batch up persist requests and use the JDBC batch api.
   * This is a performance optimisation designed to reduce the network chatter.
   * <p>
   * When true this is equivalent to {@code setPersistBatch(PersistBatch.ALL)} or
   * when false to {@code setPersistBatch(PersistBatch.NONE)}
   */
  DatabaseBuilder setPersistBatching(boolean persistBatching);

  /**
   * Set the batch size used for JDBC batching. If unset this defaults to 20.
   * <p>
   * You can also set the batch size on the transaction.
   *
   * @see Transaction#setBatchSize(int)
   */
  DatabaseBuilder setPersistBatchSize(int persistBatchSize);


  /**
   * Set to true to disable lazy loading by default.
   * <p>
   * It can be turned on per query via {@link Query#setDisableLazyLoading(boolean)}.
   */
  DatabaseBuilder setDisableLazyLoading(boolean disableLazyLoading);

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
  DatabaseBuilder setLazyLoadBatchSize(int lazyLoadBatchSize);

  /**
   * Set the clock used for setting the timestamps (e.g. @UpdatedTimestamp) on objects.
   */
  DatabaseBuilder setClock(Clock clock);

  /**
   * Set the slow query time in millis.
   */
  DatabaseBuilder setSlowQueryMillis(long slowQueryMillis);

  /**
   * Set the slow query event listener.
   */
  DatabaseBuilder setSlowQueryListener(SlowQueryListener slowQueryListener);

  /**
   * Put a service object into configuration such that it can be used by ebean or a plugin.
   * <p>
   * For example, put IgniteConfiguration in to be passed to the Ignite plugin.
   */
  DatabaseBuilder putServiceObject(String key, Object configObject);

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
  <T> DatabaseBuilder putServiceObject(Class<T> iface, T configObject);

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
  DatabaseBuilder putServiceObject(Object configObject);

  /**
   * Set the Jackson JsonFactory to use.
   * <p>
   * If not set a default implementation will be used.
   */
  DatabaseBuilder setJsonFactory(JsonFactory jsonFactory);

  /**
   * Set the JSON format to use for DateTime types.
   */
  DatabaseBuilder setJsonDateTime(JsonConfig.DateTime jsonDateTime);

  /**
   * Set the JSON format to use for Date types.
   */
  DatabaseBuilder setJsonDate(JsonConfig.Date jsonDate);

  /**
   * Set the JSON include mode used when writing JSON.
   * <p>
   * Set to NON_NULL or NON_EMPTY to suppress nulls or null and empty collections respectively.
   */
  DatabaseBuilder setJsonInclude(JsonConfig.Include jsonInclude);

  /**
   * Set the default MutableDetection to use with {@code @DbJson} using Jackson.
   *
   * @see DbJson#mutationDetection()
   */
  DatabaseBuilder setJsonMutationDetection(MutationDetection jsonMutationDetection);

  /**
   * Set the container / clustering configuration.
   * <p/>
   * The container holds all the Database instances and provides clustering communication
   * services to all the Database instances.
   */
  DatabaseBuilder setContainerConfig(ContainerConfig containerConfig);

  /**
   * Set the CurrentUserProvider. This is used to populate @WhoCreated, @WhoModified and
   * support other audit features (who executed a query etc).
   */
  DatabaseBuilder setCurrentUserProvider(CurrentUserProvider currentUserProvider);

  /**
   * Set the tenancy mode to use.
   */
  DatabaseBuilder setTenantMode(TenantMode tenantMode);

  /**
   * Set the column name used for TenantMode.PARTITION.
   */
  DatabaseBuilder setTenantPartitionColumn(String tenantPartitionColumn);

  /**
   * Set the current tenant provider.
   */
  DatabaseBuilder setCurrentTenantProvider(CurrentTenantProvider currentTenantProvider);

  /**
   * Set the tenancy datasource provider.
   */
  DatabaseBuilder setTenantDataSourceProvider(TenantDataSourceProvider tenantDataSourceProvider);

  /**
   * Set the tenancy schema provider.
   */
  DatabaseBuilder setTenantSchemaProvider(TenantSchemaProvider tenantSchemaProvider);

  /**
   * Set the tenancy catalog provider.
   */
  DatabaseBuilder setTenantCatalogProvider(TenantCatalogProvider tenantCatalogProvider);

  /**
   * Set to true if dirty beans are automatically persisted.
   */
  DatabaseBuilder setAutoPersistUpdates(boolean autoPersistUpdates);

  /**
   * Sets the query batch size. This defaults to 100.
   *
   * @param queryBatchSize the new query batch size
   */
  DatabaseBuilder setQueryBatchSize(int queryBatchSize);

  DatabaseBuilder setDefaultEnumType(EnumType defaultEnumType);

  /**
   * Set the number of sequences to fetch/preallocate when using DB sequences.
   * <p>
   * This is a performance optimisation to reduce the number times Ebean
   * requests a sequence to be used as an Id for a bean (aka reduce network
   * chatter).
   */
  DatabaseBuilder setDatabaseSequenceBatchSize(int databaseSequenceBatchSize);

  /**
   * Set the default JDBC fetchSize hint for findList queries.
   */
  DatabaseBuilder setJdbcFetchSizeFindList(int jdbcFetchSizeFindList);

  /**
   * Set the default JDBC fetchSize hint for findEach/findEachWhile queries.
   */
  DatabaseBuilder setJdbcFetchSizeFindEach(int jdbcFetchSizeFindEach);

  /**
   * Set the ChangeLogPrepare.
   * <p>
   * This is used to set user context information to the ChangeSet in the
   * foreground thread prior to the logging occurring in a background thread.
   */
  DatabaseBuilder setChangeLogPrepare(ChangeLogPrepare changeLogPrepare);

  /**
   * Set the ChangeLogListener which actually performs the logging of change sets
   * in the background.
   */
  DatabaseBuilder setChangeLogListener(ChangeLogListener changeLogListener);

  /**
   * Set the ChangeLogRegister which controls which ChangeLogFilter is used for each
   * bean type and in this way provide fine grained control over which persist requests
   * are included in the change log.
   */
  DatabaseBuilder setChangeLogRegister(ChangeLogRegister changeLogRegister);

  /**
   * Set if inserts should be included in the change log by default.
   */
  DatabaseBuilder setChangeLogIncludeInserts(boolean changeLogIncludeInserts);

  /**
   * Sets if the changelog should be written async (default = true).
   */
  DatabaseBuilder setChangeLogAsync(boolean changeLogAsync);

  /**
   * Set the ReadAuditLogger to use. If not set the default implementation is used
   * which logs the read events in JSON format to a standard named SLF4J logger
   * (which can be configured in say logback to log to a separate log file).
   */
  DatabaseBuilder setReadAuditLogger(ReadAuditLogger readAuditLogger);

  /**
   * Set the ReadAuditPrepare to use.
   * <p>
   * It is expected that an implementation is used that read user context information
   * (user id, user ip address etc) and sets it on the ReadEvent bean before it is sent
   * to the ReadAuditLogger.
   */
  DatabaseBuilder setReadAuditPrepare(ReadAuditPrepare readAuditPrepare);

  /**
   * Set the configuration for profiling.
   */
  DatabaseBuilder setProfilingConfig(ProfilingConfig profilingConfig);

  /**
   * Set the suffix appended to the base table to derive the view that contains the union
   * of the base table and the history table in order to support asOf queries.
   */
  DatabaseBuilder setAsOfViewSuffix(String asOfViewSuffix);

  /**
   * Set the database column used to support history and 'As of' queries. This column is a timestamp range
   * or equivalent.
   */
  DatabaseBuilder setAsOfSysPeriod(String asOfSysPeriod);

  /**
   * Set the history table suffix.
   */
  DatabaseBuilder setHistoryTableSuffix(String historyTableSuffix);

  /**
   * Set to true if we are running in a JTA Transaction manager.
   */
  DatabaseBuilder setUseJtaTransactionManager(boolean useJtaTransactionManager);

  /**
   * Set the external transaction manager.
   */
  DatabaseBuilder setExternalTransactionManager(ExternalTransactionManager externalTransactionManager);

  /**
   * Set the ServerCachePlugin to use.
   */
  DatabaseBuilder setServerCachePlugin(ServerCachePlugin serverCachePlugin);

  /**
   * Set to true if you want LOB's to be fetch eager by default.
   * By default this is set to false and LOB's must be explicitly fetched.
   */
  DatabaseBuilder setEagerFetchLobs(boolean eagerFetchLobs);

  /**
   * Set the max call stack to use for origin location.
   */
  DatabaseBuilder setMaxCallStack(int maxCallStack);

  /**
   * Set to true if transactions should by default rollback on checked exceptions.
   */
  DatabaseBuilder setTransactionRollbackOnChecked(boolean transactionRollbackOnChecked);

  /**
   * Set the Background executor schedule pool size.
   */
  DatabaseBuilder setBackgroundExecutorSchedulePoolSize(int backgroundExecutorSchedulePoolSize);

  /**
   * Set the Background executor shutdown seconds. This is the time allowed for the pool to shutdown nicely
   * before it is forced shutdown.
   */
  DatabaseBuilder setBackgroundExecutorShutdownSecs(int backgroundExecutorShutdownSecs);

  /**
   * Sets the background executor wrapper. The wrapper is used when a task is sent to background and should copy the thread-locals.
   */
  DatabaseBuilder setBackgroundExecutorWrapper(BackgroundExecutorWrapper backgroundExecutorWrapper);

  /**
   * Set the L2 cache default max size.
   */
  DatabaseBuilder setCacheMaxSize(int cacheMaxSize);

  /**
   * Set the L2 cache default max idle time in seconds.
   */
  DatabaseBuilder setCacheMaxIdleTime(int cacheMaxIdleTime);

  /**
   * Set the L2 cache default max time to live in seconds.
   */
  DatabaseBuilder setCacheMaxTimeToLive(int cacheMaxTimeToLive);

  /**
   * Set the L2 query cache default max size.
   */
  DatabaseBuilder setQueryCacheMaxSize(int queryCacheMaxSize);

  /**
   * Set the L2 query cache default max idle time in seconds.
   */
  DatabaseBuilder setQueryCacheMaxIdleTime(int queryCacheMaxIdleTime);

  /**
   * Set the L2 query cache default max time to live in seconds.
   */
  DatabaseBuilder setQueryCacheMaxTimeToLive(int queryCacheMaxTimeToLive);

  /**
   * Set the NamingConvention.
   * <p>
   * If none is set the default UnderscoreNamingConvention is used.
   */
  DatabaseBuilder setNamingConvention(NamingConvention namingConvention);

  /**
   * Set to true if all DB column and table names should use quoted identifiers.
   * <p>
   * For Postgres pgjdbc version 42.3.0 should be used with datasource property
   * <em>quoteReturningIdentifiers</em> set to <em>false</em> (refer #2303).
   */
  DatabaseBuilder setAllQuotedIdentifiers(boolean allQuotedIdentifiers);

  /**
   * Set to true if this Database is Document store only instance (has no JDBC DB).
   */
  DatabaseBuilder setDocStoreOnly(boolean docStoreOnly);

  /**
   * Set the configuration for the ElasticSearch integration.
   */
  DatabaseBuilder setDocStoreConfig(DocStoreConfig docStoreConfig);

  /**
   * Set the constraint naming convention used in DDL generation.
   */
  DatabaseBuilder setConstraintNaming(DbConstraintNaming constraintNaming);

  /**
   * Set the configuration for AutoTune.
   */
  DatabaseBuilder setAutoTuneConfig(AutoTuneConfig autoTuneConfig);

  /**
   * Set to true to skip the startup DataSource check.
   */
  DatabaseBuilder setSkipDataSourceCheck(boolean skipDataSourceCheck);

  /**
   * Set a DataSource.
   */
  DatabaseBuilder setDataSource(DataSource dataSource);

  /**
   * Set the read only DataSource.
   * <p>
   * Note that the DataSource is expected to use AutoCommit true mode avoiding the need
   * for explicit commit (or rollback).
   * <p>
   * This read only DataSource will be used for implicit query only transactions. It is not
   * used if the transaction is created explicitly or if the query is an update or delete query.
   */
  DatabaseBuilder setReadOnlyDataSource(DataSource readOnlyDataSource);

  /**
   * Set the configuration required to build a DataSource using Ebean's own
   * DataSource implementation.
   */
  DatabaseBuilder setDataSourceConfig(DataSourceBuilder dataSourceConfig);

  /**
   * Set to true if Ebean should create a DataSource for use with implicit read only transactions.
   */
  DatabaseBuilder setAutoReadOnlyDataSource(boolean autoReadOnlyDataSource);

  /**
   * Set the configuration for the read only DataSource.
   */
  DatabaseBuilder setReadOnlyDataSourceConfig(DataSourceBuilder readOnlyDataSourceConfig);

  /**
   * Set the value to represent TRUE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * <p>
   * The value set is either a Integer or a String (e.g. "1", or "T").
   */
  DatabaseBuilder setDatabaseBooleanTrue(String databaseTrue);

  /**
   * Set the value to represent FALSE in the database.
   * <p>
   * This is used for databases that do not support boolean natively.
   * <p>
   * The value set is either a Integer or a String (e.g. "0", or "F").
   */
  DatabaseBuilder setDatabaseBooleanFalse(String databaseFalse);

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
  DatabaseBuilder setDatabaseSequenceBatch(int databaseSequenceBatchSize);

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
  DatabaseBuilder setDatabasePlatformName(String databasePlatformName);

  /**
   * Explicitly set the database platform to use.
   * <p>
   * If none is set then the platform is determined via the databasePlatformName
   * or automatically via the JDBC driver information.
   */
  DatabaseBuilder setDatabasePlatform(DatabasePlatform databasePlatform);

  /**
   * Set the preferred DB platform IdType.
   */
  DatabaseBuilder setIdType(IdType idType);

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
  DatabaseBuilder setEncryptKeyManager(EncryptKeyManager encryptKeyManager);

  /**
   * Set the EncryptDeployManager.
   * <p>
   * This is optionally used to programmatically define which columns are
   * encrypted instead of using the {@link Encrypted} Annotation.
   */
  DatabaseBuilder setEncryptDeployManager(EncryptDeployManager encryptDeployManager);

  /**
   * Set the Encryptor used to encrypt data on the java client side (as opposed
   * to DB encryption functions).
   * <p>
   * Ebean has a default implementation that it will use if you do not set your
   * own Encryptor implementation.
   */
  DatabaseBuilder setEncryptor(Encryptor encryptor);

  /**
   * Set to true if the Database instance should be created in offline mode.
   * <p>
   * Typically used to create an Database instance for DDL Migration generation
   * without requiring a real DataSource / Database to connect to.
   */
  DatabaseBuilder setDbOffline(boolean dbOffline);

  /**
   * Set the DbEncrypt used to encrypt and decrypt properties.
   * <p>
   * Note that if this is not set then the DbPlatform may already have a
   * DbEncrypt set (H2, MySql, Postgres and Oracle platforms have a DbEncrypt)
   */
  DatabaseBuilder setDbEncrypt(DbEncrypt dbEncrypt);

  /**
   * Set the configuration for DB platform (such as UUID and custom mappings).
   */
  DatabaseBuilder setPlatformConfig(PlatformConfig platformConfig);

  /**
   * Set the DB type used to store UUID.
   */
  DatabaseBuilder setDbUuid(PlatformConfig.DbUuid dbUuid);

  /**
   * Sets the UUID version mode.
   */
  DatabaseBuilder setUuidVersion(DatabaseConfig.UuidVersion uuidVersion);

  /**
   * Set the UUID state file.
   */
  DatabaseBuilder setUuidStateFile(String uuidStateFile);

  /**
   * Sets the V1-UUID-NodeId.
   */
  DatabaseBuilder setUuidNodeId(String uuidNodeId);

  /**
   * Set to true if LocalTime should be persisted with nanos precision.
   * <p>
   * Otherwise it is persisted using java.sql.Time which is seconds precision.
   */
  DatabaseBuilder setLocalTimeWithNanos(boolean localTimeWithNanos);

  /**
   * Set to true if Duration should be persisted with nanos precision (SQL DECIMAL).
   * <p>
   * Otherwise it is persisted with second precision (SQL INTEGER).
   */
  DatabaseBuilder setDurationWithNanos(boolean durationWithNanos);

  /**
   * Set to true to run DB migrations on server start.
   * <p>
   * This is the same as config.getMigrationConfig().setRunMigration(). We have added this method here
   * as it is often the only thing we need to configure for migrations.
   */
  DatabaseBuilder setRunMigration(boolean runMigration);

  /**
   * Set to true to generate the "create all" DDL on startup.
   * <p>
   * Typically we want this on when we are running tests locally (and often using H2)
   * and we want to create the full DB schema from scratch to run tests.
   */
  DatabaseBuilder setDdlGenerate(boolean ddlGenerate);

  /**
   * Set to true to run the generated "create all DDL" on startup.
   * <p>
   * Typically we want this on when we are running tests locally (and often using H2)
   * and we want to create the full DB schema from scratch to run tests.
   */
  DatabaseBuilder setDdlRun(boolean ddlRun);

  /**
   * Set to false if you not want to run the extra-ddl.xml scripts. (default = true)
   * <p>
   * Typically we want this on when we are running tests.
   */
  DatabaseBuilder setDdlExtra(boolean ddlExtra);

  /**
   * Set to true if the "drop all ddl" should be skipped.
   * <p>
   * Typically we want to do this when using H2 (in memory) as our test database and the drop statements
   * are not required so skipping the drop table statements etc makes it faster with less noise in the logs.
   */
  DatabaseBuilder setDdlCreateOnly(boolean ddlCreateOnly);

  /**
   * Set a SQL script to execute after the "create all" DDL has been run.
   * <p>
   * Typically this is a sql script that inserts test seed data when running tests.
   * Place a sql script in src/test/resources that inserts test seed data.
   */
  DatabaseBuilder setDdlSeedSql(String ddlSeedSql);

  /**
   * Set a SQL script to execute before the "create all" DDL has been run.
   */
  DatabaseBuilder setDdlInitSql(String ddlInitSql);

  /**
   * Set the header to use with DDL generation.
   */
  DatabaseBuilder setDdlHeader(String ddlHeader);

  /**
   * Set to false to turn off strict mode allowing non-null columns to not have a default value.
   */
  DatabaseBuilder setDdlStrictMode(boolean ddlStrictMode);

  /**
   * Set a comma and equals delimited placeholders that are substituted in DDL scripts.
   */
  DatabaseBuilder setDdlPlaceholders(String ddlPlaceholders);

  /**
   * Set a map of placeholder values that are substituted in DDL scripts.
   */
  DatabaseBuilder setDdlPlaceholderMap(Map<String, String> ddlPlaceholderMap);

  /**
   * Set to true to disable the class path search even for the case where no entity bean classes
   * have been registered. This can be used to start an Database instance just to use the
   * SQL functions such as SqlQuery, SqlUpdate etc.
   */
  DatabaseBuilder setDisableClasspathSearch(boolean disableClasspathSearch);

  /**
   * Set the mode to use for Joda LocalTime support 'normal' or 'utc'.
   */
  DatabaseBuilder setJodaLocalTimeMode(String jodaLocalTimeMode);

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
  DatabaseBuilder addClass(Class<?> cls);

  /**
   * Register all the classes (typically entity classes).
   */
  DatabaseBuilder addAll(Collection<Class<?>> classList);

  /**
   * Add a package to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   */
  DatabaseBuilder addPackage(String packageName);

  /**
   * Set packages to search for entities via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   */
  DatabaseBuilder setPackages(List<String> packages);

  /**
   * Set the list of classes (entities, listeners, scalarTypes etc) that should
   * be used for this database.
   * <p>
   * If no classes are specified then the classes are found automatically via
   * searching the class path.
   * <p>
   * Alternatively the classes can contain added via {@link #addClass(Class)}.
   */
  DatabaseBuilder classes(Collection<Class<?>> classes);

  /**
   * Set to false when we still want to hit the cache after a write has occurred on a transaction.
   */
  DatabaseBuilder setSkipCacheAfterWrite(boolean skipCacheAfterWrite);

  /**
   * Set to false if by default updates in JDBC batch should not include all properties.
   * <p>
   * This mode can be explicitly set per transaction.
   *
   * @see Transaction#setUpdateAllLoadedProperties(boolean)
   */
  DatabaseBuilder setUpdateAllPropertiesInBatch(boolean updateAllPropertiesInBatch);

  /**
   * Sets the resource directory.
   */
  DatabaseBuilder setResourceDirectory(String resourceDirectory);

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
  DatabaseBuilder addCustomMapping(DbType type, String columnDefinition, Platform platform);

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
  DatabaseBuilder addCustomMapping(DbType type, String columnDefinition);

  /**
   * Register a BeanQueryAdapter instance.
   * <p>
   * Note alternatively you can use {@link #setQueryAdapters(List)} to set all
   * the BeanQueryAdapter instances.
   */
  DatabaseBuilder add(BeanQueryAdapter beanQueryAdapter);

  /**
   * Register all the BeanQueryAdapter instances.
   * <p>
   * Note alternatively you can use {@link #add(BeanQueryAdapter)} to add
   * BeanQueryAdapter instances one at a time.
   */
  DatabaseBuilder setQueryAdapters(List<BeanQueryAdapter> queryAdapters);

  /**
   * Set the custom IdGenerator instances.
   */
  DatabaseBuilder setIdGenerators(List<IdGenerator> idGenerators);

  /**
   * Register a customer IdGenerator instance.
   */
  DatabaseBuilder add(IdGenerator idGenerator);

  /**
   * Register a BeanPersistController instance.
   * <p>
   * Note alternatively you can use {@link #setPersistControllers(List)} to set
   * all the BeanPersistController instances.
   */
  DatabaseBuilder add(BeanPersistController beanPersistController);

  /**
   * Register a BeanPostLoad instance.
   * <p>
   * Note alternatively you can use {@link #setPostLoaders(List)} to set
   * all the BeanPostLoad instances.
   */
  DatabaseBuilder add(BeanPostLoad postLoad);

  /**
   * Register a BeanPostConstructListener instance.
   * <p>
   * Note alternatively you can use {@link #setPostConstructListeners(List)} to set
   * all the BeanPostConstructListener instances.
   */
  DatabaseBuilder add(BeanPostConstructListener listener);

  /**
   * Set the list of BeanFindController instances.
   */
  DatabaseBuilder setFindControllers(List<BeanFindController> findControllers);

  /**
   * Set the list of BeanPostLoader instances.
   */
  DatabaseBuilder setPostLoaders(List<BeanPostLoad> postLoaders);

  /**
   * Set the list of BeanPostLoader instances.
   */
  DatabaseBuilder setPostConstructListeners(List<BeanPostConstructListener> listeners);

  /**
   * Register all the BeanPersistController instances.
   * <p>
   * Note alternatively you can use {@link #add(BeanPersistController)} to add
   * BeanPersistController instances one at a time.
   */
  DatabaseBuilder setPersistControllers(List<BeanPersistController> persistControllers);

  /**
   * Register a BeanPersistListener instance.
   * <p>
   * Note alternatively you can use {@link #setPersistListeners(List)} to set
   * all the BeanPersistListener instances.
   */
  DatabaseBuilder add(BeanPersistListener beanPersistListener);

  /**
   * Add a BulkTableEventListener
   */
  DatabaseBuilder add(BulkTableEventListener bulkTableEventListener);

  /**
   * Add a ServerConfigStartup.
   */
  DatabaseBuilder addServerConfigStartup(ServerConfigStartup configStartupListener);

  /**
   * Register all the BeanPersistListener instances.
   * <p>
   * Note alternatively you can use {@link #add(BeanPersistListener)} to add
   * BeanPersistListener instances one at a time.
   */
  DatabaseBuilder setPersistListeners(List<BeanPersistListener> persistListeners);

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
  DatabaseBuilder setPersistenceContextScope(PersistenceContextScope persistenceContextScope);

  /**
   * Set the ClassLoadConfig which is used to detect Joda, Java8 types etc and also
   * create new instances of plugins given a className.
   */
  DatabaseBuilder setClassLoadConfig(ClassLoadConfig classLoadConfig);

  /**
   * Load settings from application.properties, application.yaml and other sources.
   * <p>
   * Uses <code>avaje-config</code> to load configuration properties.  Goto https://avaje.io/config
   * for detail on how and where properties are loaded from.
   */
  DatabaseBuilder loadFromProperties();

  /**
   * Load the settings from the given properties
   */
  DatabaseBuilder loadFromProperties(Properties properties);

  /**
   * Set the Jackson ObjectMapper.
   * <p>
   * Note that this is not strongly typed as Jackson ObjectMapper is an optional dependency.
   */
  DatabaseBuilder setObjectMapper(Object objectMapper);

  /**
   * Set to true if you want eq("someProperty", null) to generate "1=1" rather than "is null" sql expression.
   * <p>
   * Setting this to true has the effect that eq(propertyName, value), ieq(propertyName, value) and
   * ne(propertyName, value) have no effect when the value is null. The expression factory adds a NoopExpression
   * which will add "1=1" into the SQL rather than "is null".
   */
  DatabaseBuilder setExpressionEqualsWithNullAsNoop(boolean expressionEqualsWithNullAsNoop);

  /**
   * Set to true to use native ILIKE expression if supported by the database platform (e.g. Postgres).
   */
  DatabaseBuilder setExpressionNativeIlike(boolean expressionNativeIlike);

  /**
   * Set the enabled L2 cache regions (comma delimited).
   */
  DatabaseBuilder setEnabledL2Regions(String enabledL2Regions);

  /**
   * Set to true to disable L2 caching. Typically useful in performance testing.
   */
  DatabaseBuilder setDisableL2Cache(boolean disableL2Cache);

  /**
   * Force the use of local only L2 cache. Effectively ignore l2 cache plugin like ebean-redis etc.
   */
  DatabaseBuilder setLocalOnlyL2Cache(boolean localOnlyL2Cache);

  /**
   * Controls if Ebean should ignore <code>&x64;javax.validation.contstraints.NotNull</code> or
   * <code>&x64;jakarta.validation.contstraints.NotNull</code>
   * with respect to generating a <code>NOT NULL</code> column.
   * <p>
   * Normally when Ebean sees javax NotNull annotation it means that column is defined as NOT NULL.
   * Set this to <code>false</code> and the javax NotNull annotation is effectively ignored (and
   * we instead use Ebean's own NotNull annotation or JPA Column(nullable=false) annotation.
   */
  DatabaseBuilder setUseValidationNotNull(boolean useValidationNotNull);

  /**
   * Set this to true to run L2 cache notification in the foreground.
   * <p>
   * In general we don't want to do that as when we use a distributed cache (like Ignite, Hazelcast etc)
   * we are making network calls and we prefer to do this in background and not impact the response time
   * of the executing transaction.
   */
  DatabaseBuilder setNotifyL2CacheInForeground(boolean notifyL2CacheInForeground);

  /**
   * Set the time to live for ebean's internal query plan.
   * <p>
   * This is the plan that knows how to execute the query, read the result
   * and collects execution metrics. By default this is set to 5 mins.
   */
  DatabaseBuilder setQueryPlanTTLSeconds(int queryPlanTTLSeconds);

  /**
   * Create a new PlatformConfig based of the one held but with overridden properties by reading
   * properties with the given path and prefix.
   * <p>
   * Typically used in Db Migration generation for many platform targets that might have different
   * configuration for IdType, UUID, quoted identifiers etc.
   *
   * @param propertiesPath The properties path used for loading and setting properties
   * @param platformPrefix The prefix used for loading and setting properties
   * @return A copy of the PlatformConfig with overridden properties
   */
  PlatformConfig newPlatformConfig(String propertiesPath, String platformPrefix);

  /**
   * Add a mapping location to search for xml mapping via class path search.
   */
  DatabaseBuilder addMappingLocation(String mappingLocation);

  /**
   * Set mapping locations to search for xml mapping via class path search.
   * <p>
   * This is only used if classes have not been explicitly specified.
   */
  DatabaseBuilder setMappingLocations(List<String> mappingLocations);

  /**
   * Set to false such that Id properties require explicit <code>@GeneratedValue</code>
   * mapping before they are assigned Identity or Sequence generation based on platform.
   */
  DatabaseBuilder setIdGeneratorAutomatic(boolean idGeneratorAutomatic);

  /**
   * Set to true to enable query plan capture.
   */
  DatabaseBuilder setQueryPlanEnable(boolean queryPlanEnable);

  /**
   * Set the query plan collection threshold in microseconds.
   * <p>
   * Queries executing slower than this will have bind values captured such that later
   * the query plan can be captured and reported.
   */
  DatabaseBuilder setQueryPlanThresholdMicros(long queryPlanThresholdMicros);

  /**
   * Set to true to turn on periodic capture of query plans.
   */
  DatabaseBuilder setQueryPlanCapture(boolean queryPlanCapture);

  /**
   * Set the frequency in seconds to capture query plans.
   */
  DatabaseBuilder setQueryPlanCapturePeriodSecs(long queryPlanCapturePeriodSecs);

  /**
   * Set the time after which a capture query plans request will
   * stop capturing more query plans.
   * <p>
   * Effectively this controls the amount of load/time we want to
   * allow for query plan capture.
   */
  DatabaseBuilder setQueryPlanCaptureMaxTimeMillis(long queryPlanCaptureMaxTimeMillis);

  /**
   * Set the max number of query plans captured per request.
   */
  DatabaseBuilder setQueryPlanCaptureMaxCount(int queryPlanCaptureMaxCount);

  /**
   * Set the listener used to process captured query plans.
   */
  DatabaseBuilder setQueryPlanListener(QueryPlanListener queryPlanListener);

  /**
   * Set to true if metrics should be dumped when the server is shutdown.
   */
  DatabaseBuilder setDumpMetricsOnShutdown(boolean dumpMetricsOnShutdown);

  /**
   * Include 'sql' or 'hash' in options such that they are included in the output.
   *
   * @param dumpMetricsOptions Example "sql,hash", "sql"
   */
  DatabaseBuilder setDumpMetricsOptions(String dumpMetricsOptions);

  /**
   * Set false to turn off automatic registration of entity beans.
   * <p>
   * When using query beans that also generates a module info class that
   * can register the entity bean classes (to aDatabaseBuilder classpath scanning).
   * This is on by default and setting this to false turns it off.
   */
  DatabaseBuilder setLoadModuleInfo(boolean loadModuleInfo);

  /**
   * Set the naming convention to apply to metrics names.
   */
  DatabaseBuilder setMetricNaming(Function<String, String> metricNaming);


  /**
   * Provides read access (getters) for the DatabaseBuilder configuration
   * that has been set.
   */
  interface Settings extends DatabaseBuilder {

    /**
     * @deprecated - migrate to {@link Settings#isLoadModuleInfo()}.
     */
    @Deprecated(forRemoval = true)
    boolean isAutoLoadModuleInfo();

    /**
     * Return the Jackson JsonFactory to use.
     * <p>
     * If not set a default implementation will be used.
     */
    JsonFactory getJsonFactory();

    /**
     * Get the clock used for setting the timestamps (e.g. @UpdatedTimestamp) on objects.
     */
    Clock getClock();

    /**
     * Return the slow query time in millis.
     */
    long getSlowQueryMillis();

    /**
     * Return the slow query event listener.
     */
    SlowQueryListener getSlowQueryListener();

    /**
     * Return the service object given the key.
     */
    Object getServiceObject(String key);

    /**
     * Used by ebean or plugins to obtain service objects.
     *
     * <pre>{@code
     *
     *   JedisPool jedisPool = config.getServiceObject(JedisPool.class);
     *
     * }</pre>
     *
     * @param cls The type of the service object to obtain
     * @return The service object given the class type
     */
    @SuppressWarnings("unchecked")
    <P> P getServiceObject(Class<P> cls);

    /**
     * Return the JSON format used for DateTime types.
     */
    JsonConfig.DateTime getJsonDateTime();

    /**
     * Return the JSON format used for Date types.
     */
    JsonConfig.Date getJsonDate();

    /**
     * Return the JSON include mode used when writing JSON.
     */
    JsonConfig.Include getJsonInclude();

    /**
     * Return the default MutableDetection to use with {@code @DbJson} using Jackson.
     *
     * @see DbJson#mutationDetection()
     */
    MutationDetection getJsonMutationDetection();

    /**
     * Return the name of the Database.
     */
    String getName();

    /**
     * Return the container / clustering configuration.
     * <p/>
     * The container holds all the Database instances and provides clustering communication
     * services to all the Database instances.
     */
    ContainerConfig getContainerConfig();

    /**
     * Return true if this server should be registered with the Ebean singleton
     * when it is created.
     * <p>
     * By default this is set to true.
     */
    boolean isRegister();

    /**
     * Return true if this server should be registered as the "default" server
     * with the Ebean singleton.
     * <p>
     * This is only used when {@link #setRegister(boolean)} is also true.
     */
    boolean isDefaultServer();

    /**
     * Return the CurrentUserProvider. This is used to populate @WhoCreated, @WhoModified and
     * support other audit features (who executed a query etc).
     */
    CurrentUserProvider getCurrentUserProvider();

    /**
     * Return the tenancy mode used.
     */
    TenantMode getTenantMode();

    /**
     * Return the column name used for TenantMode.PARTITION.
     */
    String getTenantPartitionColumn();

    /**
     * Return the current tenant provider.
     */
    CurrentTenantProvider getCurrentTenantProvider();

    /**
     * Return the tenancy datasource provider.
     */
    TenantDataSourceProvider getTenantDataSourceProvider();

    /**
     * Return the tenancy schema provider.
     */
    TenantSchemaProvider getTenantSchemaProvider();

    /**
     * Return the PersistBatch mode to use by default at the transaction level.
     * <p>
     * When INSERT or ALL is used then save(), delete() etc do not execute immediately but instead go into
     * a JDBC batch execute buffer that is flushed. The buffer is flushed if a query is executed, transaction ends
     * or the batch size is meet.
     */
    PersistBatch getPersistBatch();

    /**
     * Return the JDBC batch mode to use per save(), delete(), insert() or update() request.
     * <p>
     * This makes sense when a save() or delete() cascades and executes multiple child statements. The best case
     * for this is when saving a master/parent bean this cascade inserts many detail/child beans.
     * <p>
     * This only takes effect when the persistBatch mode at the transaction level does not take effect.
     */
    PersistBatch getPersistBatchOnCascade();

    /**
     * Return the batch size used for JDBC batching. This defaults to 20.
     */
    int getPersistBatchSize();

    /**
     * Gets the query batch size. This defaults to 100.
     *
     * @return the query batch size
     */
    int getQueryBatchSize();

    EnumType getDefaultEnumType();

    /**
     * Return true if lazy loading is disabled on queries by default.
     */
    boolean isDisableLazyLoading();

    /**
     * Return the default batch size for lazy loading of beans and collections.
     */
    int getLazyLoadBatchSize();


    /**
     * Return the default JDBC fetchSize hint for findList queries.
     */
    int getJdbcFetchSizeFindList();

    /**
     * Return the default JDBC fetchSize hint for findEach/findEachWhile queries.
     */
    int getJdbcFetchSizeFindEach();

    /**
     * Return the ChangeLogPrepare.
     * <p>
     * This is used to set user context information to the ChangeSet in the
     * foreground thread prior to the logging occurring in a background thread.
     */
    ChangeLogPrepare getChangeLogPrepare();

    /**
     * Return the ChangeLogListener which actually performs the logging of change sets
     * in the background.
     */
    ChangeLogListener getChangeLogListener();

    /**
     * Return the ChangeLogRegister which controls which ChangeLogFilter is used for each
     * bean type and in this way provide fine grained control over which persist requests
     * are included in the change log.
     */
    ChangeLogRegister getChangeLogRegister();

    /**
     * Return true if inserts should be included in the change log by default.
     */
    boolean isChangeLogIncludeInserts();

    /**
     * Return true (default) if the changelog should be written async.
     */
    boolean isChangeLogAsync();

    /**
     * Return the ReadAuditLogger to use.
     */
    ReadAuditLogger getReadAuditLogger();

    /**
     * Return the ReadAuditPrepare to use.
     */
    ReadAuditPrepare getReadAuditPrepare();

    /**
     * Return the tenancy catalog provider.
     */
    TenantCatalogProvider getTenantCatalogProvider();

    /**
     * Return the configuration for profiling.
     */
    ProfilingConfig getProfilingConfig();

    /**
     * Return the DB schema to use.
     */
    String getDbSchema();

    /**
     * Return the Geometry SRID.
     */
    int getGeometrySRID();

    /**
     * Return the time zone to use when reading/writing Timestamps via JDBC.
     * <p>
     * When set a Calendar object is used in JDBC calls when reading/writing Timestamp objects.
     */
    String getDataTimeZone();

    /**
     * Return the suffix appended to the base table to derive the view that contains the union
     * of the base table and the history table in order to support asOf queries.
     */
    String getAsOfViewSuffix();

    /**
     * Return the database column used to support history and 'As of' queries. This column is a timestamp range
     * or equivalent.
     */
    String getAsOfSysPeriod();

    /**
     * Return the history table suffix (defaults to _history).
     */
    String getHistoryTableSuffix();

    /**
     * Return true if we are running in a JTA Transaction manager.
     */
    boolean isUseJtaTransactionManager();

    /**
     * Return the external transaction manager.
     */
    ExternalTransactionManager getExternalTransactionManager();

    /**
     * Return the ServerCachePlugin.
     */
    ServerCachePlugin getServerCachePlugin();

    /**
     * Return true if LOB's should default to fetch eager.
     * By default this is set to false and LOB's must be explicitly fetched.
     */
    boolean isEagerFetchLobs();

    /**
     * Return the max call stack to use for origin location.
     */
    int getMaxCallStack();

    /**
     * Return true if transactions should rollback on checked exceptions.
     */
    boolean isTransactionRollbackOnChecked();

    /**
     * Return the Background executor schedule pool size. Defaults to 1.
     */
    int getBackgroundExecutorSchedulePoolSize();

    /**
     * Return the Background executor shutdown seconds. This is the time allowed for the pool to shutdown nicely
     * before it is forced shutdown.
     */
    int getBackgroundExecutorShutdownSecs();

    /**
     * Return the background executor wrapper.
     */
    BackgroundExecutorWrapper getBackgroundExecutorWrapper();

    /**
     * Return true if dirty beans are automatically persisted.
     */
    boolean isAutoPersistUpdates();

    /**
     * Return the L2 cache default max size.
     */
    int getCacheMaxSize();

    /**
     * Return the L2 cache default max idle time in seconds.
     */
    int getCacheMaxIdleTime();

    /**
     * Return the L2 cache default max time to live in seconds.
     */
    int getCacheMaxTimeToLive();

    /**
     * Return the L2 query cache default max size.
     */
    int getQueryCacheMaxSize();

    /**
     * Return the L2 query cache default max idle time in seconds.
     */
    int getQueryCacheMaxIdleTime();

    /**
     * Return the L2 query cache default max time to live in seconds.
     */
    int getQueryCacheMaxTimeToLive();

    /**
     * Return the NamingConvention.
     * <p>
     * If none has been set the default UnderscoreNamingConvention is used.
     */
    NamingConvention getNamingConvention();

    /**
     * Return true if all DB column and table names should use quoted identifiers.
     */
    boolean isAllQuotedIdentifiers();

    /**
     * Return true if this Database is a Document store only instance (has no JDBC DB).
     */
    boolean isDocStoreOnly();

    /**
     * Return the configuration for the ElasticSearch integration.
     */
    DocStoreConfig getDocStoreConfig();

    /**
     * Return the constraint naming convention used in DDL generation.
     */
    DbConstraintNaming getConstraintNaming();

    /**
     * Return the configuration for AutoTune.
     */
    AutoTuneConfig getAutoTuneConfig();

    /**
     * Return true if the startup DataSource check should be skipped.
     */
    boolean skipDataSourceCheck();

    /**
     * Return the DataSource.
     */
    DataSource getDataSource();

    /**
     * Return the read only DataSource.
     */
    DataSource getReadOnlyDataSource();

    /**
     * Return the configuration to build a DataSource using Ebean's own DataSource
     * implementation.
     */
    DataSourceBuilder getDataSourceConfig();

    /**
     * Return true if Ebean should create a DataSource for use with implicit read only transactions.
     */
    boolean isAutoReadOnlyDataSource();

    /**
     * Return the configuration for the read only DataSource.
     * <p>
     * This is only used if autoReadOnlyDataSource is true.
     * <p>
     * The driver, url, username and password default to the configuration for the main DataSource if they are not
     * set on this configuration. This means there is actually no need to set any configuration here and we only
     * set configuration for url, username and password etc if it is different from the main DataSource.
     */
    DataSourceBuilder getReadOnlyDataSourceConfig();

    /**
     * Return a value used to represent TRUE in the database.
     * <p>
     * This is used for databases that do not support boolean natively.
     * <p>
     * The value returned is either a Integer or a String (e.g. "1", or "T").
     */
    String getDatabaseBooleanTrue();

    /**
     * Return a value used to represent FALSE in the database.
     * <p>
     * This is used for databases that do not support boolean natively.
     * <p>
     * The value returned is either a Integer or a String (e.g. "0", or "F").
     */
    String getDatabaseBooleanFalse();

    /**
     * Return the number of DB sequence values that should be preallocated.
     */
    int getDatabaseSequenceBatchSize();

    /**
     * Return the database platform name (can be null).
     * <p>
     * If null then the platform is determined automatically via the JDBC driver
     * information.
     */
    String getDatabasePlatformName();

    /**
     * Return the database platform to use for this database.
     */
    DatabasePlatform getDatabasePlatform();

    /**
     * Return the preferred DB platform IdType.
     */
    IdType getIdType();

    /**
     * Return the EncryptKeyManager.
     */
    EncryptKeyManager getEncryptKeyManager();

    /**
     * Return the EncryptDeployManager.
     * <p>
     * This is optionally used to programmatically define which columns are
     * encrypted instead of using the {@link Encrypted} Annotation.
     */
    EncryptDeployManager getEncryptDeployManager();

    /**
     * Return the Encryptor used to encrypt data on the java client side (as
     * opposed to DB encryption functions).
     */
    Encryptor getEncryptor();

    /**
     * Return true if the Database instance should be created in offline mode.
     */
    boolean isDbOffline();

    /**
     * Return the DbEncrypt used to encrypt and decrypt properties.
     * <p>
     * Note that if this is not set then the DbPlatform may already have a
     * DbEncrypt set and that will be used.
     */
    DbEncrypt getDbEncrypt();

    /**
     * Return the configuration for DB types (such as UUID and custom mappings).
     */
    PlatformConfig getPlatformConfig();

    /**
     * Return the PersistBatch mode to use for 'batchOnCascade' taking into account if the database
     * platform supports getGeneratedKeys in batch mode.
     */
    PersistBatch appliedPersistBatchOnCascade();

    /**
     * Returns the UUID version mode.
     */
    DatabaseConfig.UuidVersion getUuidVersion();

    /**
     * Return the UUID state file.
     */
    String getUuidStateFile();

    /**
     * Returns the V1-UUID-NodeId
     */
    String getUuidNodeId();

    /**
     * Return true if LocalTime should be persisted with nanos precision.
     */
    boolean isLocalTimeWithNanos();

    /**
     * Return true if Duration should be persisted with nanos precision (SQL DECIMAL).
     * <p>
     * Otherwise it is persisted with second precision (SQL INTEGER).
     */
    boolean isDurationWithNanos();

    /**
     * Return true if the DB migration should run on server start.
     */
    boolean isRunMigration();

    /**
     * Return true if the "drop all ddl" should be skipped.
     * <p>
     * Typically we want to do this when using H2 (in memory) as our test database and the drop statements
     * are not required so skipping the drop table statements etc makes it faster with less noise in the logs.
     */
    boolean isDdlCreateOnly();

    /**
     * Return SQL script to execute after the "create all" DDL has been run.
     * <p>
     * Typically this is a sql script that inserts test seed data when running tests.
     * Place a sql script in src/test/resources that inserts test seed data.
     */
    String getDdlSeedSql();

    /**
     * Return a SQL script to execute before the "create all" DDL has been run.
     */
    String getDdlInitSql();

    /**
     * Return true if the DDL should be generated.
     */
    boolean isDdlGenerate();

    /**
     * Return true if the DDL should be run.
     */
    boolean isDdlRun();

    /**
     * Return true, if extra-ddl.xml should be executed.
     */
    boolean isDdlExtra();

    /**
     * Return true if strict mode is used which includes a check that non-null columns have a default value.
     */
    boolean isDdlStrictMode();

    /**
     * Return the header to use with DDL generation.
     */
    String getDdlHeader();

    /**
     * Return a comma and equals delimited placeholders that are substituted in DDL scripts.
     */
    String getDdlPlaceholders();

    /**
     * Return a map of placeholder values that are substituted in DDL scripts.
     */
    Map<String, String> getDdlPlaceholderMap();

    /**
     * Return true if the class path search should be disabled.
     */
    boolean isDisableClasspathSearch();

    /**
     * Return the mode to use for Joda LocalTime support 'normal' or 'utc'.
     */
    String getJodaLocalTimeMode();

    /**
     * Return packages to search for entities via class path search.
     * <p>
     * This is only used if classes have not been explicitly specified.
     */
    List<String> getPackages();

    /**
     * Return the classes registered for this database. Typically, this includes
     * entities and perhaps listeners.
     */
    Set<Class<?>> classes();

    /**
     * Return true if L2 bean cache should be skipped once writes have occurred on a transaction.
     * <p>
     * This defaults to true and means that for "find by id" and "find by natural key"
     * queries that normally hit L2 bean cache automatically will not do so after a write/persist
     * on the transaction.
     * <p>
     * <pre>{@code
     *
     *   // assume Customer has L2 bean caching enabled ...
     *
     *   try (Transaction transaction = DB.beginTransaction()) {
     *
     *     // this uses L2 bean cache as the transaction
     *     // ... is considered "query only" at this point
     *     Customer.find.byId(42);
     *
     *     // transaction no longer "query only" once
     *     // ... a bean has been saved etc
     *     DB.save(someBean);
     *
     *     // will NOT use L2 bean cache as the transaction
     *     // ... is no longer considered "query only"
     *     Customer.find.byId(55);
     *
     *
     *
     *     // explicit control - please use L2 bean cache
     *
     *     transaction.setSkipCache(false);
     *     Customer.find.byId(77); // hit the l2 bean cache
     *
     *
     *     // explicit control - please don't use L2 bean cache
     *
     *     transaction.setSkipCache(true);
     *     Customer.find.byId(99); // skips l2 bean cache
     *
     *   }
     *
     * }</pre>
     *
     * @see Transaction#setSkipCache(boolean)
     */
    boolean isSkipCacheAfterWrite();

    /**
     * Returns true if updates in JDBC batch default to include all properties by default.
     */
    boolean isUpdateAllPropertiesInBatch();

    /**
     * Returns the resource directory.
     */
    String getResourceDirectory();

    /**
     * @deprecated - migrate to {@link Settings#classes()}.
     * <p>
     * Sorry if returning Set rather than List breaks code but it feels safer to
     * do that than a subtle change to return a shallow copy which you will not detect.
     */
    @Deprecated(forRemoval = true)
    Set<Class<?>> getClasses();

    /**
     * Return the BeanQueryAdapter instances.
     */
    List<BeanQueryAdapter> getQueryAdapters();

    /**
     * Return the custom IdGenerator instances.
     */
    List<IdGenerator> getIdGenerators();

    /**
     * Return the list of BeanFindController instances.
     */
    List<BeanFindController> getFindControllers();

    /**
     * Return the list of BeanPostLoader instances.
     */
    List<BeanPostLoad> getPostLoaders();

    /**
     * Return the list of BeanPostLoader instances.
     */
    List<BeanPostConstructListener> getPostConstructListeners();

    /**
     * Return the BeanPersistController instances.
     */
    List<BeanPersistController> getPersistControllers();

    /**
     * Return the BeanPersistListener instances.
     */
    List<BeanPersistListener> getPersistListeners();

    /**
     * Return the list of BulkTableEventListener instances.
     */
    List<BulkTableEventListener> getBulkTableEventListeners();

    /**
     * Return the list of ServerConfigStartup instances.
     */
    List<ServerConfigStartup> getServerConfigStartupListeners();

    /**
     * Return the default PersistenceContextScope to be used if one is not explicitly set on a query.
     * <p/>
     * The PersistenceContextScope can specified on each query via {@link io.ebean
     * .Query#setPersistenceContextScope(io.ebean.PersistenceContextScope)}. If it
     * is not set on the query this default scope is used.
     *
     * @see Query#setPersistenceContextScope(PersistenceContextScope)
     */
    PersistenceContextScope getPersistenceContextScope();

    /**
     * Return the ClassLoadConfig which is used to detect Joda, Java8 types etc and also
     * create new instances of plugins given a className.
     */
    ClassLoadConfig getClassLoadConfig();

    /**
     * Return the properties that we used for configuration and were set via a call to loadFromProperties().
     */
    Properties getProperties();

    /**
     * Return the Jackson ObjectMapper.
     * <p>
     * Note that this is not strongly typed as Jackson ObjectMapper is an optional dependency.
     */
    Object getObjectMapper();

    /**
     * Return true if eq("someProperty", null) should to generate "1=1" rather than "is null" sql expression.
     */
    boolean isExpressionEqualsWithNullAsNoop();

    /**
     * Return true if native ILIKE expression should be used if supported by the database platform (e.g. Postgres).
     */
    boolean isExpressionNativeIlike();

    /**
     * Return the enabled L2 cache regions.
     */
    String getEnabledL2Regions();

    /**
     * Return true if L2 cache is disabled.
     */
    boolean isDisableL2Cache();

    /**
     * Return true to use local only L2 cache. Effectively ignore l2 cache plugin like ebean-redis etc.
     */
    boolean isLocalOnlyL2Cache();

    /**
     * Returns if we use javax.validation.constraints.NotNull
     */
    boolean isUseValidationNotNull();

    /**
     * Return true if L2 cache notification should run in the foreground.
     */
    boolean isNotifyL2CacheInForeground();

    /**
     * Return the time to live for ebean's internal query plan.
     */
    int getQueryPlanTTLSeconds();

    /**
     * Return mapping locations to search for xml mapping via class path search.
     */
    List<String> getMappingLocations();

    /**
     * When false we need explicit <code>@GeneratedValue</code> mapping to assign
     * Identity or Sequence generated values. When true Id properties are automatically
     * assigned Identity or Sequence without the GeneratedValue mapping.
     */
    boolean isIdGeneratorAutomatic();

    /**
     * Return true if query plan capture is enabled.
     */
    boolean isQueryPlanEnable();

    /**
     * Return the query plan collection threshold in microseconds.
     */
    long getQueryPlanThresholdMicros();

    /**
     * Return true if periodic capture of query plans is enabled.
     */
    boolean isQueryPlanCapture();

    /**
     * Return the frequency to capture query plans.
     */
    long getQueryPlanCapturePeriodSecs();

    /**
     * Return the time after which a capture query plans request will
     * stop capturing more query plans.
     * <p>
     * Effectively this controls the amount of load/time we want to
     * allow for query plan capture.
     */
    long getQueryPlanCaptureMaxTimeMillis();

    /**
     * Return the max number of query plans captured per request.
     */
    int getQueryPlanCaptureMaxCount();

    /**
     * Return the listener used to process captured query plans.
     */
    QueryPlanListener getQueryPlanListener();

    /**
     * Return true if metrics should be dumped when the server is shutdown.
     */
    boolean isDumpMetricsOnShutdown();

    /**
     * Return the options for dumping metrics.
     */
    String getDumpMetricsOptions();

    /**
     * Return true if entity classes should be loaded and registered via EntityClassRegister.
     * <p>
     * When false we either register entity classes via application code or use classpath
     * scanning to find and register entity classes.
     */
    boolean isLoadModuleInfo();

    /**
     * Return the naming convention to apply to metrics names.
     */
    Function<String, String> getMetricNaming();

  }
}
