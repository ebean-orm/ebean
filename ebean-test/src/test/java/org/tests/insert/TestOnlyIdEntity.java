package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOnlyIdEntity extends BaseTestCase {

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void insert() {

    final Database database = DB.getDefault();

    LoggedSql.start();
    OnlyIdEntity bean = new OnlyIdEntity();
    database.save(bean);

    List<String> sql = LoggedSql.stop();

    assertThat(bean.getId()).isGreaterThan(0);
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("insert into only_id_entity default values");
  }
}
