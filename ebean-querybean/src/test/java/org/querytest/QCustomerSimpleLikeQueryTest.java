package org.querytest;

import org.example.domain.Customer;
import org.example.domain.query.QCustomer;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class QCustomerSimpleLikeQueryTest {

  @Test
  public void testQuery() {

    List<Customer> customers =
        new QCustomer()
        .name.ilike("rob")
        .status.equalTo(Customer.Status.GOOD)
        .registered.after(new Date())
        .contacts.email.endsWith("@foo.com")
        .orderBy()
          .name.asc()
          .registered.desc()
        .findList();

    //where lower(t0.name) like ?  and t0.status = ?  and t0.registered > ?  and u1.email like ?  order by t0.name, t0.registered desc; --bind(rob,GOOD,Mon Jul 27 12:05:37 NZST 2015,%@foo.com)
  }

  @Test
  public void testFindEach() {

    new QCustomer()
         .status.equalTo(Customer.Status.GOOD)
         .orderBy().id.asc()
         .findEach(customer -> System.out.println("-- visit " + customer));
  }
}
