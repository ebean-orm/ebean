package io.ebean;

import com.fasterxml.jackson.core.JsonFactory;
import io.ebean.annotation.MutationDetection;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
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

final class DDatabaseBuilder implements DatabaseBuilder.WithAllOptions {

  private final DatabaseConfig config = new DatabaseConfig();

  @Override
  public Database build() {
    return DatabaseFactory.create(config);
  }

  @Override
  public WithQueryPlanOptions withQueryPlanOptions() {
    return this;
  }

  @Override
  public WithAllOptions withAllOptions() {
    return this;
  }

  @Override
  public DatabaseBuilder name(String name) {
    config.setName(name);
    return this;
  }

  @Override
  public DatabaseBuilder register(boolean register) {
    config.setRegister(register);
    return this;
  }

  @Override
  public DatabaseBuilder defaultDatabase(boolean defaultDatabase) {
    config.setDefaultServer(defaultDatabase);
    return this;
  }

  @Override
  public DatabaseBuilder dataSource(DataSource dataSource) {
    config.setDataSource(dataSource);
    return this;
  }

  @Override
  public DatabaseBuilder readOnlyDataSource(DataSource readOnlyDataSource) {
    config.setReadOnlyDataSource(readOnlyDataSource);
    return this;
  }

  @Override
  public DatabaseBuilder dataSourceConfig(DataSourceConfig dataSourceConfig) {
    config.setDataSourceConfig(dataSourceConfig);
    return this;
  }

  @Override
  public DatabaseBuilder autoReadOnlyDataSource(boolean autoReadOnlyDataSource) {
    config.setAutoReadOnlyDataSource(autoReadOnlyDataSource);
    return this;
  }

  @Override
  public DatabaseBuilder readOnlyDataSourceConfig(DataSourceConfig readOnlyDataSourceConfig) {
    config.setReadOnlyDataSourceConfig(readOnlyDataSourceConfig);
    return this;
  }

  @Override
  public DatabaseBuilder runMigration(boolean runMigration) {
    config.setRunMigration(runMigration);
    return this;
  }

  @Override
  public DatabaseBuilder loadFromProperties() {
    config.loadFromProperties();
    return this;
  }

  @Override
  public DatabaseBuilder loadFromProperties(Properties properties) {
    config.loadFromProperties(properties);
    return this;
  }

  @Override
  public DatabaseBuilder enabledL2Regions(String enabledL2Regions) {
    config.setEnabledL2Regions(enabledL2Regions);
    return this;
  }

  @Override
  public DatabaseBuilder disableL2Cache(boolean disableL2Cache) {
    config.setDisableL2Cache(disableL2Cache);
    return this;
  }

  @Override
  public DatabaseBuilder localOnlyL2Cache(boolean localOnlyL2Cache) {
    config.setLocalOnlyL2Cache(localOnlyL2Cache);
    return this;
  }


  @Override
  public DatabaseBuilder queryPlanTTLSeconds(int queryPlanTTLSeconds) {
    config.setQueryPlanTTLSeconds(queryPlanTTLSeconds);
    return this;
  }

  @Override
  public DatabaseBuilder queryPlanEnable(boolean queryPlanEnable) {
    config.setQueryPlanEnable(queryPlanEnable);
    return this;
  }

  @Override
  public DatabaseBuilder queryPlanThresholdMicros(long queryPlanThresholdMicros) {
    config.setQueryPlanThresholdMicros(queryPlanThresholdMicros);
    return this;
  }

  @Override
  public DatabaseBuilder queryPlanCapture(boolean queryPlanCapture) {
    config.setQueryPlanCapture(queryPlanCapture);
    return this;
  }

  @Override
  public DatabaseBuilder queryPlanCapturePeriodSecs(long queryPlanCapturePeriodSecs) {
    config.setQueryPlanCapturePeriodSecs(queryPlanCapturePeriodSecs);
    return this;
  }

  @Override
  public DatabaseBuilder queryPlanCaptureMaxTimeMillis(long queryPlanCaptureMaxTimeMillis) {
    config.setQueryPlanCaptureMaxTimeMillis(queryPlanCaptureMaxTimeMillis);
    return this;
  }

  @Override
  public DatabaseBuilder queryPlanCaptureMaxCount(int queryPlanCaptureMaxCount) {
    config.setQueryPlanCaptureMaxCount(queryPlanCaptureMaxCount);
    return this;
  }

  @Override
  public DatabaseBuilder queryPlanListener(QueryPlanListener queryPlanListener) {
    config.setQueryPlanListener(queryPlanListener);
    return this;
  }

