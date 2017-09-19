package org.tests.history;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.annotation.Platform;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import io.ebean.Version;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.config.dbplatform.DbDefaultValue;

import org.tests.model.converstation.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistoryInsert extends BaseTestCase {

  private final Logger logger = LoggerFactory.getLogger(TestHistoryInsert.class);

  @Test
  @IgnorePlatform(Platform.SQLSERVER) // RPr: I have issues with SQLServer in different timezone
  public void testClockSync() {
    Timestamp serverTime = getServerTime();
    Timestamp javaTime = new Timestamp(System.currentTimeMillis());
    
    assertThat(javaTime).isCloseTo(serverTime, 2*1000);
  }
  
  private Timestamp getServerTime() {
    DbDefaultValue deflt = server().getPluginApi().getDatabasePlatform().getDbDefaultValue();
    String query = "select " + deflt.convert(DbDefaultValue.NOW) + " as jetzt";
    if (isOracle()) {
      query = query + " from dual";
    } else if (isDb2()) {
      query = query + " from SYSIBM.SYSDUMMY1";
    }
    SqlQuery select = server().createSqlQuery(query);
    List<SqlRow> result = select.findList();
    SqlRow row = result.get(0);
    return row.getTimestamp("jetzt");
  }

  @Test
  @IgnorePlatform(Platform.SQLSERVER)
  public void testClockSyncJDBC() throws SQLException {
    DbDefaultValue deflt = server().getPluginApi().getDatabasePlatform().getDbDefaultValue();
    
    String sql = "select " + deflt.convert(DbDefaultValue.NOW) + " as jetzt";
    if (isOracle()) {
      sql = sql + " from dual";
    } else if (isDb2()) {
      sql = sql + " from SYSIBM.SYSDUMMY1";
    }
    Transaction txn = server().beginTransaction();
    try {
      Connection conn = txn.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sql);
      ResultSet rset = pstmt.executeQuery();
      rset.next();
            
      Timestamp serverTime = rset.getTimestamp(1);
      Timestamp javaTime = new Timestamp(System.currentTimeMillis());
      
      assertThat(javaTime).isCloseTo(serverTime, 2*1000);
    } finally {
      txn.end();
    }
  }

  
  
  @Test
  @ForPlatform({Platform.H2, Platform.POSTGRES, Platform.SQLSERVER})
  public void test() throws InterruptedException {
   
    User user = new User();
    user.setName("Jim");
    user.setEmail("one@email.com");
    user.setPasswordHash("someHash");

    Ebean.save(user);
    logger.info("-- initial save");

    Thread.sleep(100);
    Timestamp afterInsert = getServerTime();
    Thread.sleep(100);
    
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

  /**
   * Use SqlQuery to query the history table directly.
   */
  private List<SqlRow> fetchHistory(User user) {
    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from c_user_history where id = :id order by when_modified");
    sqlQuery.setParameter("id", user.getId());
    return sqlQuery.findList();
  }
}
