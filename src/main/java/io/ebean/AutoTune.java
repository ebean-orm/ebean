package io.ebean;

/**
 * Administrative control of AutoTune during runtime.
 */
public interface AutoTune {

  /**
   * Fire a garbage collection (hint to the JVM). Assuming garbage collection
   * fires this will gather remaining usage profiling information.
   */
  void collectProfiling();

  /**
   * Output the profiling.
   * <p>
   * When profiling updates are applied to tuning at runtime this reports all tuning and profiling combined.
   * When profiling is not applied at runtime then this reports the diff report with new and diff entries relative
   * to the existing tuning.
   * </p>
   */
  void reportProfiling();

}
