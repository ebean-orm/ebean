/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public class RemoteTransactionEvent implements Runnable {

    private List<BeanPersistIds> beanPersistList = new ArrayList<BeanPersistIds>();
    
    private List<TableIUD> tableList;

    private List<BeanDeltaList> beanDeltaLists;
    
    private BeanDeltaMap beanDeltaMap;

    private List<IndexEvent> indexEventList;
    
    private Set<IndexInvalidate> indexInvalidations;
    
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
        
        if (indexInvalidations != null){
            for (IndexInvalidate indexInvalidate : indexInvalidations) {
                indexInvalidate.writeBinaryMessage(msgList);
            }
        }
        
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
        
        if (indexEventList != null){
            for (int i = 0; i < indexEventList.size(); i++) {
                indexEventList.get(i).writeBinaryMessage(msgList);
            }
        }
    }
    
    public boolean isEmpty() {
        return beanPersistList.isEmpty() && (tableList == null || tableList.isEmpty());
    }
    
    public void addBeanPersistIds(BeanPersistIds beanPersist){
        beanPersistList.add(beanPersist);
    }

    public void addIndexInvalidate(IndexInvalidate indexInvalidate){
        if (indexInvalidations == null){
            indexInvalidations = new HashSet<IndexInvalidate>();
        }
        indexInvalidations.add(indexInvalidate);
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
    
    public void addIndexEvent(IndexEvent indexEvent){
        if (indexEventList == null){
            indexEventList = new ArrayList<IndexEvent>(2);
        }
        indexEventList.add(indexEvent);
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

    public Set<IndexInvalidate> getIndexInvalidations() {
        return indexInvalidations;
    }

    public List<IndexEvent> getIndexEventList() {
        return indexEventList;
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
