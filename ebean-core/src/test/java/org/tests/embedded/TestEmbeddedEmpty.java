package org.tests.embedded;

import io.ebean.DB;
import org.junit.Test;
import org.tests.model.embedded.EAddress;
import org.tests.model.embedded.EAddressStatus;
import org.tests.model.embedded.EPerson3;

public class TestEmbeddedEmpty {

  @Test
  public void insertEmptyEmbedded() {

    EPerson3 person = new EPerson3();
    person.setName("with empty embedded");

    person.setAddress(new EAddress());
    DB.save(person);

    // treat all embedded properties as loaded
    person.getAddress().getCity();
  }

  @Test
  public void testAbstractEnumBinding() {

    EAddressStatus.TWO.doIt();

    DB.find(EPerson3.class)
      .where().eq("address.status", EAddressStatus.TWO)
      .findList();

  }
}
