package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOnlyKillOrphans extends BaseTestCase {

  @Test
  public void test() {
    final COOne one = setup();

    assertThat(one.getChildren()).hasSize(2);

    CORoot root = new CORoot("P1", one);
    DB.insert(root);

    // assert
    CORoot check = DB.find(CORoot.class, root.getId());
    assertThat(check.getOne().getChildren()).hasSize(2);
    DB.delete(check);
  }

  private COOne setup() {
    COOne one = new COOne("P0");
    one.setChildren(createManies("M1", "M2"));
    DB.insert(one);
    return one;
  }

  private List<COOneMany> createManies(String... names) {
    List<COOneMany> manies = new ArrayList<>();
    for (String name : names) {
      manies.add(new COOneMany(name));
    }
    return manies;
  }
}
