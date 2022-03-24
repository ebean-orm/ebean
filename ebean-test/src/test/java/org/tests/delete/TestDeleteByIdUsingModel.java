package org.tests.delete;

import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.onetoone.Account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
