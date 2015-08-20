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
  public void write(String name, Object value) throws IOException {

    if (value instanceof String) {
      writeJson.writeStringField(name, (String)value);

    } else {
      ScalarType scalarType = (ScalarType)typeManager.getScalarType(value.getClass());
      if (scalarType == null) {
        throw new IllegalArgumentException("unhandled type " + value.getClass());
      }
      scalarType.jsonWrite(writeJson, name, value);
    }
  }
}
