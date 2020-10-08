package io.ebeanservice.docstore.api;

import io.ebean.DocStoreQueueEntry;
import io.ebean.DocStoreQueueEntry.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of document store updates that are either sent to the document store
 * or queued for future processing
 */
public class DocStoreUpdates {

  /**
   * Persist inserts and updates.
   */
  private final List<DocStoreUpdate> persistEvents = new ArrayList<>();

  /**
   * Delete by Id.
   */
  private final List<DocStoreUpdate> deleteEvents = new ArrayList<>();

  /**
   * Nested updates.
   */
  private final List<DocStoreQueueEntry> nestedEvents = new ArrayList<>();

  /**
   * Entries sent to the queue for later processing.
   */
  private final List<DocStoreQueueEntry> queueEntries = new ArrayList<>();

  public DocStoreUpdates() {
  }

  /**
   * Return true if there are no events to process.
   */
  public boolean isEmpty() {
    return persistEvents.isEmpty() && deleteEvents.isEmpty() && nestedEvents.isEmpty() && queueEntries.isEmpty();
  }

  /**
   * Add a persist request.
   */
  public void addPersist(DocStoreUpdate bulkRequest) {
    persistEvents.add(bulkRequest);
  }

  /**
   * Add a delete request.
   */
  public void addDelete(DocStoreUpdate bulkRequest) {
    deleteEvents.add(bulkRequest);
  }

  /**
   * Add a nested update.
   */
  public void addNested(String queueId, String path, Object beanId) {
    nestedEvents.add(new DocStoreQueueEntry(Action.NESTED, queueId, path, beanId));
  }

  /**
   * Queue an 'index' request.
   */
  public void queueIndex(String queueId, Object beanId) {
    queueEntries.add(new DocStoreQueueEntry(Action.INDEX, queueId, beanId));
  }

  /**
   * Queue a 'delete' request.
   */
  public void queueDelete(String queueId, Object beanId) {
    queueEntries.add(new DocStoreQueueEntry(Action.DELETE, queueId, beanId));
  }

  /**
   * Queue an update to a nested/embedded object.
   */
  public void queueNested(String queueId, String path, Object beanId) {
    queueEntries.add(new DocStoreQueueEntry(Action.NESTED, queueId, path, beanId));
  }

  /**
   * Return the persist insert and update requests to be sent to the document store.
   */
  public List<DocStoreUpdate> getPersistEvents() {
    return persistEvents;
  }

  /**
   * Return delete events.
   */
  public List<DocStoreUpdate> getDeleteEvents() {
    return deleteEvents;
  }

  /**
   * Return the list of nested update events.
   */
  public List<DocStoreQueueEntry> getNestedEvents() {
    return nestedEvents;
  }

  /**
   * Return the entries for sending to the queue.
   */
  public List<DocStoreQueueEntry> getQueueEntries() {
    return queueEntries;
  }

}
