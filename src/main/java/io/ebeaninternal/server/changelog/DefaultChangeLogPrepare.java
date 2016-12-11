package io.ebeaninternal.server.changelog;

import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeSet;

/**
 * Placeholder/default implementation that does not do anything.
 * <p>
 * Generally an implementation should be provided that reads context
 * information such as user id and user ip address etc and sets that
 * on the changeSet.
 * </p>
 */
public class DefaultChangeLogPrepare implements ChangeLogPrepare {

  /**
   * Just return true to send change set through to the logger.
   */
  @Override
  public boolean prepare(ChangeSet changeSet) {
    return true;
  }
}
