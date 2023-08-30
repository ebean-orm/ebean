/*
 * Licensed Materials - Property of FOCONIS AG
 * (C) Copyright FOCONIS AG.
 */

package org.tests.model.docstore;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.text.json.JsonReadOptions;

import java.io.IOException;

/**
 * Deserializer, that uses ebean deserialization when deserializing beans from DB.
 */
public class ReportJsonDeserializer extends JsonDeserializer<Report> {


  @Override
  public Report deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
    JsonReadOptions options = new JsonReadOptions();
    options.setEnableLazyLoading(true);
    Report bean = DB.json().toBean(Report.class, parser, options);
    ((EntityBean) bean)._ebean_getIntercept().setLoaded(); // to make beanstate work
    return bean;
  }

}
