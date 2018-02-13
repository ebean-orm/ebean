package org.tests.embedded;

import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.embedded.EAddress;
import org.tests.model.embedded.EAddressStatus;
import org.tests.model.embedded.EPerson;

public class TestEmbeddedEmpty {

  @Test
  public void insertEmptyEmbedded() {

    EPerson person = new EPerson();
    person.setName("with empty embedded");

    // set empty embedded bean
    person.setAddress(new EAddress());

    Ebean.save(person);

    // treat all embedded properties as loaded
    person.getAddress().getCity();
  }

  @Test
  public void testAbstractEnumBinding() {

    EAddressStatus.TWO.doIt();

    Ebean.find(EPerson.class)
      .where().eq("address.status", EAddressStatus.TWO)
      .findList();

  }
}
