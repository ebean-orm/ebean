package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;
import io.ebean.TransactionCallback;
import io.ebean.annotation.DocStoreMode;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.api.SpiPersistenceContext;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.server.core.PersistDeferredRelationship;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeanservice.docstore.api.DocStoreTransaction;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Placeholder for use with SUPPORTS and NEVER transactional when there really isn't a transaction.
 */
final class NoTransaction implements SpiTransaction {

  static final NoTransaction INSTANCE = new NoTransaction();

  @Override
  public void setAutoPersistUpdates(boolean autoPersistUpdates) {
    // do nothing
  }

  @Override
  public boolean isAutoPersistUpdates() {
    return false;
  }

  @Override
  public void setLabel(String label) {
    // do nothing
  }

  @Override
  public String label() {
    return null;
  }

  @Override
  public boolean isNestedUseSavepoint() {
    return false;
  }

  @Override
  public void setNestedUseSavepoint() {

  }

  @Override
  public Instant startTime() {
    // not used
    return Instant.now();
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
  public void rollbackAndContinue() {
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
  public void preCommit() {
    // do nothing
  }

  @Override
  public void postCommit() {
    // do nothing
  }

  @Override
  public void postRollback(Throwable cause) {
    // do nothing
  }

  @Override
  public void deactivateExternal() {
    // do nothing
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
  public void logSql(String msg, Object... args) {

  }

  @Override
  public void logSummary(String msg, Object... args) {

  }

  @Override
  public void logTxn(String msg, Object... args) {

  }

  @Override
  public void registerDeferred(PersistDeferredRelationship derived) {

  }

  @Override
  public void registerDeleteBean(Class<?> type, Object id) {

  }

  @Override
  public boolean isRegisteredDeleteBean(Class<?> type, Object id) {
    return false;
  }

  @Override
  public void unregisterBeans() {
  }

  @Override
  public boolean isRegisteredBean(Object bean) {
    return false;
  }

  @Override
  public String id() {
    return null;
  }

  @Override
  public Boolean isUpdateAllLoadedProperties() {
    return null;
  }

  @Override
  public DocStoreMode docStoreMode() {
    return null;
  }

  @Override
  public int getDocStoreBatchSize() {
    return 0;
  }

  @Override
  public void register(TransactionCallback callback) {
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
  }

  @Override
  public void setRollbackOnly() {
  }

  @Override
  public boolean isRollbackOnly() {
    return false;
  }


  @Override
  public void setDocStoreMode(DocStoreMode mode) {
  }

  @Override
  public void setDocStoreBatchSize(int batchSize) {
  }

  @Override
  public void setPersistCascade(boolean persistCascade) {
  }

  @Override
  public void setUpdateAllLoadedProperties(boolean updateAllLoadedProperties) {
  }

  @Override
  public void setSkipCache(boolean skipCache) {
  }

  @Override
  public boolean isSkipCache() {
    return false;
  }

  @Override
  public void setBatchMode(boolean useBatch) {
  }

  @Override
  public boolean isBatchMode() {
    return false;
  }

  @Override
  public void setBatchOnCascade(boolean batchMode) {
  }

  @Override
  public boolean isBatchOnCascade() {
    return false;
  }

  @Override
  public void setBatchSize(int batchSize) {
  }

  @Override
  public int getBatchSize() {
    return 0;
  }

  @Override
  public void setGetGeneratedKeys(boolean getGeneratedKeys) {
  }

  @Override
  public void setFlushOnMixed(boolean batchFlushOnMixed) {
  }

  @Override
  public void setFlushOnQuery(boolean batchFlushOnQuery) {
  }

  @Override
  public boolean isFlushOnQuery() {
    return false;
  }

  @Override
  public void flush() throws PersistenceException {
  }

  @Override
  public Connection connection() {
    return null;
  }

  @Override
  public void addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
  }

  @Override
  public void putUserObject(String name, Object value) {
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
  public boolean isExplicit() {
    return false;
  }

  @Override
  public TransactionEvent event() {
    return null;
  }

  @Override
  public boolean isPersistCascade() {
    return false;
  }

  @Override
  public boolean isBatchThisRequest() {
    return false;
  }

  @Override
  public BatchControl batchControl() {
    return null;
  }

  @Override
  public void setBatchControl(BatchControl control) {
  }

  @Override
  public SpiPersistenceContext persistenceContext() {
    return null;
  }

  @Override
  public void setPersistenceContext(SpiPersistenceContext context) {
  }

  @Override
  public Connection internalConnection() {
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
  public DocStoreTransaction docStoreTransaction() {
    return null;
  }

  @Override
  public void setTenantId(Object tenantId) {
  }

  @Override
  public Object tenantId() {
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
  public ProfileLocation profileLocation() {
    return null;
  }
}
