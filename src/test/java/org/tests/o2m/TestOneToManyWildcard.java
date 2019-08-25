package org.tests.o2m;

import io.ebean.DB;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToManyWildcard {

  @Test
  public void test() {

    OmBasicParent parent = new OmBasicParent("p1");
    OmBasicChild c1 = new OmBasicChild("c1", parent);
    OmBasicChild c2 = new OmBasicChild("c2", parent);

    DB.save(parent);
    DB.saveAll(Arrays.asList(c1, c2));

    // we can't add to this in Java but Kotlin can
    //parent.getChildren().add(c1);

    // exercise
    final OmBasicParent found = DB.find(OmBasicParent.class)
      .where().idEq(parent.getId())
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getChildren()).hasSize(2);

    DB.delete(parent);
  }
}
