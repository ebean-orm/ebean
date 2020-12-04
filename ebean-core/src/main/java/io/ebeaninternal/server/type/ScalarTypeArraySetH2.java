package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.EMPTY_SET;

/**
 * H2 database support for DB ARRAY.
 */
@SuppressWarnings("rawtypes")
class ScalarTypeArraySetH2 extends ScalarTypeArraySet {

  static PlatformArrayTypeFactory factory() {
    return new ScalarTypeArraySetH2.Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, ScalarTypeArraySetH2> cache = new HashMap<>();

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarType<?> typeFor(Type valueType, boolean nullable) {
      lock.lock();
      try {
        String key = valueType + ":" + nullable;
        if (valueType.equals(UUID.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySetH2(nullable, "uuid", DocPropertyType.UUID, ArrayElementConverter.UUID));
        }
        if (valueType.equals(Long.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySetH2(nullable, "bigint", DocPropertyType.LONG, ArrayElementConverter.LONG));
        }
        if (valueType.equals(Integer.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySetH2(nullable, "integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER));
        }
        if (valueType.equals(Double.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySetH2(nullable, "float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE));
        }
        if (valueType.equals(String.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySetH2(nullable, "varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING));
        }
        throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
      } finally {
        lock.unlock();
      }
    }

    @Override
    public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
      return new ScalarTypeArraySetH2(nullable, "varchar", DocPropertyType.TEXT, new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  @SuppressWarnings("rawtypes")
  private ScalarTypeArraySetH2(boolean nullable, String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(nullable, arrayType, docPropertyType, converter);
  }

  @Override
  public void bind(DataBinder binder, Set value) throws SQLException {
    if (value == null) {
      bindNull(binder);
    } else {
      binder.setObject(toArray(value));
    }
  }

  @Override
  protected void bindNull(DataBinder binder) throws SQLException {
    if (nullable) {
      binder.setNull(Types.ARRAY);
    } else {
      binder.setObject(toArray(EMPTY_SET));
    }
  }
}
