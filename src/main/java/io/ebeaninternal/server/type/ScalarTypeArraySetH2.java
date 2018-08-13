package io.ebeaninternal.server.type;

import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;
import java.util.UUID;

/**
 * H2 database support for DB ARRAY.
 */
class ScalarTypeArraySetH2<T> extends ScalarTypeArraySet<T> {

  private static final ScalarTypeArraySetH2<UUID> UUID = new ScalarTypeArraySetH2<>("uuid", DocPropertyType.UUID, ArrayElementConverter.UUID);
  private static final ScalarTypeArraySetH2<Long> LONG = new ScalarTypeArraySetH2<>("bigint", DocPropertyType.LONG, ArrayElementConverter.LONG);
  private static final ScalarTypeArraySetH2<Integer> INTEGER = new ScalarTypeArraySetH2<>("integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER);
  private static final ScalarTypeArraySetH2<Double> DOUBLE = new ScalarTypeArraySetH2<>("double", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE);
  private static final ScalarTypeArraySetH2<String> STRING = new ScalarTypeArraySetH2<>("varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING);

  static PlatformArrayTypeFactory factory() {
    return new ScalarTypeArraySetH2.Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarTypeArraySetH2<?> typeFor(Type valueType) {
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

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ScalarTypeArraySetH2 typeForEnum(ScalarType<?> scalarType) {
      return new ScalarTypeArraySetH2("varchar", DocPropertyType.TEXT, new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ScalarTypeArraySetH2(String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(arrayType, docPropertyType, converter);
  }

  @Override
  public void bind(DataBind bind, Set<T> value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setObject(toArray(value));
    }
  }
}
