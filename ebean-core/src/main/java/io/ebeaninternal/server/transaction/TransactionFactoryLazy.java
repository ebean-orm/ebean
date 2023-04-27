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

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transaction factory used with multi-tenancy.
 */
class TransactionFactoryLazy extends TransactionFactory {

  final TransactionFactory delegate;

  TransactionFactoryLazy(TransactionFactory delegate) {
    super(delegate.manager);
    this.delegate = delegate;
  }

  interface LazyFactory {
    SpiTransaction create();
  }
  class TransactionLazy implements SpiTransaction {

    final LazyFactory factory;
    SpiTransaction delegate;
    private boolean active = true;

    SpiTransaction delegate() {
      if (delegate == null) {
        delegate = factory.create();
      }
      return delegate;
    }
    TransactionLazy(LazyFactory factory) {
      this.factory = factory;
    }

    @Override
    public void register(TransactionCallback callback) {
      delegate().register(callback);
    }

    @Override
    public void setAutoPersistUpdates(boolean autoPersistUpdates) {
      delegate().setAutoPersistUpdates(autoPersistUpdates);
    }

    @Override
    public void setLabel(String label) {
      delegate().setLabel(label);
    }

    @Override
    public boolean isReadOnly() {
      return delegate().isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
      delegate().setReadOnly(readOnly);
    }

    @Override
    public void commitAndContinue() {
      delegate().commitAndContinue();
    }

    @Override
    public void commit() {
      if (delegate != null) {
        delegate.commit();
      } else {
        System.out.println("TransactionFactoryLazy: skip commit()");
      }
      active = false;
    }

    @Override
    public void rollback() throws PersistenceException {
      if (delegate != null) {
        delegate.rollback();
      } else {
        System.out.println("TransactionFactoryLazy: skip rollback()");
      }
      active = false;
    }

    @Override
    public void rollback(Throwable e) throws PersistenceException {
      if (delegate != null) {
        delegate.rollback(e);
      } else {
        System.out.println("TransactionFactoryLazy: skip rollback()");
      }
      active = false;
    }

    @Override
    public void rollbackAndContinue() {
      delegate().rollbackAndContinue();
    }

    @Override
    public void setNestedUseSavepoint() {
      delegate().setNestedUseSavepoint();
    }

    @Override
    public void setRollbackOnly() {
      delegate().setRollbackOnly();
    }

    @Override
    public boolean isRollbackOnly() {
      return delegate().isRollbackOnly();
    }

    @Override
    public void end() {
      if (delegate != null) {
        delegate.end();
      } else {
        System.out.println("TransactionFactoryLazy: skip end()");
      }
      active = false;
    }

    @Override
    public void close() {
      if (delegate != null) {
        delegate.close();
      } else {
        System.out.println("TransactionFactoryLazy: skip close()");
      }
      active = false;
    }

    @Override
    public boolean isActive() {
      if (delegate == null) {
        return active;
      }
      return delegate().isActive();
    }

    @Override
    public void setDocStoreMode(DocStoreMode mode) {
      delegate().setDocStoreMode(mode);
    }

    @Override
    public void setDocStoreBatchSize(int batchSize) {
      delegate().setDocStoreBatchSize(batchSize);
    }

    @Override
    public void setPersistCascade(boolean persistCascade) {
      delegate().setPersistCascade(persistCascade);
    }

    @Override
    public void setUpdateAllLoadedProperties(boolean updateAllLoadedProperties) {
      delegate().setUpdateAllLoadedProperties(updateAllLoadedProperties);
    }

    @Override
    public void setSkipCache(boolean skipCache) {
      delegate().setSkipCache(skipCache);
    }

    @Override
    public boolean isSkipCache() {
      if (delegate != null) {
        return delegate.isSkipCache();
      }
      return false;
    }

    @Override
    public void setBatchMode(boolean useBatch) {
      delegate().setBatchMode(useBatch);
    }

    @Override
    public boolean isBatchMode() {
      return delegate().isBatchMode();
    }

    @Override
    public void setBatchOnCascade(boolean batchMode) {
      delegate().setBatchOnCascade(batchMode);
    }

    @Override
    public boolean isBatchOnCascade() {
      return delegate().isBatchOnCascade();
    }

    @Override
    public void setBatchSize(int batchSize) {
      delegate().setBatchSize(batchSize);
    }

    @Override
    public void setGetGeneratedKeys(boolean getGeneratedKeys) {
      delegate().setGetGeneratedKeys(getGeneratedKeys);
    }

    @Override
    public void setFlushOnMixed(boolean batchFlushOnMixed) {
      delegate().setFlushOnMixed(batchFlushOnMixed);
    }

    @Override
    public void setFlushOnQuery(boolean batchFlushOnQuery) {
      delegate().setFlushOnQuery(batchFlushOnQuery);
    }

    @Override
    public boolean isFlushOnQuery() {
      return delegate().isFlushOnQuery();
    }

    @Override
    public void flush() throws PersistenceException {
      delegate().flush();
    }

    @Override
    public Connection connection() {
      return delegate().connection();
    }

    @Override
    public void addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
      delegate().addModification(tableName, inserts, updates, deletes);
    }

    @Override
    public void putUserObject(String name, Object value) {
      delegate().putUserObject(name, value);
    }

    @Override
    public Object getUserObject(String name) {
      return delegate().getUserObject(name);
    }

    @Override
    public String getLabel() {
      return delegate().getLabel();
    }

    @Override
    public boolean isLogSql() {
      return delegate().isLogSql();
    }

    @Override
    public boolean isLogSummary() {
      return delegate().isLogSummary();
    }

    @Override
    public void logSql(String msg, Object... args) {
      delegate().logSql(msg, args);
    }

