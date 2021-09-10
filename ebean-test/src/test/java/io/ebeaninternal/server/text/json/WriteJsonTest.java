package io.ebeaninternal.server.text.json;

import io.ebean.DB;
import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.text.PathProperties;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WriteJsonTest {

  @Test
  public void test_push() {

    ResetBasicData.reset();

    FetchPath fetchPath = PathProperties.parse("id,status,name,customer(id,name,billingAddress(street,city)),details(qty,product(sku,prodName))");

    Query<Order> query = DB.find(Order.class);

    fetchPath.apply(query);
    List<Order> list = query.findList();

    String json = DB.json().toJsonPretty(list);

    assertThat(json).contains("\"customer\": {");
    assertThat(json).contains("\"billingAddress\": {");
    assertThat(json).contains("\"details\": [ {");
  }
}
