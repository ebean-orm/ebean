package org.querytest;

import io.ebean.DB;
import org.example.domain.Customer;
import org.example.domain.Order;
import org.example.domain.OrderDetail;
import org.example.domain.Product;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FindNative_withSchema {

  @Test
  public void test_rootLevelUsesSchema() {

    Product p = new Product();
    p.setName("prod1");
    p.setSku("p1");
    p.save();

    String sql = "select * from foo.o_product p where p.sku = ?";

    Product product = DB.findNative(Product.class, sql)
      .setParameter("p1")
      .findOne();

    assertThat(product).isNotNull();
    assertThat(product.getName()).isEqualTo("prod1");
  }

  @Test
  public void test_joinToSchema() {

    Order order = setupData();

    String sql = "select l.*, p.* " +
      " from o_order_detail l " +
      " join foo.o_product p on p.id = l.product_id " +
      " where l.order_id = ?";

    List<OrderDetail> lines = DB.findNative(OrderDetail.class, sql)
      .setParameter(order.getId())
      .findList();

    assertThat(lines).hasSize(1);
    OrderDetail line = lines.get(0);
    Product product = line.getProduct();

    // check normal bean population
    assertThat(line.getOrderQty()).isEqualTo(10);
    assertThat(line.getOrder().getId()).isEqualTo(order.getId());
    // check associated bean with schema is populated
    assertThat(product).isNotNull();
    assertThat(product.getName()).isEqualTo("prod2");
  }

  private Order setupData() {
    Product p = new Product();
    p.setName("prod2");
    p.setSku("p2");
    p.save();

    Customer customer = new Customer();
    customer.setName("junk");
    customer.save();
    Order order = new Order();
    order.setCustomer(customer);

    OrderDetail line = new OrderDetail(p, 10, 10.0);
    order.getDetails().add(line);

    order.save();
    return order;
  }
}
