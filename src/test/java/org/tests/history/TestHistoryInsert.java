package org.tests.history;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import io.ebean.Version;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;

import org.tests.model.converstation.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistoryInsert extends BaseTestCase {

  private final Logger logger = LoggerFactory.getLogger(TestHistoryInsert.class);

  /**
   * Looks like we MUST use useLegacyDatetimeCode=false ... in order for
   * the correct server timezone to be honored by the MariaDB JDBC driver.
   */
  @Test
  @ForPlatform({Platform.MARIADB})
  public void mariadb_simple_history() {

    Timestamp t0 = new Timestamp(System.currentTimeMillis());
    littleSleep();

    User user = new User();
    user.setName("Jim");
    user.setEmail("one@email.com");
    user.setPasswordHash("someHash");
    DB.save(user);
    Timestamp t1 = new Timestamp(System.currentTimeMillis());

    littleSleep();
    user.setName("NotJim");
    user.save();
    Timestamp t2 = new Timestamp(System.currentTimeMillis());

    littleSleep();
    user.setName("NotJimV2");
    user.setEmail("two@email.com");
    user.save();
    Timestamp t3 = new Timestamp(System.currentTimeMillis());

    List<Version<User>> versions = DB.find(User.class).setId(user.getId()).findVersionsBetween(t0, t3);
    assertThat(versions).hasSize(3);

    // use useLegacyDatetimeCode=false with MariaDB JDBC driver
    final User user0 = DB.find(User.class).setId(user.getId()).asOf(t0).findOne();
    final User user1 = DB.find(User.class).setId(user.getId()).asOf(t1).findOne();
    final User user2 = DB.find(User.class).setId(user.getId()).asOf(t2).findOne();
    final User user3 = DB.find(User.class).setId(user.getId()).asOf(t3).findOne();

    assertThat(user1.getName()).isEqualTo("Jim");
    assertThat(user1.getEmail()).isEqualTo("one@email.com");
    assertThat(user2.getName()).isEqualTo("NotJim");
    assertThat(user2.getEmail()).isEqualTo("one@email.com");
    assertThat(user3.getName()).isEqualTo("NotJimV2");
    assertThat(user3.getEmail()).isEqualTo("two@email.com");
    assertThat(user0).isNull();
  }

  private void littleSleep() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @ForPlatform({Platform.H2, Platform.POSTGRES})
  public void test() throws InterruptedException {

    User user = new User();
    user.setName("Jim");
    user.setEmail("one@email.com");
    user.setPasswordHash("someHash");

    DB.save(user);
    logger.info("-- initial save");

    Thread.sleep(DB_CLOCK_DELTA); // wait, so that our system clock can catch up
    Timestamp afterInsert = new Timestamp(System.currentTimeMillis());

    List<SqlRow> history = fetchHistory(user);
    assertThat(history).isEmpty();

    List<Version<User>> versions = DB.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(1);

    user.setName("Jim v2");
    user.setPasswordHash("anotherHash");
    Thread.sleep(50); // wait, to ensure that whenModified differs
    logger.info("-- update v2");
    DB.save(user);

    history = fetchHistory(user);
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getString("name")).isEqualTo("Jim");

    versions = DB.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(2);
    assertThat(versions.get(0).getDiff()).containsKeys("name", "version", "whenModified");

    user.setName("Jim v3");
    user.setEmail("three@email.com");
    Thread.sleep(50); // otherwise the timestamp of "whenModified" may not change

    logger.info("-- update v3");
    DB.save(user);

    history = fetchHistory(user);
    assertThat(history).hasSize(2);
    assertThat(history.get(1).getString("name")).isEqualTo("Jim v2");
    assertThat(history.get(1).getString("email")).isEqualTo("one@email.com");

    versions = DB.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(3);
    assertThat(versions.get(0).getDiff()).containsKeys("name", "email", "version", "whenModified");

    logger.info("-- delete");
    DB.delete(user);

    User earlyVersion = DB.find(User.class).setId(user.getId()).asOf(afterInsert).findOne();
    assertThat(earlyVersion.getName()).isEqualTo("Jim");
    assertThat(earlyVersion.getEmail()).isEqualTo("one@email.com");

    DB.find(User.class).setId(user.getId()).asOf(afterInsert).findOne();

    logger.info("-- last fetchHistory");

    history = fetchHistory(user);
    assertThat(history).hasSize(3);
    assertThat(history.get(2).getString("name")).isEqualTo("Jim v3");
    assertThat(history.get(2).getString("email")).isEqualTo("three@email.com");

    versions = DB.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(3);
  }

  @Test
  @ForPlatform({Platform.POSTGRES})
  public void test_singleTransaction_multipleHistory() {

    User user = new User();
    user.setName("First");
    user.setEmail("first@email.com");
    user.setPasswordHash("someHash");

    try (Transaction transaction = DB.beginTransaction()) {
      // insert and many updates inside transaction
      DB.save(user);
      user.setEmail("first2@email.com");
      DB.save(user);
      user.setEmail("first3@email.com");
      DB.save(user);
      user.setEmail("first4@email.com");
      DB.save(user);

      transaction.commit();
    }

    // a couple more updates outside of the first transaction
    user.setEmail("first5@email.com");
    DB.save(user);

    user.setEmail("first6@email.com");
    DB.save(user);

    List<SqlRow> sqlRows =
      DB.sqlQuery("select lower(sys_period) lowerBound, upper(sys_period) upperBound from c_user_history where id = :id order by when_modified")
        .setParameter("id", user.getId())
        .findList();

    Timestamp previousUpper = null;

    for (SqlRow sqlRow : sqlRows) {
      Timestamp nextLower = sqlRow.getTimestamp("lowerBound");
      Timestamp nextUpper = sqlRow.getTimestamp("upperBound");
      if (previousUpper != null) {
        assertThat(previousUpper).isEqualTo(nextLower);
      }
      previousUpper = nextUpper;
    }
  }

  /**
   * Use SqlQuery to query the history table directly.
   */
  private List<SqlRow> fetchHistory(User user) {
    SqlQuery sqlQuery = DB.sqlQuery("select * from c_user_history where id = :id order by when_modified");
    sqlQuery.setParameter("id", user.getId());
    return sqlQuery.findList();
  }
}
