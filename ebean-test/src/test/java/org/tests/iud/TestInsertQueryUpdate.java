package org.tests.iud;

import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestInsertQueryUpdate extends BaseTestCase {

  @Test
  public void test() {

    EBasicVer e0 = new EBasicVer("name0");
    e0.setDescription("desc0");
    DB.save(e0);

    EBasicVer e1 = DB.find(EBasicVer.class)
      .select("name")
      .setId(e0.getId())
      .findOne();

    BeanState beanState = DB.beanState(e1);
    Set<String> loadedProps = beanState.loadedProps();
    assertFalse(loadedProps.contains("description"));
    //lastUpdate

    e1.setName("name1");
    DB.save(e1);

    e1.setDescription("desc1");
    DB.save(e1);

  }

}
