package io.ebeaninternal.server.type;

import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

/**
 * H2 database support for DB ARRAY.
 */
class ScalarTypeArraySetH2 extends ScalarTypeArraySet {

  private static ScalarTypeArraySetH2 UUID = new ScalarTypeArraySetH2("uuid", DocPropertyType.UUID, ArrayElementConverter.UUID);
  private static ScalarTypeArraySetH2 LONG = new ScalarTypeArraySetH2("bigint", DocPropertyType.LONG, ArrayElementConverter.LONG);
  private static ScalarTypeArraySetH2 INTEGER = new ScalarTypeArraySetH2("integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER);
  private static ScalarTypeArraySetH2 DOUBLE = new ScalarTypeArraySetH2("double", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE);
  private static ScalarTypeArraySetH2 STRING = new ScalarTypeArraySetH2("varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING);

  static PlatformArrayTypeFactory factory() {
    return new ScalarTypeArraySetH2.Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarTypeArraySetH2 typeFor(Type valueType) {
      if (valueType.equals(java.util.UUID.class)) {
        return UUID;
      }
      if (valueType.equals(Integer.class)) {
        return INTEGER;
      }
      if (valueType.equals(Long.class)) {
        return LONG;
      }
      if (valueType.equals(Double.class)) {
        return DOUBLE;
      }
      if (valueType.equals(String.class)) {
        return STRING;
      }
      throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
    }
  }

  private ScalarTypeArraySetH2(String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(arrayType, docPropertyType, converter);
  }

  @Override
  public void bind(DataBind bind, Set value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setObject(toArray(value));
    }
  }
}
