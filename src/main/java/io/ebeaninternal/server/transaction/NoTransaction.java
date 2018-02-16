package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;
import io.ebean.Transaction;
import io.ebean.TransactionCallback;
import io.ebean.annotation.DocStoreMode;
import io.ebean.annotation.PersistBatch;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.server.core.PersistDeferredRelationship;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeanservice.docstore.api.DocStoreTransaction;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Place holder for use with SUPPORTS and NEVER transactional when there really isn't a transaction.
 */
class NoTransaction implements SpiTransaction {

  static final NoTransaction INSTANCE = new NoTransaction();

  @Override
  public Transaction setLabel(String label) {
    return this;
  }

  @Override
  public String getLabel() {
    return null;
  }

  @Override
  public boolean isActive() {
    // always false
    return false;
  }

  @Override
  public void commitAndContinue() {
    // do nothing
  }

  @Override
  public void commit() {
    // do nothing
  }

  @Override
  public void rollback() throws PersistenceException {
    // do nothing
  }


  @Override
  public void rollback(Throwable e) throws PersistenceException {
    // do nothing
  }

  @Override
  public void end() {
    // do nothing
  }

  @Override
  public void close() {
    // do nothing
  }

  @Override
  public String getLogPrefix() {
    return null;
  }

  @Override
  public boolean isLogSql() {
    return false;
  }

  @Override
  public boolean isLogSummary() {
    return false;
  }

  @Override
  public void logSql(String msg) {

  }

  @Override
  public void logSummary(String msg) {

  }

  @Override
  public void registerDeferred(PersistDeferredRelationship derived) {

  }

  @Override
  public void registerDeleteBean(Integer hash) {

  }

  @Override
  public void unregisterDeleteBean(Integer hash) {

  }

  @Override
  public boolean isRegisteredDeleteBean(Integer hash) {
    return false;
  }

  @Override
  public void unregisterBean(Object bean) {

  }

  @Override
  public boolean isRegisteredBean(Object bean) {
    return false;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public Boolean isUpdateAllLoadedProperties() {
    return null;
  }

  @Override
  public DocStoreMode getDocStoreMode() {
    return null;
  }

  @Override
  public int getDocStoreBatchSize() {
    return 0;
  }

  @Override
  public Transaction register(TransactionCallback callback) {
    return this;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public Transaction setReadOnly(boolean readOnly) {
    return this;
  }

  @Override
  public Transaction setRollbackOnly() {
    return this;
  }

  @Override
  public boolean isRollbackOnly() {
    return false;
  }


  @Override
  public Transaction setDocStoreMode(DocStoreMode mode) {
    return this;
  }

  @Override
  public Transaction setDocStoreBatchSize(int batchSize) {
    return this;
  }

  @Override
  public Transaction setPersistCascade(boolean persistCascade) {
    return this;
  }

  @Override
  public Transaction setUpdateAllLoadedProperties(boolean updateAllLoadedProperties) {
    return this;
  }

  @Override
  public Transaction setSkipCache(boolean skipCache) {
    return this;
  }

  @Override
  public boolean isSkipCache() {
    return false;
  }

  @Override
  public Transaction setBatchMode(boolean useBatch) {
    return this;
  }

  @Override
  public Transaction setBatch(PersistBatch persistBatchMode) {
    return this;
  }

  @Override
  public PersistBatch getBatch() {
    return null;
  }

  @Override
  public Transaction setBatchOnCascade(PersistBatch batchOnCascadeMode) {
    return this;
  }

  @Override
  public PersistBatch getBatchOnCascade() {
    return null;
  }

  @Override
  public Transaction setBatchSize(int batchSize) {
    return this;
  }

  @Override
  public int getBatchSize() {
    return 0;
  }

  @Override
  public Transaction setBatchGetGeneratedKeys(boolean getGeneratedKeys) {
    return this;
  }

  @Override
  public Transaction setBatchFlushOnMixed(boolean batchFlushOnMixed) {
    return this;
  }

  @Override
  public Transaction setBatchFlushOnQuery(boolean batchFlushOnQuery) {
    return this;
  }

  @Override
  public boolean isBatchFlushOnQuery() {
    return false;
  }

  @Override
  public void flush() throws PersistenceException {

  }

  @Override
  public void flushBatch() throws PersistenceException {

  }

  @Override
  public Connection getConnection() {
    return null;
  }

  @Override
  public Transaction addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    return this;
  }

  @Override
  public Transaction putUserObject(String name, Object value) {
    return this;
  }

  @Override
  public Object getUserObject(String name) {
    return null;
  }

  @Override
  public Boolean getBatchGetGeneratedKeys() {
    return null;
  }

  @Override
  public void depth(int diff) {

  }

  @Override
  public int depth() {
    return 0;
  }

  @Override
  public boolean isExplicit() {
    return false;
  }

  @Override
  public TransactionEvent getEvent() {
    return null;
  }

  @Override
  public boolean isPersistCascade() {
    return false;
  }

  @Override
  public boolean isBatchThisRequest(PersistRequest.Type type) {
    return false;
  }

  @Override
  public BatchControl getBatchControl() {
    return null;
  }

  @Override
  public void setBatchControl(BatchControl control) {

  }

  @Override
  public PersistenceContext getPersistenceContext() {
    return null;
  }

  @Override
  public void setPersistenceContext(PersistenceContext context) {

  }

  @Override
  public Connection getInternalConnection() {
    return null;
  }

  @Override
  public boolean isSaveAssocManyIntersection(String intersectionTable, String beanName) {
    return false;
  }

  @Override
  public boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request) {
    return false;
  }

  @Override
  public void flushBatchOnCascade() {

  }

  @Override
  public void flushBatchOnRollback() {

  }

  @Override
  public PersistenceException translate(String message, SQLException cause) {
    return null;
  }

  @Override
  public void markNotQueryOnly() {

  }

  @Override
  public void checkBatchEscalationOnCollection() {

  }

  @Override
  public void flushBatchOnCollection() {

  }

  @Override
  public void addBeanChange(BeanChange beanChange) {

  }

  @Override
  public void sendChangeLog(ChangeSet changeSet) {

  }

  @Override
  public DocStoreTransaction getDocStoreTransaction() {
    return null;
  }

  @Override
  public void setTenantId(Object tenantId) {

  }

  @Override
  public Object getTenantId() {
    return null;
  }

  @Override
  public long profileOffset() {
    return 0;
  }

  @Override
  public void profileEvent(SpiProfileTransactionEvent event) {

  }

  @Override
  public void setProfileStream(ProfileStream profileStream) {

  }

  @Override
  public ProfileStream profileStream() {
    return null;
  }

  @Override
  public void setProfileLocation(ProfileLocation profileLocation) {

  }

  @Override
  public ProfileLocation getProfileLocation() {
    return null;
  }
}
