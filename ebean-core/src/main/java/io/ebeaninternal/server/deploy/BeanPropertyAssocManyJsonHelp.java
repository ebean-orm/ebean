package io.ebeaninternal.server.deploy;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
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
    JsonReader parser = readJson.parser();
    if (parser.isNullValue()) {
      return;
    }
    if (many.isTransient()) {
      jsonReadTransientUsingObjectMapper(readJson, parentBean);
      return;
    }
    Token event = parser.currentToken();
    if (Token.BEGIN_ARRAY != event && Token.BEGIN_OBJECT != event) {
      throw new IllegalStateException("Unexpected token " + event + " - expecting start array or object");
    }
    if (readJson.update()) {
      many.setValueIntercept(parentBean, many.jsonReadCollection(readJson, parentBean, many.getValue(parentBean)));
    } else {
      many.setValue(parentBean, many.jsonReadCollection(readJson, parentBean, null));
    }
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
