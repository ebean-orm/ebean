package org.tests.softdelete;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.softdelete.ESoftDelOneA;
import org.tests.model.softdelete.ESoftDelOneB;
import org.tests.model.softdelete.ESoftDelOneBOwner;

import static org.assertj.core.api.Assertions.assertThat;

class TestSoftDeleteOtoImported extends BaseTestCase {

  @Test
  void extraJoinToOtoImported_expect_softDeletePredicate() {
    ESoftDelOneB b = new ESoftDelOneB("xbImported");
    DB.save(b);

    ESoftDelOneA a = new ESoftDelOneA("xaImported");
    a.setOneb(b);
    DB.save(a);

    ESoftDelOneBOwner co = new ESoftDelOneBOwner("xoImport");
    co.setOneb(b);
    DB.save(co);

    LoggedSql.start();
    var listResult = DB.find(ESoftDelOneBOwner.class)
      .where()
      .eq("oneb.onea.name", "xaImported")
      .findList();

    var countResult = DB.find(ESoftDelOneBOwner.class)
      .where()
      .eq("oneb.onea.name", "xaImported")
      .findCount();

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);

    assertThat(listResult).hasSize(1);
    assertThat(countResult).isEqualTo(1);
    assertThat(sql.get(0)).contains("and t2.deleted = ");
    assertThat(sql.get(1)).contains("and t2.deleted = ");
  }
}
