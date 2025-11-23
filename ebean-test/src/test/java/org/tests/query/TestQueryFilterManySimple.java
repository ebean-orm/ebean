package org.tests.query;

import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryFilterManySimple extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // not really last week :)
    Date lastWeek = Date.valueOf("2010-01-01");

    LoggedSql.start();
    List<Customer> list = DB.find(Customer.class)
      // .join("orders", new JoinConfig().lazy())
      // .join("orders", new JoinConfig().query())
      .fetch("orders").fetchQuery("contacts").where().ilike("name", "rob%")
      .filterMany("orders").eq("status", Order.Status.NEW).gt("orderDate", lastWeek)
      .filterMany("contacts").isNotNull("firstName").findList();

    // invoke lazy loading
    list.get(0).getOrders().size();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null left join o_customer t2 on t2.id = t1.kcustomer_id and t1.status = ? and t1.order_date > ?");
    assertThat(sql.get(0)).contains("order by t0.id");
    if (isPostgresCompatible()) {
      assertThat(sql.get(1)).contains("from contact t0 where (t0.customer_id) = any(?) and t0.first_name is not null;");
    } else {
      assertThat(sql.get(1)).contains("from contact t0 where (t0.customer_id) in (?) and t0.first_name is not null;");
    }
  }
}
