package io.ebeaninternal.server.text.json;

import io.ebeaninternal.server.type.ScalarType;
import io.ebeaninternal.server.type.TypeManager;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.List;

/**
 * Default implementation of JsonScalar.
 */
public class DJsonScalar {

  private final TypeManager typeManager;

  public DJsonScalar(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void write(JsonGenerator gen, Object value) throws IOException {

    if (value instanceof String) {
      gen.writeString((String) value);

    } else if (value instanceof List) {
      // expected for @DbArray values
      List list = (List)value;
      gen.writeRaw('[');
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) {
          gen.writeRaw(',');
        }
        write(gen, list.get(i));
      }
      gen.writeRaw(']');

    } else {
      ScalarType scalarType = typeManager.getScalarType(value.getClass());
      if (scalarType == null) {
        throw new IllegalArgumentException("unhandled type " + value.getClass());
      }
      scalarType.jsonWrite(gen, value);
    }
  }
}
