package com.avaje.ebean;

/**
 * Administrative control of Autofetch during runtime.
 */
public interface AdminAutofetch {

  /**
   * Fire a garbage collection (hint to the JVM). Assuming garbage collection
   * fires this will gather the usage profiling information.
   */
  void collectUsageViaGC();

  /**
   * This will take the current profiling information and update the "tuned
   * query detail".
   * <p>
   * This is done periodically and can also be manually invoked.
   * </p>
   */
  void updateTunedQueryInfo();

}
