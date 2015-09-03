package com.avaje.ebeaninternal.server.readaudit;

import com.avaje.ebean.event.readaudit.ReadAuditPrepare;
import com.avaje.ebean.event.readaudit.ReadEvent;

/**
 * A placeholder implementation for ReadAuditPrepare.
 * <p>
 * A real application specific implementation is required to obtain and set
 * the user context information on the readEvent bean (like user id).
 * </p>
 */
public class DefaultReadAuditPrepare implements ReadAuditPrepare {

  @Override
  public void prepare(ReadEvent readEvent) {
    // do nothing by default.
  }
}
