package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSqlUpdateExceptions extends BaseTestCase {

  private String sql = "insert into uuone (id, name, version) values (?,?,?)";

  @Test
  public void simpleInsert() {

    UUID id = UUID.randomUUID();

    Ebean.createSqlUpdate(sql)
      .setParams(id, "hi", 1)
      .executeNow();

    UUID foundId = Ebean.createSqlQuery("select id from uuone where id = ?")
      .setParams(id)
      .findSingleAttribute(UUID.class);

    assertThat(foundId).isEqualTo(id);
  }

  @Test(expected = DuplicateKeyException.class)
  public void duplicateKey() {

    UUID id = UUID.randomUUID();

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "hi");
    sqlUpdate.setParameter(3, 1);
    sqlUpdate.execute();

    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "fail");
    sqlUpdate.setParameter(3, 1);
    sqlUpdate.execute();
  }

  @Test(expected = DuplicateKeyException.class)
  public void duplicateKey_executeNow() {


    UUID id = UUID.randomUUID();

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "hi");
    sqlUpdate.setParameter(3, 1);
    sqlUpdate.executeNow();

    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "fail");
    sqlUpdate.setParameter(3, 1);
    sqlUpdate.executeNow();
  }

  @Test(expected = DuplicateKeyException.class)
  public void duplicateKey_inBatch() {

    UUID id = UUID.randomUUID();

    try (Transaction transaction = Ebean.beginTransaction()) {

      SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
      sqlUpdate.setParameter(1, id);
      sqlUpdate.setParameter(2, "hi in batch");
      sqlUpdate.setParameter(3, 1);
      sqlUpdate.addBatch();

      sqlUpdate.setParameter(1, id);
      sqlUpdate.setParameter(2, "fail in batch");
      sqlUpdate.setParameter(3, 1);
      sqlUpdate.addBatch();

      transaction.commit();
    }
  }

  @Test(expected = DuplicateKeyException.class)
  public void duplicateKey_executeBatch() {

    UUID id = UUID.randomUUID();

    try (Transaction transaction = Ebean.beginTransaction()) {

      SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
      sqlUpdate.setParameter(1, id);
      sqlUpdate.setParameter(2, "hi in batch");
      sqlUpdate.setParameter(3, 1);
      sqlUpdate.addBatch();

      sqlUpdate.setParameter(1, id);
      sqlUpdate.setParameter(2, "fail in batch");
      sqlUpdate.setParameter(3, 1);
      sqlUpdate.addBatch();
      int[] ints = sqlUpdate.executeBatch();

      transaction.commit();
    }
  }
}