    @Override
    public void logSummary(String msg, Object... args) {
      delegate().logSummary(msg, args);
    }

    @Override
    public void logTxn(String msg, Object... args) {
      delegate().logTxn(msg, args);
    }

    @Override
    public void registerDeferred(PersistDeferredRelationship derived) {
      delegate().registerDeferred(derived);
    }

    @Override
    public void registerDeleteBean(Integer hash) {
      delegate().registerDeleteBean(hash);
    }

    @Override
    public boolean isRegisteredDeleteBean(Integer hash) {
      return delegate().isRegisteredDeleteBean(hash);
    }

    @Override
    public void unregisterBeans() {
      delegate().unregisterBeans();
    }

    @Override
    public boolean isRegisteredBean(Object bean) {
      return delegate().isRegisteredBean(bean);
    }

    @Override
    public String getId() {
      return delegate().getId();
    }

    @Override
    public long getStartNanoTime() {
      return delegate().getStartNanoTime();
    }

    @Override
    public Boolean isUpdateAllLoadedProperties() {
      return delegate().isUpdateAllLoadedProperties();
    }

    @Override
    public DocStoreMode getDocStoreMode() {
      return delegate().getDocStoreMode();
    }

    @Override
    public int getDocStoreBatchSize() {
      return delegate().getDocStoreBatchSize();
    }

    @Override
    public int getBatchSize() {
      return delegate().getBatchSize();
    }

    @Override
    public Boolean getBatchGetGeneratedKeys() {
      return delegate().getBatchGetGeneratedKeys();
    }

    @Override
    public void depth(int diff) {
      delegate().depth(diff);
    }

    @Override
    public void depthDecrement() {
      delegate().depthDecrement();
    }

    @Override
    public void depthReset() {
      delegate().depthReset();
    }

    @Override
    public int depth() {
      return delegate().depth();
    }

    @Override
    public boolean isAutoPersistUpdates() {
      return delegate().isAutoPersistUpdates();
    }

    @Override
    public boolean isExplicit() {
      return delegate().isExplicit();
    }

    @Override
    public TransactionEvent getEvent() {
      return delegate().getEvent();
    }

    @Override
    public boolean isPersistCascade() {
      return delegate().isPersistCascade();
    }

    @Override
    public boolean isBatchThisRequest() {
      return delegate().isBatchThisRequest();
    }

    @Override
    public BatchControl getBatchControl() {
      return delegate().getBatchControl();
    }

    @Override
    public void setBatchControl(BatchControl control) {
      delegate().setBatchControl(control);
    }

    @Override
    public SpiPersistenceContext getPersistenceContext() {
      return delegate().getPersistenceContext();
    }

    @Override
    public void setPersistenceContext(SpiPersistenceContext context) {
      delegate().setPersistenceContext(context);
    }

    @Override
    public Connection getInternalConnection() {
      return delegate().getInternalConnection();
    }

    @Override
    public boolean isSaveAssocManyIntersection(String intersectionTable, String beanName) {
      return delegate().isSaveAssocManyIntersection(intersectionTable, beanName);
    }

    @Override
    public boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request) {
      return delegate().checkBatchEscalationOnCascade(request);
    }

    @Override
    public void flushBatchOnCascade() {
      delegate().flushBatchOnCascade();
    }

    @Override
    public void flushBatchOnRollback() {
      delegate().flushBatchOnRollback();
    }

    @Override
    public PersistenceException translate(String message, SQLException cause) {
      return delegate().translate(message, cause);
    }

    @Override
    public void markNotQueryOnly() {
      delegate().markNotQueryOnly();
    }

    @Override
    public void checkBatchEscalationOnCollection() {
      delegate().checkBatchEscalationOnCollection();
    }

    @Override
    public void flushBatchOnCollection() {
      delegate().flushBatchOnCollection();
    }

    @Override
    public void addBeanChange(BeanChange beanChange) {
      delegate().addBeanChange(beanChange);
    }

    @Override
    public void sendChangeLog(ChangeSet changeSet) {
      delegate().sendChangeLog(changeSet);
    }

    @Override
    public DocStoreTransaction getDocStoreTransaction() {
      return delegate().getDocStoreTransaction();
    }

    @Override
    public void setTenantId(Object tenantId) {
      delegate().setTenantId(tenantId);
    }

    @Override
    public Object getTenantId() {
      return delegate().getTenantId();
    }

    @Override
    public long profileOffset() {
      return delegate().profileOffset();
    }

    @Override
    public void profileEvent(SpiProfileTransactionEvent event) {
      delegate().profileEvent(event);
    }

    @Override
    public void setProfileStream(ProfileStream profileStream) {
      delegate().setProfileStream(profileStream);
    }

    @Override
    public ProfileStream profileStream() {
      return delegate().profileStream();
    }

    @Override
    public void setProfileLocation(ProfileLocation profileLocation) {
      delegate().setProfileLocation(profileLocation);
    }

    @Override
    public ProfileLocation getProfileLocation() {
      return delegate().getProfileLocation();
    }

    @Override
    public boolean isNestedUseSavepoint() {
      return delegate().isNestedUseSavepoint();
    }

    @Override
    public boolean isSkipCacheExplicit() {
      return delegate().isSkipCacheExplicit();
    }

    @Override
    public void preCommit() {
      delegate().preCommit();
    }

    @Override
    public void postCommit() {
      delegate().postCommit();
    }

    @Override
    public void postRollback(Throwable cause) {
      delegate().postRollback(cause);
    }
  }

  @Override
  SpiTransaction createReadOnlyTransaction(Object tenantId) {
    return new TransactionLazy(()->delegate.createReadOnlyTransaction(tenantId));
  }

  @Override
  SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    return new TransactionLazy(()->delegate.createTransaction(explicit, isolationLevel));
  }
}
