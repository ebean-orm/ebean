package io.ebeaninternal.api;

import io.ebean.ProfileLocation;
import io.ebean.Transaction;
import io.ebean.TransactionCallback;
import io.ebean.annotation.DocStoreMode;
import io.ebean.annotation.PersistBatch;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.server.core.PersistDeferredRelationship;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.transaction.ProfileStream;
import io.ebeanservice.docstore.api.DocStoreTransaction;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Proxy for an underlying SpiTransaction (most of the API).
 */
abstract class SpiTransactionProxy implements SpiTransaction {

  protected SpiTransaction transaction;

  @Override
  public PersistenceException translate(String message, SQLException cause) {
    return transaction.translate(message, cause);
  }

  @Override
  public Transaction setLabel(String label) {
    transaction.setLabel(label);
    return this;
  }

  @Override
  public String getLabel() {
    return transaction.getLabel();
  }

  @Override
  public void commitAndContinue() {
    transaction.commitAndContinue();
  }

  @Override
  public boolean isRollbackOnly() {
    return transaction.isRollbackOnly();
  }

  @Override
  public long profileOffset() {
    return transaction.profileOffset();
  }

  @Override
  public void profileEvent(SpiProfileTransactionEvent event) {
    transaction.profileEvent(event);
  }

  @Override
  public void setProfileStream(ProfileStream profileStream) {
    transaction.setProfileStream(profileStream);
  }

  @Override
  public ProfileStream profileStream() {
    return transaction.profileStream();
  }

  @Override
  public void setProfileLocation(ProfileLocation profileLocation) {
    transaction.setProfileLocation(profileLocation);
  }

  @Override
  public ProfileLocation getProfileLocation() {
    return transaction.getProfileLocation();
  }

  @Override
  public void setTenantId(Object tenantId) {
    transaction.setTenantId(tenantId);
  }

  @Override
  public Object getTenantId() {
    return transaction.getTenantId();
  }

  @Override
  public DocStoreTransaction getDocStoreTransaction() {
    return transaction.getDocStoreTransaction();
  }

  @Override
  public DocStoreMode getDocStoreMode() {
    return transaction.getDocStoreMode();
  }

  @Override
  public Transaction setDocStoreMode(DocStoreMode mode) {
    transaction.setDocStoreMode(mode);
    return this;
  }

  @Override
  public int getDocStoreBatchSize() {
    return transaction.getDocStoreBatchSize();
  }

  @Override
  public Transaction setDocStoreBatchSize(int batchSize) {
    transaction.setDocStoreBatchSize(batchSize);
    return this;
  }

  @Override
  public String getLogPrefix() {
    return transaction.getLogPrefix();
  }

  @Override
  public boolean isLogSql() {
    return transaction.isLogSql();
  }

  @Override
  public boolean isLogSummary() {
    return transaction.isLogSummary();
  }

  @Override
  public void logSql(String msg) {
    transaction.logSql(msg);
  }

  @Override
  public void logSummary(String msg) {
    transaction.logSummary(msg);
  }

  @Override
  public Transaction setSkipCache(boolean skipCache) {
    transaction.setSkipCache(skipCache);
    return this;
  }

  @Override
  public boolean isSkipCache() {
    return transaction.isSkipCache();
  }

  @Override
  public void addBeanChange(BeanChange beanChange) {
    transaction.addBeanChange(beanChange);
  }

  @Override
  public void sendChangeLog(ChangeSet changes) {
    transaction.sendChangeLog(changes);
  }

  @Override
  public void registerDeferred(PersistDeferredRelationship derived) {
    transaction.registerDeferred(derived);
  }

  @Override
  public void registerDeleteBean(Integer hash) {
    transaction.registerDeleteBean(hash);
  }

  @Override
  public void unregisterDeleteBean(Integer hash) {
    transaction.unregisterDeleteBean(hash);
  }

  @Override
  public boolean isRegisteredDeleteBean(Integer hash) {
    return transaction.isRegisteredDeleteBean(hash);
  }

  @Override
  public void unregisterBean(Object bean) {
    transaction.unregisterBean(bean);
  }

  @Override
  public boolean isRegisteredBean(Object bean) {
    return transaction.isRegisteredBean(bean);
  }

  @Override
  public String getId() {
    return transaction.getId();
  }

  @Override
  public Transaction register(TransactionCallback callback) {
    transaction.register(callback);
    return this;
  }

  @Override
  public boolean isReadOnly() {
    return transaction.isReadOnly();
  }

  @Override
  public Transaction setReadOnly(boolean readOnly) {
    transaction.setReadOnly(readOnly);
    return this;
  }

  @Override
  public boolean isActive() {
    return transaction.isActive();
  }

