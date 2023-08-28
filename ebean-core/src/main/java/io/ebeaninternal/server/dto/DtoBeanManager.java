package io.ebeaninternal.server.dto;

import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.server.type.TypeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all the DTO bean descriptors.
 */
@SuppressWarnings("rawtypes")
public final class DtoBeanManager {

  private static final Map<String,String> EMPTY_NAMED_QUERIES = new HashMap<>();

  private final TypeManager typeManager;
  private final Map<Class, DtoBeanDescriptor> descriptorMap = new ConcurrentHashMap<>();

  public DtoBeanManager(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  /**
   * Return the descriptor for the given DTO bean class.
   */
  @SuppressWarnings("unchecked")
  public <T> DtoBeanDescriptor<T> descriptor(Class<T> dtoType) {
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

  public void visitMetrics(MetricVisitor visitor) {
    for (DtoBeanDescriptor value : descriptorMap.values()) {
      value.visit(visitor);
    }
  }
}
