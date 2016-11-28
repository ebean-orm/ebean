package com.avaje.ebeaninternal.api;

import com.avaje.ebean.*;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.event.readaudit.ReadAuditLogger;
import com.avaje.ebean.event.readaudit.ReadAuditPrepare;
import com.avaje.ebean.meta.MetaInfoManager;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebean.text.csv.CsvReader;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebeaninternal.server.core.timezone.DataTimeZone;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.query.CQuery;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * Test double for SpiEbeanServer.
 */
public class TDSpiEbeanServer implements SpiEbeanServer {

  String name;

  public TDSpiEbeanServer() {
  }

  public TDSpiEbeanServer(String name) {
    this.name = name;
  }

  @Override
  public void shutdownManaged() {

  }

  @Override
  public Object currentTenantId() {
    return null;
  }

  @Override
  public DataTimeZone getDataTimeZone() {
    return null;
  }

  @Override
  public SpiServer getPluginApi() {
    return null;
  }

  @Override
  public boolean isCollectQueryOrigins() {
    return false;
  }

  @Override
  public boolean isUpdateAllPropertiesInBatch() {
    return false;
  }

  @Override
  public ServerConfig getServerConfig() {
    return null;
  }

  @Override
  public DatabasePlatform getDatabasePlatform() {
    return null;
  }

  @Override
  public CallStack createCallStack() {
    return null;
  }

  @Override
  public PersistenceContextScope getPersistenceContextScope(SpiQuery<?> query) {
    return null;
  }

  @Override
  public DocumentStore docStore() {
    return null;
  }

  @Override
  public ReadAuditLogger getReadAuditLogger() {
    return null;
  }

  @Override
  public ReadAuditPrepare getReadAuditPrepare() {
    return null;
  }

  @Override
  public void clearQueryStatistics() {

  }

  @Override
  public BeanDescriptor<?> getBeanDescriptorByQueueId(String queueId) {
    return null;
  }

  @Override
  public List<BeanDescriptor<?>> getBeanDescriptors() {
    return null;
  }

  @Override
  public <T> BeanDescriptor<T> getBeanDescriptor(Class<T> type) {
    return null;
  }

  @Override
  public BeanDescriptor<?> getBeanDescriptorById(String descriptorId) {
    return null;
  }

  @Override
  public List<BeanDescriptor<?>> getBeanDescriptors(String tableName) {
    return null;
  }

  @Override
  public void externalModification(TransactionEventTable event) {

  }

  @Override
  public SpiTransaction createServerTransaction(boolean isExplicit, int isolationLevel) {
    return null;
  }

  @Override
  public SpiTransaction getCurrentServerTransaction() {
    return null;
  }

  @Override
  public ScopeTrans createScopeTrans(TxScope txScope) {
    return null;
  }

  @Override
  public SpiTransaction createQueryTransaction() {
    return null;
  }

  @Override
  public void remoteTransactionEvent(RemoteTransactionEvent event) {

  }

  @Override
  public <T> CQuery<T> compileQuery(Query<T> query, Transaction t) {
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
  public <T> List<Version<T>> findVersions(Query<T> query, Transaction transaction) {
    return null;
  }

  @Override
  public <A, T> List<A> findIdsWithCopy(Query<T> query, Transaction t) {
    return null;
  }

  @Override
  public <T> int findRowCountWithCopy(Query<T> query, Transaction t) {
    return 0;
  }

  @Override
  public void loadBean(LoadBeanRequest loadRequest) {

  }

  @Override
  public void loadMany(LoadManyRequest loadRequest) {

  }

  @Override
  public int getLazyLoadBatchSize() {
    return 0;
  }

  @Override
  public boolean isSupportedType(Type genericType) {
    return false;
  }

  @Override
  public void collectQueryStats(ObjectGraphNode objectGraphNode, long loadedBeanCount, long timeMicros) {

  }

  @Override
  public void loadMany(BeanCollection<?> collection, boolean onlyIds) {

  }

  @Override
  public void loadBean(EntityBeanIntercept ebi) {

  }

  @Override
  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver) {

  }

  @Override
  public AutoTune getAutoTune() {
    return null;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ExpressionFactory getExpressionFactory() {
    return null;
  }

  @Override
  public MetaInfoManager getMetaInfoManager() {
    return null;
  }

  @Override
  public BeanState getBeanState(Object bean) {
    return null;
  }

  @Override
  public Object setBeanId(Object bean, Object id) {
    return id;
  }

  @Override
  public Object getBeanId(Object bean) {
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
  public SqlQuery createSqlQuery(String sql) {
    return null;
  }

  @Override
  public SqlUpdate createSqlUpdate(String sql) {
    return null;
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
  public <T> T find(Class<T> beanType, Object uid) {
    return null;
  }

  @Override
  public <T> T getReference(Class<T> beanType, Object id) {
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
  public <T> void findEach(Query<T> query, Consumer<T> consumer, Transaction transaction) {

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
  public <T> T findUnique(Query<T> query, Transaction transaction) {
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
  public SqlRow findUnique(SqlQuery query, Transaction transaction) {
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
  public void update(Object bean, Transaction transaction, boolean deleteMissingChildren) throws OptimisticLockException {

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
  public int execute(CallableSql callableSql, Transaction t) {
    return 0;
  }

  @Override
  public void execute(TxScope scope, TxRunnable r) {

  }

  @Override
  public void execute(TxRunnable r) {

  }

  @Override
  public <T> T execute(TxScope scope, TxCallable<T> c) {
    return null;
  }

  @Override
  public <T> T execute(TxCallable<T> c) {
    return null;
  }

  @Override
  public ServerCacheManager getServerCacheManager() {
    return null;
  }

  @Override
  public BackgroundExecutor getBackgroundExecutor() {
    return null;
  }

  @Override
  public JsonContext json() {
    return null;
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
}
