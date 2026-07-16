package io.ebeaninternal.server.type;

import io.ebean.annotation.MutationDetection;
import io.ebean.core.type.DocPropertyType;

import java.util.UUID;

/**
 * Base for the JSON collection value types (List, Set).
 * <p>
 * Adds the {@link ScalarTypeArray} aspect (logical DB array column definition) used when the
 * same type backs a {@code @DbArray} mapping on a platform without native array support.
 */
abstract class ScalarTypeJsonCollectionValue<T> extends ScalarTypeJsonValue<T> implements ScalarTypeArray {

  ScalarTypeJsonCollectionValue(Class<T> type, int jdbcType, JsonStorage storage, MutationDetection mutationDetection,
                                boolean nullable, DocPropertyType docType) {
    super(type, jdbcType, storage, mutationDetection, nullable, "[]", docType);
  }

  @Override
  public final String getDbColumnDefn() {
    switch (docType()) {
      case SHORT:
      case INTEGER:
      case LONG:
        return "integer[]";
      case DOUBLE:
      case FLOAT:
        return "decimal[]";
      default:
        return "varchar[]";
    }
  }

  /**
   * Derive the element type from the docType - used only to determine the ScalarType to use
   * when binding an element (e.g. for an empty collection where the element type can't be
   * determined from the collection content).
   */
  @Override
  public Class<?> elementType() {
    switch (docType()) {
      case UUID:
        return UUID.class;
      case SHORT:
      case INTEGER:
        return Integer.class;
      case LONG:
        return Long.class;
      case FLOAT:
      case DOUBLE:
        return Double.class;
      default:
        return String.class;
    }
  }
}
