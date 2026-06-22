package io.ebeaninternal.server.type;

import io.ebean.core.type.DocPropertyType;

/**
 * Base for the JSON collection value types (List, Set).
 * <p>
 * Adds the {@link ScalarTypeArray} aspect (logical DB array column definition) used when the
 * same type backs a {@code @DbArray} mapping on a platform without native array support.
 */
abstract class ScalarTypeJsonCollectionValue<T> extends ScalarTypeJsonValue<T> implements ScalarTypeArray {

  ScalarTypeJsonCollectionValue(Class<T> type, int jdbcType, JsonStorage storage, boolean keepSource,
                                boolean nullable, DocPropertyType docType) {
    super(type, jdbcType, storage, keepSource, nullable, "[]", docType);
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
}
