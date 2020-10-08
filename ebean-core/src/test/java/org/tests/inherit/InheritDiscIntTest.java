package org.tests.inherit;

import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class InheritDiscIntTest {

  @Test
  public void test() {

    DIntChild2 child2 = new DIntChild2(42, "fortyTwo");
    DB.save(child2);

    final DIntParent found = DB.find(DIntParent.class, child2.getId());
    assertNotNull(found);

    assertThat(found).isInstanceOf(DIntChild2.class);
  }
}
