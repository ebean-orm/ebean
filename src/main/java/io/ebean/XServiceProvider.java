package io.ebean;

import io.ebean.plugin.SpiRawSqlService;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Lookup internal services.
 */
class XServiceProvider {

  private static SpiRawSqlService builder = init();

  private static SpiRawSqlService init() {

    Iterator<SpiRawSqlService> loader = ServiceLoader.load(SpiRawSqlService.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for SpiRawSqlService?");
  }

  /**
   * Return the RawSqlService implementation.
   */
  static SpiRawSqlService get() {
    return builder;
  }
}
