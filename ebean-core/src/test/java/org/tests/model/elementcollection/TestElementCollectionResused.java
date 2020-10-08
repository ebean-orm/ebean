package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionResused extends BaseTestCase {

  @Test
  public void insert_find_delete() {

    EcsmOne one = new EcsmOne("One028");
    one.getValues().add("128-1");
    one.getValues().add("128-2");

    EcsmTwo two = new EcsmTwo("Two028");
    two.getValues().add("228-1");
    two.getValues().add("228-2");

    DB.save(one);
    DB.save(two);

    final EcsmOne foundOne = DB.find(EcsmOne.class, one.getOneId());
    assertThat(foundOne.getName()).isEqualTo("One028");
    assertThat(foundOne.getValues()).contains("128-1", "128-2");

    final EcsmTwo foundTwo = DB.find(EcsmTwo.class, two.getId());
    assertThat(foundTwo.getName()).isEqualTo("Two028");
    assertThat(foundTwo.getValues()).contains("228-1", "228-2");

    DB.delete(foundOne);
    DB.delete(foundTwo);
  }
}