  @Override
  public WithAllOptions notifyL2CacheInForeground(boolean notifyL2CacheInForeground) {
    config.setNotifyL2CacheInForeground(notifyL2CacheInForeground);
    return this;
  }

  @Override
  public WithAllOptions dumpMetricsOnShutdown(boolean dumpMetricsOnShutdown) {
    config.setDumpMetricsOnShutdown(dumpMetricsOnShutdown);
    return this;
  }

  @Override
  public WithAllOptions dumpMetricsOptions(String dumpMetricsOptions) {
    config.setDumpMetricsOptions(dumpMetricsOptions);
    return this;
  }

  @Override
  public WithAllOptions putServiceObject(String key, Object configObject) {
    config.putServiceObject(key, configObject);
    return this;
  }

  @Override
  public <T> WithAllOptions putServiceObject(Class<T> iface, T configObject) {
    config.putServiceObject(iface, configObject);
    return this;
  }

  @Override
  public WithAllOptions putServiceObject(Object configObject) {
    config.putServiceObject(configObject);
    return this;
  }

  @Override
  public WithAllOptions persistBatch(PersistBatch persistBatch) {
    config.setPersistBatch(persistBatch);
    return this;
  }

  @Override
  public WithAllOptions persistBatchOnCascade(PersistBatch persistBatchOnCascade) {
    config.setPersistBatchOnCascade(persistBatchOnCascade);
    return this;
  }

  @Override
  public WithAllOptions persistBatchSize(int persistBatchSize) {
    config.setPersistBatchSize(persistBatchSize);
    return this;
  }

  @Override
  public WithAllOptions dbSchema(String dbSchema) {
    config.setDbSchema(dbSchema);
    return this;
  }

  @Override
  public WithAllOptions geometrySRID(int geometrySRID) {
    config.setGeometrySRID(geometrySRID);
    return this;
  }

  @Override
  public WithAllOptions dataTimeZone(String dataTimeZone) {
    config.setDataTimeZone(dataTimeZone);
    return this;
  }

  @Override
  public WithAllOptions useJtaTransactionManager(boolean useJtaTransactionManager) {
    config.setUseJtaTransactionManager(useJtaTransactionManager);
    return this;
  }

  @Override
  public WithAllOptions externalTransactionManager(ExternalTransactionManager externalTransactionManager) {
    config.setExternalTransactionManager(externalTransactionManager);
    return this;
  }

  @Override
  public WithAllOptions cacheMaxSize(int cacheMaxSize) {
    config.setCacheMaxSize(cacheMaxSize);
    return this;
  }

  @Override
  public WithAllOptions cacheMaxIdleTime(int cacheMaxIdleTime) {
    config.setCacheMaxIdleTime(cacheMaxIdleTime);
    return this;
  }

  @Override
  public WithAllOptions cacheMaxTimeToLive(int cacheMaxTimeToLive) {
    config.setCacheMaxTimeToLive(cacheMaxTimeToLive);
    return this;
  }

  @Override
  public WithAllOptions queryCacheMaxSize(int queryCacheMaxSize) {
    config.setQueryCacheMaxSize(queryCacheMaxSize);
    return this;
  }

  @Override
  public WithAllOptions queryCacheMaxIdleTime(int queryCacheMaxIdleTime) {
    config.setQueryCacheMaxIdleTime(queryCacheMaxIdleTime);
    return this;
  }

  @Override
  public WithAllOptions queryCacheMaxTimeToLive(int queryCacheMaxTimeToLive) {
    config.setQueryCacheMaxTimeToLive(queryCacheMaxTimeToLive);
    return this;
  }

  @Override
  public WithAllOptions allQuotedIdentifiers(boolean allQuotedIdentifiers) {
    config.setAllQuotedIdentifiers(allQuotedIdentifiers);
    return this;
  }

  @Override
  public WithAllOptions objectMapper(Object objectMapper) {
    config.setObjectMapper(objectMapper);
    return this;
  }

  @Override
  public WithAllOptions metricNaming(Function<String, String> metricNaming) {
    config.setMetricNaming(metricNaming);
    return this;
  }

  @Override
  public WithAllOptions containerConfig(ContainerConfig containerConfig) {
    config.setContainerConfig(containerConfig);
    return this;
  }

  @Override
  public WithAllOptions queryBatchSize(int queryBatchSize) {
    config.setQueryBatchSize(queryBatchSize);
    return this;
  }

