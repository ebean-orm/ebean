package io.ebean.xtest.internal.server.text.json;

import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.json.WriteJson;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WriteJsonDirtyTest {

  private static final JsonStream JSON_STREAM = JsonStream.builder().build();

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();
    List<Customer> customers = DB.find(Customer.class).findList();

    Customer customer = DB.find(Customer.class).setId(customers.get(0).getId())
      .setUseCache(false)
      .findOne();

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    BeanDescriptor<Customer> descriptor = server.descriptor(Customer.class);

    customer.setName("dirtyCustName");
    customer.setAnniversary(new Date(System.currentTimeMillis()));

    EntityBean entityBean = (EntityBean) customer;
    boolean[] dirtyProperties = entityBean._ebean_getIntercept().dirtyProperties();

    StringWriter writer = new StringWriter();
    JsonWriter generator = JSON_STREAM.writer(writer);

    WriteJson writeJson = new WriteJson(server, generator, null, null, null, null, true);
    descriptor.jsonWriteDirty(writeJson, entityBean, dirtyProperties);

    generator.flush();

    String jsonContent = writer.toString();
    assertTrue(jsonContent.contains("\"name\":"));
    assertTrue(jsonContent.contains("\"anniversary\":"));
  }
}
