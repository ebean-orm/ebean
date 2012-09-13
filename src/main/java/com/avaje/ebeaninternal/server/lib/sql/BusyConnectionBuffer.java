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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A buffer especially designed for Busy PooledConnections.
 * <p>
 * All thread safety controlled externally (by PooledConnectionQueue).
 * </p>
 * <p>
 * It has a set of 'slots' and PooledConnections know which slot they went into
 * and this allows for fast addition and removal (by slotId without looping).
 * The capacity will increase on demand by the 'growBy' amount.
 * </p>
 * 
 * @author rbygrave
 * 
 */
class BusyConnectionBuffer {

    private PooledConnection[] slots;
    
    private int growBy;
    
    private int size;
    
    private int pos = -1;
    
    /**
     * Create the buffer with an initial capacity and fixed growBy.
     * We generally do not want the buffer to grow very often.
     * 
     * @param capacity
     *            the initial capacity
     * @param growBy
     *            the fixed amount to grow the buffer by.
     */
    protected BusyConnectionBuffer(int capacity, int growBy) {
        this.slots = new PooledConnection[capacity];
        this.growBy = growBy;
    }
    
    /**
     * We can only grow (not shrink) the capacity.
     */
    private void setCapacity(int newCapacity) {
        if (newCapacity > slots.length){
            PooledConnection[] current = this.slots;
            this.slots = new PooledConnection[newCapacity];
            System.arraycopy(current, 0, this.slots, 0, current.length);
        }
    }
    
    public String toString() {
        return Arrays.toString(slots);
    }
    
    protected int getCapacity() {
        return slots.length;
    }
    
    protected int size(){
        return size;
    }
    
    protected boolean isEmpty(){
        return size == 0;
    }
    
    protected int add(PooledConnection pc){
        if (size == slots.length){
            // grow the capacity
            setCapacity(slots.length + growBy);
        }
        ++size;
        int slot = nextEmptySlot();
        pc.setSlotId(slot);
        slots[slot] = pc;
        return size;
    }
    
    protected boolean remove(PooledConnection pc) {
        --size;
        int slotId = pc.getSlotId();
        if (slots[slotId] != pc){
            return false;
        }
        slots[slotId] = null;
        return true;
    }
    
    
    /**
     * Get a shallow read only List of the busy connections.
     * <p>
     * Note that the {@link #remove(PooledConnection)} MUST be used to remove PooledConnections.
     * </p>
     * @return
     */
    protected List<PooledConnection> getShallowCopy() {
        ArrayList<PooledConnection> tmp = new ArrayList<PooledConnection>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null){
                tmp.add(slots[i]);
            }
        }
        return Collections.unmodifiableList(tmp);
    }
    
    /**
     * Return the position of the next empty slot.
     */
    private int nextEmptySlot() {

        // search forward 
        while(++pos < slots.length) {
            if (slots[pos] == null){
                return pos;
            } 
        }
        // search from beginning 
        pos = -1;
        while(++pos < slots.length) {
            if (slots[pos] == null){
                return pos;
            }
        }
        
        // not expecting this 
        throw new RuntimeException("No Empty Slot Found?");
    }
    
}
