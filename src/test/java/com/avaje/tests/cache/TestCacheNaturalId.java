package com.avaje.tests.cache;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheStatistics;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TestCacheNaturalId extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    ServerCache contactCache = Ebean.getServerCacheManager().getBeanCache(Contact.class);

    List<Contact> list = Ebean.find(Contact.class).setLoadBeanCache(true).findList();

    assertTrue(contactCache.size() > 0);

    String emailToSearch = null;
    for (Contact contact : list) {
      if (contact.getEmail() != null) {
        emailToSearch = contact.getEmail();
        break;
      }
    }

    contactCache.getStatistics(true);

    Contact c0 = Ebean.find(Contact.class).where().eq("email", emailToSearch).findUnique();

    ServerCacheStatistics stats0 = contactCache.getStatistics(false);

    Contact c1 = Ebean.find(Contact.class).where().eq("email", emailToSearch).findUnique();

    ServerCacheStatistics stats1 = contactCache.getStatistics(false);

    assertNotNull(c0);
    assertNotNull(c1);

    assertEquals(1, stats0.getHitCount());
    assertEquals(2, stats1.getHitCount());

    c1.setEmail("mychangedemail@what.com");
    Ebean.save(c1);
    awaitL2Cache();

    Contact c2 = Ebean.find(Contact.class).where().eq("email", "mychangedemail@what.com")
      .findUnique();

    ServerCacheStatistics stats2 = contactCache.getStatistics(false);

    assertNotNull(c2);
    assertEquals(c2.getId(), c1.getId());
    assertEquals(c0.getId(), c1.getId());
    assertTrue(stats2.getHitCount() > stats1.getHitCount());

  }
}
