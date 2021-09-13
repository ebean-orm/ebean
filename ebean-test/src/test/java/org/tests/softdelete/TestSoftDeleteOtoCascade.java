package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
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

    DB.save(a);

    LoggedSql.start();

    // delete doesn't cascade to ESoftDelOneB as this is a
    // soft delete and ESoftDelOneB doesn't have soft delete flag
    DB.delete(a);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update esoft_del_one_a set deleted=?, version=? where id=? and version=?");

  }
}
