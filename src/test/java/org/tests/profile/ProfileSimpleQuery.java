package org.tests.profile;

import org.tests.model.basic.Customer;
//import org.tests.model.basic.ResetBasicData;

public class ProfileSimpleQuery {

  public static void main(String[] args) {

//    ResetBasicData.reset();
    ProfileSimpleQuery me = new ProfileSimpleQuery();
    me.runIt();

  }

  private long counter;

  private void runIt() {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000_000; i++) {
      readCustomer();
    }
    long exe = System.currentTimeMillis() - start;
    System.out.println("run in " + exe);
  }

  private void readCustomer() {
    Customer customer = Customer.find.byName("Rob");
    String name = customer.getName();
    if (name.length() > 999) {
      counter++;
    }
  }

}
