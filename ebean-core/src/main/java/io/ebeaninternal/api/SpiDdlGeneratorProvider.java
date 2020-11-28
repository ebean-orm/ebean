package io.ebeaninternal.api;

/**
 * Provides the DDL Generator for create-all/drop-all.
 */
public interface SpiDdlGeneratorProvider {

  /**
   * Provide the DDL generator.
   */
  SpiDdlGenerator generator(SpiEbeanServer server);

}
