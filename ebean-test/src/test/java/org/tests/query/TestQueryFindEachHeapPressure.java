package org.tests.query;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.m2m.softdelete.MsManyA;
import org.tests.m2m.softdelete.MsManyB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryFindEachHeapPressure extends BaseTestCase {

  private byte[] buffer;

  @BeforeEach
  public void before() {
    DB.find(MsManyB.class).delete();
    DB.find(MsManyA.class).delete();

    List<MsManyB> children = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      children.add(new MsManyB("child_" + i));
    }
    DB.saveAll(children);

    try (Transaction txn = DB.beginTransaction()) {
      for (int i = 0; i < 100; i++) {
        MsManyA parent = new MsManyA("parent_" + i);

        Collections.shuffle(children);

        parent.getManybs().add(children.get(0));

        DB.save(parent);
      }
      txn.commit();
    }
  }

  /**
   * Reproduce quickly, by manually adding System.gc at the beginning of CQuery#setLazyLoadedChildBean.
   */
  @Test
  @Disabled("Run manually with -Xmx128M")
  public void test() throws Exception {
    for (int j = 0; j < 1000; j++) {
      System.out.println("Iteration " + j);
      DB.find(MsManyA.class)
        .findEach(parent -> {
          if (Math.random() > 0.9) {
            buffer = new byte[1_000_000];
            List<MsManyB> children = parent.getManybs();
            assertThat(children.size()).isEqualTo(1);
            assertThat(buffer.length).isEqualTo(1_000_000);
          }
        });
    }
  }

}
