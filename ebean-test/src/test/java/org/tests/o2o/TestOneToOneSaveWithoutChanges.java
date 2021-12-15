package org.tests.o2o;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.ebean.DB;
import io.ebean.test.LoggedSql;

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
  
  @Test
  public void testSave2LevelsLazyPersistTrigger() {
    OtoLevelALazy a = new OtoLevelALazy("A");
    OtoLevelBLazy b = new OtoLevelBLazy("B");
    b.setA(a);
    DB.save(a);
    DB.save(b);

    OtoLevelALazy dbA = DB.find(OtoLevelALazy.class).select("*").where().idEq(1).findList().get(0);
    LoggedSql.start();
    DB.save(dbA);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(0);
  }
}
