package org.tests.sets;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestO2MMap2 extends BaseTestCase {

  @Test
  void lazyLoadO2M_when_setWithHashCode_expect_selectProperties() {
    final MapDepart2 department = new MapDepart2("Test");
    department.addEmployee(new MapEmp2("Emp0", "Code0"));
    department.addEmployee(new MapEmp2("Emp1", "Code1"));
    DB.save(department);

    MapDepart2 mapDepart = DB.find(MapDepart2.class, department.getId());
    Map<String, MapEmp2> employees = mapDepart.employees();
    assertThat(employees).hasSize(2);
    assertThat(employees).containsKeys("Code0", "Code1");
    assertThat(employees.get("Code1").getName()).isEqualTo("Emp1");


    LoggedSql.start();
    DB.find(MapDepart2.class, department.getId())
      .employees().forEach((k, v) -> {
      assertThat(k).isEqualTo(v.getCode());
    });

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from map_depart2 t0 where t0.id = ?");
    assertThat(sql.get(1)).contains("select t0.department_id, t0.id, t0.code, t0.name, t0.department_id from map_emp2 t0 where (t0.department_id)");
  }

  @Test
  void beanMap_when_clear_thenAddSave() {

    final MapDepart2 department = new MapDepart2("clearAndAdd");
    final MapEmp2 employee0 = new MapEmp2("Init1", "Code0");
    final MapEmp2 employee1 = new MapEmp2("Init2", "Code1");
    department.addEmployee(employee0);
    department.addEmployee(employee1);
    DB.save(department);

    LoggedSql.start();

    MapDepart2 dept = DB.find(MapDepart2.class, department.getId());

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from map_depart2 t0 where t0.id = ?");

    Map<String, MapEmp2> employees = dept.employees();
    employees.clear(); // No orphan Removal to no lazy loading invoked by the clear()

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(0);

    final MapEmp2 employee2 = new MapEmp2("After1", "Code3");
    dept.addEmployee(employee2);

    DB.save(dept);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(3);
    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("insert into map_emp2 (id, code, name, department_id) values (?,?,?,?)");
    } else {
      assertThat(sql.get(0)).contains("insert into map_emp2 (code, name, department_id) values (?,?,?)");
    }
    assertThat(sql.get(1)).contains(" -- bind");
  }
}
