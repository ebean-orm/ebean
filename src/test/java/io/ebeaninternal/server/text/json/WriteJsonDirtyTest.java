package io.ebeaninternal.server.text.json;

import io.ebean.Ebean;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class WriteJsonDirtyTest {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();
    List<Customer> customers = Ebean.find(Customer.class).findList();

    Customer customer = Ebean.find(Customer.class).setId(customers.get(0).getId())
      .setUseCache(false)
      .findOne();

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    BeanDescriptor<Customer> descriptor = server.getBeanDescriptor(Customer.class);

    customer.setName("dirtyCustName");
    customer.setAnniversary(new Date(System.currentTimeMillis()));

    EntityBean entityBean = (EntityBean) customer;
    boolean[] dirtyProperties = entityBean._ebean_getIntercept().getDirtyProperties();

    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    JsonGenerator generator = jsonFactory.createGenerator(writer);

    WriteJson writeJson = new WriteJson(server, generator, null, null, null, null);
    descriptor.jsonWriteDirty(writeJson, entityBean, dirtyProperties);

    generator.flush();
    generator.close();

    String jsonContent = writer.toString();
    assertTrue(jsonContent.contains("\"name\":"));
    assertTrue(jsonContent.contains("\"anniversary\":"));
  }
}
