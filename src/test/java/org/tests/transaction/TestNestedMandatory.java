package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.TxType;
import io.ebean.annotation.Transactional;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNestedMandatory extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestNestedMandatory.class);

  static Transaction outerTxn;

  @Test
  public void test() {

    new Outer().doOuter();
  }

  class Outer {

    @Transactional
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
