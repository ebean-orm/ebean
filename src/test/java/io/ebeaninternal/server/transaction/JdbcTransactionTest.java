package io.ebeaninternal.server.transaction;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcTransactionTest {

  @Test
  public void isSkipCache() {

    try (Transaction transaction = DB.beginTransaction()) {
      // implicitly skip false due to query only at this point
      assertThat(transaction.isSkipCache()).isFalse();

      EBasic basic = new EBasic("b1");
      DB.save(basic);

      // implicitly skip true due to save
      assertThat(transaction.isSkipCache()).isTrue();

      Customer.find.byId(1);

      // explicit control
      transaction.setSkipCache(false);
      assertThat(transaction.isSkipCache()).isFalse();

      transaction.setSkipCache(true);
      assertThat(transaction.isSkipCache()).isTrue();
    }
  }

  @Test
  public void skipCacheAfterSave() {

    ServerCacheManager cacheManager = DB.getDefault().getServerCacheManager();
    ServerCache customerBeanCache = cacheManager.getBeanCache(Customer.class);
    ServerCache contactNatKeyCache = cacheManager.getNaturalKeyCache(Contact.class);

    try (Transaction transaction = DB.beginTransaction()) {
      customerBeanCache.getStatistics(true);
      contactNatKeyCache.getStatistics(true);

      Customer.find.byId(19898989);
      DB.find(Contact.class).where().eq("email", "junk@foo.com").findOne();

      assertThat(customerBeanCache.getStatistics(true).getMissCount()).isEqualTo(1);
      assertThat(contactNatKeyCache.getStatistics(true).getMissCount()).isEqualTo(1);

      EBasic basic = new EBasic("b1");
      DB.save(basic);

      // these don't hit L2 cache due to the save of b1
      Customer.find.byId(29898989);
      DB.find(Contact.class).where().eq("email", "junk2@foo.com").findOne();

      assertThat(customerBeanCache.getStatistics(true).getMissCount()).isEqualTo(0);
      assertThat(contactNatKeyCache.getStatistics(true).getMissCount()).isEqualTo(0);
    }
  }

}
