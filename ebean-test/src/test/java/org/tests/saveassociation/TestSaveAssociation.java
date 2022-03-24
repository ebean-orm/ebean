package org.tests.saveassociation;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSaveAssociation extends BaseTestCase {

  @Test
  public void test() {

    TSMaster m0 = new TSMaster();
    m0.setName("master1");

    DB.save(m0);

    m0.addDetail(new TSDetail("master1 detail1"));
    m0.addDetail(new TSDetail("master1 detail2"));

    DB.save(m0);

    TSMaster m0Check = DB.find(TSMaster.class).fetch("details").where().idEq(m0.getId())
      .findOne();

    assertEquals(2, m0Check.getDetails().size());
  }

}
