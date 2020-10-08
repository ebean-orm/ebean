package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

public class TestOneToOneJoinColumn extends BaseTestCase {

  @Test
  public void test() {

    ORoadShowMsg msg = new ORoadShowMsg();
    OCompany company = new OCompany();
    company.setCorpId("corp_id_1000000");
    msg.setCompany(company);

    Ebean.save(msg);
    Ebean.find(ORoadShowMsg.class, msg.getId());
  }
}
