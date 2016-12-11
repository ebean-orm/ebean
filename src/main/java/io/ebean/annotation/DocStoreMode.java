package io.ebean.annotation;

/**
 * Defines the behavior options when a Insert, Update or Delete event occurs
 * on a bean with an associated ElasticSearch index.
 * <p>
 * For some indexes or some transactions it can be beneficial to queue the event
 * for later processing rather than look to update ElasticSearch at that time.
 * </p>
 */
public enum DocStoreMode {

  /**
   * Add the event to the queue for processing later (delaying the update to the document store).
   */
  QUEUE,

  /**
   * Update the document store when transaction succeeds.
   */
  UPDATE,

  /**
   * Ignore the event and not update the document store.
   * <p>
   * This can be used on a index or for a transaction where you want to have more
   * manual programmatic control over the updating of the document store.  Say you want to
   * IGNORE on a particular transaction and instead manually queue a bulk update.
   * </p>
   */
  IGNORE,

  /**
   * The actual mode of QUEUE, UPDATE or IGNORE is set from the default configuration.
   */
  DEFAULT

}
