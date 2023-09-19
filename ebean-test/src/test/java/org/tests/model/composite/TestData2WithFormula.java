package org.tests.model.composite;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class TestData2WithFormula extends BaseTestCase {

  @Test
  void test1() {

    Data2WithFormulaMain main = new Data2WithFormulaMain();
    main.setId(UUID.randomUUID());
    main.setTitle("Main");

    Data2WithFormula data = new Data2WithFormula();
    data.setMainId(main.getId());
    data.setMetaKey("meta");
    data.setValueIndex(42);
    data.setStringValue("SomeValue");
    main.setMetaData(List.of(data));

    LoggedSql.start();
    DB.save(main);
    List<String> sqls = LoggedSql.stop();

    assertThat(sqls).hasSize(3);
    assertThat(sqls.get(0)).contains("insert into data2_with_formula_main (id, title) values (?,?);"); // main
    assertThat(sqls.get(1)).contains("insert into data2_with_formula (main_id, meta_key, value_index, string_value) values (?,?,?,?)"); // main
    assertThat(sqls.get(2)).contains("-- bind");

  }
}
