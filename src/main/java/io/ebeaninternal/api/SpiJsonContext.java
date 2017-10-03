package io.ebeaninternal.api;

import com.fasterxml.jackson.core.JsonGenerator;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import io.ebeaninternal.server.text.json.SpiJsonWriter;

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
}
