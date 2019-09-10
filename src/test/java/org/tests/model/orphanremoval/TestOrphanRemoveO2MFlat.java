package org.tests.model.orphanremoval;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrphanRemoveO2MFlat extends BaseTestCase {

  @Test
  public void testCacheUse() {

    // add test data first
    OrpMaster2 m0 = new OrpMaster2("m2", "master2");
    m0.getDetails().add(new OrpDetail2("d21", "d1", "m2"));
    m0.getDetails().add(new OrpDetail2("d22", "d2", "m2"));

    Ebean.save(m0);

    OrpMaster2 m1 = Ebean.find(OrpMaster2.class, "m2");
    m1.getDetails().size();

    m1.getDetails().clear();
    m1.getDetails().add(new OrpDetail2("d23", "d3", "m2"));
    Ebean.save(m1);

    m1 = Ebean.find(OrpMaster2.class, "m2");
    // Expect only one.
    assertThat(m1.getDetails()).hasSize(1);
    assertThat(m1.getDetails()).extracting("id").contains("d23");

    m1.getDetails().clear();
    m1.getDetails().add(new OrpDetail2("d24", "d4", "m2"));
    m1.getDetails().add(new OrpDetail2("d25", "d5", "m2"));
    Ebean.save(m1);

    m1 = Ebean.find(OrpMaster2.class, "m2");
    assertThat(m1.getDetails()).hasSize(2);
    assertThat(m1.getDetails()).extracting("id").contains("d24", "d25");


    m1 = Ebean.find(OrpMaster2.class)
      .setId("m2")
      .setUseCache(false)
      .findOne();

    // Expect only one.
    assertThat(m1.getDetails()).hasSize(2);
    assertThat(m1.getDetails()).extracting("id").contains("d24", "d25");
  }


}
