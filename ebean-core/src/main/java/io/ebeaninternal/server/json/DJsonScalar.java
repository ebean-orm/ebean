package io.ebeaninternal.server.json;

import io.avaje.json.JsonWriter;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.type.TypeManager;

import java.io.IOException;
import java.util.List;

/**
 * Default implementation of JsonScalar.
 */
public final class DJsonScalar {

  private final TypeManager typeManager;

  public DJsonScalar(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void write(JsonWriter gen, Object value) throws IOException {
    if (value instanceof String) {
      gen.value((String) value);

    } else if (value instanceof List) {
      // expected for @DbArray values
      List list = (List)value;
      gen.beginArray();
      for (int i = 0; i < list.size(); i++) {
        write(gen, list.get(i));
      }
      gen.endArray();

    } else {
      ScalarType scalarType = typeManager.type(value.getClass());
      if (scalarType == null) {
        throw new IllegalArgumentException("unhandled type " + value.getClass());
      }
      scalarType.jsonWrite(gen, value);
    }
  }
}
