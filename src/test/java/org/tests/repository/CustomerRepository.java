package org.tests.repository;

import io.ebean.BeanRepository;
import io.ebean.EbeanServer;
import org.tests.model.basic.Customer;

import java.util.List;

//import javax.inject.Inject;

public class CustomerRepository extends BeanRepository<Integer, Customer> {

  //@Inject
  public CustomerRepository(EbeanServer server) {
    super(Customer.class, server);
  }

  public List<Customer> findByName(String nameStart) {
    return query().where()
      .istartsWith("name", nameStart)
      .findList();
  }

  public Customer findMatchName(String matchName) {
    return nativeSql("select id, name from o_customer where name = ?")
      .setParameter(1, matchName)
      .findOne();
  }

  public int updateNotes(String blah, String whot) {

    return updateQuery()
      .set("smallnote", whot)
      .where().eq("name", blah)
      .update();
  }
}
