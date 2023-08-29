package org.tests.sets;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestO2MSet {

  @Test
  void lazyLoadO2M_when_setWithHashCode_expect_selectProperties() {
    final O2MDepart department = new O2MDepart("Test");
    final O2MEmp employee = new O2MEmp("Test", "Code");
    department.addEmployee(employee);
    DB.save(department);

    LoggedSql.start();
    DB.find(O2MDepart.class, department.getId())
      .employees()
      .forEach(e -> assertThat(e.getName()).isNotNull());

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from o2_mdepart t0 where t0.id = ?");
    assertThat(sql.get(1)).contains("select t0.department_id, t0.id, t0.code, t0.name, t0.department_id from o2_memp t0 where");
  }
}
