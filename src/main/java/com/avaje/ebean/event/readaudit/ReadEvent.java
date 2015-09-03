package com.avaje.ebean.event.readaudit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read event sent to the ReadEventLogger.
 * <p>
 * This is a flattened in that it contains either a read bean or list of beans. It is flattened
 * in this way to simplify logging and processing and simply means that it either contains an
 * id or a list of ids.
 * </p>
 */
public class ReadEvent {

  /**
   * User defined 'source' such as the application name.
   */
  protected String source;

  /**
   * Application user id expected to be optionally populated by ChangeLogPrepare.
   */
  protected String userId;

  /**
   * Application user ip address expected to be optionally populated by ChangeLogPrepare.
   */
  protected String userIpAddress;

  /**
   * Arbitrary user context information expected to be optionally populated by ChangeLogPrepare.
   */
  protected Map<String, String> userContext;

  /**
   * The time the bean change was created.
   */
  protected long eventTime;

  /**
   * The type of the bean(s) read.
   */
  protected String beanType;

  /**
   * The query key (relative to the bean type).
   */
  protected String queryKey;

  /**
   * The bind log when the query was executed.
   */
  protected String bindLog;

  /**
   * The id of the bean read.
   */
  protected Object id;

  /**
   * The ids of the beans read.
   */
  protected List<Object> ids;

  /**
   * Common constructor for single bean and multi-bean read events.
   */
  protected ReadEvent(String beanType, String queryKey, String bindLog) {
    this.beanType = beanType;
    this.queryKey = queryKey;
    this.bindLog = bindLog;
    this.eventTime = System.currentTimeMillis();
  }

  /**
   * Construct for a single bean read.
   */
  public ReadEvent(String beanType, String queryKey, String bindLog, Object id) {
    this(beanType, queryKey, bindLog);
    this.id = id;
  }

  /**
   * Construct for many beans read.
   */
  public ReadEvent(String beanType, String queryKey, String bindLog, List<Object> ids) {
    this(beanType, queryKey, bindLog);
    this.ids = ids;
  }

  /**
   * Construct for many future list query.
   */
  public ReadEvent(String beanType) {
    this.beanType = beanType;
    this.eventTime = System.currentTimeMillis();
  }

  /**
   * Constructor for JSON tools.
   */
  public ReadEvent() {
  }

  /**
   * Return a code that identifies the source of the change (like the name of the application).
   */
  public String getSource() {
    return source;
  }

  /**
   * Set the source of the change (like the name of the application).
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * Return the application user Id.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the application user Id.
   * <p>
   * This can be set by the ChangeLogListener in the prepare() method which is called
   * in the foreground thread.
   * </p>
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Return the application users ip address.
   */
  public String getUserIpAddress() {
    return userIpAddress;
  }

  /**
   * Set the application users ip address.
   * <p>
   * This can be set by the ChangeLogListener in the prepare() method which is called
   * in the foreground thread.
   * </p>
   */
  public void setUserIpAddress(String userIpAddress) {
    this.userIpAddress = userIpAddress;
  }

  /**
   * Return a user context value - anything you set yourself in ChangeLogListener prepare().
   */
  public Map<String, String> getUserContext() {
    if (userContext == null) {
      userContext = new LinkedHashMap<String, String>();
    }
    return userContext;
  }

  /**
   * Set a user context value (anything you like).
   * <p>
   * This can be set by the ChangeLogListener in the prepare() method which is called
   * in the foreground thread.
   * </p>
   */
  public void setUserContext(Map<String, String> userContext) {
    this.userContext = userContext;
  }

  /**
   * Return the type of bean read.
   */
  public String getBeanType() {
    return beanType;
  }

  /**
   * Set the type of bean read.
   */
  public void setBeanType(String beanType) {
    this.beanType = beanType;
  }

  /**
   * Return the query key (relative to the bean type).
   */
  public String getQueryKey() {
    return queryKey;
  }

  /**
   * Set the query key (relative to the bean type).
   */
  public void setQueryKey(String queryKey) {
    this.queryKey = queryKey;
  }

  /**
   * Return the bind log used when executing the query.
   */
  public String getBindLog() {
    return bindLog;
  }

  /**
   * Set the bind log used when executing the query.
   */
  public void setBindLog(String bindLog) {
    this.bindLog = bindLog;
  }

  /**
   * Return the event date time.
   */
  public long getEventTime() {
    return eventTime;
  }

  /**
   * Set the event date time.
   */
  public void setEventTime(long eventTime) {
    this.eventTime = eventTime;
  }

  /**
   * Return the id of the bean read.
   */
  public Object getId() {
    return id;
  }

  /**
   * Set the id of the bean read.
   */
  public void setId(Object id) {
    this.id = id;
  }

  /**
   * Return the ids of the beans read.
   */
  public List<Object> getIds() {
    return ids;
  }

  /**
   * Set the ids of the beans read.
   */
  public void setIds(List<Object> ids) {
    this.ids = ids;
  }
}
