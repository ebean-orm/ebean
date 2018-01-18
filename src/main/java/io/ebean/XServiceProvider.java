package io.ebean;

import io.ebean.service.SpiProfileLocationFactory;
import io.ebean.service.SpiRawSqlService;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Lookup internal services.
 */
class XServiceProvider {

  private static SpiRawSqlService rawSqlService = initRawSql();

  private static SpiProfileLocationFactory profileLocationFactory = initProfileLocation();

  private static SpiRawSqlService initRawSql() {

    Iterator<SpiRawSqlService> loader = ServiceLoader.load(SpiRawSqlService.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for SpiRawSqlService?");
  }

  private static SpiProfileLocationFactory initProfileLocation() {

    Iterator<SpiProfileLocationFactory> loader = ServiceLoader.load(SpiProfileLocationFactory.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for SpiProfileLocationFactory?");
  }

  /**
   * Return the RawSqlService implementation.
   */
  static SpiRawSqlService rawSql() {
    return rawSqlService;
  }

  /**
   * Return the RawSqlService implementation.
   */
  static SpiProfileLocationFactory profileLocationFactory() {
    return profileLocationFactory;
  }

}
