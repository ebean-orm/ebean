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
   * Create a profile location with a line number.
   */
  ProfileLocation create(int lineNumber, String label);

  /**
   * Create a known location.
   */
  ProfileLocation createAt(String location);
}
