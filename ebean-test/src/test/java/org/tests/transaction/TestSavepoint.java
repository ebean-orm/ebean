package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;
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
    DB.save(basicVer);

    try (Transaction transaction = DB.beginTransaction()) {

      Connection connection = transaction.connection();
      Savepoint savepoint = connection.setSavepoint();

      basicVer.setOther("changeOther");
      DB.save(basicVer);

      connection.rollback(savepoint);
      transaction.commit();
    }

    EBasicVer found = DB.find(EBasicVer.class, basicVer.getId());
    assertThat(found.getOther()).isEqualTo("other1");

  }

  @ForPlatform({Platform.POSTGRES, Platform.H2})
  @Test
  public void rollbackOnly_expect_similarResult() {

    EBasicVer basicVer = new EBasicVer("save2");
    basicVer.setOther("other2");
    DB.save(basicVer);

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setRollbackOnly();

      basicVer.setOther("changeOther");
      DB.save(basicVer);

      transaction.commit();
    }

    EBasicVer found = DB.find(EBasicVer.class, basicVer.getId());
    assertThat(found.getOther()).isEqualTo("other2");

  }
}
