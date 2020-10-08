package io.ebeaninternal.server.readaudit;

import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.event.readaudit.ReadEvent;

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
