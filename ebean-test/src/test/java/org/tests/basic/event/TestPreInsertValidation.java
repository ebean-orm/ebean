package org.tests.basic.event;


import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TWithPreInsert;
import org.tests.model.basic.TWithPreInsertChild;
import org.tests.model.basic.event.TWithPreInsertPersistAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestPreInsertValidation extends BaseTestCase {

  @Test
  void test() {

    TWithPreInsert e = new TWithPreInsert();
    e.setTitle("Mister");
    // the perInsert should populate the
    // name with should not be null
    DB.save(e);

    // the save worked and name set in preInsert
    assertNotNull(e.getId());
    assertNotNull(e.getName());
    assertThat(e.requestCascadeState()).isEqualTo(2);

    TWithPreInsert e1 = DB.find(TWithPreInsert.class, e.getId());
    assert e1 != null;

    e1.setTitle("Missus");
    DB.save(e1);

    assertThat(e1.requestCascadeState()).isEqualTo(12);
  }

  @Test
  void test_cascade() {

    TWithPreInsert e = new TWithPreInsert();
    e.setTitle("ParentCascading");
    e.children().add(new TWithPreInsertChild("Child0"));
    // the perInsert should populate the
    // name with should not be null
    DB.save(e);

    // the save worked and name set in preInsert
    assertNotNull(e.getId());
    assertNotNull(e.getName());
    assertThat(e.requestCascadeState()).isEqualTo(2);
    assertThat(e.children().get(0).requestCascadeState()).isEqualTo(1);

    TWithPreInsert e1 = DB.find(TWithPreInsert.class, e.getId());
    assert e1 != null;

    e1.setTitle("ParentCascading-changed");
    TWithPreInsertChild childBean = e1.children().get(0);
    childBean.setName("Child0-changed");
    DB.save(e1);

    assertThat(e1.requestCascadeState()).isEqualTo(12);
    assertThat(childBean.requestCascadeState()).isEqualTo(11);

    DB.delete(e1);

    assertThat(e1.requestCascadeState()).isEqualTo(22);

    // assert that isCascade() was true for the child bean
    assertThat(TWithPreInsertPersistAdapter.cascadeDelete).hasSize(1);
    String deleteCascade = TWithPreInsertPersistAdapter.cascadeDelete.get(0);
    assertThat(deleteCascade).isEqualTo("class org.tests.model.basic.TWithPreInsertChild:1");
  }

  @Test
  void testStatelessUpdate() {

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
