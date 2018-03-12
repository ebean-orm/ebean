package org.example;

import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for Ebean Spring Module.
 * @since 18.05.2009
 * @author E Mc Greal
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/init-database.xml"})
public class EbeanSpringModuleTest {

  /** The Constant logger. */
  private final static Logger logger = LoggerFactory.getLogger(EbeanSpringModuleTest.class);

  /** The user service. */
  @Autowired
  private UserService userService;

  /**
   * Create the test case.
   */
  public EbeanSpringModuleTest() {
    super();
  }

  /**
   * Test app.
   */
  @Test
  public void testInactiveTransaction() {

    Transaction transaction = Ebean.beginTransaction();
    long id;
    try {
      User user = new User();
      user.setName("save with txn 1");
      Ebean.save(user);
      transaction.commit();
      id = user.getOid();
    } finally {
      // don't end the transaction ...
      //transaction.end();
    }

    Ebean.delete(User.class, id);
  }

  @Test
  public void testBatchInsert() {

    userService.batchInsert();
  }

  /**
   * Test app.
   */
  @Test
  public void testSaveUser() {
    logger.info("Saving new User...");
    User user = new User();
    user.setName("ebean");
    userService.save(user);
    logger.info("Saved new User");
  }

  /**
   * Test app.
   */
  @Test
  public void testFindUser() {
    logger.info("Finding User with OID = 1 ...");
    User user = userService.find(1);

    assertNotNull(user);
    assertTrue("ebean".equals(user.getName()));
    logger.info("Found User with OID = 1");
  }

  @Test
  public void testNonTransactional() {
    userService.nonTransactional();
    logger.info("nonTransactional done");
  }

  @Test
  public void testFindNonTransactional() {
    logger.info("Finding User with OID = 1 ...");
    User user = userService.find(1);
    User user2 = userService.findNoCurrentTransaction(2);
    logger.info("nonTransactional find user1:{} user2:{}", user, user2);
  }

  /**
   * Return the user service.
   */
  public UserService getUserService() {
    return userService;
  }


  /**
   * Sets the user service.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
