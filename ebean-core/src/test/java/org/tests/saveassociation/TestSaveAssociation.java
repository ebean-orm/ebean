package org.tests.saveassociation;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSaveAssociation extends BaseTestCase {

  @Test
  public void test() {

    TSMaster m0 = new TSMaster();
    m0.setName("master1");

    Ebean.save(m0);

    m0.addDetail(new TSDetail("master1 detail1"));
    m0.addDetail(new TSDetail("master1 detail2"));

    Ebean.save(m0);

    TSMaster m0Check = Ebean.find(TSMaster.class).fetch("details").where().idEq(m0.getId())
      .findOne();

    assertEquals(2, m0Check.getDetails().size());
  }

}
