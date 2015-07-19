package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.text.PathProperties;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class WriteJsonTest {

  @Test
  public void test_push() throws IOException {

    JsonFactory jsonFactory = new JsonFactory();
    JsonGenerator generator = jsonFactory.createGenerator(new StringWriter());

    PathProperties pathProperties = PathProperties.parse("id,status,name,customer(id,name,address(street,city)),orders(qty,product(sku,prodName))");
    WriteJson writeJson = new WriteJson(null, generator, pathProperties, null);

    WriteJson.WriteBean rootLevel = writeJson.createWriteBean(null, null);
    assertTrue(rootLevel.currentIncludeProps.contains("id"));
    assertTrue(rootLevel.currentIncludeProps.contains("status"));
    assertTrue(rootLevel.currentIncludeProps.contains("name"));
    assertTrue(rootLevel.currentIncludeProps.contains("customer"));

    writeJson.beginAssocOne("customer", null);
    WriteJson.WriteBean customerLevel = writeJson.createWriteBean(null, null);
    assertTrue(customerLevel.currentIncludeProps.contains("id"));
    assertTrue(customerLevel.currentIncludeProps.contains("name"));
    assertTrue(customerLevel.currentIncludeProps.contains("address"));

    writeJson.beginAssocOne("address", null);
    WriteJson.WriteBean addressLevel = writeJson.createWriteBean(null, null);
    assertTrue(addressLevel.currentIncludeProps.contains("street"));
    assertTrue(addressLevel.currentIncludeProps.contains("city"));

    writeJson.endAssocOne();
    writeJson.endAssocOne();

    writeJson.beginAssocMany("orders");
    WriteJson.WriteBean orderLevel = writeJson.createWriteBean(null, null);
    assertTrue(orderLevel.currentIncludeProps.contains("qty"));
    assertTrue(orderLevel.currentIncludeProps.contains("product"));

    writeJson.beginAssocOne("product", null);
    WriteJson.WriteBean productLevel = writeJson.createWriteBean(null, null);
    assertTrue(productLevel.currentIncludeProps.contains("sku"));
    assertTrue(productLevel.currentIncludeProps.contains("prodName"));
    writeJson.endAssocOne();
    writeJson.endAssocMany();

  }
}