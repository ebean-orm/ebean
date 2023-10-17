package org.tests.model.site;

import io.ebean.xtest.BaseTestCase;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

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

    LoggedSql.start();
    grandparent.save();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(7);
    assertSql(sql.get(0)).contains("insert into tree_entity");
    assertSql(sql.get(1)).contains("insert into tree_entity");
    assertThat(sql.get(4)).contains("insert into tree_entity");
  }

}
