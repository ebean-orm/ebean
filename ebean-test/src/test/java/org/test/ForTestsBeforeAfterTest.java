package org.test;

import io.ebean.DB;
import io.ebean.annotation.Transactional;
import io.ebean.test.ForTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ForTestsBeforeAfterTest {

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


  @Test
  public void createRollbackAll() {

    doInsert();
    assertThat(getCount()).isEqualTo(1);
  }

  private int getCount() {
    return DB.find(BSimpleWithGen.class)
        .where().eq("name", "ForTestsBeforeAfterTest")
        .findCount();
  }

  @Transactional
  private void doInsert() {

    BSimpleWithGen bean = new BSimpleWithGen("ForTestsBeforeAfterTest");
    DB.save(bean);
  }

}
