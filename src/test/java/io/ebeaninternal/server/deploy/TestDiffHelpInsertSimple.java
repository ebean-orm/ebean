package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonWriter;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;


public class TestDiffHelpInsertSimple extends BaseTestCase {

  private long firstTime = System.currentTimeMillis() - 10000;

  private EbeanServer server;

  private BeanDescriptor<Order> orderDesc;

  public TestDiffHelpInsertSimple() {
    server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    orderDesc = spiServer.getBeanDescriptor(Order.class);
  }

  private Order createBaseOrder(EbeanServer server) {
    Order order1 = new Order();
    order1.setId(12);
    order1.setCretime(new Timestamp(firstTime));
    order1.setCustomer(server.getReference(Customer.class, 1234));
    order1.setStatus(Status.NEW);
    //order1.setShipDate(new Date(firstTime));
    order1.setOrderDate(new Date(firstTime));
    return order1;
  }

  @Test
  public void basic() throws IOException {

    Date date = Date.valueOf("2000-01-01");

    Order order1 = createBaseOrder(server);
    order1.setOrderDate(date);

    StringWriter buffer = new StringWriter();

    SpiJsonWriter jsonWriter = spiEbeanServer().jsonExtended().createJsonWriter(buffer);

    orderDesc.jsonWriteForInsert(jsonWriter, (EntityBean) order1);
    jsonWriter.flush();

    String asJson = buffer.toString();

    assertThat(asJson).startsWith("{\"status\":\"NEW\",\"orderDate\":9466");
    assertThat(asJson).endsWith(",\"customer\":{\"id\":1234}}");
  }

}
