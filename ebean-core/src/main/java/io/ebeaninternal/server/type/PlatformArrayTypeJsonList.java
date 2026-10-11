package io.ebeaninternal.server.type;

import io.ebean.annotation.MutationDetection;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    if (valueType.equals(Long.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.LONG, nullable, ArrayElementConverter.LONG);
    }
    if (valueType.equals(Integer.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.INTEGER, nullable, ArrayElementConverter.INTEGER);
    }
    if (valueType.equals(Double.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.DOUBLE, nullable, ArrayElementConverter.DOUBLE);
    }
    if (valueType.equals(Float.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.DOUBLE, nullable, ArrayElementConverter.FLOAT);
    }
    if (valueType.equals(BigDecimal.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.DOUBLE, nullable, ArrayElementConverter.BIG_DECIMAL);
    }
    if (valueType.equals(Instant.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.TEXT, nullable, ArrayElementConverter.INSTANT);
    }
    if (valueType.equals(LocalDate.class)) {
      return new ScalarTypeJsonList.VarcharWithConverter(DocPropertyType.TEXT, nullable, ArrayElementConverter.LOCAL_DATE);
    }
    return new ScalarTypeJsonList(java.sql.Types.VARCHAR, JsonStorage.VARCHAR, docType(valueType), nullable, MutationDetection.DEFAULT);
  }

  @Override
  public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
    final ArrayElementConverter.EnumConverter converter = new ArrayElementConverter.EnumConverter(scalarType);
    return new ScalarTypeJsonList.VarcharWithConverter(scalarType.docType(), nullable, converter);
  }
}
