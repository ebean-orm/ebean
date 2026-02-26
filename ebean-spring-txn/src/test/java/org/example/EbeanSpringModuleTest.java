package org.example;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.PersistenceContextAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for Ebean Spring Module.
 *
 * @author E Mc Greal
 * @since 18.05.2009
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"/init-database.xml"})
public class EbeanSpringModuleTest {

  /**
   * The Constant logger.
   */
  private final static Logger logger = LoggerFactory.getLogger(EbeanSpringModuleTest.class);

  /**
   * The user service.
   */
  @Autowired
  private UserService userService;

  /**
   * Create the test case.
   */
  public EbeanSpringModuleTest() {
    super();
  }

  @Transactional
  @Rollback
  @Test
  public void testWithRollback() {
    // setup
    User user = new User();
    user.setName("rollback1");
    DB.save(user);

    // this loads the user into the [transaction scoped] persistence context
    User found = DB.find(User.class, user.getOid());
    found.setName("mutated");

    PersistenceContextAccess.clear();

    userService.insideTestRollback(user.getOid());
  }

  /**
   * Test app.
   */
  @Test
  public void testInactiveTransaction() {

    Transaction transaction = DB.beginTransaction();
    long id;
    try {
      User user = new User();
      user.setName("save with txn 1");
      DB.save(user);
      transaction.commit();
      id = user.getOid();
    } finally {
      // don't end the transaction ...
      //transaction.end();
    }

    DB.delete(User.class, id);
  }

  @Test
  public void testBatchInsert() {
    userService.batchInsert();
  }

  @Test
  public void testRequiresNew() {
    userService.requiresNew();
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
    assertThat(user.getOid()).isGreaterThan(0);

    final User found = DB.find(User.class, user.getOid());
    assertThat(found).isNotNull();
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
