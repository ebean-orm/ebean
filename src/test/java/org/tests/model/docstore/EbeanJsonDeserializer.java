package org.tests.model.docstore;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.ebean.Ebean;
import io.ebean.text.json.JsonReadOptions;

public class EbeanJsonDeserializer<T> extends JsonDeserializer<T> {

  private Class<? extends T> beanType;

  @Override
  public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonReadOptions options = new JsonReadOptions();
    options.setEnableLazyLoading(true);
    return Ebean.json().toBean(beanType, p, options);
  }
  public void setBeanType(Class<? extends T> beanType) {
    this.beanType = beanType;
  }

}
