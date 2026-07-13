package io.ebeaninternal.api;

/**
 * DDL generate and run for drop all/create all.
 */
public interface SpiDdlGenerator {

  /**
   * Generate and run the DDL for drop-all and create-all scripts.
   * <p>
   * Run based on on property settings for ebean.ddl.generate and ebean.ddl.run etc.
   */
  void execute(boolean online);

  /**
   * Run DDL manually. This can be used to initialize multi tenant environments or if you plan not to run
   * DDL on startup
   */
  void runDdl();
}
