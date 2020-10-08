package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNestedTransaction extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestNestedTransaction.class);

  private EBasic bean;

  @Before
  public void init() {
    bean = new EBasic("new");
    Ebean.save(bean);
  }

  private void assertClean() {
    EBasic myBean = Ebean.find(EBasic.class, bean.getId());
    assertThat(myBean.getName()).isEqualTo("new");
  }

  private void assertModified() {
    EBasic myBean = Ebean.find(EBasic.class, bean.getId());
    assertThat(myBean.getName()).isEqualTo("modified");
  }

  private void modify() {
    bean.setName("modified");
    Ebean.save(bean);
  }

  // ===== level 0 =======
  @Test
  public void testNested_0() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      modify();
      // no commit
    }
    assertClean();
  }

  @Test
  public void testNested_1() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      modify();
      txn0.commit();
    }
    assertModified();
  }

  // ===== level 1 =======
  @Test
  public void testNested_00() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        modify();
        // no commit
      }
      // no commit
    }
    assertClean();
  }

  @Test
  public void testNested_01() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        modify();
        // no commit
      }
      attemptCommit(txn0);
    }
    assertClean();
  }

  private void attemptCommit(Transaction txn) {
    try {
      txn.commit();
    } catch (IllegalStateException e) {
      // expected
      log.info("Expected IllegalStateException as transaction already rolled back " + e.getMessage());
    }
  }

  @Test
  public void testNested_10() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        modify();
        txn1.commit();
      }
      // no commit
    }
    assertClean();
  }

  @Test
  public void testNested_11() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        modify();
        txn1.commit();
      }
      txn0.commit();
    }
    assertModified();
  }

  // ===== level 2 =======
  @Test
  public void testNested_000() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          // no commit
        }
        // no commit
      }
      // no commit
    }
    assertClean();
  }

  @Test
  public void testNested_001() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          // no commit
        }
        // no commit
      }
      attemptCommit(txn0);
    }
    assertClean();
  }

  @Test
  public void testNested_010() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          // no commit
        }
        txn1.commit();
      }
      // no commit
    }
    assertClean();
  }

  @Test
  public void testNested_011() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          // no commit
        }
        txn1.commit();
      }
      attemptCommit(txn0);
    }
    assertClean();
  }

  @Test
  public void testNested_100() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          txn2.commit();
        }
        // no commit
      }
      // no commit
    }
    assertClean();
  }

  @Test
  public void testNested_101() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          txn2.commit();
        }
        // no commit
      }
      attemptCommit(txn0);
    }
    assertClean();
  }

  @Test
  public void testNested_110() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          txn2.commit();
        }
        txn1.commit();
      }
      // no commit
    }
    assertClean();
  }

  @Test
  public void testNested_111() {
    try (Transaction txn0 = Ebean.beginTransaction()) {
      try (Transaction txn1 = Ebean.beginTransaction()) {
        try (Transaction txn2 = Ebean.beginTransaction()) {
          modify();
          txn2.commit();
        }
        txn1.commit();
      }
      txn0.commit();
    }
    assertModified();
  }
}
