package io.ebean.xtest.internal.server.text.json;

import io.ebean.DB;
import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.text.PathProperties;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WriteJsonTest {

  @Test
  void test_push() {

    ResetBasicData.reset();

    FetchPath fetchPath = PathProperties.parse("id,status,customer(id,name,billingAddress(line1,city)),details(orderQty,product(sku,name))");

    Query<Order> query = DB.find(Order.class);

    fetchPath.apply(query);
    List<Order> list = query.findList();

    String json = DB.json().toJsonPretty(list);

    assertThat(json).contains("\"customer\": {");
    assertThat(json).contains("\"billingAddress\": {");
    assertThat(json).contains("\"details\": [ {");
  }
}
