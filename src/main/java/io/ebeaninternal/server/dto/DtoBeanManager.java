package io.ebeaninternal.server.dto;

import io.ebean.meta.MetaQueryMetric;
import io.ebeaninternal.metric.MetricFactory;
import io.ebeaninternal.metric.QueryPlanCollector;
import io.ebeaninternal.server.type.TypeManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all the DTO bean descriptors.
 */
public class DtoBeanManager {

  private final TypeManager typeManager;

  private final Map<Class, DtoBeanDescriptor> descriptorMap = new ConcurrentHashMap<>();

  public DtoBeanManager(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  /**
   * Return the descriptor for the given DTO bean class.
   */
  @SuppressWarnings("unchecked")
  public <T> DtoBeanDescriptor<T> getDescriptor(Class<T> dtoType) {

    return descriptorMap.computeIfAbsent(dtoType, this::createDescriptor);
  }

  private <T> DtoBeanDescriptor createDescriptor(Class<T> dtoType) {

    try {
      DtoMeta meta = new DtoMetaBuilder(dtoType, typeManager).build();
      return new DtoBeanDescriptor<>(dtoType, meta);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public List<MetaQueryMetric> collectStats(boolean reset) {

    QueryPlanCollector collector = MetricFactory.get().createCollector(reset);

    for (DtoBeanDescriptor value : descriptorMap.values()) {
      value.collectStats(collector);
    }
    return collector.complete();
  }
}
