package io.ebeaninternal.server.transaction;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCacheNotify;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiProfileHandler;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionException;

import static org.mockito.Mockito.*;

class TransactionManagerNotifyOfCommitTest {

  @Test
  void rejectedSubmission_notifiesListenerSynchronously() {
    var executor = mock(BackgroundExecutor.class);
    doThrow(new RejectedExecutionException()).when(executor).execute(any(Runnable.class));
    var config = new DatabaseConfig();
    config.setDatabasePlatform(new DatabasePlatform());
    var manager = new TransactionManager(new TransactionManagerOptions(mock(SpiServer.class), true, config,
      mock(TransactionScopeManager.class), mock(ClusterManager.class), executor,
      mock(DocStoreUpdateProcessor.class), mock(BeanDescriptorManager.class), mock(DataSourceSupplier.class),
      mock(SpiProfileHandler.class), mock(SpiLogManager.class), mock(TableModState.class), mock(ServerCacheNotify.class)));
    var listener = mock(PersistRequestBean.class);
    var event = new TransactionEvent();
    event.addListenerNotify(listener);
    var transaction = mock(SpiTransaction.class);
    when(transaction.event()).thenReturn(event);

    manager.notifyOfCommit(transaction);

    verify(listener).notifyLocalPersistListener();
  }
}
