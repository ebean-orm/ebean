package org.tests.inheritance.cache;


import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.cache.CInhOne;
import org.tests.model.basic.cache.CInhRoot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritanceCache extends BaseTestCase {

  @Test
  public void test() {

    CInhOne one = new CInhOne();
    one.setLicenseNumber("O12");
    one.setDriver("Jimmy");
    one.setNotes("Hello");

    DB.save(one);

    CInhRoot gotOne = DB.find(CInhRoot.class)
      .setId(one.getId())
      .findOne();

    assertThat(gotOne).isInstanceOf(CInhOne.class);

    CInhRoot gotOneFromCache = DB.find(CInhRoot.class)
      .setId(one.getId())
      .findOne();

    assertThat(gotOneFromCache).isInstanceOf(CInhOne.class);

    CInhRoot refOne = DB.getReference(CInhRoot.class, one.getId());
    assertThat(refOne).isInstanceOf(CInhOne.class);

    CInhRoot refOneSub = DB.getReference(CInhOne.class, one.getId());
    assertThat(refOneSub).isNotNull();
  }
}
