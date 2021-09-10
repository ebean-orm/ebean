package io.ebeaninternal.api;

import io.ebean.*;
import io.ebean.annotation.Platform;
import io.ebean.annotation.TxIsolation;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.CallOrigin;
import io.ebean.cache.ServerCacheManager;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetricVisitor;
import io.ebean.plugin.Property;
import io.ebean.plugin.SpiServer;
import io.ebean.text.csv.CsvReader;
import io.ebean.text.json.JsonContext;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.core.SpiResultSet;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.query.CQuery;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Test double for SpiEbeanServer.
 */
public class TDSpiEbeanServer extends TDSpiServer implements SpiEbeanServer {

  String name;

  public TDSpiEbeanServer() {
  }

  public TDSpiEbeanServer(String name) {
    this.name = name;
  }

  @Override
  public ExtendedServer extended() {
    return this;
  }

  @Override
  public long clockNow() {
    return System.currentTimeMillis();
  }

  @Override
  public void setClock(Clock clock) {
  }

  @Override
  public boolean isDisableL2Cache() {
    return false;
  }

  @Override
  public SpiLogManager log() {
    return null;
  }

  @Override
  public void shutdown() {
  }

  @Override
  public ScriptRunner script() {
    return null;
  }

  @Override
  public void truncate(String... tables) {
  }

  @Override
  public void truncate(Class<?>... tables) {
  }

  @Override
  public void scopedTransactionEnter(TxScope txScope) {
  }

  @Override
  public void scopedTransactionExit(Object returnOrThrowable, int opCode) {

  }

  @Override
  public Object currentTenantId() {
    return null;
  }

  @Override
  public DataTimeZone dataTimeZone() {
    return null;
  }

  @Override
  public Platform platform() {
    return Platform.GENERIC;
  }

  @Override
  public SpiServer pluginApi() {
    return null;
  }

  @Override
  public boolean isUpdateAllPropertiesInBatch() {
    return false;
  }

  @Override
  public DatabaseConfig config() {
    return null;
  }

  @Override
  public DatabasePlatform databasePlatform() {
    return null;
  }

  @Override
  public CallOrigin createCallOrigin() {
    return null;
  }

  @Override
  public PersistenceContextScope persistenceContextScope(SpiQuery<?> query) {
    return null;
  }

  @Override
  public DocumentStore docStore() {
    return null;
  }

  @Override
  public ReadAuditLogger readAuditLogger() {
    return null;
  }

  @Override
  public ReadAuditPrepare readAuditPrepare() {
    return null;
  }

  @Override
  public void clearQueryStatistics() {

  }

  @Override
  public BeanDescriptor<?> descriptorByQueueId(String queueId) {
    return null;
  }

  @Override
  public SpiTransactionManager transactionManager() {
    return null;
  }

