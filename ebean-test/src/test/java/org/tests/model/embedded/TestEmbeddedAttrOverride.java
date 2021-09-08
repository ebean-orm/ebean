package org.tests.model.embedded;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEmbeddedAttrOverride extends BaseTestCase {

  @Test
  public void insert() {

    PrimaryRevision bean = new PrimaryRevision(100);
    bean.setName("hello");

    LoggedSql.start();
    DB.save(bean);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("insert into primary_revision (id, revision, name, version) values (?,?,?,?)");
  }
}
