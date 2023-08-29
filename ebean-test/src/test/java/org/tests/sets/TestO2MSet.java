package org.tests.sets;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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

  @Test
  void beanSet_when_clear_thenAddSave() {

    final O2MDepart department = new O2MDepart("clearAndAdd");
    final O2MEmp employee0 = new O2MEmp("Init1", "Code0");
    final O2MEmp employee1 = new O2MEmp("Init2", "Code1");
    department.addEmployee(employee0);
    department.addEmployee(employee1);
    DB.save(department);

    LoggedSql.start();

    O2MDepart dept = DB.find(O2MDepart.class, department.getId());

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from o2_mdepart t0 where t0.id = ?");

    Set<O2MEmp> employees = dept.employees();
    employees.clear();

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.department_id, t0.id, t0.code, t0.name, t0.department_id from o2_memp t0 where (t0.department_id)");

    final O2MEmp employee2 = new O2MEmp("After1", "Code3");
    employees.add(employee2);

    DB.save(dept);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("delete from o2_memp where id=?");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains("insert into o2_memp (id, code, name, department_id) values (?,?,?,?)");
    assertThat(sql.get(4)).contains(" -- bind");
  }
}
