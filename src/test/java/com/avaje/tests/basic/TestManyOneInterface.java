package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.ResetBasicData;
import com.avaje.tests.model.interfaces.Address;
import com.avaje.tests.model.interfaces.IAddress;
import com.avaje.tests.model.interfaces.IPerson;
import com.avaje.tests.model.interfaces.Person;
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