  @Override
  public List<BeanDescriptor<?>> descriptors() {
    return null;
  }

  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> type) {
    return null;
  }

  @Override
  public BeanDescriptor<?> descriptorById(String descriptorId) {
    return null;
  }

  @Override
  public List<BeanDescriptor<?>> descriptors(String tableName) {
    return null;
  }

  @Override
  public void externalModification(TransactionEventTable event) {

  }

  @Override
  public void clearServerTransaction() {
  }

  @Override
  public SpiTransaction beginServerTransaction() {
    return null;
  }

  @Override
  public SpiTransaction currentServerTransaction() {
    return null;
  }

  @Override
  public SpiTransaction createReadOnlyTransaction(Object tenantId) {
    return null;
  }

  @Override
  public void remoteTransactionEvent(RemoteTransactionEvent event) {
  }

  @Override
  public <T> CQuery<T> compileQuery(Type type, Query<T> query, Transaction t) {
    return null;
  }

  @Override
  public <T> int delete(Query<T> query, Transaction t) {
    return 0;
  }

  @Override
  public <T> UpdateQuery<T> update(Class<T> beanType) {
    return null;
  }

  @Override
  public <T> int update(Query<T> query, Transaction transaction) {
    return 0;
  }

  @Override
  public void merge(Object bean) {
  }

  @Override
  public void merge(Object bean, MergeOptions options) {
  }

  @Override
  public void merge(Object bean, MergeOptions options, Transaction transaction) {
  }

  @Override
  public <T> List<Version<T>> findVersions(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <A, T> List<A> findIdsWithCopy(Query<T> query, Transaction t) {
    return null;
  }

  @Override
  public <T> int findCountWithCopy(Query<T> query, Transaction t) {
    return 0;
  }

  @Override
  public void loadBean(LoadBeanRequest loadRequest) {
  }

  @Override
  public void loadMany(LoadManyRequest loadRequest) {
  }

  @Override
  public int lazyLoadBatchSize() {
    return 0;
  }

  @Override
  public boolean isSupportedType(java.lang.reflect.Type genericType) {
    return false;
  }

  @Override
  public void visitMetrics(MetricVisitor visitor) {
  }

  @Override
  public void loadMany(BeanCollection<?> collection, boolean onlyIds) {
  }

  @Override
  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {
  }

  @Override
  public AutoTune autoTune() {
    return null;
  }

  @Override
  public DataSource dataSource() {
    return null;
  }

  @Override
  public DataSource readOnlyDataSource() {
    return null;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public ExpressionFactory expressionFactory() {
    return null;
  }

  @Override
  public MetaInfoManager metaInfo() {
    return null;
  }

  @Override
  public BeanState beanState(Object bean) {
    return null;
  }

  @Override
  public Object beanId(Object bean, Object id) {
    return id;
  }

  @Override
  public Object beanId(Object bean) {
    return null;
  }

  @Override
  public Map<String, ValuePair> diff(Object a, Object b) {
    return null;
  }

  @Override
  public <T> T createEntityBean(Class<T> type) {
    return null;
  }

  @Override
  public <T> CsvReader<T> createCsvReader(Class<T> beanType) {
    return null;
  }

  @Override
  public <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery) {
    return null;
  }

  @Override
  public <T> Query<T> createQuery(Class<T> beanType, String eql) {
    return null;
  }

  @Override
  public <T> Query<T> createQuery(Class<T> beanType) {
    return null;
  }

  @Override
  public <T> Query<T> find(Class<T> beanType) {
    return null;
  }

  @Override
  public <T> Query<T> findNative(Class<T> beanType, String nativeSql) {
    return null;
  }

  @Override
  public <T> Set<String> validateQuery(Query<T> query) {
    return null;
  }

  @Override
  public Object nextId(Class<?> beanType) {
    return null;
  }

  @Override
  public <T> Filter<T> filter(Class<T> beanType) {
    return null;
  }

  @Override
  public <T> void sort(List<T> list, String sortByClause) {
  }

  @Override
  public <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate) {
    return null;
  }

  @Override
  public <T> void findDtoEach(SpiDtoQuery<T> query, Consumer<T> consumer) {
  }

  @Override
  public <T> void findDtoEach(SpiDtoQuery<T> query, int batch, Consumer<List<T>> consumer) {
  }

  @Override
  public <T> void findDtoEachWhile(SpiDtoQuery<T> query, Predicate<T> consumer) {
  }

  @Override
  public <T> QueryIterator<T> findDtoIterate(SpiDtoQuery<T> query) {
    return null;
  }

  @Override
  public <T> Stream<T> findDtoStream(SpiDtoQuery<T> query) {
    return null;
  }

  @Override
  public <T> List<T> findDtoList(SpiDtoQuery<T> query) {
    return null;
  }

  @Override
  public <T> T findDtoOne(SpiDtoQuery<T> query) {
    return null;
  }

  @Override
  public <D> DtoQuery<D> findDto(Class<D> dtoType, String sql) {
    return null;
  }

  @Override
  public <T> DtoQuery<T> createNamedDtoQuery(Class<T> dtoType, String namedQuery) {
    return null;
  }

  @Override
  public <D> DtoQuery<D> findDto(Class<D> dtoType, SpiQuery<?> ormQuery) {
    return null;
  }

  @Override
  public SpiResultSet findResultSet(SpiQuery<?> ormQuery, SpiTransaction transaction) {
    return null;
  }

  @Override
  public <T> T findSingleAttribute(SpiSqlQuery query, Class<T> cls) {
    return null;
  }

  @Override
  public <T> List<T> findSingleAttributeList(SpiSqlQuery query, Class<T> cls) {
    return null;
  }

  @Override
  public <T> void findSingleAttributeEach(SpiSqlQuery query, Class<T> cls, Consumer<T> consumer) {
  }

  @Override
  public <T> T findOneMapper(SpiSqlQuery query, RowMapper<T> mapper) {
    return null;
  }

  @Override
  public <T> List<T> findListMapper(SpiSqlQuery query, RowMapper<T> mapper) {
    return null;
  }

  @Override
  public void findEachRow(SpiSqlQuery query, RowConsumer consumer) {
  }

  @Override
  public SqlQuery sqlQuery(String sql) {
    return null;
  }

  @Override
  public SqlQuery createSqlQuery(String sql) {
    return sqlQuery(sql);
  }

  @Override
  public SqlUpdate sqlUpdate(String sql) {
    return null;
  }

  @Override
  public SqlUpdate createSqlUpdate(String sql) {
    return sqlUpdate(sql);
  }

  @Override
  public CallableSql createCallableSql(String callableSql) {
    return null;
  }

  @Override
  public void register(TransactionCallback transactionCallback) throws PersistenceException {
  }

  @Override
  public <T> T publish(Class<T> beanType, Object id) {
    return null;
  }

  @Override
  public <T> List<T> publish(Query<T> query) {
    return null;
  }

  @Override
  public <T> T publish(Class<T> beanType, Object id, Transaction transaction) {
    return null;
  }

  @Override
  public <T> List<T> publish(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> T draftRestore(Class<T> beanType, Object id, Transaction transaction) {
    return null;
  }

  @Override
  public <T> List<T> draftRestore(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> T draftRestore(Class<T> beanType, Object id) {
    return null;
  }

  @Override
  public <T> List<T> draftRestore(Query<T> query) {
    return null;
  }

  @Override
  public Transaction createTransaction() {
    return null;
  }

  @Override
  public Transaction createTransaction(TxIsolation isolation) {
    return null;
  }

  @Override
  public Transaction beginTransaction() {
    return null;
  }

  @Override
  public Transaction beginTransaction(TxScope scope) {
    return null;
  }

  @Override
  public Transaction beginTransaction(TxIsolation isolation) {
    return null;
  }

  @Override
  public Transaction currentTransaction() {
    return null;
  }

  @Override
  public void flush() {
  }

  @Override
  public void commitTransaction() {
  }

  @Override
  public void rollbackTransaction() {
  }

  @Override
  public void endTransaction() {
  }

  @Override
  public void refresh(Object bean) {
  }

  @Override
  public void refreshMany(Object bean, String propertyName) {
  }

  @Override
  public boolean exists(Class<?> beanType, Object beanId, Transaction transaction) {
    return false;
  }

  @Override
  public <T> boolean exists(Query<T> ormQuery, Transaction transaction) {
    return false;
  }

  @Override
  public <T> T find(Class<T> beanType, Object uid) {
    return null;
  }

  @Override
  public <T> T reference(Class<T> beanType, Object id) {
    return null;
  }

  @Override
  public <T> int findCount(Query<T> query, Transaction transaction) {
    return 0;
  }

  @Override
  public <A, T> List<A> findIds(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> QueryIterator<T> findIterate(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> Stream<T> findStream(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> Stream<T> findLargeStream(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> void findEach(Query<T> query, Consumer<T> consumer, Transaction transaction) {
  }

  @Override
  public <T> void findEach(Query<T> query, int batch, Consumer<List<T>> consumer, Transaction t) {
  }

  @Override
  public <T> void findEachWhile(Query<T> query, Predicate<T> consumer, Transaction transaction) {
  }

  @Override
  public <T> List<T> findList(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> FutureRowCount<T> findFutureCount(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> FutureIds<T> findFutureIds(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> FutureList<T> findFutureList(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> PagedList<T> findPagedList(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> Set<T> findSet(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <K, T> Map<K, T> findMap(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <A, T> List<A> findSingleAttributeList(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> T findOne(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <T> Optional<T> findOneOrEmpty(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public List<SqlRow> findList(SqlQuery query, Transaction transaction) {
    return null;
  }

  @Override
  public void findEach(SqlQuery query, Consumer<SqlRow> consumer, Transaction transaction) {
  }

  @Override
  public void findEachWhile(SqlQuery query, Predicate<SqlRow> consumer, Transaction transaction) {
  }

  @Override
  public SqlRow findOne(SqlQuery query, Transaction transaction) {
    return null;
  }

  @Override
  public void save(Object bean) throws OptimisticLockException {
  }

  @Override
  public boolean delete(Object bean) throws OptimisticLockException {
    return false;
  }

  @Override
  public int delete(Class<?> beanType, Object id) {
    return 0;
  }

  @Override
  public int delete(Class<?> beanType, Object id, Transaction transaction) {
    return 0;
  }

  @Override
  public int deletePermanent(Class<?> beanType, Object id) {
    return 0;
  }

  @Override
  public int deletePermanent(Class<?> beanType, Object id, Transaction transaction) {
    return 0;
  }

  @Override
  public int execute(SqlUpdate updSql) {
    return 0;
  }

  @Override
  public int execute(Update<?> update) {
    return 0;
  }

  @Override
  public int execute(Update<?> update, Transaction t) {
    return 0;
  }

  @Override
  public int execute(CallableSql callableSql) {
    return 0;
  }

  @Override
  public void externalModification(String tableName, boolean inserted, boolean updated, boolean deleted) {
  }

  @Override
  public <T> T find(Class<T> beanType, Object uid, Transaction transaction) {
    return null;
  }

  @Override
  public void save(Object bean, Transaction transaction) throws OptimisticLockException {
  }

  @Override
  public void markAsDirty(Object bean) {
  }

  @Override
  public void update(Object bean) throws OptimisticLockException {
  }

  @Override
  public void update(Object bean, Transaction t) throws OptimisticLockException {
  }

  @Override
  public void insert(Object bean) {
  }

  @Override
  public void insert(Object bean, Transaction t) {
  }

  @Override
  public boolean delete(Object bean, Transaction t) throws OptimisticLockException {
    return false;
  }

  @Override
  public boolean deletePermanent(Object bean) throws OptimisticLockException {
    return false;
  }

  @Override
  public boolean deletePermanent(Object bean, Transaction transaction) throws OptimisticLockException {
    return false;
  }

  @Override
  public int deleteAllPermanent(Collection<?> beans) throws OptimisticLockException {
    return 0;
  }

  @Override
  public int deleteAllPermanent(Collection<?> beans, Transaction transaction) throws OptimisticLockException {
    return 0;
  }

  @Override
  public int execute(SqlUpdate updSql, Transaction t) {
    return 0;
  }

  @Override
  public int executeNow(SpiSqlUpdate sqlUpdate) {
    return 0;
  }

  @Override
  public void addBatch(SpiSqlUpdate sqlUpdate, SpiTransaction transaction) {
  }

  @Override
  public int[] executeBatch(SpiSqlUpdate defaultSqlUpdate, SpiTransaction transaction) {
    return new int[0];
  }

  @Override
  public int execute(CallableSql callableSql, Transaction t) {
    return 0;
  }

  @Override
  public void execute(TxScope scope, Runnable r) {
  }

  @Override
  public void execute(Runnable r) {
  }

  @Override
  public <T> T executeCall(TxScope scope, Callable<T> callable) {
    return null;
  }

  @Override
  public <T> T executeCall(Callable<T> callable) {
    return null;
  }

  @Override
  public ServerCacheManager cacheManager() {
    return null;
  }

  @Override
  public BackgroundExecutor backgroundExecutor() {
    return null;
  }

  @Override
  public JsonContext json() {
    return null;
  }

  @Override
  public SpiJsonContext jsonExtended() {
    return null;
  }

  @Override
  public int saveAll(Object... beans) throws OptimisticLockException {
    return 0;
  }

  @Override
  public int saveAll(Collection<?> beans) throws OptimisticLockException {
    return 0;
  }

  @Override
  public int deleteAll(Collection<?> beans) throws OptimisticLockException {
    return 0;
  }

  @Override
  public int deleteAll(Collection<?> beans, Transaction transaction) throws OptimisticLockException {
    return 0;
  }

  @Override
  public int deleteAll(Class<?> beanType, Collection<?> ids) {
    return 0;
  }

  @Override
  public int deleteAll(Class<?> beanType, Collection<?> ids, Transaction transaction) {
    return 0;
  }

  @Override
  public int deleteAllPermanent(Class<?> beanType, Collection<?> ids) {
    return 0;
  }

  @Override
  public int deleteAllPermanent(Class<?> beanType, Collection<?> ids, Transaction transaction) {
    return 0;
  }

  @Override
  public int saveAll(Collection<?> beans, Transaction transaction) throws OptimisticLockException {
    return 0;
  }

  @Override
  public void updateAll(Collection<?> beans) throws OptimisticLockException {
  }

  @Override
  public void updateAll(Collection<?> beans, Transaction transaction) throws OptimisticLockException {
  }

  @Override
  public void insertAll(Collection<?> beans) {
  }

  @Override
  public void insertAll(Collection<?> beans, Transaction transaction) {
  }

  @Override
  public void slowQueryCheck(long executionTimeMicros, int rowCount, SpiQuery<?> query) {
  }

  @Override
  public Set<Property> checkUniqueness(Object bean) {
    return Collections.emptySet();
  }

  @Override
  public Set<Property> checkUniqueness(Object bean, Transaction transaction) {
    return Collections.emptySet();
  }

  @Override
  public SpiQueryBindCapture createQueryBindCapture(SpiQueryPlan queryPlan) {
    return null;
  }
}
