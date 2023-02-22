package io.ebean.service;

import io.ebean.ProfileLocation;

/**
 * Factory for creating profile locations.
 */
public interface SpiProfileLocationFactory {

  /**
   * Create a profile location.
   */
  ProfileLocation create();

  /**
   * Create with a given label - used only with {@code @Transaction}.
   *
   * @param label the label for the transaction
   */
  ProfileLocation create(String label);

  /**
   * Create a profile location with a line number.
   *
   * @param lineNumber always 0
   * @param label      the label for the transaction
   */
  default ProfileLocation create(int lineNumber, String label) {
    return create(label);
  }

  /**
   * Create a known location.
   */
  ProfileLocation createAt(String location);
}
