package io.ebean.xtest.text;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import io.ebean.text.PathProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathPropertiesTests extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(PathPropertiesTests.class);

  @Test
  void example_withQueryAndJson() {
    ResetBasicData.reset();

    PathProperties pathProps = PathProperties.parse("id,name,billingAddress(city),shippingAddress(*))");

    Query<Customer> query = DB.find(Customer.class)
      .where().lt("id", 2)
      .query();

    pathProps.apply(query);

    List<Customer> list = query.findList();

    String asJson = DB.json().toJson(list, pathProps);
    log.info("Json: {}", asJson);
  }

  @Test
  void test_withAllPropsQuery() {
    PathProperties root = PathProperties.parse("*,billingAddress(line1)");
    LoggedSql.start();
    Query<Customer> query = DB.find(Customer.class).apply(root);
    query.findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.name, t0.smallnote, t0.anniversary, t0.cretime, t0.updtime, t0.version, t0.shipping_address_id, t1.id, t1.line_1 from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id;");
  }

}
