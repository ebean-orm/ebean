package com.avaje.ebean.event;

import java.util.Set;

import com.avaje.ebean.Ebean;

/**
 * Listen for bulk table events that occur.
 * <p>
 * These events can be triggered via
 * {@link Ebean#externalModification(String, boolean, boolean, boolean)} or
 * automatically determined from Ebean bulk update statements.
 * </p>
 * 
 * @author Robin Bygrave
 * 
 */
public interface BulkTableEventListener {

  /**
   * Return the tables that this listener is interested in.
   */
  Set<String> registeredTables();

  /**
   * Process the event.
   */
  void process(BulkTableEvent bulkTableEvent);

}
