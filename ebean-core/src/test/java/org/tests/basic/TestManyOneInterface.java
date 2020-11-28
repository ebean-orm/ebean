package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.interfaces.Address;
import org.tests.model.interfaces.IAddress;
import org.tests.model.interfaces.IPerson;
import org.tests.model.interfaces.Person;
import org.junit.Test;

public class TestManyOneInterface extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    IAddress a = new Address();

    IPerson p = new Person();

    p.setDefaultAddress(a);

    Ebean.save(a);
    Ebean.save(p);

    //Assert.assertTrue();

  }
}
