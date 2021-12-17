package io.ebeaninternal.api;

/**
 * DDL generate and run for drop all/create all.
 */
public interface SpiDdlGenerator {

  /**
   * Generate and run the DDL for drop-all and create-all scripts.
   * <p>
   * Run based on on property settings for ebean.ddl.generate
   */
  void generateDdl();

  /**
   * Runs the created DDL files.
   * <p>
   * Run based on on property settings for ebean.ddl.run etc.
   */
  void runDdl();
}
