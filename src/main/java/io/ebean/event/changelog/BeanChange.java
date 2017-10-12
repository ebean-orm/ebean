package io.ebean.event.changelog;

/**
 * A bean insert, update or delete change sent as part of a ChangeSet.
 */
public class BeanChange {

  /**
   * The underling base table name.
   */
  private String type;

  /**
   * The tenantId value.
   */
  private Object tenantId;

  /**
   * The id value.
   */
  private Object id;

  /**
   * The INSERT, UPDATE or DELETE change type.
   */
  private ChangeType event;

  /**
   * The time the bean change was created.
   */
  private long eventTime;

  /**
   * The change in JSON form.
   */
  private String data;

  /**
   * The change in JSON form.
   */
  private String oldData;

  /**
   * Constructor for JSON tools.
   */
  public BeanChange() {
  }

  /**
   * Construct with change as JSON.
   */
  public BeanChange(String type, Object tenantId, Object id, ChangeType event, String data, String oldData) {
    this.type = type;
    this.tenantId = tenantId;
    this.id = id;
    this.event = event;
    this.eventTime = System.currentTimeMillis();
    this.data = data;
    this.oldData = oldData;
  }

  /**
   * Construct with change as JSON.
   */
  public BeanChange(String table, Object tenantId, Object id, ChangeType event, String data) {
    this(table, tenantId , id , event , data , null);
  }

  @Override
  public String toString() {
    return "type:" + type + " tenantId: " + tenantId + " id:" + id + " data:" + data;
  }

  /**
   * Return the object type (typically table name).
   */
  public String getType() {
    return type;
  }

  /**
   * Return the tenant id.
   */
  public Object getTenantId() {
    return tenantId;
  }

  /**
   * Return the object id.
   */
  public Object getId() {
    return id;
  }

  /**
   * Return the change type (INSERT, UPDATE or DELETE).
   */
  public ChangeType getEvent() {
    return event;
  }

  /**
   * Return the event time in epoch millis.
   */
  public long getEventTime() {
    return eventTime;
  }

  /**
   * Return the change data in JSON form.
   */
  public String getData() {
    return data;
  }

  /**
   * Return the old data in JSON form.
   */
  public String getOldData() {
    return oldData;
  }
}
