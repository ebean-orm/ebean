package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests if outer joins are correctly used.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class TestOuterJoin extends BaseTestCase {

  @BeforeAll
  public static void setup() {
    ResetBasicData.reset();
  }

  @Test
  public void testOuterOnNullQuery() throws Exception {

    LoggedSql.start();
    List<Customer> list = DB.find(Customer.class).where()
        .isNull("orders")
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" where not exists "); // perfored with not exists query
    assertThat(list).hasSize(2);

    // use OR construct
    LoggedSql.start();
    list = DB.find(Customer.class).where()
        .or()
          .isNull("orders.details.product.name")
          .isNull("orders")
        .endOr()
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" left join o_order ")
      .contains(" left join o_order_detail ")
      .contains(" left join o_product ")
      .contains(" or not exists ");

    assertThat(list).hasSize(4);

    // use AND construct
    LoggedSql.start();
    list = DB.find(Customer.class).where()
        .and()
          .isNull("orders.details.product.name")
          .isNull("orders")
        .endAnd()
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" left join o_order ")
      .contains(" left join o_order_detail ")
      .contains(" left join o_product ")
      .contains(" and not exists ");

    assertThat(list).hasSize(2);

    LoggedSql.start();
    list = DB.find(Customer.class).where()
        .isNull("orders.details.product.name")
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" left join o_order ")
      .contains(" left join o_order_detail ")
      .contains(" left join o_product ");
    assertThat(list).hasSize(4);
    LoggedSql.stop();
  }

  @Test
  public void testInnerOnNonNullQuery() throws Exception {

    LoggedSql.start();
    List<Customer> list = DB.find(Customer.class).where()
        .isNotNull("orders")
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" where exists "); // perfored with not exists query
    assertThat(list).hasSize(2);

    // use OR construct
    LoggedSql.start();
    list = DB.find(Customer.class).where()
        .or()
          .isNotNull("orders.details.product.name")
          .isNotNull("orders")
        .endOr()
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" left join o_order ")
      .contains(" left join o_order_detail ")
      .contains(" left join o_product ")
      .contains(" or exists ");

    assertThat(list).hasSize(2);

    // use AND construct
    LoggedSql.start();
    list = DB.find(Customer.class).where()
        .and()
          .isNotNull("orders.details.product.name")
          .isNotNull("orders")
        .endAnd()
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
    .doesNotContain(" left join ")
      .contains(" and exists ");

    assertThat(list).hasSize(2);


    LoggedSql.start();
    list = DB.find(Customer.class).where()
        .isNotNull("orders.details.product.name")
        .le("id", 4) // ignore others than basicData
        .findList();

    assertThat(LoggedSql.collect().get(0))
      .doesNotContain(" left join ");
    assertThat(list).hasSize(2);
    LoggedSql.stop();
  }

  @Test
  public void testOuterOnFetch() {

    LoggedSql.start();

    List<Order> orders1 =  DB.find(Order.class).orderBy("id").findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" join o_customer") // ensure that we do not left join the customer
      .doesNotContain(" left join o_customer");


    // now fetch "details" bean, which may be optional
    LoggedSql.start();

    List<Order> orders2 =  DB.find(Order.class)
        .fetch("details", "id").orderBy("id").findList();

    assertThat(LoggedSql.collect().get(0))
      .contains(" left join o_order_detail ");

    LoggedSql.stop();

    assertThat(orders2).isEqualTo(orders1);
  }

}
