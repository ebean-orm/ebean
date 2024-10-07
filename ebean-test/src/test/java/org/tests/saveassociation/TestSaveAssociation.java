package org.tests.saveassociation;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import static org.assertj.core.api.Assertions.assertThat;

class TestSaveAssociation extends BaseTestCase {

  @Test
  void test() {

    TSMaster m0 = new TSMaster();
    m0.setName("master1");

    DB.save(m0);

    m0.addDetail(new TSDetail("master1 detail1"));
    m0.addDetail(new TSDetail("master1 detail2"));

    DB.save(m0);

    TSMaster m0Check = DB.find(TSMaster.class).fetch("details").where().idEq(m0.getId())
      .findOne();

    assertThat(m0Check.getDetails()).hasSize(2);
    DB.delete(m0);
  }


  @Test
  void testCascadeSetParent() {
    // setup
    TSDetail detail = new TSDetail("master1 detail1");
    DB.save(detail);

    // act
    TSMaster m0 = new TSMaster();
    m0.setName("master2");
    m0.addDetail(detail);
    DB.save(m0);

    // assert
    TSMaster check = DB.find(TSMaster.class).fetch("details")
      .where().idEq(m0.getId())
      .findOne();

    assertThat(check.getDetails()).hasSize(1);
    assertThat(check.getDetails().get(0).getId()).isEqualTo(detail.getId());
    DB.delete(m0);
  }

  @Test
  void testCascadeChangeParent() {
    // setup
    TSDetail detail = new TSDetail("master3 detail1");
    TSMaster m0 = new TSMaster();
    m0.setName("master3");
    m0.addDetail(detail);
    DB.save(m0);

    // act
    TSMaster m1 = new TSMaster();
    m1.setName("master4");
    m1.addDetail(detail);
    DB.save(m1);

    // assert
    TSMaster check = DB.find(TSMaster.class).fetch("details")
      .where().idEq(m1.getId())
      .findOne();

    assertThat(check.getDetails()).hasSize(1);
    assertThat(check.getDetails().get(0).getId()).isEqualTo(detail.getId());

    DB.delete(m1);
    DB.delete(TSMaster.class, m0.getId());
  }
}