  @Override
  public Transaction setPersistCascade(boolean persistCascade) {
    transaction.setPersistCascade(persistCascade);
    return this;
  }

  @Override
  public Transaction setUpdateAllLoadedProperties(boolean updateAllLoaded) {
    transaction.setUpdateAllLoadedProperties(updateAllLoaded);
    return this;
  }

  @Override
  public Boolean isUpdateAllLoadedProperties() {
    return transaction.isUpdateAllLoadedProperties();
  }

  @Override
  public Transaction setBatchMode(boolean useBatch) {
    transaction.setBatchMode(useBatch);
    return this;
  }

  @Override
  public Transaction setBatch(PersistBatch persistBatchMode) {
    transaction.setBatch(persistBatchMode);
    return this;
  }

  @Override
  public PersistBatch getBatch() {
    return transaction.getBatch();
  }

  @Override
  public Transaction setBatchOnCascade(PersistBatch batchOnCascadeMode) {
    transaction.setBatchOnCascade(batchOnCascadeMode);
    return this;
  }

  @Override
  public PersistBatch getBatchOnCascade() {
    return transaction.getBatchOnCascade();
  }

  @Override
  public Transaction setBatchSize(int batchSize) {
    transaction.setBatchSize(batchSize);
    return this;
  }

  @Override
  public int getBatchSize() {
    return transaction.getBatchSize();
  }

  @Override
  public Transaction setBatchGetGeneratedKeys(boolean getGeneratedKeys) {
    transaction.setBatchGetGeneratedKeys(getGeneratedKeys);
    return this;
  }

  @Override
  public Boolean getBatchGetGeneratedKeys() {
    return transaction.getBatchGetGeneratedKeys();
  }

  @Override
  public Transaction setBatchFlushOnMixed(boolean batchFlushOnMixed) {
    transaction.setBatchFlushOnMixed(batchFlushOnMixed);
    return this;
  }

  @Override
  public Transaction setBatchFlushOnQuery(boolean batchFlushOnQuery) {
    transaction.setBatchFlushOnQuery(batchFlushOnQuery);
    return this;
  }

  @Override
  public boolean isBatchFlushOnQuery() {
    return transaction.isBatchFlushOnQuery();
  }

  @Override
  public void flush() throws PersistenceException {
    transaction.flush();
  }

  @Override
  public void flushBatch() throws PersistenceException {
    flush();
  }

  @Override
  public Connection getConnection() {
    return transaction.getConnection();
  }

  @Override
  public Transaction addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    transaction.addModification(tableName, inserts, updates, deletes);
    return this;
  }

  @Override
  public Transaction putUserObject(String name, Object value) {
    transaction.putUserObject(name, value);
    return this;
  }

  @Override
  public Object getUserObject(String name) {
    return transaction.getUserObject(name);
  }

  @Override
  public void depth(int diff) {
    transaction.depth();
  }

  @Override
  public int depth() {
    return transaction.depth();
  }

  @Override
  public boolean isExplicit() {
    return transaction.isExplicit();
  }

  @Override
  public TransactionEvent getEvent() {
    return transaction.getEvent();
  }

  @Override
  public boolean isPersistCascade() {
    return transaction.isPersistCascade();
  }

  @Override
  public boolean isBatchThisRequest(PersistRequest.Type type) {
    return transaction.isBatchThisRequest(type);
  }

  @Override
  public BatchControl getBatchControl() {
    return transaction.getBatchControl();
  }

  @Override
  public void setBatchControl(BatchControl control) {
    transaction.setBatchControl(control);
  }

  @Override
  public PersistenceContext getPersistenceContext() {
    return transaction.getPersistenceContext();
  }

  @Override
  public void setPersistenceContext(PersistenceContext context) {
    transaction.setPersistenceContext(context);
  }

  @Override
  public Connection getInternalConnection() {
    return transaction.getInternalConnection();
  }

  @Override
  public boolean isSaveAssocManyIntersection(String intersectionTable, String beanName) {
    return transaction.isSaveAssocManyIntersection(intersectionTable, beanName);
  }

  @Override
  public boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request) {
    return transaction.checkBatchEscalationOnCascade(request);
  }

  @Override
  public void flushBatchOnCascade() {
    transaction.flushBatchOnCascade();
  }

  @Override
  public void flushBatchOnRollback() {
    transaction.flushBatchOnRollback();
  }

  @Override
  public void markNotQueryOnly() {
    transaction.markNotQueryOnly();
  }

  @Override
  public void checkBatchEscalationOnCollection() {
    transaction.checkBatchEscalationOnCollection();
  }

  @Override
  public void flushBatchOnCollection() {
    transaction.flushBatchOnCollection();
  }

}
