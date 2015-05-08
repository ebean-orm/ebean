package com.avaje.ebeaninternal.server.lib.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebeaninternal.server.lib.sql.PooledConnectionStatistics.LoadValues;

/**
 * A buffer designed especially to hold free pooled connections.
 * <p>
 * All thread safety controlled externally (by PooledConnectionQueue).
 * </p>
 */
class FreeConnectionBuffer {

  private static final Logger logger = LoggerFactory.getLogger(FreeConnectionBuffer.class);
  
  /**
   * Buffer oriented for add and remove.
   */
  private final LinkedList<PooledConnection> freeBuffer = new LinkedList<PooledConnection>();

  protected FreeConnectionBuffer() {
  }

  protected int size() {
    return freeBuffer.size();
  }

  protected boolean isEmpty() {
    return freeBuffer.isEmpty();
  }

  /**
   * Add connection to the free list.
   */
  protected void add(PooledConnection pc) {
    freeBuffer.addLast(pc);
  }

  /**
   * Remove a connection from the free list.
   */
  protected PooledConnection remove() {
    return freeBuffer.removeFirst();
  }

  /**
   * Close all connections in this buffer.
   */
  protected void closeAll(boolean logErrors) {

    // create a temporary list
    List<PooledConnection> tempList = new ArrayList<PooledConnection>(freeBuffer.size());
    
    // add all the connections into it
    for (PooledConnection c : freeBuffer) {
      tempList.add(c);
    }

    // clear the buffer (in case it takes some time to close these connections).
    freeBuffer.clear();
    
    logger.debug("... closing all {} connections from the free list with logErrors: {}", tempList.size(), logErrors);
    for (int i = 0; i < tempList.size(); i++) {
      PooledConnection pooledConnection = tempList.get(i);
      logger.debug("... closing {} of {} connections from the free list", i, tempList.size());      
      pooledConnection.closeConnectionFully(logErrors);
    }
  }
  
  /**
   * Trim any inactive connections that have not been used since usedSince.
   */
  protected int trim(long usedSince, long createdSince) {

    int trimCount = 0;

    Iterator<PooledConnection> iterator = freeBuffer.iterator();
    while (iterator.hasNext()) {
      PooledConnection pooledConnection = iterator.next();
      if (pooledConnection.shouldTrim(usedSince, createdSince)) {
        iterator.remove();
        pooledConnection.closeConnectionFully(true);
        trimCount++;
      }
    }

    return trimCount;
  }

  /**
   * Collect the load statistics from all the free connections.
   */
  protected void collectStatistics(LoadValues values, boolean reset) {

    for (PooledConnection c : freeBuffer) {
      values.plus(c.getStatistics().getValues(reset));
    }
  }
}
