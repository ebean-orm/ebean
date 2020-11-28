package org.tests.basic;

import io.ebean.DB;
import io.ebean.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tests.model.basic.ESimple;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryUsingDatabase {

  private static final String SOME_OTHER_DB_NAME = "someotherdb";
  public static final ESimple RECORD1 = new ESimple();
  public static final ESimple RECORD2 = new ESimple();

  @Before
  public void setupNonDefaultDatabase() {
    DB.byName(SOME_OTHER_DB_NAME).insert(RECORD1);
    DB.byName(SOME_OTHER_DB_NAME).insert(RECORD2);
  }

  @After
  public void shutdown() {
    DB.byName(SOME_OTHER_DB_NAME).shutdown();
  }

  @Test
  public void usingNonDefaultDatabase() {
    final Database nonDefaultDb = DB.byName(SOME_OTHER_DB_NAME);

    List<ESimple> orderList = DB.find(ESimple.class)
      .usingDatabase(nonDefaultDb)
      .findList();

    assertThat(orderList).size().isEqualTo(2);
    assertThat(orderList).containsExactlyInAnyOrder(RECORD1, RECORD2);
  }
}
