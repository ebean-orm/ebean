package io.ebeaninternal.server.type;

import io.ebean.annotation.MutationDetection;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Fallback for DbArray as Json List.
 */
class PlatformArrayTypeJsonList implements PlatformArrayTypeFactory {

  private DocPropertyType docType(Type valueType) {
    if (valueType.equals(Long.class)) {
      return DocPropertyType.LONG;
    }
    if (valueType.equals(Integer.class)) {
      return DocPropertyType.INTEGER;
    }
    if (valueType.equals(Double.class)) {
      return DocPropertyType.DOUBLE;
    }
    return DocPropertyType.TEXT;
  }

  @Override
  public ScalarType<?> typeFor(Type valueType, boolean nullable) {
    if (valueType.equals(UUID.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.UUID, nullable, ArrayElementConverter.UUID);
    }
    return new ScalarTypeJsonList(java.sql.Types.VARCHAR, JsonStorage.VARCHAR, docType(valueType), nullable, MutationDetection.DEFAULT);
  }

  @Override
  public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
    final ArrayElementConverter.EnumConverter converter = new ArrayElementConverter.EnumConverter(scalarType);
    return new ScalarTypeJsonList.VarcharWithConverter(scalarType.docType(), nullable, converter);
  }
}
