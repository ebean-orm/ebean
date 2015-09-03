package com.avaje.ebean.event.readaudit;

/**
 * Set user context information into the read event prior to it being logged.
 */
public interface ReadAuditPrepare {

  /**
   * Prepare the read event by setting any user context information into the read event such as the
   * application user id and ip address.
   * <p>
   * This method is called prior to the read event being sent to the ReadAuditLogger.
   * </p>
   * <p>
   * Note that for findFutureList() queries prepare() is called early in the foreground thread
   * prior to the query executing and at that point the ReadEvent bean only has the bean type
   * and no other details (which are populated later when the query is executed in the background
   * thread).
   * </p>
   */
  void prepare(ReadEvent readEvent);

}
