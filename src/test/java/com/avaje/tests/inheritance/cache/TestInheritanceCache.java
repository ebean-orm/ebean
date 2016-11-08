package com.avaje.tests.inheritance.cache;


import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.cache.CInhOne;
import com.avaje.tests.model.basic.cache.CInhRoot;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestInheritanceCache extends BaseTestCase {

  @Test
  public void test() {

    CInhOne one = new CInhOne();
    one.setLicenseNumber("O12");
    one.setDriver("Jimmy");
    one.setNotes("Hello");

    Ebean.save(one);

    CInhRoot gotOne = Ebean.find(CInhRoot.class)
      .setId(one.getId())
      .findUnique();

    assertThat(gotOne).isInstanceOf(CInhOne.class);

    CInhRoot gotOneFromCache = Ebean.find(CInhRoot.class)
      .setId(one.getId())
      .findUnique();

    assertThat(gotOneFromCache).isInstanceOf(CInhOne.class);

    CInhRoot refOne = Ebean.getReference(CInhRoot.class, one.getId());
    assertThat(refOne).isInstanceOf(CInhOne.class);

    CInhRoot refOneSub = Ebean.getReference(CInhOne.class, one.getId());
    assertThat(refOneSub).isNotNull();
  }
}
