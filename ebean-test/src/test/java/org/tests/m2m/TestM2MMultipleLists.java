package org.tests.m2m;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.m2m.MailBox;
import org.tests.model.m2m.MailUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestM2MMultipleLists extends BaseTestCase {

  @Test
  public void test() {

    MailUser u = new MailUser();
    u.setName("mailUser1");

    MailBox m1 = new MailBox();
    m1.setName("mailBox1");

    u.getInbox().add(m1);
    DB.save(u);

    u = DB.find(MailUser.class, u.getId());

    assertEquals(1, u.getInbox().size());
    assertEquals(0, u.getOutbox().size());

  }
}
