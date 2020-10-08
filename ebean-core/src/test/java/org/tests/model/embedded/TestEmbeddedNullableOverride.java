package org.tests.model.embedded;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestEmbeddedNullableOverride extends BaseTestCase {

  @Test
  public void test() {

    EPerson2 person = new EPerson2();
    person.setName("foo");

    EAddress address = new EAddress();
    address.setCity("myCity");
    address.setStatus(EAddressStatus.TWO);
    person.setAddress(address);

    DB.save(person);

    final EPerson2 found = DB.find(EPerson2.class, person.getId());

    assertNotNull(found);
    assertNotNull(found.getAddress());
    assertThat(found.getAddress().getCity()).isEqualTo("myCity");
    assertThat(found.getAddress().getStatus()).isEqualTo(EAddressStatus.TWO);
    assertThat(found.getAddress().getStreet()).isNull();
  }
}
