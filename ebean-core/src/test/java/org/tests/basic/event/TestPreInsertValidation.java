package org.tests.basic.event;


import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.TWithPreInsert;
import org.junit.Assert;
import org.junit.Test;

public class TestPreInsertValidation extends BaseTestCase {

  @Test
  public void test() {

    TWithPreInsert e = new TWithPreInsert();
    e.setTitle("Mister");
    // the perInsert should populate the
    // name with should not be null
    Ebean.save(e);

    // the save worked and name set in preInsert
    Assert.assertNotNull(e.getId());
    Assert.assertNotNull(e.getName());

    TWithPreInsert e1 = Ebean.find(TWithPreInsert.class, e.getId());

    e1.setTitle("Missus");
    Ebean.save(e1);
  }

  @Test
  public void testStatelessUpdate() {

    TWithPreInsert e = new TWithPreInsert();
    e.setName("BeanForUpdateTest");
    Ebean.save(e);

    TWithPreInsert bean2 = new TWithPreInsert();
    bean2.setId(e.getId());
    bean2.setName("stateless-update-name");
    bean2.setTitle(null);

    Ebean.update(bean2);

    // title set on preUpdate
    Assert.assertNotNull(bean2.getTitle());
  }

}
