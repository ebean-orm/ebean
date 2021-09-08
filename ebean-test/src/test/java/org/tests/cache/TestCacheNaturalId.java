package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCacheNaturalId extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    ServerCache contactCache = DB.cacheManager().beanCache(Contact.class);

    List<Contact> list = DB.find(Contact.class).setBeanCacheMode(CacheMode.PUT).findList();

    assertTrue(contactCache.size() > 0);

    String emailToSearch = null;
    for (Contact contact : list) {
      if (contact.getEmail() != null) {
        emailToSearch = contact.getEmail();
        break;
      }
    }

    contactCache.statistics(true);

    Contact c0 = DB.find(Contact.class).where().eq("email", emailToSearch).findOne();

    ServerCacheStatistics stats0 = contactCache.statistics(false);

    Contact c1 = DB.find(Contact.class).where().eq("email", emailToSearch).findOne();

    ServerCacheStatistics stats1 = contactCache.statistics(false);

    assertNotNull(c0);
    assertNotNull(c1);

    assertEquals(1, stats0.getHitCount());
    assertEquals(2, stats1.getHitCount());

    c1.setEmail("mychangedemail@what.com");
    DB.save(c1);
    awaitL2Cache();

    Contact c2 = DB.find(Contact.class).where().eq("email", "mychangedemail@what.com")
      .findOne();

    ServerCacheStatistics stats2 = contactCache.statistics(false);

    assertNotNull(c2);
    assertEquals(c2.getId(), c1.getId());
    assertEquals(c0.getId(), c1.getId());
    assertTrue(stats2.getHitCount() > stats1.getHitCount());

  }
}
