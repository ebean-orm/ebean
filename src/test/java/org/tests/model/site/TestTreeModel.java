package org.tests.model.site;

import io.ebean.BaseTestCase;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestTreeModel extends BaseTestCase {


  @Test
  public void saveCascade() {

    TreeEntity grandparent = new TreeEntity("grandparent");
    TreeEntity parent = new TreeEntity("parent");
    TreeEntity child = new TreeEntity("child");
    grandparent.setChildren(singletonList(parent));
    parent.setChildren(singletonList(child));

    LoggedSqlCollector.start();
    grandparent.save();

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(5);
    assertSql(sql.get(0)).contains("insert into tree_entity");
    assertSql(sql.get(1)).contains("insert into tree_entity");
    assertThat(sql.get(3)).contains("insert into tree_entity");
  }

}
