package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * H2 database support for DB ARRAY.
 */
@SuppressWarnings("rawtypes")
final class ScalarTypeArrayListH2 extends ScalarTypeArrayList {

  static PlatformArrayTypeFactory factory() {
    return new ScalarTypeArrayListH2.Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, ScalarTypeArrayListH2> cache = new HashMap<>();

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarType<?> typeFor(Type valueType, boolean nullable) {
      lock.lock();
      try {
        String key = valueType + ":" + nullable;
        if (valueType.equals(UUID.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "uuid", DocPropertyType.UUID, ArrayElementConverter.UUID));
        }
        if (valueType.equals(Long.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "bigint", DocPropertyType.LONG, ArrayElementConverter.LONG));
        }
        if (valueType.equals(Integer.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER));
        }
        if (valueType.equals(Float.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "real", DocPropertyType.DOUBLE, ArrayElementConverter.FLOAT));
        }
        if (valueType.equals(Double.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE));
        }
        if (valueType.equals(BigDecimal.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "float", DocPropertyType.DOUBLE, ArrayElementConverter.BIG_DECIMAL));
        }
        if (valueType.equals(String.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING));
        }
        if (valueType.equals(Instant.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "timestamp", DocPropertyType.TEXT, ArrayElementConverter.INSTANT));
        }
        if (valueType.equals(LocalDate.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "date", DocPropertyType.TEXT, ArrayElementConverter.LOCAL_DATE));
        }
        throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
      } finally {
        lock.unlock();
      }
    }

    @Override
    public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
      return new ScalarTypeArrayListH2(nullable, "varchar", DocPropertyType.TEXT, new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  private ScalarTypeArrayListH2(boolean nullable, String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(nullable, arrayType, docPropertyType, converter);
  }

  @Override
  public void bind(DataBinder binder, List value) throws SQLException {
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
      binder.setObject(EMPTY_ARRAY);
    }
  }
}
