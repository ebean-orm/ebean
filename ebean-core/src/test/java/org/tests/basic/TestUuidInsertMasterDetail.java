package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.UUOne;
import org.tests.model.basic.UUTwo;
import org.junit.Test;

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

    Ebean.save(one);

    UUOne oneB = Ebean.find(UUOne.class, one.getId());

    UUTwo twoB = new UUTwo();
    twoB.setName("another something");
    oneB.getComments().add(twoB);

    Ebean.save(oneB);
  }

  public void testNullFK() {

    UUTwo two = new UUTwo();
    two.setName("something");
    Ebean.save(two);
  }

}
