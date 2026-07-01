package org.tests.model.noid;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestInsertNoIdBean extends BaseTestCase {

  @Test
  void testInsert() {
    NoIdBean bean = new NoIdBean();
    bean.setName("Rocky");
    bean.setSubject("Blowing up stuff");

    DB.save(bean);

    int rowCount = DB.find(NoIdBean.class).findCount();
    assertThat(rowCount).isGreaterThan(0);
  }

  @Test
  void testInvalidSelectColumn_expect_PersistenceException() {
    assertThatThrownBy(() ->
      DB.find(NoIdBean.class)
        .select("does_not_exist_user_id, name")
        .where().eq("name", "foo")
        .findList()
    ).isInstanceOf(PersistenceException.class)
    .hasMessageContaining("Property not found - does_not_exist_user_id");
  }
}
