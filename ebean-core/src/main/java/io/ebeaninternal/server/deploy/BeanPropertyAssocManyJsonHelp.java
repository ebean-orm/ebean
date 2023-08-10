package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.json.SpiJsonReader;

import java.io.IOException;

/**
 * Help BeanPropertyAssocMany with JSON processing.
 */
class BeanPropertyAssocManyJsonHelp {

  /**
   * The associated many property.
   */
  private final BeanPropertyAssocMany<?> many;
  /**
   * Helper used to read json for transient 'many' properties.
   */
  private final BeanPropertyAssocManyJsonTransient jsonTransient;

  /**
   * Construct for the owning many property.
   */
  BeanPropertyAssocManyJsonHelp(BeanPropertyAssocMany<?> many) {
    this.many = many;
    boolean objectMapperPresent = many.descriptor().config().getClassLoadConfig().isJacksonObjectMapperPresent();
    this.jsonTransient = !objectMapperPresent ? null : new BeanPropertyAssocManyJsonTransient();
  }

  /**
   * Read the JSON for this property.
   */
  public void jsonRead(SpiJsonReader readJson, EntityBean parentBean) throws IOException {
    if (!this.many.jsonDeserialize) {
      return;
    }
    JsonParser parser = readJson.parser();
    JsonToken event = parser.nextToken();
    if (JsonToken.VALUE_NULL == event) {
      return;
    }
    if (many.isTransient()) {
      jsonReadTransientUsingObjectMapper(readJson, parentBean);
      return;
    }
    if (JsonToken.START_ARRAY != event && JsonToken.START_OBJECT != event) {
      throw new JsonParseException(parser, "Unexpected token " + event + " - expecting start array or object");
    }

    many.setValue(parentBean, many.jsonReadCollection(readJson, parentBean));
  }

  /**
   * Read a Transient property using Jackson ObjectMapper.
   */
  private void jsonReadTransientUsingObjectMapper(SpiJsonReader readJson, EntityBean parentBean) throws IOException {
    if (jsonTransient == null) {
      throw new IllegalStateException("Jackson ObjectMapper is required to read this Transient property " + many.fullName());
    }
    jsonTransient.jsonReadUsingObjectMapper(many, readJson, parentBean);
  }
}
