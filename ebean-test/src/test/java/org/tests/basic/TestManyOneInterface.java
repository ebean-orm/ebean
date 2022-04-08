package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.interfaces.Address;
import org.tests.model.interfaces.ExtPerson1and2;
import org.tests.model.interfaces.IAddress;
import org.tests.model.interfaces.IExtPerson1;
import org.tests.model.interfaces.IExtPerson2;
import org.tests.model.interfaces.IPerson;
import org.tests.model.interfaces.Person;

import static org.assertj.core.api.Assertions.assertThat;

public class TestManyOneInterface extends BaseTestCase {

  @Test
  public void testEntityOverrideEntityImplements() {
    ResetBasicData.reset();

    IAddress a = DB.getDefault().createEntityBean(IAddress.class);
    assertThat(a).isInstanceOf(Address.class);

    IPerson p = DB.getDefault().createEntityBean(Person.class);
    assertThat(p).isInstanceOf(ExtPerson1and2.class);
    p = DB.getDefault().pluginApi().createEntityBean(IPerson.class);
    assertThat(p).isInstanceOf(ExtPerson1and2.class);

    p.setDefaultAddress(a);

    IAddress ea1 = DB.getDefault().createEntityBean(IAddress.class);
    IAddress ea2 = DB.getDefault().createEntityBean(IAddress.class);

    p.getExtraAddresses().add(ea1);
    p.getExtraAddresses().add(ea2);

    DB.save(p);

    IAddress a2 = DB.find(IAddress.class, a.getOid());
    IPerson p2 = DB.find(IPerson.class, p.getOid());

    assertThat(a2).isInstanceOf(Address.class);
    assertThat(p2).isNotNull().isInstanceOf(ExtPerson1and2.class);
    assertThat(p2.getDefaultAddress()).isInstanceOf(Address.class);

    // some more checks
    IExtPerson1 pe1 = DB.getDefault().pluginApi().createEntityBean(IExtPerson1.class);
    IExtPerson2 pe2 = DB.getDefault().pluginApi().createEntityBean(IExtPerson2.class);
    assertThat(pe1).isInstanceOf(ExtPerson1and2.class);
    assertThat(pe2).isInstanceOf(ExtPerson1and2.class);

  }
}
