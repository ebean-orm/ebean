package org.tests.o2o;


import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.update.EPersonOnline;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReferenceSave {

  @Test
  public void test() {

    DB.getDefault();

    LoggedSql.start();

    EPersonOnline bean = DB.reference(EPersonOnline.class, 1L);
    DB.save(bean);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(0);
  }
}
