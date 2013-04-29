package com.avaje.tests.query;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagingList;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestPagingListLoop extends BaseTestCase {

  @Test
  public void test() throws InterruptedException, ExecutionException {

    // boolean autoRunTest = false;
    // if (!autoRunTest){
    // // we only want to run this test manually.
    // return;
    // }

    try {
      ResetBasicData.reset();

      for (int i = 0; i < 50; i++) {
        PagingList<Customer> pagingList = Ebean.find(Customer.class).findPagingList(10);
        pagingList.getFutureRowCount();// .get();

        // createLeak();
        Thread.sleep(10);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  // private void createLeak() {
  //
  // // create a transaction we never close ...
  // // ... a connection pool leak
  // Ebean.getServer(null).createTransaction();
  // }

}
