package io.ebeaninternal.server.type;

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
      // TODO: keepSource for @DbArray?
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.UUID, nullable, false, ArrayElementConverter.UUID);
    }
    return new ScalarTypeJsonList.Varchar(docType(valueType), nullable, false);
  }

  @Override
  public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
    final ArrayElementConverter.EnumConverter converter = new ArrayElementConverter.EnumConverter(scalarType);
    return new ScalarTypeJsonList.VarcharWithConverter(scalarType.getDocType(), nullable, false, converter);
  }
}
