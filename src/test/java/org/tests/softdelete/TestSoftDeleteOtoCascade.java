package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.softdelete.ESoftDelOneA;
import org.tests.model.softdelete.ESoftDelOneB;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteOtoCascade extends BaseTestCase {

  @Test
  public void test() {

    ESoftDelOneA a = new ESoftDelOneA("a1");
    ESoftDelOneB b = new ESoftDelOneB("b1");

    a.setOneb(b);
    b.setOnea(a);

    Ebean.save(a);

    LoggedSqlCollector.start();

    // delete doesn't cascade to ESoftDelOneB as this is a
    // soft delete and ESoftDelOneB doesn't have soft delete flag
    Ebean.delete(a);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update esoft_del_one_a set deleted=?, version=? where id=? and version=?");

  }
}
