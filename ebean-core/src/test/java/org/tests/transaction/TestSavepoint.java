package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSavepoint extends BaseTestCase {

  @ForPlatform({Platform.POSTGRES, Platform.H2})
  @Test
  public void test() throws SQLException {

    EBasicVer basicVer = new EBasicVer("save1");
    basicVer.setOther("other1");
    Ebean.save(basicVer);

    try (Transaction transaction = Ebean.beginTransaction()) {

      Connection connection = transaction.getConnection();
      Savepoint savepoint = connection.setSavepoint();

      basicVer.setOther("changeOther");
      Ebean.save(basicVer);

      connection.rollback(savepoint);
      transaction.commit();
    }

    EBasicVer found = Ebean.find(EBasicVer.class, basicVer.getId());
    assertThat(found.getOther()).isEqualTo("other1");

  }

  @ForPlatform({Platform.POSTGRES, Platform.H2})
  @Test
  public void rollbackOnly_expect_similarResult() {

    EBasicVer basicVer = new EBasicVer("save2");
    basicVer.setOther("other2");
    Ebean.save(basicVer);

    try (Transaction transaction = Ebean.beginTransaction()) {
      transaction.setRollbackOnly();

      basicVer.setOther("changeOther");
      Ebean.save(basicVer);

      transaction.commit();
    }

    EBasicVer found = Ebean.find(EBasicVer.class, basicVer.getId());
    assertThat(found.getOther()).isEqualTo("other2");

  }
}
