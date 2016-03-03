package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.text.json.JsonScalar;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.TypeManager;

import java.io.IOException;

/**
 * Default implementation of JsonScalar.
 */
public class DefaultJsonScalar implements JsonScalar {


  private final TypeManager typeManager;

  private final WriteJson writeJson;

  public DefaultJsonScalar(TypeManager typeManager, WriteJson writeJson) {
    this.typeManager = typeManager;
    this.writeJson = writeJson;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void write(Object value) throws IOException {

    if (value instanceof String) {
      writeJson.writeString((String)value);

    } else {
      ScalarType scalarType = typeManager.getScalarType(value.getClass());
      if (scalarType == null) {
        throw new IllegalArgumentException("unhandled type " + value.getClass());
      }
      scalarType.jsonWrite(writeJson.gen(), value);
    }
  }
}
