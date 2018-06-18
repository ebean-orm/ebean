package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.BinaryReadContext;
import io.ebeaninternal.api.BinaryWritable;
import io.ebeaninternal.api.BinaryWriteContext;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.server.cache.RemoteCacheEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteTransactionEvent implements Runnable, BinaryWritable {

  private final List<BeanPersistIds> beanPersistList = new ArrayList<>();

  private List<TableIUD> tableList;

  private DeleteByIdMap deleteByIdMap;

  private RemoteCacheEvent remoteCacheEvent;

  private RemoteTableMod remoteTableMod;

  private String serverName;

  private transient SpiEbeanServer server;

  /**
   * Create for sending to other servers in the cluster.
   */
  public RemoteTransactionEvent(String serverName) {
    this.serverName = serverName;
  }

  /**
   * Create from Reading and processing from remote server.
   */
  public RemoteTransactionEvent(SpiEbeanServer server) {
    this.server = server;
  }

  @Override
  public void run() {
    server.remoteTransactionEvent(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(100);
    sb.append("TransEvent[");
    if (remoteTableMod != null) {
      sb.append(remoteTableMod);
    }
    if (!beanPersistList.isEmpty()) {
      sb.append(beanPersistList);
    }
    if (tableList != null) {
      sb.append(tableList);
    }
    if (deleteByIdMap != null) {
      sb.append(deleteByIdMap);
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Read the binary message.
   */
  public void readBinary(BinaryReadContext dataInput) throws IOException {

    boolean more = dataInput.readBoolean();
    while (more) {
      int msgType = dataInput.readInt();
      readBinaryMessage(msgType, dataInput);
      more = dataInput.readBoolean();
    }
  }

  private void readBinaryMessage(int msgType, BinaryReadContext dataInput) throws IOException {

    switch (msgType) {
      case BinaryWritable.TYPE_BEANIUD:
        addBeanPersistIds(BeanPersistIds.readBinaryMessage(server, dataInput));
        break;

      case BinaryWritable.TYPE_TABLEIUD:
        addTableIUD(TransactionEventTable.TableIUD.readBinaryMessage(dataInput));
        break;

      case BinaryWritable.TYPE_CACHE:
        addRemoteCacheEvent(RemoteCacheEvent.readBinaryMessage(dataInput));
        break;

      case BinaryWritable.TYPE_TABLEMOD:
        addRemoteTableMod(RemoteTableMod.readBinaryMessage(dataInput));
        break;

      default:
        throw new RuntimeException("Invalid Transaction msgType " + msgType);
    }
  }

  /**
   * Write a binary message to byte array given an initial buffer size.
   */
  public byte[] writeBinaryAsBytes(int bufferSize) throws IOException {

    ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
    DataOutputStream out = new DataOutputStream(buffer);
    BinaryWriteContext context = new BinaryWriteContext(out);

    writeBinary(context);
    out.close();

    return buffer.toByteArray();
  }

  @Override
  public void writeBinary(BinaryWriteContext out) throws IOException {

    DataOutputStream os = out.os();
    os.writeUTF(serverName);
    if (remoteTableMod != null) {
      remoteTableMod.writeBinary(out);
    }
    if (tableList != null) {
      for (TableIUD aTableList : tableList) {
        aTableList.writeBinary(out);
      }
    }
    if (deleteByIdMap != null) {
      for (BeanPersistIds deleteIds : deleteByIdMap.values()) {
        deleteIds.writeBinary(out);
      }
    }
    for (BeanPersistIds aBeanPersistList : beanPersistList) {
      aBeanPersistList.writeBinary(out);
    }
    if (remoteCacheEvent != null) {
      remoteCacheEvent.writeBinary(out);
    }
    out.end();
    os.flush();
  }

  public boolean isEmpty() {
    return beanPersistList.isEmpty()
      && (tableList == null || tableList.isEmpty())
      && (deleteByIdMap == null || deleteByIdMap.isEmpty());
  }

  public void addBeanPersistIds(BeanPersistIds beanPersist) {
    beanPersistList.add(beanPersist);
  }

  /**
   * Add a cache clearAll event.
   */
  public RemoteTransactionEvent cacheClearAll() {
    this.remoteCacheEvent = new RemoteCacheEvent(true);
    return this;
  }

  /**
   * Add a cache clear event for the given bean type.
   */
  public RemoteTransactionEvent cacheClear(Class<?> beanType) {
    this.remoteCacheEvent = new RemoteCacheEvent(beanType);
    return this;
  }

  /**
   * Set the RemoteCacheEvent.
   */
  public void addRemoteCacheEvent(RemoteCacheEvent remoteCacheEvent) {
    this.remoteCacheEvent = remoteCacheEvent;
  }

  public void addTableIUD(TableIUD tableIud) {
    if (tableList == null) {
      tableList = new ArrayList<>(4);
    }
    tableList.add(tableIud);
  }

  public void addRemoteTableMod(RemoteTableMod remoteTableMod) {
    this.remoteTableMod = remoteTableMod;
  }

  public String getServerName() {
    return serverName;
  }

  public SpiEbeanServer getServer() {
    return server;
  }

  public void setServer(SpiEbeanServer server) {
    this.server = server;
  }

  void setDeleteByIdMap(DeleteByIdMap deleteByIdMap) {
    this.deleteByIdMap = deleteByIdMap;
  }

  public List<TableIUD> getTableIUDList() {
    return tableList;
  }

  public List<BeanPersistIds> getBeanPersistList() {
    return beanPersistList;
  }

  public RemoteCacheEvent getRemoteCacheEvent() {
    return remoteCacheEvent;
  }

  public RemoteTableMod getRemoteTableMod() {
    return remoteTableMod;
  }

}
