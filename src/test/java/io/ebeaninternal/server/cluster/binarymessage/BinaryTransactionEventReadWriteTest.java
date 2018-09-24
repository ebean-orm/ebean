package io.ebeaninternal.server.cluster.binarymessage;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebeaninternal.api.TDSpiEbeanServer;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.server.cache.RemoteCacheEvent;
import io.ebeaninternal.server.cluster.BinaryTransactionEventReader;
import io.ebeaninternal.server.cluster.ServerLookup;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.transaction.BeanPersistIds;
import io.ebeaninternal.server.transaction.RemoteTableMod;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BinaryTransactionEventReadWriteTest extends BaseTestCase {

  private BeanDescriptor<Customer> customerBeanDescriptor = getBeanDescriptor(Customer.class);

  private TDEbeanServer mockEbeanServer = new TDEbeanServer();

  private BinaryTransactionEventReader reader = new BinaryTransactionEventReader(new TDServerLookup());

  @Override
  protected <T> BeanDescriptor<T> getBeanDescriptor(Class<T> cls) {
    return super.getBeanDescriptor(cls);
  }

  @Test
  public void readWrite() throws IOException {

    RemoteTransactionEvent event = new RemoteTransactionEvent("db");

    long timestamp = System.currentTimeMillis();
    Set<String> tables = new HashSet<>();
    Collections.addAll(tables, "one", "two", "three");

    event.addRemoteTableMod(new RemoteTableMod(timestamp, tables));
    event.addRemoteCacheEvent(new RemoteCacheEvent(Customer.class));

    event.addTableIUD(new TransactionEventTable.TableIUD("foo", true, false, true));
    event.addTableIUD(new TransactionEventTable.TableIUD("bar", false, true, false));


    BeanPersistIds beanPersistIds = new BeanPersistIds(customerBeanDescriptor);
    beanPersistIds.addId(PersistRequest.Type.INSERT, 42);
    beanPersistIds.addId(PersistRequest.Type.INSERT, 43);
    beanPersistIds.addId(PersistRequest.Type.UPDATE, 55);
    beanPersistIds.addId(PersistRequest.Type.DELETE, 66);
    beanPersistIds.addId(PersistRequest.Type.UPDATE, 92);

    event.addBeanPersistIds(beanPersistIds);

    byte[] binaryMessage = event.writeBinaryAsBytes(256);
    RemoteTransactionEvent read = reader.read(binaryMessage);

    // cache event
    RemoteCacheEvent remoteCacheEvent = read.getRemoteCacheEvent();
    assertThat(remoteCacheEvent.getClearCaches()).containsOnly(Customer.class.getName());

    // table mod
    RemoteTableMod remoteTableMod = read.getRemoteTableMod();
    assertThat(remoteTableMod.getTimestamp()).isEqualTo(timestamp);
    assertThat(remoteTableMod.getTables()).isEqualTo(tables);

    // Bean persist ids
    List<BeanPersistIds> beanPersistList = read.getBeanPersistList();
    assertThat(beanPersistList).hasSize(1);
    assertThat(beanPersistList.get(0).getIds()).containsOnly(55, 66, 92);
  }

  @Test
  public void readWrite_RemoteTableMod() throws IOException {

    RemoteTransactionEvent event = new RemoteTransactionEvent("db");

    long timestamp = System.currentTimeMillis();
    Set<String> tables = new HashSet<>();
    Collections.addAll(tables, "one", "two", "three");

    event.addRemoteTableMod(new RemoteTableMod(timestamp, tables));

    byte[] binaryMessage = event.writeBinaryAsBytes(256);
    RemoteTransactionEvent read = reader.read(binaryMessage);

    RemoteTableMod remoteTableMod = read.getRemoteTableMod();

    assertThat(remoteTableMod.getTimestamp()).isEqualTo(timestamp);
    assertThat(remoteTableMod.getTables()).isEqualTo(tables);
  }

  class TDServerLookup implements ServerLookup {

    @Override
    public EbeanServer getServer(String name) {
      return mockEbeanServer;
    }
  }

  class TDEbeanServer extends TDSpiEbeanServer {
    @Override
    public BeanDescriptor<?> getBeanDescriptorById(String descriptorId) {
      return customerBeanDescriptor;
    }
  }
}