  @Override
  public WithAllOptions defaultEnumType(EnumType defaultEnumType) {
    config.setDefaultEnumType(defaultEnumType);
    return this;
  }

  @Override
  public WithAllOptions disableLazyLoading(boolean disableLazyLoading) {
    config.setDisableLazyLoading(disableLazyLoading);
    return this;
  }

  @Override
  public WithAllOptions changeLogPrepare(ChangeLogPrepare changeLogPrepare) {
    config.setChangeLogPrepare(changeLogPrepare);
    return this;
  }

  @Override
  public WithAllOptions changeLogListener(ChangeLogListener changeLogListener) {
    config.setChangeLogListener(changeLogListener);
    return this;
  }

  @Override
  public WithAllOptions changeLogRegister(ChangeLogRegister changeLogRegister) {
    config.setChangeLogRegister(changeLogRegister);
    return this;
  }

  @Override
  public WithAllOptions changeLogIncludeInserts(boolean changeLogIncludeInserts) {
    config.setChangeLogIncludeInserts(changeLogIncludeInserts);
    return this;
  }

  @Override
  public WithAllOptions changeLogAsync(boolean changeLogAsync) {
    config.setChangeLogAsync(changeLogAsync);
    return this;
  }

  @Override
  public WithAllOptions namingConvention(NamingConvention namingConvention) {
    config.setNamingConvention(namingConvention);
    return this;
  }

  @Override
  public WithAllOptions platformConfig(PlatformConfig platformConfig) {
    config.setPlatformConfig(platformConfig);
    return this;
  }

  @Override
  public WithAllOptions addClass(Class<?> cls) {
    config.addClass(cls);
    return this;
  }

  @Override
  public WithAllOptions addAll(Collection<Class<?>> classList) {
    config.addAll(classList);
    return this;
  }

  @Override
  public WithAllOptions databasePlatformName(String databasePlatformName) {
    config.setDatabasePlatformName(databasePlatformName);
    return this;
  }

  @Override
  public WithAllOptions databasePlatform(DatabasePlatform databasePlatform) {
    config.setDatabasePlatform(databasePlatform);
    return this;
  }

  @Override
  public WithAllOptions idType(IdType idType) {
    config.setIdType(idType);
    return this;
  }

  @Override
  public WithAllOptions ddlGenerate(boolean ddlGenerate) {
    config.setDdlGenerate(ddlGenerate);
    return this;
  }

  @Override
  public WithAllOptions ddlRun(boolean ddlRun) {
    config.setDdlRun(ddlRun);
    return this;
  }

  @Override
  public WithAllOptions ddlExtra(boolean ddlExtra) {
    config.setDdlExtra(ddlExtra);
    return this;
  }

  @Override
  public WithAllOptions ddlCreateOnly(boolean ddlCreateOnly) {
    config.setDdlExtra(ddlCreateOnly);
    return this;
  }

  @Override
  public WithAllOptions ddlSeedSql(String ddlSeedSql) {
    config.setDdlSeedSql(ddlSeedSql);
    return this;
  }

  @Override
  public WithAllOptions ddlInitSql(String ddlInitSql) {
    config.setDdlInitSql(ddlInitSql);
    return this;
  }

  @Override
  public WithAllOptions ddlHeader(String ddlHeader) {
    config.setDdlHeader(ddlHeader);
    return this;
  }

  @Override
  public WithAllOptions ddlStrictMode(boolean ddlStrictMode) {
    config.setDdlStrictMode(ddlStrictMode);
    return this;
  }

  @Override
  public WithAllOptions ddlPlaceholders(String ddlPlaceholders) {
    config.setDdlPlaceholders(ddlPlaceholders);
    return this;
  }

  @Override
  public WithAllOptions ddlPlaceholderMap(Map<String, String> ddlPlaceholderMap) {
    config.setDdlPlaceholderMap(ddlPlaceholderMap);
    return this;
  }

  @Override
  public WithAllOptions clock(Clock clock) {
    config.setClock(clock);
    return this;
  }

  @Override
  public WithAllOptions slowQueryMillis(long slowQueryMillis) {
    config.setSlowQueryMillis(slowQueryMillis);
    return this;
  }

  @Override
  public WithAllOptions slowQueryListener(SlowQueryListener slowQueryListener) {
    config.setSlowQueryListener(slowQueryListener);
    return this;
  }

  @Override
  public WithAllOptions jsonFactory(JsonFactory jsonFactory) {
    config.setJsonFactory(jsonFactory);
    return this;
  }

