package com.avaje.ebeaninternal.server.cluster;

import java.io.Serializable;

/**
 * Simple holder of binary data.
 * Used to use Packet based serialisation of RemoteTransactionEvent
 * with simple Java Serialisation of the DataHolder.
 */
public class DataHolder implements Serializable {

    private static final long serialVersionUID = 9090748723571322192L;

    private final byte[] data;
    
    public DataHolder(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
    
}
