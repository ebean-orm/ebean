package io.ebeaninternal.api;

import io.ebean.ProfileLocation;
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
public abstract class SpiTransactionProxy implements SpiTransaction {

  protected SpiTransaction transaction;

  @Override
  public PersistenceException translate(String message, SQLException cause) {
    return transaction.translate(message, cause);
  }

  @Override
  public long getStartMillis() {
    return transaction.getStartMillis();
  }

  @Override
  public void setLabel(String label) {
    transaction.setLabel(label);
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
  public void setNestedUseSavepoint() {
    transaction.setNestedUseSavepoint();
  }

  @Override
  public boolean isNestedUseSavepoint() {
    return transaction.isNestedUseSavepoint();
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
  public void setDocStoreMode(DocStoreMode mode) {
    transaction.setDocStoreMode(mode);
  }

  @Override
  public int getDocStoreBatchSize() {
    return transaction.getDocStoreBatchSize();
  }

  @Override
  public void setDocStoreBatchSize(int batchSize) {
    transaction.setDocStoreBatchSize(batchSize);
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
  public void setSkipCache(boolean skipCache) {
    transaction.setSkipCache(skipCache);
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
  public void register(TransactionCallback callback) {
    transaction.register(callback);
  }

  @Override
  public boolean isReadOnly() {
    return transaction.isReadOnly();
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    transaction.setReadOnly(readOnly);
  }

  @Override
  public boolean isActive() {
    return transaction.isActive();
  }

  @Override
  public void setPersistCascade(boolean persistCascade) {
    transaction.setPersistCascade(persistCascade);
  }

  @Override
  public void setUpdateAllLoadedProperties(boolean updateAllLoaded) {
    transaction.setUpdateAllLoadedProperties(updateAllLoaded);
  }

  @Override
  public Boolean isUpdateAllLoadedProperties() {
    return transaction.isUpdateAllLoadedProperties();
  }

  @Override
  public void setBatchMode(boolean useBatch) {
    transaction.setBatchMode(useBatch);
  }

  @Override
  public boolean isBatchMode() {
    return transaction.isBatchMode();
  }

  @Override
  public void setBatch(PersistBatch persistBatchMode) {
    transaction.setBatch(persistBatchMode);
  }

  @Override
  public PersistBatch getBatch() {
    return transaction.getBatch();
  }

  @Override
  public void setBatchOnCascade(boolean batchMode) {
    transaction.setBatchOnCascade(batchMode);
  }

  @Override
  public void setBatchOnCascade(PersistBatch batchOnCascadeMode) {
    transaction.setBatchOnCascade(batchOnCascadeMode);
  }

  @Override
  public boolean isBatchOnCascade() {
    return transaction.isBatchOnCascade();
  }

  @Override
  public PersistBatch getBatchOnCascade() {
    return transaction.getBatchOnCascade();
  }

  @Override
  public void setBatchSize(int batchSize) {
    transaction.setBatchSize(batchSize);
  }

  @Override
  public int getBatchSize() {
    return transaction.getBatchSize();
  }

  @Override
  public void setBatchGetGeneratedKeys(boolean getGeneratedKeys) {
    transaction.setBatchGetGeneratedKeys(getGeneratedKeys);
  }

  @Override
  public Boolean getBatchGetGeneratedKeys() {
    return transaction.getBatchGetGeneratedKeys();
  }

  @Override
  public void setBatchFlushOnMixed(boolean batchFlushOnMixed) {
    transaction.setBatchFlushOnMixed(batchFlushOnMixed);
  }

  @Override
  public void setBatchFlushOnQuery(boolean batchFlushOnQuery) {
    transaction.setBatchFlushOnQuery(batchFlushOnQuery);
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
  public void addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    transaction.addModification(tableName, inserts, updates, deletes);
  }

  @Override
  public void putUserObject(String name, Object value) {
    transaction.putUserObject(name, value);
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
  public boolean isBatchThisRequest() {
    return transaction.isBatchThisRequest();
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