  @Override
  public WithAllOptions jsonDateTime(JsonConfig.DateTime jsonDateTime) {
    config.setJsonDateTime(jsonDateTime);
    return this;
  }

  @Override
  public WithAllOptions jsonDate(JsonConfig.Date jsonDate) {
    config.setJsonDate(jsonDate);
    return this;
  }

  @Override
  public WithAllOptions jsonInclude(JsonConfig.Include jsonInclude) {
    config.setJsonInclude(jsonInclude);
    return this;
  }

  @Override
  public WithAllOptions jsonMutationDetection(MutationDetection jsonMutationDetection) {
    config.setJsonMutationDetection(jsonMutationDetection);
    return this;
  }

  @Override
  public WithAllOptions currentUserProvider(CurrentUserProvider currentUserProvider) {
    config.setCurrentUserProvider(currentUserProvider);
    return this;
  }

  @Override
  public WithAllOptions tenantMode(TenantMode tenantMode) {
    config.setTenantMode(tenantMode);
    return this;
  }

  @Override
  public WithAllOptions tenantPartitionColumn(String tenantPartitionColumn) {
    config.setTenantPartitionColumn(tenantPartitionColumn);
    return this;
  }

  @Override
  public WithAllOptions currentTenantProvider(CurrentTenantProvider currentTenantProvider) {
    config.setCurrentTenantProvider(currentTenantProvider);
    return this;
  }

  @Override
  public WithAllOptions tenantDataSourceProvider(TenantDataSourceProvider tenantDataSourceProvider) {
    config.setTenantDataSourceProvider(tenantDataSourceProvider);
    return this;
  }

  @Override
  public WithAllOptions tenantSchemaProvider(TenantSchemaProvider tenantSchemaProvider) {
    config.setTenantSchemaProvider(tenantSchemaProvider);
    return this;
  }

  @Override
  public WithAllOptions tenantCatalogProvider(TenantCatalogProvider tenantCatalogProvider) {
    config.setTenantCatalogProvider(tenantCatalogProvider);
    return this;
  }

  @Override
  public WithAllOptions autoPersistUpdates(boolean autoPersistUpdates) {
    config.setAutoPersistUpdates(autoPersistUpdates);
    return this;
  }

  @Override
  public WithAllOptions lazyLoadBatchSize(int lazyLoadBatchSize) {
    config.setLazyLoadBatchSize(lazyLoadBatchSize);
    return this;
  }

  @Override
  public WithAllOptions databaseSequenceBatchSize(int databaseSequenceBatchSize) {
    config.setDatabaseSequenceBatchSize(databaseSequenceBatchSize);
    return this;
  }

  @Override
  public WithAllOptions jdbcFetchSizeFindList(int jdbcFetchSizeFindList) {
    config.setJdbcFetchSizeFindList(jdbcFetchSizeFindList);
    return this;
  }

  @Override
  public WithAllOptions jdbcFetchSizeFindEach(int jdbcFetchSizeFindEach) {
    config.setJdbcFetchSizeFindEach(jdbcFetchSizeFindEach);
    return this;
  }

  @Override
  public WithAllOptions readAuditLogger(ReadAuditLogger readAuditLogger) {
    config.setReadAuditLogger(readAuditLogger);
    return this;
  }

  @Override
  public WithAllOptions readAuditPrepare(ReadAuditPrepare readAuditPrepare) {
    config.setReadAuditPrepare(readAuditPrepare);
    return this;
  }

  @Override
  public WithAllOptions profilingConfig(ProfilingConfig profilingConfig) {
    config.setProfilingConfig(profilingConfig);
    return this;
  }

  @Override
  public WithAllOptions asOfViewSuffix(String asOfViewSuffix) {
    config.setAsOfViewSuffix(asOfViewSuffix);
    return this;
  }

  @Override
  public WithAllOptions asOfSysPeriod(String asOfSysPeriod) {
    config.setAsOfSysPeriod(asOfSysPeriod);
    return this;
  }

  @Override
  public WithAllOptions historyTableSuffix(String historyTableSuffix) {
    config.setHistoryTableSuffix(historyTableSuffix);
    return this;
  }

  @Override
  public WithAllOptions serverCachePlugin(ServerCachePlugin serverCachePlugin) {
    config.setServerCachePlugin(serverCachePlugin);
    return this;
  }

  @Override
  public WithAllOptions eagerFetchLobs(boolean eagerFetchLobs) {
    config.setEagerFetchLobs(eagerFetchLobs);
    return this;
  }

