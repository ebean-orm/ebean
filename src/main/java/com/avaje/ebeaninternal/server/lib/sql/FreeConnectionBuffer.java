package com.avaje.ebeaninternal.server.lib.sql;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.lib.sql.PooledConnectionStatistics.LoadValues;

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

    /**
     * The buffer itself.
     */
    private PooledConnection[] conns;

    /**
     * The position in the buffer where the next connection is removed from.
     */
    private int removeIndex;

    /**
     * Position in the buffer where the next connection is added to.
     */
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
        if (conns[addIndex] != null) {
          throw new IllegalStateException("Buffer slot ["+addIndex+"] already full?");
        }
        conns[addIndex] = pc;
        addIndex = inc(addIndex);
        ++size;
    }

    /**
     * Close all connections in this buffer.
     */
    protected void closeAll(boolean logErrors) {
      
      final PooledConnection[] items = this.conns;
      
      this.conns = new PooledConnection[items.length];
      this.size = 0;
      this.removeIndex = 0;
      this.addIndex = 0;

      for (int i = 0; i < items.length; i++) {
        PooledConnection c = items[i];
        if (c != null) {
          c.closeConnectionFully(logErrors);
        }
      }
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
     * Trim any inactive connections that have not been used since usedSince.
     */
    protected int trim(long usedSince) {
      
      int trimCount = 0;
      for (int i = 0; i < conns.length; i++) {
        if (conns[i] != null){
          if (conns[i].getLastUsedTime() < usedSince) {
            trimCount++;
            conns[i].closeConnectionFully(true);
            conns[i] = null;
            --size;
          }
        }
      }      
      
      return trimCount;
    }
        
    /**
     * Collect the load statistics from all the free connections.
     */
    protected void collectStatistics(LoadValues values, boolean reset) {    
        
        for (int i = 0; i < conns.length; i++) {
            if (conns[i] != null){
              values.plus(conns[i].getStatistics().getValues(reset));
            }
        }
    }
    
    /**
     * Return a shallow copy of the free connections.
     */
    private List<PooledConnection> getShallowCopy() {
    
        List<PooledConnection> copy = new ArrayList<PooledConnection>(conns.length);
        for (int i = 0; i < conns.length; i++) {
            if (conns[i] != null){
                copy.add(conns[i]);
            }
        }
        return copy;
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
