package com.avaje.tests.history;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Version;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.converstation.User;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistoryInsert extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer defaultServer = (SpiEbeanServer)Ebean.getDefaultServer();
    if (!"h2".equals(defaultServer.getDatabasePlatform().getName())) {
      // Oracle for example uses total recall so we can select the explicit
      // history tables as we do in this test
      return;
    }

    User user = new User();
    user.setName("Jim");
    user.setEmail("one@email.com");

    Ebean.save(user);

    Timestamp afterInsert = new Timestamp(System.currentTimeMillis());

    List<SqlRow> history = fetchHistory(user);
    assertThat(history).isEmpty();

    List<Version<User>> versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(1);

    user.setName("Jim v2");
    Ebean.save(user);

    history = fetchHistory(user);
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getString("name")).isEqualTo("Jim");

    versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(2);
    assertThat(versions.get(0).getDiff()).containsKeys("name", "version", "whenModified");

    user.setName("Jim v3");
    user.setEmail("three@email.com");
    Ebean.save(user);

    history = fetchHistory(user);
    assertThat(history).hasSize(2);
    assertThat(history.get(1).getString("name")).isEqualTo("Jim v2");
    assertThat(history.get(1).getString("email")).isEqualTo("one@email.com");

    versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(3);
    assertThat(versions.get(0).getDiff()).containsKeys("name", "email", "version", "whenModified");

    Ebean.delete(user);

    User earlyVersion = Ebean.find(User.class).setId(user.getId()).asOf(afterInsert).findUnique();
    assertThat(earlyVersion.getName()).isEqualTo("Jim");
    assertThat(earlyVersion.getEmail()).isEqualTo("one@email.com");

    Ebean.find(User.class).setId(user.getId()).asOf(afterInsert).findUnique();


    history = fetchHistory(user);
    assertThat(history).hasSize(3);
    assertThat(history.get(2).getString("name")).isEqualTo("Jim v3");
    assertThat(history.get(2).getString("email")).isEqualTo("three@email.com");

    versions = Ebean.find(User.class).setId(user.getId()).findVersions();
    assertThat(versions).hasSize(3);
  }

  /**
   * Use SqlQuery to query the history table directly.
   */
  private List<SqlRow> fetchHistory(User user) {
    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from c_user_history where id = :id order by sys_period_start");
    sqlQuery.setParameter("id", user.getId());
    return sqlQuery.findList();
  }
}
