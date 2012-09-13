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
package com.avaje.ebeaninternal.server.cluster.socket;

/**
 * The current state of this cluster member. 
 * 
 * @author rbygrave
 */
public class SocketClusterStatus {

    private final int currentGroupSize;
    private final int txnIncoming;
    private final int txtOutgoing;
    
    public SocketClusterStatus(int currentGroupSize, int txnIncoming, int txnOutgoing) {
        this.currentGroupSize = currentGroupSize;
        this.txnIncoming = txnIncoming;
        this.txtOutgoing = txnOutgoing;
    }
    
    /**
     * Return the number of members of the cluster currently online.
     */
    public int getCurrentGroupSize() {
        return currentGroupSize;
    }

    /**
     * Return the number of Remote transactions received.
     */
    public int getTxnIncoming() {
        return txnIncoming;
    }

    /**
     * Return the number of transactions sent to the cluster.
     */
    public int getTxtOutgoing() {
        return txtOutgoing;
    }
    
}
