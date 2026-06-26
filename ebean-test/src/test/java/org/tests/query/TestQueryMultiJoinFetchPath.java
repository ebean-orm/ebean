package org.tests.query;

import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.join.*;
import org.tests.model.join.initfields.Order;
import org.tests.model.join.initfields.OrderDetail;
import org.tests.model.join.initfields.OrderInvoice;
import org.tests.model.join.initfields.OrderItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestQueryMultiJoinFetchPath extends BaseTestCase {

  @Test
  void test() {
    HCustomer c1 = new HCustomer("c1", "c1");
    DB.save(c1);

    HCustomer c2 = new HCustomer("c2", "c2");
    DB.save(c2);

    HCustomer c3 = new HCustomer("c3", "c3");
    DB.save(c3);

    HAccount a1 = new BankAccount();
    a1.setAccountNumber("a1");
    a1.setOwner(c1);
    DB.save(a1);

    CustomerAccess ca = new CustomerAccess();
    ca.setAccessor(c3);
    ca.setPrincipal(c1);
    DB.save(ca);

    AccountAccess aa = new AccountAccess();
    aa.setAccessor(c2);
    aa.setAccount(a1);
    DB.save(aa);

    List<Object> ids = DB.find(HAccess.class)
      .where()
      .eq("principal.status", "A")
      .eq("accessor.status", "A")
      .findIds();

    assertThat(ids).hasSize(2);

    Query<HAccess> query = DB.find(HAccess.class)
      .fetch("account","accountNumber")
      .fetch("accessor","name")
      .where()
      .eq("accessor.status", "A")
      .eq("principal.status", "A")
      .idIn(ids)
      .query();

    List<HAccess> accesses = query.findList();

    assertThat(accesses).hasSize(2);
    if (isH2()) {
      assertThat(query.getGeneratedSql()).isEqualTo("select t0.dtype, t0.id, t3.dtype, t0.access_account_number, t0.accessor_id, t1.cid, t1.name from haccess t0 left join hcustomer t1 on t1.cid = t0.accessor_id left join haccount t3 on t3.account_number = t0.access_account_number left join hcustomer t2 on t2.cid = t0.principal_id where t1.status = ? and t2.status = ? and t0.id in (?,?,?,?,?)");
    } else {
      // TODO
      assertThat(query.getGeneratedSql()).contains("select t0.dtype, t0.id, t0.accessor_id, t0.principal_id, t2.dtype, t0.access_account_number, t1.cid, t1.name, t2.dtype, t2.account_number from haccess t0 left join hcustomer t1 on t1.cid = t0.accessor_id left join haccount t2 on t2.account_number = t0.access_account_number and t2.dtype = 'B' left join hcustomer t3 on t3.cid = t0.principal_id where t1.status = ? and t3.status = ? and t0.id ");
    }
  }

  @Test
  public void test_manyNonRoot_RootHasNoMany() {
    Order o = new Order();
    DB.save(o);

    OrderItem p1 = new OrderItem(o);
    OrderItem p2 = new OrderItem(o);
    OrderDetail d1 = new OrderDetail(o);
    OrderDetail d2 = new OrderDetail(o);
    OrderInvoice i1 = new OrderInvoice(o);
    OrderInvoice i2 = new OrderInvoice(o);

    DB.saveAll(p1, p2, d1, d2, i1, i2);

    // This first query behaves as expected: a main query and its secondary query.
    LoggedSql.start();
    List<Order> list1 = DB.find(Order.class)
      .fetch("orderItems")
      .fetch("orderDetails")
      .where().gt("id", 0)
      .findList();

    List<String> sql1 = LoggedSql.collect();
    assertThat(sql1).hasSize(2);

    assertThat(list1.get(0).orderItems()).hasSize(2);
    assertThat(list1.get(0).orderDetails()).hasSize(2);

    sql1 = LoggedSql.collect();
    assertThat(sql1).describedAs("no further lazy loading occurs").isEmpty();

    // This query does not eager fetch invoices. We get an NPE on orderInvoices. Only the main query is executed.
    LoggedSql.collect();
    List<Order> list2 = DB.find(Order.class)
      .fetch("orderItems")
      .fetch("orderInvoices")
      .where().gt("id", 0)
      .findList();

    List<String> sql2 = LoggedSql.collect();
    assertThat(sql2).hasSize(2);
    assertThat(sql2.get(0)).contains("from join_initfields_order t0 left join join_initfields_order_item t1 on t1.order_id = t0.id where");
    assertThat(sql2.get(1)).contains("from join_initfields_order_invoice t0 where ");

    assertThat(list2.get(0).orderItems()).hasSize(2);
    assertThat(list2.get(0).orderInvoices()).hasSize(2);

    sql2 = LoggedSql.stop();
    assertThat(sql2).describedAs("no further lazy loading occurs").isEmpty();
  }
}
