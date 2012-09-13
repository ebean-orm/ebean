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
package com.avaje.ebeaninternal.server.lib.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * A buffer designed especially to hold free pooled connections.
 * <p>
 * It is circular in nature.
 * </p>
 * <p>
 * All thread safety controlled externally (by PooledConnectionQueue).
 * </p>
 * 
 * @author rbygrave
 *
 */
class FreeConnectionBuffer {

    private PooledConnection[] conns;

    private int removeIndex;

    private int addIndex;

    /** 
     * The current number of connections in the buffer 
     */
    private int size;
    
    protected FreeConnectionBuffer(int capacity) {
        this.conns = new PooledConnection[capacity];
    }

    protected int getCapacity() {
        return conns.length;
    }
    
    protected int size() {
        return size;
    }
    
    protected boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Add at connection.
     */
    protected void add(PooledConnection pc) {
        conns[addIndex] = pc;
        addIndex = inc(addIndex);
        ++size;
    }

    /**
     * Remove a connection at current remove position.
     */
    protected PooledConnection remove() {
        final PooledConnection[] items = this.conns;
        PooledConnection pc = items[removeIndex];
        items[removeIndex] = null;
        removeIndex = inc(removeIndex);
        --size;
        return pc;
    }
    
    /**
     * Return a shallow copy of the free connections.
     */
    protected List<PooledConnection> getShallowCopy() {
    
        List<PooledConnection> copy = new ArrayList<PooledConnection>(conns.length);
        for (int i = 0; i < conns.length; i++) {
            if (conns[i] != null){
                copy.add(conns[i]);
            }
        }
        return copy;
    }
    
    /**
     * Set the free list to be the connections in this copy. This is done after
     * unused connections have been trimmed.
     * <p>
     * Not a particularly performant approach but this should not be called very
     * often
     * </p>
     */
    protected void setShallowCopy(List<PooledConnection> copy) {
        
        // reset to empty state
        this.removeIndex = 0;
        this.addIndex = 0;
        this.size = 0;

        // null all the current connections
        for (int i = 0; i < conns.length; i++) {
            conns[i] = null;
        }

        // add connections from the copy
        for (int i = 0; i < copy.size(); i++) {
            add(copy.get(i));
        }
    }
    
    /**
     * Increase the capacity of the buffer. This is a relatively expensive
     * operation but should occur very infrequently.
     */
    protected void setCapacity(int newCapacity) {
        if (newCapacity > conns.length){
            
            List<PooledConnection> copy = getShallowCopy();
            
            // reset to empty state
            this.removeIndex = 0;
            this.addIndex = 0;
            this.size = 0;

            this.conns = new PooledConnection[newCapacity];

            // add the connections back from the copy
            for (int i = 0; i < copy.size(); i++) {
                add(copy.get(i));
            }
        }
    }
      
    /**
     * Circularly increment i.
     */
    private final int inc(int i) {
        return (++i == conns.length)? 0 : i;
    }

}
