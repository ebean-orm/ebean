package io.ebeaninternal.server.text.json;

import io.ebean.Ebean;
import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.text.PathProperties;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WriteJsonTest {

  @Test
  public void test_push() throws IOException {

    ResetBasicData.reset();

    FetchPath fetchPath = PathProperties.parse("id,status,name,customer(id,name,billingAddress(street,city)),details(qty,product(sku,prodName))");

    Query<Order> query = Ebean.find(Order.class);

    fetchPath.apply(query);
    List<Order> list = query.findList();

    String json = Ebean.json().toJson(list);
    System.out.println(json);

    assertThat(json).contains("\"customer\":{");
    assertThat(json).contains("\"billingAddress\":{");
    assertThat(json).contains("\"details\":[{");
  }
}
