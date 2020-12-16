package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteO2MOrphans extends BaseTestCase {

  @Test
  public void test() {
    final long id = setup();
    // act
    setNewChildren(id);

    // assert
    COOne check = findById(id);
    assertThat(check.getChildren()).hasSize(2);
    DB.delete(check);
  }

  private COOne findById(long id) {
    return DB.find(COOne.class).where().idEq(id).findOne();
  }

  private void setNewChildren(long id) {
    COOne found = findById(id);
    found.setChildren(createManies("M3", "M4"));
    DB.update(found);
  }

  private long setup() {
    COOne company = new COOne("P0");
    company.setChildren(createManies("M1", "M2"));
    DB.insert(company);
    return company.getId();
  }

  private List<COOneMany> createManies(String name1, String name2) {
    COOneMany employee1 = new COOneMany(name1);
    COOneMany employee2 = new COOneMany(name2);
    List<COOneMany> employees = new ArrayList<>();
    employees.add(employee1);
    employees.add(employee2);
    return employees;
  }
}
