package com.avaje.tests.delete;

import com.avaje.ebean.BaseTestCase;
import com.avaje.tests.model.onetoone.Account;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestDeleteByIdUsingModel extends BaseTestCase {

  @Test
  public void test() {

    Account account = new Account();
    account.setName("GoingToDeleteThis");
    account.save();

    Account account1 = Account.find.byId(account.getId());
    assertEquals(account.getName(), account1.getName());

    Account.find.deleteById(account.getId());

    Account account2 = Account.find.byId(account.getId());
    assertNull(account2);
  }
}
