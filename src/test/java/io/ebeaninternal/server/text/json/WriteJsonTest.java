package io.ebeaninternal.server.text.json;

import io.ebean.FetchPath;
import io.ebean.config.JsonConfig;
import io.ebean.text.PathProperties;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;

public class WriteJsonTest {

  @Test
  @Ignore // does not work with jackson 2.8.9
  public void test_push() throws IOException {

    JsonFactory jsonFactory = new JsonFactory();
    StringWriter sw = new StringWriter();
    JsonGenerator generator = jsonFactory.createGenerator(sw);

    FetchPath fetchPath = PathProperties.parse("id,status,name,customer(id,name,address(street,city)),orders(qty,product(sku,prodName))");
    WriteJson writeJson = new WriteJson(null, generator, fetchPath, null, null, JsonConfig.Include.ALL, false);

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
