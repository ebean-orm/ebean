package com.avaje.ebean.elasticsearch;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.squareup.okhttp.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class TestBasicPush extends BaseTestCase {

  public static final MediaType JSON
          = MediaType.parse("application/json; charset=utf-8");

  OkHttpClient client = new OkHttpClient();

  @Ignore
  @Test
  public void testBulkUpdate() throws IOException {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);
    JsonContext jsonContext = server.json();

    StringWriter writer = new StringWriter();
    JsonGenerator generator = jsonContext.createGenerator(writer);

    List<Customer> customers = Ebean.find(Customer.class).findList();

    for (Customer customer : customers) {

      customer.setName(customer.getName()+"esMod");
      PathProperties updateProps = PathProperties.parse("name");

      generator.writeStartObject();
      generator.writeFieldName("update");
      generator.writeStartObject();
      generator.writeStringField("_id", customer.getId().toString());
      generator.writeStringField("_type", "customer");
      generator.writeStringField("_index", "customer");
      generator.writeEndObject();
      generator.writeEndObject();
      generator.writeRaw("\n");

      generator.writeStartObject();
      generator.writeFieldName("doc");
      jsonContext.toJson(customer, generator, updateProps);
      generator.writeEndObject();
      generator.writeRaw("\n");
    }

    generator.close();
    String json = writer.toString();

    post("http://localhost:9200/_bulk", json);

    //curl -s -XPOST localhost:9200/_bulk

//    { "update" : {"_id" : "1", "_type" : "type1", "_index" : "index1"} }
//    { "doc" : {"field2" : "value2"} }


  }

  @Ignore
  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);
    JsonContext jsonContext = server.json();

    List<Customer> customers = Ebean.find(Customer.class).findList();

    PathProperties paths = PathProperties.parse("name, status, anniversary");
    for (Customer customer : customers) {

      String json = jsonContext.toJson(customer, paths);
      put("http://localhost:9200/customer/customer/"+customer.getId(), json);
    }

  }

  String post(String url, String json) throws IOException {
    RequestBody body = RequestBody.create(JSON, json);
    Request request = new Request.Builder()
            .url(url)
            .put(body)
            .build();
    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  String put(String url, String json) throws IOException {
    RequestBody body = RequestBody.create(JSON, json);
    Request request = new Request.Builder()
            .url(url)
            .put(body)
            .build();
    Response response = client.newCall(request).execute();
    return response.body().string();
  }

}
