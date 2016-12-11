package io.ebean.event;

import io.ebean.Ebean;

import java.util.Set;

/**
 * Listen for bulk table events that occur.
 * <p>
 * These events can be triggered via
 * {@link Ebean#externalModification(String, boolean, boolean, boolean)} or
 * automatically determined from Ebean bulk update statements.
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
