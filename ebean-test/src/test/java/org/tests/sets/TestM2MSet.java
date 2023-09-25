package org.tests.sets;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestM2MSet {

  @Test
  void lazyLoadM2M_when_setWithHashCode_expect_selectProperties() {
    final M2MDepart department = new M2MDepart("Test");
    final M2MEmp employee = new M2MEmp("Test", "Code");
    DB.save(employee);
    department.addEmployee(employee);
    DB.save(department);

    LoggedSql.start();
    DB.find(M2MDepart.class, department.getId())
      .employees()
      .forEach(e -> assertThat(e.getName()).isNotNull());

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from m2_mdepart t0 where t0.id = ?");
    assertThat(sql.get(1)).contains("select int_.m2_mdepart_id, t0.id, t0.code, t0.name from m2_memp t0 left join m2_mdepart_memp int_ on int_.m2_memp_id = t0.id where");
  }
}
