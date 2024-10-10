package org.querytest;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.example.domain.DataWithFormulaMain;
import org.example.domain.query.QDataWithFormulaMain;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QDataWithFormulaMainTest {

  @Test
  void testFilterMany() {
    LoggedSql.start();

    new QDataWithFormulaMain()
      .metaData.filterMany(metadata -> metadata.id.metaKey.eq(""))
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains(" where ((t1.main_id is null and t1.meta_key is null and t1.value_index is null) or (t1.meta_key = ?)) order by t0.id");
  }

  @Test
  void testFilterManyComposite() {
    LoggedSql.start();

    DB.find(DataWithFormulaMain.class)
      .where()
      .filterMany("metaData").eq("metaKey", "")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains(" where ((t1.main_id is null and t1.meta_key is null and t1.value_index is null) or (t1.meta_key = ?)) order by t0.id");
  }
}
