package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestCacheNaturalId extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    ServerCache contactCache = Ebean.getServerCacheManager().getBeanCache(Contact.class);

    List<Contact> list = Ebean.find(Contact.class).setBeanCacheMode(CacheMode.PUT).findList();

    assertTrue(contactCache.size() > 0);

    String emailToSearch = null;
    for (Contact contact : list) {
      if (contact.getEmail() != null) {
        emailToSearch = contact.getEmail();
        break;
      }
    }

    contactCache.getStatistics(true);

    Contact c0 = Ebean.find(Contact.class).where().eq("email", emailToSearch).findOne();

    ServerCacheStatistics stats0 = contactCache.getStatistics(false);

    Contact c1 = Ebean.find(Contact.class).where().eq("email", emailToSearch).findOne();

    ServerCacheStatistics stats1 = contactCache.getStatistics(false);

    assertNotNull(c0);
    assertNotNull(c1);

    assertEquals(1, stats0.getHitCount());
    assertEquals(2, stats1.getHitCount());

    c1.setEmail("mychangedemail@what.com");
    Ebean.save(c1);
    awaitL2Cache();

    Contact c2 = Ebean.find(Contact.class).where().eq("email", "mychangedemail@what.com")
      .findOne();

    ServerCacheStatistics stats2 = contactCache.getStatistics(false);

    assertNotNull(c2);
    assertEquals(c2.getId(), c1.getId());
    assertEquals(c0.getId(), c1.getId());
    assertTrue(stats2.getHitCount() > stats1.getHitCount());

  }
}
