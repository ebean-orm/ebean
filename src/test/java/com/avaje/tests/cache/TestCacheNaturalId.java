package com.avaje.tests.cache;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheStatistics;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestCacheNaturalId extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    ServerCache contactCache = Ebean.getServerCacheManager().getBeanCache(Contact.class);

    List<Contact> list = Ebean.find(Contact.class).setLoadBeanCache(true).findList();

    Assert.assertTrue(contactCache.size() > 0);

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

    Assert.assertNotNull(c0);
    Assert.assertNotNull(c1);

    Assert.assertEquals(1, stats0.getHitCount());
    Assert.assertEquals(2, stats1.getHitCount());

    c1.setEmail("mychangedemail@what.com");
    Ebean.save(c1);

    Contact c2 = Ebean.find(Contact.class).where().eq("email", "mychangedemail@what.com")
        .findUnique();

    ServerCacheStatistics stats2 = contactCache.getStatistics(false);

    Assert.assertNotNull(c2);
    Assert.assertEquals(c2.getId(), c1.getId());
    Assert.assertEquals(c0.getId(), c1.getId());
    Assert.assertTrue(stats2.getHitCount() > stats1.getHitCount());

  }
}
