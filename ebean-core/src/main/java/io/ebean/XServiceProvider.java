package io.ebean;

import io.ebean.service.SpiFetchGroupQuery;
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
    return loadFirstService(SpiFetchGroupService.class);
  }

  private static SpiRawSqlService initRawSql() {
    return loadFirstService(SpiRawSqlService.class);
  }

  private static SpiProfileLocationFactory initProfileLocation() {
    return loadFirstService(SpiProfileLocationFactory.class);
  }

  private static <T> T loadFirstService(Class<T> cls) {
    Iterator<T> loader = ServiceLoader.load(cls).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for " + cls);
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

  /**
   * Return the FetchGroup Query for building fetch groups via query beans.
   */
  static <T> SpiFetchGroupQuery<T> fetchGroupQueryFor(Class<T> cls) {
    return fetchGroupService.queryFor(cls);
  }
}
