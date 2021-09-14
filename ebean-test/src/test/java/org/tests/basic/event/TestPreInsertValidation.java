package org.tests.basic.event;


import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TWithPreInsert;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPreInsertValidation extends BaseTestCase {

  @Test
  public void test() {

    TWithPreInsert e = new TWithPreInsert();
    e.setTitle("Mister");
    // the perInsert should populate the
    // name with should not be null
    DB.save(e);

    // the save worked and name set in preInsert
    assertNotNull(e.getId());
    assertNotNull(e.getName());

    TWithPreInsert e1 = DB.find(TWithPreInsert.class, e.getId());

    e1.setTitle("Missus");
    DB.save(e1);
  }

  @Test
  public void testStatelessUpdate() {

    TWithPreInsert e = new TWithPreInsert();
    e.setName("BeanForUpdateTest");
    DB.save(e);

    TWithPreInsert bean2 = new TWithPreInsert();
    bean2.setId(e.getId());
    bean2.setName("stateless-update-name");
    bean2.setTitle(null);

    DB.update(bean2);

    // title set on preUpdate
    assertNotNull(bean2.getTitle());
  }

}
