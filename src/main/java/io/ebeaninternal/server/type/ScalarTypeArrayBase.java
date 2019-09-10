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
        return fromArray(convertArray(array.getArray()));
      } finally {
        array.free();
      }
    }
  }

  private Object[] convertArray(Object array) {
    if (array instanceof Object[]) {
      return (Object[]) array;
    }
    if (array instanceof long[]) {
      return convertLongs((long[]) array);
    }
    if (array instanceof int[]) {
      return convertInts((int[]) array);
    }
    if (array instanceof double[]) {
      return convertDoubles((double[]) array);
    }
    throw new IllegalArgumentException("Unable to convert array " + array);
  }

  private Object[] convertLongs(long[] o) {
    Long[] list = new Long[o.length];
    for (int i = 0; i < o.length; i++) {
      list[i] = o[i];
    }
    return list;
  }

  private Object[] convertInts(int[] o) {
    Integer[] list = new Integer[o.length];
    for (int i = 0; i < o.length; i++) {
      list[i] = o[i];
    }
    return list;
  }

  private Object[] convertDoubles(double[] o) {
    Double[] list = new Double[o.length];
    for (int i = 0; i < o.length; i++) {
      list[i] = o[i];
    }
    return list;
  }

  protected abstract T fromArray(Object[] array1);

}
