package io.ebean;

import io.ebean.metric.MetricFactory;
import io.ebean.service.*;

import java.util.ServiceLoader;

/**
 * Bootstrap internal services.
 */
public final class XBootstrapService {

  private static final SpiContainerFactory containerFactory;
  private static final SpiRawSqlService rawSqlService;
  private static final SpiProfileLocationFactory profileLocationFactory;
  private static final SpiFetchGroupService fetchGroupService;
  private static final MetricFactory metricFactory;
  private static final SpiJsonService jsonService;
  static {
    SpiContainerFactory _factory = null;
    SpiRawSqlService _raw = null;
    SpiProfileLocationFactory _profile = null;
    SpiFetchGroupService _fetch = null;
    MetricFactory _metric = null;
    SpiJsonService _json = null;
    for (BootstrapService extension : ServiceLoader.load(BootstrapService.class)) {
      if (extension instanceof SpiContainerFactory) {
        _factory = (SpiContainerFactory)extension;
      } else if (extension instanceof SpiRawSqlService) {
        _raw = (SpiRawSqlService)extension;
      } else if (extension instanceof SpiProfileLocationFactory) {
        _profile = (SpiProfileLocationFactory)extension;
      } else if (extension instanceof SpiFetchGroupService) {
        _fetch = (SpiFetchGroupService)extension;
      } else if (extension instanceof MetricFactory) {
        _metric = (MetricFactory)extension;
      } else if (extension instanceof SpiJsonService) {
        _json = (SpiJsonService)extension;
      }
    }
    containerFactory = _factory;
    rawSqlService = _raw;
    profileLocationFactory = _profile;
    fetchGroupService = _fetch;
    metricFactory = _metric;
    jsonService = _json;
  }

  /**
   * Return the MetricFactory found in boostrap service loading.
   */
  public static MetricFactory metricFactory() {
    return metricFactory;
  }

  /**
   * Return the SpiJsonService found in boostrap service loading.
   */
  public static SpiJsonService jsonService() {
    return jsonService;
  }

  static SpiContainerFactory containerFactory() {
    return containerFactory;
  }

  static SpiRawSqlService rawSql() {
    return rawSqlService;
  }

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
