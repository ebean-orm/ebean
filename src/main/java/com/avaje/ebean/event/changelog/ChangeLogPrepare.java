package com.avaje.ebean.event.changelog;

/**
 * Listen for changes.
 * <p>
 * Implementations can take the changes and store them in a document store for auditing purposes etc.
 * </p>
 */
public interface ChangeLogPrepare {

  /**
   * In the foreground prepare the changeLog for sending.
   * <p>
   * This is intended to set extra context information onto the ChangeSet such
   * as the application user id and client ip address.
   * </p>
   * <p>
   * Returning false means the changeLog is not sent to the log() method in a background thread
   * and implies that the changeSet should be ignored or that is has been handled in this prepare()
   * method call.
   * </p>
   *
   * @return true if the changeLog should then be sent to the log method in a background thread.
   */
  boolean prepare(ChangeSet changeSet);

}
