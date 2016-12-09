package com.avaje.tests.model.basic.finder;

import com.avaje.ebean.Finder;
import com.avaje.tests.model.basic.Customer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Finder methods for Customer.
 */
public class CustomerFinder extends Finder<Integer, Customer> {

  public CustomerFinder() {
    super(Customer.class);
  }

  public CustomerFinder(String serverName) {
    super(Customer.class, serverName);
  }

  /**
   * Find customer by unique name.
   */
  @Nullable
  public Customer byName(String name) {

    return query().where().eq("name", name).findUnique();
  }

  public List<String> namesStartingWith(String name) {

    return nativeSql("select name from o_customer where name like ? order by name")
      .setParameter(1, name+"%")
      .findSingleAttributeList();
  }
}
