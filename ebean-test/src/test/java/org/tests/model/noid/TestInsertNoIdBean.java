package org.tests.model.noid;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInsertNoIdBean extends BaseTestCase {

  @Test
  public void testInsert() {
    NoIdBean bean = new NoIdBean();
    bean.setName("Rocky");
    bean.setSubject("Blowing up stuff");

    DB.save(bean);

    int rowCount = DB.find(NoIdBean.class).findCount();
    assertThat(rowCount).isGreaterThan(0);
  }
}
