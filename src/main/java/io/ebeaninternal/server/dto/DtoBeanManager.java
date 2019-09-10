package io.ebeaninternal.server.dto;

import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.server.type.TypeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all the DTO bean descriptors.
 */
public class DtoBeanManager {

  private static final Map<String,String> EMPTY_NAMED_QUERIES = new HashMap<>();

  private final TypeManager typeManager;

  private final Map<Class<?>, DtoNamedQueries> namedQueries;

  private final Map<Class, DtoBeanDescriptor> descriptorMap = new ConcurrentHashMap<>();

  public DtoBeanManager(TypeManager typeManager, Map<Class<?>, DtoNamedQueries> namedQueries) {
    this.typeManager = typeManager;
    this.namedQueries = namedQueries;
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
      return new DtoBeanDescriptor<>(dtoType, meta, namedQueries(dtoType));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private <T> Map<String, String> namedQueries(Class<T> dtoType) {
    DtoNamedQueries namedQueries = this.namedQueries.get(dtoType);
    return (namedQueries == null) ? EMPTY_NAMED_QUERIES : namedQueries.map();
  }

  public void visitMetrics(MetricVisitor visitor) {
    for (DtoBeanDescriptor value : descriptorMap.values()) {
      value.visit(visitor);
    }
  }
}