  @Override
  public WithAllOptions maxCallStack(int maxCallStack) {
    config.setMaxCallStack(maxCallStack);
    return this;
  }

  @Override
  public WithAllOptions transactionRollbackOnChecked(boolean transactionRollbackOnChecked) {
    config.setTransactionRollbackOnChecked(transactionRollbackOnChecked);
    return this;
  }

  @Override
  public WithAllOptions backgroundExecutorSchedulePoolSize(int backgroundExecutorSchedulePoolSize) {
    config.setBackgroundExecutorSchedulePoolSize(backgroundExecutorSchedulePoolSize);
    return this;
  }

  @Override
  public WithAllOptions backgroundExecutorShutdownSecs(int backgroundExecutorShutdownSecs) {
    config.setBackgroundExecutorShutdownSecs(backgroundExecutorShutdownSecs);
    return this;
  }

  @Override
  public WithAllOptions backgroundExecutorWrapper(BackgroundExecutorWrapper backgroundExecutorWrapper) {
    config.setBackgroundExecutorWrapper(backgroundExecutorWrapper);
    return this;
  }

  @Override
  public WithAllOptions constraintNaming(DbConstraintNaming constraintNaming) {
    config.setConstraintNaming(constraintNaming);
    return this;
  }

  @Override
  public WithAllOptions autoTuneConfig(AutoTuneConfig autoTuneConfig) {
    config.setAutoTuneConfig(autoTuneConfig);
    return this;
  }

  @Override
  public WithAllOptions skipDataSourceCheck(boolean skipDataSourceCheck) {
    config.setSkipDataSourceCheck(skipDataSourceCheck);
    return this;
  }

  @Override
  public WithAllOptions databaseBooleanTrue(String databaseTrue) {
    config.setDatabaseBooleanTrue(databaseTrue);
    return this;
  }

  @Override
  public WithAllOptions databaseBooleanFalse(String databaseFalse) {
    config.setDatabaseBooleanFalse(databaseFalse);
    return this;
  }

  @Override
  public WithAllOptions databaseSequenceBatch(int databaseSequenceBatchSize) {
    config.setDatabaseSequenceBatch(databaseSequenceBatchSize);
    return this;
  }

  @Override
  public WithAllOptions encryptKeyManager(EncryptKeyManager encryptKeyManager) {
    config.setEncryptKeyManager(encryptKeyManager);
    return this;
  }

  @Override
  public WithAllOptions encryptDeployManager(EncryptDeployManager encryptDeployManager) {
    config.setEncryptDeployManager(encryptDeployManager);
    return this;
  }

  @Override
  public WithAllOptions encryptor(Encryptor encryptor) {
    config.setEncryptor(encryptor);
    return this;
  }

  @Override
  public WithAllOptions dbOffline(boolean dbOffline) {
    config.setDbOffline(dbOffline);
    return this;
  }

  @Override
  public WithAllOptions dbEncrypt(DbEncrypt dbEncrypt) {
    config.setDbEncrypt(dbEncrypt);
    return this;
  }

  @Override
  public WithAllOptions dbUuid(PlatformConfig.DbUuid dbUuid) {
    config.setDbUuid(dbUuid);
    return this;
  }

  @Override
  public WithAllOptions uuidVersion(DatabaseConfig.UuidVersion uuidVersion) {
    config.setUuidVersion(uuidVersion);
    return this;
  }

  @Override
  public WithAllOptions uuidStateFile(String uuidStateFile) {
    config.setUuidStateFile(uuidStateFile);
    return this;
  }

  @Override
  public WithAllOptions uuidNodeId(String uuidNodeId) {
    config.setUuidNodeId(uuidNodeId);
    return this;
  }

  @Override
  public WithAllOptions localTimeWithNanos(boolean localTimeWithNanos) {
    config.setLocalTimeWithNanos(localTimeWithNanos);
    return this;
  }

  @Override
  public WithAllOptions durationWithNanos(boolean durationWithNanos) {
    config.setDurationWithNanos(durationWithNanos);
    return this;
  }

  @Override
  public WithAllOptions disableClasspathSearch(boolean disableClasspathSearch) {
    config.setDisableClasspathSearch(disableClasspathSearch);
    return this;
  }

  @Override
  public WithAllOptions jodaLocalTimeMode(String jodaLocalTimeMode) {
    config.setJodaLocalTimeMode(jodaLocalTimeMode);
    return this;
  }

