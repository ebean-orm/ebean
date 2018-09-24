package io.ebean;

import io.ebean.service.SpiFetchGroupService;
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

  private static SpiFetchGroupService fetchGroupService = initSpiFetchGroupService();

  private static SpiFetchGroupService initSpiFetchGroupService() {
    Iterator<SpiFetchGroupService> loader = ServiceLoader.load(SpiFetchGroupService.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for SpiFetchGroupService?");
  }

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

  /**
   * Return the FetchGroup with the given select clause.
   */
  static <T> FetchGroup<T> fetchGroupOf(Class<T> cls, String select) {
    return fetchGroupService.of(cls, select);
  }

  /**
   * Return the FetchGroupBuilder with the given select clause.
   */
  static <T> FetchGroupBuilder<T> fetchGroupOf(Class<T> cls) {
    return fetchGroupService.of(cls);
  }
}
