package org.tests.basic;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.interfaces.Address;
import org.tests.model.interfaces.IAddress;
import org.tests.model.interfaces.IPerson;
import org.tests.model.interfaces.Person;

public class TestManyOneInterface extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    IAddress a = new Address("hello");

    IPerson p = new Person();

    p.setDefaultAddress(a);

    DB.save(a);
    DB.save(p);

    //Assert.assertTrue();

  }
}
