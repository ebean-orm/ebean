package com.avaje.ebean;

/**
 * Administrative control of AutoTune during runtime.
 */
public interface AutoTune {

  /**
   * Fire a garbage collection (hint to the JVM). Assuming garbage collection
   * fires this will gather remaining usage profiling information.
   */
  void collectProfiling();

}
