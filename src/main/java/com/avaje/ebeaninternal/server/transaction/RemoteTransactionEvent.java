package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteTransactionEvent implements Runnable {

    private List<BeanPersistIds> beanPersistList = new ArrayList<BeanPersistIds>();
    
    private List<TableIUD> tableList;

    private List<BeanDeltaList> beanDeltaLists;
    
    private BeanDeltaMap beanDeltaMap;
    
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
        StringBuilder sb = new StringBuilder();
        if (beanDeltaMap != null){
            sb.append(beanDeltaMap);
        }
        sb.append(beanPersistList);
        if (tableList != null){
            sb.append(tableList);
        }
        return sb.toString();
    }
    
    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        
        if (tableList != null){
            for (int i = 0; i < tableList.size(); i++) {
                tableList.get(i).writeBinaryMessage(msgList);
            }
        }
        
        if (deleteByIdMap != null){
            for (BeanPersistIds deleteIds : deleteByIdMap.values()) {
                deleteIds.writeBinaryMessage(msgList);
            }
        }
        
        if (beanPersistList != null){
            for (int i = 0; i < beanPersistList.size(); i++) {
                beanPersistList.get(i).writeBinaryMessage(msgList);
            }
        }
        
        if (beanDeltaLists != null){
            for (int i = 0; i < beanDeltaLists.size(); i++) {
                beanDeltaLists.get(i).writeBinaryMessage(msgList);
            }
        }
    }
    
    public boolean isEmpty() {
        return beanPersistList.isEmpty() && (tableList == null || tableList.isEmpty());
    }
    
    public void addBeanPersistIds(BeanPersistIds beanPersist){
        beanPersistList.add(beanPersist);
    }

    public void addTableIUD(TableIUD tableIud){
        if (tableList == null){
            tableList = new ArrayList<TableIUD>(4);
        }
        tableList.add(tableIud);
    }
    
    public void addBeanDeltaList(BeanDeltaList deltaList){
        if (beanDeltaLists == null){
            beanDeltaLists = new ArrayList<BeanDeltaList>();
        }
        beanDeltaLists.add(deltaList);
    }
        
    public void addBeanDelta(BeanDelta beanDelta){
        if (beanDeltaMap == null){
            beanDeltaMap = new BeanDeltaMap();
        }
        beanDeltaMap.addBeanDelta(beanDelta);
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
    
    public DeleteByIdMap getDeleteByIdMap() {
        return deleteByIdMap;
    }

    public void setDeleteByIdMap(DeleteByIdMap deleteByIdMap) {
        this.deleteByIdMap = deleteByIdMap;
    }

    public List<TableIUD> getTableIUDList() {
        return tableList;
    }

    public List<BeanPersistIds> getBeanPersistList() {
        return beanPersistList;
    }

    public List<BeanDeltaList> getBeanDeltaLists() {
        if (beanDeltaMap != null){
            beanDeltaLists.addAll(beanDeltaMap.deltaLists());
        }
        return beanDeltaLists;
    }
}
