package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteTransactionEvent implements Runnable {

  private final List<BeanPersistIds> beanPersistList = new ArrayList<>();

  private List<TableIUD> tableList;

  private DeleteByIdMap deleteByIdMap;

  private String serverName;

  private transient SpiEbeanServer server;

  public RemoteTransactionEvent(String serverName) {
    this.serverName = serverName;
  }

  public RemoteTransactionEvent(SpiEbeanServer server) {
    this.server = server;
  }

  public void run() {
    server.remoteTransactionEvent(this);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(100);
    if (!beanPersistList.isEmpty()) {
      sb.append(beanPersistList);
    }
    if (tableList != null) {
      sb.append(tableList);
    }
    if (deleteByIdMap != null) {
      sb.append(deleteByIdMap.values());
    }
    return sb.toString();
  }

  public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {

    if (tableList != null) {
      for (TableIUD aTableList : tableList) {
        aTableList.writeBinaryMessage(msgList);
      }
    }

    if (deleteByIdMap != null) {
      for (BeanPersistIds deleteIds : deleteByIdMap.values()) {
        deleteIds.writeBinaryMessage(msgList);
      }
    }

    for (BeanPersistIds aBeanPersistList : beanPersistList) {
      aBeanPersistList.writeBinaryMessage(msgList);
    }
  }

  public boolean isEmpty() {
    return beanPersistList.isEmpty()
        && (tableList == null || tableList.isEmpty())
        && (deleteByIdMap == null || deleteByIdMap.isEmpty());
  }

  public void addBeanPersistIds(BeanPersistIds beanPersist) {
    beanPersistList.add(beanPersist);
  }

  public void addTableIUD(TableIUD tableIud) {
    if (tableList == null) {
      tableList = new ArrayList<>(4);
    }
    tableList.add(tableIud);
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

}
