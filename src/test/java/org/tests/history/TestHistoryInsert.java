package org.tests.history;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
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

  @Test
  @ForPlatform({Platform.H2, Platform.POSTGRES})
  public void test() throws InterruptedException {

    User user = new User();
    user.setName("Jim");
    user.setEmail("one@email.com");
    user.setPasswordHash("someHash");

    Ebean.save(user);
    logger.info("-- initial save");

    Thread.sleep(DB_CLOCK_DELTA); // wait, so that our system clock can catch up
    Timestamp afterInsert = new Timestamp(System.currentTimeMillis());

    List<SqlRow> history = fetchHistory(user);
    assertThat(history).isEmpty();

    List<Version<User>> versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(1);

    user.setName("Jim v2");
    user.setPasswordHash("anotherHash");
    Thread.sleep(50); // wait, to ensure that whenModified differs
    logger.info("-- update v2");
    Ebean.save(user);

    history = fetchHistory(user);
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getString("name")).isEqualTo("Jim");

    versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(2);
    assertThat(versions.get(0).getDiff()).containsKeys("name", "version", "whenModified");

    user.setName("Jim v3");
    user.setEmail("three@email.com");
    Thread.sleep(50); // otherwise the timestamp of "whenModified" may not change

    logger.info("-- update v3");
    Ebean.save(user);

    history = fetchHistory(user);
    assertThat(history).hasSize(2);
    assertThat(history.get(1).getString("name")).isEqualTo("Jim v2");
    assertThat(history.get(1).getString("email")).isEqualTo("one@email.com");

    versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(3);
    assertThat(versions.get(0).getDiff()).containsKeys("name", "email", "version", "whenModified");

    logger.info("-- delete");
    Ebean.delete(user);

    User earlyVersion = Ebean.find(User.class).setId(user.getId()).asOf(afterInsert).findOne();
    assertThat(earlyVersion.getName()).isEqualTo("Jim");
    assertThat(earlyVersion.getEmail()).isEqualTo("one@email.com");

    Ebean.find(User.class).setId(user.getId()).asOf(afterInsert).findOne();

    logger.info("-- last fetchHistory");

    history = fetchHistory(user);
    assertThat(history).hasSize(3);
    assertThat(history.get(2).getString("name")).isEqualTo("Jim v3");
    assertThat(history.get(2).getString("email")).isEqualTo("three@email.com");

    versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(3);
  }

  @Test
  @ForPlatform({Platform.POSTGRES})
  public void test_singleTransaction_multipleHistory() {

    User user = new User();
    user.setName("First");
    user.setEmail("first@email.com");
    user.setPasswordHash("someHash");

    try (Transaction transaction = Ebean.beginTransaction()) {
      // insert and many updates inside transaction
      Ebean.save(user);
      user.setEmail("first2@email.com");
      Ebean.save(user);
      user.setEmail("first3@email.com");
      Ebean.save(user);
      user.setEmail("first4@email.com");
      Ebean.save(user);

      transaction.commit();
    }

    // a couple more updates outside of the first transaction
    user.setEmail("first5@email.com");
    Ebean.save(user);

    user.setEmail("first6@email.com");
    Ebean.save(user);

    List<SqlRow> sqlRows =
      Ebean.createSqlQuery("select lower(sys_period) lowerBound, upper(sys_period) upperBound from c_user_history where id = :id order by when_modified")
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
    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from c_user_history where id = :id order by when_modified");
    sqlQuery.setParameter("id", user.getId());
    return sqlQuery.findList();
  }
}
