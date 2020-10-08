package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import io.ebean.meta.MetaTimedMetric;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNestedMandatory extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestNestedMandatory.class);

  private static Transaction outerTxn;

  @Test
  public void test() {

    resetAllMetrics();

    new Outer().doOuter();

    List<MetaTimedMetric> txnMetrics = visitTimedMetrics();
    for (MetaTimedMetric txnTimed : txnMetrics) {
      System.out.println(txnTimed);
    }

    assertThat(txnMetrics).hasSize(2);
    assertThat(txnMetrics.get(1).getName()).isEqualTo("txn.named.outer");
  }

  class Outer {

    @Transactional(label = "outer")
    void doOuter() {
      outerTxn = Ebean.currentTransaction();

      log.info("outer start ...{}", outerTxn);
      new Inner().doInner();
      log.info("outer end ...{}", outerTxn);
    }
  }

  class Inner {

    @Transactional(type = TxType.MANDATORY)
    void doInner() {
      Transaction innerTxn = Ebean.currentTransaction();
      log.info("inner ...{}", innerTxn);

      assertThat(innerTxn).isSameAs(outerTxn);
    }
  }
}
