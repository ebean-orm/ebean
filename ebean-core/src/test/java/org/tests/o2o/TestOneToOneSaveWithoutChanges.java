package org.tests.o2o;


import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneSaveWithoutChanges {

  @Test
  public void testSave3Levels() {

    OtoLevelA a = new OtoLevelA("A");
    a.setB(new OtoLevelB("B"));
    a.getB().setC(new OtoLevelC("C"));

    Ebean.save(a);

    OtoLevelA dbA = Ebean.find(OtoLevelA.class, 1);
    OtoLevelB dbB = dbA.getB();
    OtoLevelC dbC = dbB.getC();

    LoggedSqlCollector.start();

    Ebean.save(dbA);
    Ebean.save(dbB);
    Ebean.save(dbC);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(0);
  }
}
