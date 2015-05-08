package com.avaje.ebeaninternal.server.lib.sql;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebeaninternal.server.lib.sql.PooledConnectionStatistics.LoadValues;

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

    private static final Logger logger = LoggerFactory.getLogger(BusyConnectionBuffer.class);
  
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
    protected void setCapacity(int newCapacity) {
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
    
    protected boolean isEmpty() {
      return size == 0;
    }
    
    protected int add(PooledConnection pc){
        if (size == slots.length){
            // grow the capacity
            setCapacity(slots.length + growBy);
        }
        int slot = nextEmptySlot();
        pc.setSlotId(slot);
        slots[slot] = pc;
        return ++size;
    }
    
    protected boolean remove(PooledConnection pc) {
        
        int slotId = pc.getSlotId();
        if (slots[slotId] != pc){
            PooledConnection heldBy = slots[slotId];
            logger.warn("Failed to remove from slot[{}] PooledConnection[{}] - HeldBy[{}]", pc.getSlotId(), pc, heldBy);
            return false;
        }
        slots[slotId] = null;
        --size;
        return true;
    }
    
    /**
     * Collect the load statistics from all the busy connections.
     * @param reset 
     */
    protected void collectStatistics(LoadValues values, boolean reset) {    
        
      for (int i = 0; i < slots.length; i++) {
        if (slots[i] != null){
          values.plus(slots[i].getStatistics().getValues(reset));
        }
      }
    }
    
    /**
     * Close connections that should be considered leaked.
     */
    protected void closeBusyConnections(long leakTimeMinutes) {

      long olderThanTime = System.currentTimeMillis() - (leakTimeMinutes*60000);

      logger.debug("Closing busy connections using leakTimeMinutes {}", leakTimeMinutes);

      for (int i = 0; i < slots.length; i++) {
        if (slots[i] != null){
            //tmp.add(slots[i]);
            PooledConnection pc = slots[i];
            if (pc.isLongRunning() || pc.getLastUsedTime() > olderThanTime) {
              // PooledConnection has been used recently or
              // expected to be longRunning so not closing...
            } else {
              slots[i] = null;
              --size; 
              closeBusyConnection(pc);
            }
        }
      }
    }
    
    private void closeBusyConnection(PooledConnection pc) {
      try {
          
          logger.warn("DataSourcePool closing busy connection? "+pc.getFullDescription());
          System.out.println("CLOSING busy connection: "+pc.getFullDescription());
          
          pc.closeConnectionFully(false);

      } catch (Exception ex) {
          // this should never actually happen
          logger.error("Error when closing potentially leaked connection "+pc.getDescription(), ex);
      }
  }
    
    /**
     * Returns information describing connections that are currently being used.
     */
    protected String getBusyConnectionInformation(boolean toLogger) {
               
          if (toLogger) {
            logger.info("Dumping [{}] busy connections: (Use datasource.xxx.capturestacktrace=true  ... to get stackTraces)", size());
          }

          StringBuilder sb = new StringBuilder();

          for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null){
                PooledConnection pc = slots[i];
                if (toLogger) {
                    logger.info("Busy Connection - {}", pc.getFullDescription());
                } else {
                    sb.append(pc.getFullDescription()).append("\r\n");
                }
            }
          }
          
          return sb.toString();
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
