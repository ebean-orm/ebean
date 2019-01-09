package org.tests.model.embedded;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Property;
import org.junit.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestEmbeddedManyToOne extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    BeanType<EAddr> embType = Ebean.getDefaultServer().getPluginApi().getBeanType(EAddr.class);

    Country nz = Ebean.getReference(Country.class, "NZ");

    EAddr addr = new EAddr("Foo", "Bar", nz);
    EPerAddr perAddr = new EPerAddr("Embed", addr);

    Property country = embType.getProperty("country");
    Object val = country.getVal(addr);

    assertThat(val).isSameAs(nz);

    Ebean.save(perAddr);

    EPerAddr found = Ebean.find(EPerAddr.class, perAddr.getId());

    assertThat(found.getAddress().getCountry().getCode()).isEqualTo("NZ");
    assertThat(found.getAddress().getCountry().getName()).startsWith("New");

  }
}
