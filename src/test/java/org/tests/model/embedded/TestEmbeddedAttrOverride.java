package org.tests.model.embedded;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEmbeddedAttrOverride extends BaseTestCase {

  @Test
  public void insert() {

    PrimaryRevision bean = new PrimaryRevision(100);
    bean.setName("hello");

    LoggedSqlCollector.start();
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("insert into primary_revision (id, revision, name, version) values (?,?,?,?)");
  }
}
