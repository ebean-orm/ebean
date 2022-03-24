package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UUOne;
import org.tests.model.basic.UUTwo;

import java.util.ArrayList;

public class TestUuidInsertMasterDetail extends BaseTestCase {

  @Test
  public void testInsert() {

    UUTwo two = new UUTwo();
    two.setName("something");

    ArrayList<UUTwo> list = new ArrayList<>();
    list.add(two);

    UUOne one = new UUOne();
    one.setName("some one");
    one.setComments(list);

    DB.save(one);

    UUOne oneB = DB.find(UUOne.class, one.getId());

    UUTwo twoB = new UUTwo();
    twoB.setName("another something");
    oneB.getComments().add(twoB);

    DB.save(oneB);
  }

  public void testNullFK() {

    UUTwo two = new UUTwo();
    two.setName("something");
    DB.save(two);
  }

}
