package io.ebeaninternal.server.type;

import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.sql.Array;
import java.sql.SQLException;

abstract class ScalarTypeArrayBase<T> extends ScalarTypeJsonCollection<T> {

  ScalarTypeArrayBase(Class<T> type, int dbType, DocPropertyType docPropertyType) {
    super(type, dbType, docPropertyType);
  }

  @Override
  public T read(DataReader reader) throws SQLException {
    Array array = reader.getArray();
    if (array == null) {
      return null;
    } else {
      try {
        return fromArray((Object[]) array.getArray());
      } finally {
        array.free();
      }
    }
  }

  protected abstract T fromArray(Object[] array1);

}
