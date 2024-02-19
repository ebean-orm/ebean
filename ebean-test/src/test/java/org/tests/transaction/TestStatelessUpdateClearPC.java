package org.tests.transaction;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestStatelessUpdateClearPC extends BaseTestCase {

  @Test
  public void test() {
    try (Transaction txn = DB.beginTransaction()) {
      EBasic newUser = new EBasic();
      newUser.setName("any@email.com");
      DB.save(newUser);

      // load the bean into the persistence context
      EBasic dummyLoadedUser = DB.find(EBasic.class, newUser.getId()); // This row is added
      assertNotNull(dummyLoadedUser);

      // stateless update (should clear the bean from the persistence context)
      EBasic updateUser = new EBasic();
      updateUser.setId(newUser.getId());
      updateUser.setName("anyNew@email.com");
      DB.update(updateUser);

      EBasic loadedUser = DB.find(EBasic.class, newUser.getId());
      assertEquals("anyNew@email.com", loadedUser.getName());
    }
  }
}
