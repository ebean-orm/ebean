package org.tests.update;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSqlUpdateExceptions extends BaseTestCase {

  private String sql = "insert into uuone (id, name, version) values (?,?,?)";

  @Test
  public void simpleInsert() {

    UUID id = UUID.randomUUID();

    DB.sqlUpdate(sql)
      .setParameters(id, "hi", 1)
      .executeNow();

    UUID foundId = DB.sqlQuery("select id from uuone where id = ?")
      .setParameters(id)
      .mapToScalar(UUID.class)
      .findOne();

    assertThat(foundId).isEqualTo(id);
  }

  @Test
  public void duplicateKey() {
    UUID id = UUID.randomUUID();

    SqlUpdate sqlUpdate = DB.sqlUpdate(sql);
    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "hi");
    sqlUpdate.setParameter(3, 1);
    sqlUpdate.execute();

    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "fail");
    sqlUpdate.setParameter(3, 1);
    assertThrows(DuplicateKeyException.class, sqlUpdate::execute);
  }

  @Test
  public void duplicateKey_executeNow() {
    UUID id = UUID.randomUUID();

    SqlUpdate sqlUpdate = DB.sqlUpdate(sql);
    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "hi");
    sqlUpdate.setParameter(3, 1);
    sqlUpdate.executeNow();

    sqlUpdate.setParameter(1, id);
    sqlUpdate.setParameter(2, "fail");
    sqlUpdate.setParameter(3, 1);
    assertThrows(DuplicateKeyException.class, sqlUpdate::executeNow);
  }

  @Test
  public void duplicateKey_inBatch() {
    UUID id = UUID.randomUUID();
    assertThrows(DuplicateKeyException.class, () -> {
      try (Transaction transaction = DB.beginTransaction()) {

        SqlUpdate sqlUpdate = DB.sqlUpdate(sql);
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
    });
  }

  @Test
  public void duplicateKey_executeBatch() {
    UUID id = UUID.randomUUID();
    assertThrows(DuplicateKeyException.class, () -> {
      try (Transaction transaction = DB.beginTransaction()) {
        SqlUpdate sqlUpdate = DB.sqlUpdate(sql);
        sqlUpdate.setParameter(1, id);
        sqlUpdate.setParameter(2, "hi in batch");
        sqlUpdate.setParameter(3, 1);
        sqlUpdate.addBatch();

        sqlUpdate.setParameter(1, id);
        sqlUpdate.setParameter(2, "fail in batch");
        sqlUpdate.setParameter(3, 1);
        sqlUpdate.addBatch();

        sqlUpdate.executeBatch();
        transaction.commit();
      }
    });
  }
}
