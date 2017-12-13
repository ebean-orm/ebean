package io.ebeaninternal.server.type;

import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * H2 database support for DB ARRAY.
 */
class ScalarTypeArrayListH2 extends ScalarTypeArrayList {

  private static ScalarTypeArrayListH2 UUID = new ScalarTypeArrayListH2("uuid", DocPropertyType.UUID, ArrayElementConverter.UUID);
  private static ScalarTypeArrayListH2 LONG = new ScalarTypeArrayListH2("bigint", DocPropertyType.LONG, ArrayElementConverter.LONG);
  private static ScalarTypeArrayListH2 INTEGER = new ScalarTypeArrayListH2("integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER);
  private static ScalarTypeArrayListH2 DOUBLE = new ScalarTypeArrayListH2("double", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE);
  private static ScalarTypeArrayListH2 STRING = new ScalarTypeArrayListH2("varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING);

  static PlatformArrayTypeFactory factory() {
    return new ScalarTypeArrayListH2.Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarTypeArrayListH2 typeFor(Type valueType) {
      if (valueType.equals(java.util.UUID.class)) {
        return UUID;
      }
      if (valueType.equals(Long.class)) {
        return LONG;
      }
      if (valueType.equals(Integer.class)) {
        return INTEGER;
      }
      if (valueType.equals(Double.class)) {
        return DOUBLE;
      }
      if (valueType.equals(String.class)) {
        return STRING;
      }
      throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
    }

    @Override
    public ScalarType<?> typeForEnum(ScalarType<?> scalarType) {
      return new ScalarTypeArrayListH2("varchar", DocPropertyType.TEXT, new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  @SuppressWarnings("rawtypes")
  private ScalarTypeArrayListH2(String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(arrayType, docPropertyType, converter);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void bind(DataBind bind, List value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setObject(toArray(value));
    }
  }
}
