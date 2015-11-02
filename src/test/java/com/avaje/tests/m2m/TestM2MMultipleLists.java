package com.avaje.tests.m2m;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.m2m.MailBox;
import com.avaje.tests.model.m2m.MailUser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestM2MMultipleLists extends BaseTestCase {

  @Test
  public void test() {

    MailUser u = new MailUser();
    u.setName("mailUser1");

    MailBox m1 = new MailBox();
    m1.setName("mailBox1");

    u.getInbox().add(m1);
    Ebean.save(u);

    u = Ebean.find(MailUser.class, u.getId());

    assertEquals(1, u.getInbox().size());
    assertEquals(0, u.getOutbox().size());

  }
}
