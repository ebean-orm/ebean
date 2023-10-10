package org.tests.sets;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestO2MMap {

  @Test
  void lazyLoadO2M_when_setWithHashCode_expect_selectProperties() {
    final MapDepart department = new MapDepart("Test");
    department.addEmployee(new MapEmp("Emp0", "Code0"));
    department.addEmployee(new MapEmp("Emp1", "Code1"));
    DB.save(department);

    MapDepart mapDepart = DB.find(MapDepart.class, department.getId());
    Map<String, MapEmp> employees = mapDepart.employees();
    assertThat(employees).hasSize(2);
    assertThat(employees).containsKeys("Code0", "Code1");
    assertThat(employees.get("Code1").getName()).isEqualTo("Emp1");


    LoggedSql.start();
    DB.find(MapDepart.class, department.getId())
      .employees().forEach((k, v) -> {
      assertThat(k).isEqualTo(v.getCode());
    });

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from map_depart t0 where t0.id = ?");
    assertThat(sql.get(1)).contains("select t0.department_id, t0.id, t0.code, t0.name, t0.department_id from map_emp t0 where (t0.department_id)");
  }

  @Test
  void beanMap_when_clear_thenAddSave() {

    final MapDepart department = new MapDepart("clearAndAdd");
    final MapEmp employee0 = new MapEmp("Init1", "Code0");
    final MapEmp employee1 = new MapEmp("Init2", "Code1");
    department.addEmployee(employee0);
    department.addEmployee(employee1);
    DB.save(department);

    LoggedSql.start();

    MapDepart dept = DB.find(MapDepart.class, department.getId());

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from map_depart t0 where t0.id = ?");

    Map<String, MapEmp> employees = dept.employees();
    employees.clear();

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.department_id, t0.id, t0.code from map_emp t0 where (t0.department_id)");

    final MapEmp employee2 = new MapEmp("After1", "Code3");
    dept.addEmployee(employee2);

    DB.save(dept);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("delete from map_emp where id=?");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains("insert into map_emp");
    assertThat(sql.get(4)).contains(" -- bind");
  }
}
