package org.tests.o2o;


import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneSaveWithoutChanges {

  @Test
  public void testSave3Levels() {

    OtoLevelA a = new OtoLevelA("A");
    a.setB(new OtoLevelB("B"));
    a.getB().setC(new OtoLevelC("C"));

    DB.save(a);

    OtoLevelA dbA = DB.find(OtoLevelA.class, 1);
    OtoLevelB dbB = dbA.getB();
    OtoLevelC dbC = dbB.getC();

    LoggedSql.start();

    DB.save(dbA);
    DB.save(dbB);
    DB.save(dbC);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(0);
  }
}
