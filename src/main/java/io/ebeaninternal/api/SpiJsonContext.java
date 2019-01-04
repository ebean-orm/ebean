package io.ebeaninternal.api;

import com.fasterxml.jackson.core.JsonGenerator;
import io.ebean.plugin.BeanType;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.Writer;

/**
 * Extended Json Context for internal server use.
 */
public interface SpiJsonContext extends JsonContext {

  /**
   * Create a Json Writer for writing beans as JSON.
   */
  SpiJsonWriter createJsonWriter(JsonGenerator gen, JsonWriteOptions options);

  /**
   * Create a Json Writer for writing beans as JSON supplying a writer.
   */
  SpiJsonWriter createJsonWriter(Writer writer);

  /**
   * Create a Json Reader for reading the JSON content.
   */
  SpiJsonReader createJsonRead(BeanType<?> beanType, String json);

}
