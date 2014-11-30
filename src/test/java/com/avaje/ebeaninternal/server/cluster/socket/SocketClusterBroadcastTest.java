package com.avaje.ebeaninternal.server.cluster.socket;

import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebeaninternal.api.TDSpiEbeanServer;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SocketClusterBroadcastTest {

  class TestServer extends TDSpiEbeanServer {

    RemoteTransactionEvent event;

    TestServer(String name) {
      super(name);
    }

    @Override
    public void remoteTransactionEvent(RemoteTransactionEvent event) {
      this.event = event;
    }
  }

  private ContainerConfig createContainerConfig(String local, String threadPoolName) {
    ContainerConfig container0 = new ContainerConfig();
    container0.setMode(ContainerConfig.ClusterMode.SOCKET);

    ContainerConfig.SocketConfig socketConfig = new ContainerConfig.SocketConfig();
    socketConfig.setLocalHostPort(local);
    socketConfig.setThreadPoolName(threadPoolName);
    socketConfig.setMembers(Arrays.asList("127.0.0.1:9876", "127.0.0.1:9866"));

    container0.setSocketConfig(socketConfig);
    return container0;
  }



  @Test
  public void testStartup() throws Exception {

    ContainerConfig container0 = createContainerConfig("127.0.0.1:9876", "pool0");
    ClusterManager mgr0 = new ClusterManager(container0);

    TestServer server0 = new TestServer("s001");
    mgr0.registerServer(server0);

    ContainerConfig container1 = createContainerConfig("127.0.0.1:9866", "pool1");
    ClusterManager mgr1 = new ClusterManager(container1);

    TestServer server1 = new TestServer("s001");
    mgr1.registerServer(server1);

    Thread.sleep(1000);

    RemoteTransactionEvent evt = new RemoteTransactionEvent("s001");
    TransactionEventTable.TableIUD tableIUD = new TransactionEventTable.TableIUD("noSuchTable", true, false, false);
    evt.addTableIUD(tableIUD);

    assertNull(server1.event);

    mgr0.broadcast(evt);
    Thread.sleep(100);

    assertNotNull(server1.event);

    Thread.sleep(1000);
    mgr0.shutdown();
    mgr1.shutdown();
  }

}