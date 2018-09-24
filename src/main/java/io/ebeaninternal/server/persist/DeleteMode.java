package io.ebeaninternal.server.persist;

import io.ebeaninternal.server.core.PersistRequest;

/**
 * The delete mode of the persistence request. The mode is determined at the beginning of the request
 * as being a Hard or Soft delete based and then the mode is used for that request and any cascading.
 */
public enum DeleteMode {

  /**
   * Soft/logical delete.
   */
  SOFT(PersistRequest.Type.DELETE_SOFT, false),

  /**
   * Hard/permanent delete.
   */
  HARD(PersistRequest.Type.DELETE_PERMANENT, true);

  private boolean hard;

  private PersistRequest.Type persistType;

  DeleteMode(PersistRequest.Type persistType, boolean hard) {
    this.persistType = persistType;
    this.hard = hard;
  }

  public PersistRequest.Type persistType() {
    return persistType;
  }

  public boolean isHard() {
    return hard;
  }

}
