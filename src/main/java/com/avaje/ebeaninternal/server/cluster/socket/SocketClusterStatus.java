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
