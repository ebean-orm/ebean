package org.etest;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import io.ebean.test.ForTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ForTestsBeforeAfterTest extends BaseTestCase {

  private ForTests.RollbackAll rollbackAll;

  @BeforeEach
  public void before() {
    rollbackAll = ForTests.createRollbackAll();
  }

  @AfterEach
  public void after() {
    rollbackAll.close();
    assertThat(getCount()).isEqualTo(0);
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.ORACLE})
  @Test
  public void createRollbackAll() {
    doInsert();
    assertThat(getCount()).isEqualTo(1);
  }

  private int getCount() {
    return DB.find(BSimpleFor.class)
        .where().eq("name", "ForTestsBeforeAfterTest")
        .findCount();
  }

  @Transactional
  private void doInsert() {

    BSimpleFor bean = new BSimpleFor("ForTestsBeforeAfterTest");
    DB.save(bean);
  }

}
