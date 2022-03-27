package io.ebeaninternal.server.deploy;

import io.ebean.Database;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.json.SpiJsonWriter;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;


public class TestDiffHelpInsertSimple extends BaseTest {

  private final long firstTime = System.currentTimeMillis() - 10000;

  private final BeanDescriptor<Order> orderDesc;

  public TestDiffHelpInsertSimple() {
    orderDesc = db.descriptor(Order.class);
  }

  private Order createBaseOrder(Database server) {
    Order order1 = new Order();
    order1.setId(12);
    order1.setCretime(new Timestamp(firstTime));
    order1.setCustomer(server.reference(Customer.class, 1234));
    order1.setStatus(Status.NEW);
    //order1.setShipDate(new Date(firstTime));
    order1.setOrderDate(new Date(firstTime));
    return order1;
  }

  @Test
  public void basic() throws IOException {

    Date date = Date.valueOf("2000-01-01");

    Order order1 = createBaseOrder(db);
    order1.setOrderDate(date);

    StringWriter buffer = new StringWriter();

    SpiJsonWriter jsonWriter = spiEbeanServer().jsonExtended().createJsonWriter(buffer);

    orderDesc.jsonWriteForInsert(jsonWriter, (EntityBean) order1);
    jsonWriter.flush();

    String asJson = buffer.toString();

    assertThat(asJson).startsWith("{\"status\":\"NEW\",\"orderDate\":\"2000-01-01\"");
    assertThat(asJson).endsWith(",\"customer\":{\"id\":1234}}");
  }

}
