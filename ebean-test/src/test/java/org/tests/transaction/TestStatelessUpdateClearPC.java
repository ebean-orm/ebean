package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.EBasic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestStatelessUpdateClearPC extends BaseTestCase {

  @Test
  public void test() {


    DB.beginTransaction();
    try {
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
    } finally {
      DB.rollbackTransaction();
    }
  }
}
