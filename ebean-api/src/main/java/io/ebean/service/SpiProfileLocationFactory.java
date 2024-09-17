package io.ebean.service;

import io.ebean.ProfileLocation;

/**
 * Factory for creating profile locations.
 */
public interface SpiProfileLocationFactory extends BootstrapService {

  /**
   * Create a profile location.
   */
  ProfileLocation create();

  /**
   * Create a profile location with line numbering.
   */
  ProfileLocation createWithLine();

  /**
   * Create with a given label - used only with {@code @Transaction}.
   *
   * @param label the label for the transaction
   */
  ProfileLocation create(String label);

  /**
   * Create a known location.
   */
  ProfileLocation createAt(String location);
}
