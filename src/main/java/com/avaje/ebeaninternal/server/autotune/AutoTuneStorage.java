package com.avaje.ebeaninternal.server.autotune;

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
