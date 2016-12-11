package io.ebean;

/**
 * Bean holding the details to update the document store.
 */
public final class DocStoreQueueEntry {

  /**
   * Action to either update or delete a document from the index.
   */
  public enum Action {

    /**
     * Action is to update a document in the doc store.
     */
    INDEX(1),

    /**
     * Action is to delete a document from the doc store..
     */
    DELETE(2),

    /**
     * An update is required based on a change to a nested/embedded object at a given path.
     */
    NESTED(3);

    int value;

    Action(int value) {
      this.value = value;
    }

    /**
     * Return the value associated with this action type.
     */
    public int getValue() {
      return value;
    }
  }

  private final Action type;

  private final String queueId;

  private final String path;

  private final Object beanId;

  /**
   * Construct for an INDEX or DELETE action.
   */
  public DocStoreQueueEntry(Action type, String queueId, Object beanId) {
    this(type, queueId, null, beanId);
  }

  /**
   * Construct for an NESTED/embedded path invalidation action.
   */
  public DocStoreQueueEntry(Action type, String queueId, String path, Object beanId) {
    this.type = type;
    this.queueId = queueId;
    this.path = path;
    this.beanId = beanId;
  }

  /**
   * Return the event type.
   */
  public Action getType() {
    return type;
  }

  /**
   * Return the associate queueId.
   */
  public String getQueueId() {
    return queueId;
  }

  /**
   * Return the path if this is a nested update.
   */
  public String getPath() {
    return path;
  }

  /**
   * Return the bean id (which matches the document id).
   */
  public Object getBeanId() {
    return beanId;
  }
}
