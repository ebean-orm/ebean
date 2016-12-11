package io.ebeaninternal.server.text.json;

import io.ebeaninternal.server.type.ScalarType;
import io.ebeaninternal.server.type.TypeManager;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * Default implementation of JsonScalar.
 */
public class DJsonScalar {

  private final TypeManager typeManager;

  public DJsonScalar(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  @SuppressWarnings("unchecked")
  public void write(JsonGenerator gen, Object value) throws IOException {

    if (value instanceof String) {
      gen.writeString((String) value);

    } else {
      ScalarType scalarType = typeManager.getScalarType(value.getClass());
      if (scalarType == null) {
        throw new IllegalArgumentException("unhandled type " + value.getClass());
      }
      scalarType.jsonWrite(gen, value);
    }
  }
}
