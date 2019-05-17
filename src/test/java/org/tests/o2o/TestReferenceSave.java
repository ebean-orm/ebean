package org.tests.o2o;


import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.update.EPersonOnline;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReferenceSave {

  @Test
  public void test() {

    Ebean.getDefaultServer();

    LoggedSqlCollector.start();

    EPersonOnline bean = Ebean.getReference(EPersonOnline.class, 1L);
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(0);
  }
}
