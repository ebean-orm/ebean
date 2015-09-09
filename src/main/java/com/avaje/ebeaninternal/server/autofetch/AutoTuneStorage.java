package com.avaje.ebeaninternal.server.autofetch;

/**
 *
 */
public interface AutoTuneStorage {

  /**
   * Load and return the tuning information.
   */
  AutoTuneCollection load();

  /**
   * Store the collected profiling information.
   */
  void store(AutoTuneCollection profiling);
}
