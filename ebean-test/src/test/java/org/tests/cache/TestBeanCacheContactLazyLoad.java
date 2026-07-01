package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class testing a wrong behaviour of the bean cache.
 */
public class TestBeanCacheContactLazyLoad extends BaseTestCase {

  @BeforeAll
  static void before() {
    ResetBasicData.reset();
  }

  /**
   * This test shows a wrong behaviour of the bean cache up to at least Ebean 12.4.*:
   * <ul>
   *   <li>bean partially fetched via natural key, filling the cache</li>
   *   <li>bean modified using a setter</li>
   *   <li>getter called on non-loaded property to trigger lazy load</li>
   *   <li>bean fetched again using the same natural key, to hit cache</li>
   *   <li>it is expected, that the fetched bean does not contain the modification from before</li>
   * </ul>
   */
  @Test
  public void testBeanCacheWithLazyLoading() {
    final Customer customer = new Customer();
    customer.setName("Customer");
    customer.setAnniversary(Date.valueOf(LocalDate.of(2010, 1, 1)));
    DB.save(customer);

    final Contact contact = new Contact();
    contact.setFirstName("Tim");
    contact.setLastName("Button");
    contact.setPhone("1234567890");
    contact.setMobile("4567890123");
    contact.setEmail("tim@button.com");
    contact.setCustomer(customer);

    DB.save(contact);

    try {

      // Only get two properties, so we have to lazy-load later
      final Contact contactDb = DB.find(Contact.class).where().eq("email", "tim@button.com").select("email,lastName").findOne();
      assertThat(contactDb).isNotNull();
      LoggedSql.start();
      contactDb.setLastName("Buttonnnn");
      List<String> sql = LoggedSql.collect();
      assertThat(sql).isEmpty(); // setter did not trigger lazy load

      // trigger lazy load
      assertThat(contactDb.getPhone()).isEqualTo("1234567890");
      sql = LoggedSql.collect();
      assertThat(sql).isNotEmpty(); // Lazy-load took place

      final Contact contactDb2 = DB.find(Contact.class).where().eq("email", "tim@button.com").select("email,lastName").findOne();
      sql = LoggedSql.stop();
      assertThat(sql).isEmpty(); // We expect that the bean was loaded from cache
      assertThat(contactDb2).isNotNull();
      assertThat(contactDb2.getLastName()).isEqualTo("Button");
    } finally {
      DB.delete(customer);
      DB.find(Contact.class).where().eq("email", "tim@button.com").delete();
    }
  }

}