  @Override
  public WithAllOptions skipCacheAfterWrite(boolean skipCacheAfterWrite) {
    config.setSkipCacheAfterWrite(skipCacheAfterWrite);
    return this;
  }

  @Override
  public WithAllOptions updateAllPropertiesInBatch(boolean updateAllPropertiesInBatch) {
    config.setUpdateAllPropertiesInBatch(updateAllPropertiesInBatch);
    return this;
  }

  @Override
  public WithAllOptions resourceDirectory(String resourceDirectory) {
    config.setResourceDirectory(resourceDirectory);
    return this;
  }

  @Override
  public WithAllOptions addCustomMapping(DbType type, String columnDefinition, Platform platform) {
    config.addCustomMapping(type, columnDefinition, platform);
    return this;
  }

  @Override
  public WithAllOptions addCustomMapping(DbType type, String columnDefinition) {
    config.addCustomMapping(type, columnDefinition);
    return this;
  }

  @Override
  public WithAllOptions add(BeanQueryAdapter beanQueryAdapter) {
    config.add(beanQueryAdapter);
    return this;
  }

  @Override
  public WithAllOptions queryAdapters(List<BeanQueryAdapter> queryAdapters) {
    config.setQueryAdapters(queryAdapters);
    return this;
  }

  @Override
  public WithAllOptions idGenerators(List<IdGenerator> idGenerators) {
    config.setIdGenerators(idGenerators);
    return this;
  }

  @Override
  public WithAllOptions add(IdGenerator idGenerator) {
    config.add(idGenerator);
    return this;
  }

  @Override
  public WithAllOptions add(BeanPersistController beanPersistController) {
    config.add(beanPersistController);
    return this;
  }

  @Override
  public WithAllOptions add(BeanPostLoad postLoad) {
    config.add(postLoad);
    return this;
  }

  @Override
  public WithAllOptions add(BeanPostConstructListener listener) {
    config.add(listener);
    return this;
  }

  @Override
  public WithAllOptions findControllers(List<BeanFindController> findControllers) {
    config.setFindControllers(findControllers);
    return this;
  }

  @Override
  public WithAllOptions postLoaders(List<BeanPostLoad> postLoaders) {
    config.setPostLoaders(postLoaders);
    return this;
  }

  @Override
  public WithAllOptions postConstructListeners(List<BeanPostConstructListener> listeners) {
    config.setPostConstructListeners(listeners);
    return this;
  }

  @Override
  public WithAllOptions persistControllers(List<BeanPersistController> persistControllers) {
    config.setPersistControllers(persistControllers);
    return this;
  }

  @Override
  public WithAllOptions add(BeanPersistListener beanPersistListener) {
    config.add(beanPersistListener);
    return this;
  }

  @Override
  public WithAllOptions add(BulkTableEventListener bulkTableEventListener) {
    config.add(bulkTableEventListener);
    return this;
  }

  @Override
  public WithAllOptions addServerConfigStartup(ServerConfigStartup configStartupListener) {
    config.addServerConfigStartup(configStartupListener);
    return this;
  }

  @Override
  public WithAllOptions persistListeners(List<BeanPersistListener> persistListeners) {
    config.setPersistListeners(persistListeners);
    return this;
  }

  @Override
  public WithAllOptions persistenceContextScope(PersistenceContextScope persistenceContextScope) {
    config.setPersistenceContextScope(persistenceContextScope);
    return this;
  }

  @Override
  public WithAllOptions classLoadConfig(ClassLoadConfig classLoadConfig) {
    config.setClassLoadConfig(classLoadConfig);
    return this;
  }

  @Override
  public WithAllOptions expressionEqualsWithNullAsNoop(boolean expressionEqualsWithNullAsNoop) {
    config.setExpressionEqualsWithNullAsNoop(expressionEqualsWithNullAsNoop);
    return this;
  }

  @Override
  public WithAllOptions expressionNativeIlike(boolean expressionNativeIlike) {
    config.setExpressionNativeIlike(expressionNativeIlike);
    return this;
  }

  @Override
  public WithAllOptions useValidationNotNull(boolean useValidationNotNull) {
    config.setUseValidationNotNull(useValidationNotNull);
    return this;
  }

  @Override
  public WithAllOptions idGeneratorAutomatic(boolean idGeneratorAutomatic) {
    config.setIdGeneratorAutomatic(idGeneratorAutomatic);
    return this;
  }

  @Override
  public WithAllOptions loadModuleInfo(boolean loadModuleInfo) {
    config.setLoadModuleInfo(loadModuleInfo);
    return this;
  }
}
