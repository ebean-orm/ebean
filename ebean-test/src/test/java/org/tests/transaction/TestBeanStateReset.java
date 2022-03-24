package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.m2m.MnyB;
import org.tests.model.m2m.MnyC;

import javax.persistence.PersistenceException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestBeanStateReset extends BaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger(TestBeanStateReset.class);

  @Test
  public void resetForInsert() {

    // setup to fail foreign key constraint
    MnyC c = new MnyC();
    c.setId(Long.MAX_VALUE);

    MnyB b = new MnyB();
    b.getCs().add(c);

    try {
      // inserts of b succeeds but intersection insert fails FK check on c
      b.save();

    } catch (PersistenceException e) {
      logger.info("expected error " + e.getMessage());

      DB.beanState(b).resetForInsert();
      b.getCs().clear();

      LoggedSql.start();
      b.setName("mod");
      b.save();

      List<String> sql = LoggedSql.stop();
      assertSql(sql.get(0)).contains("insert into mny_b (id, name, version, when_created, when_modified, a_id) values (");
    }

  }

  @Test
  public void alternativeVia_persistCascadeOff_with_commitAndContinue() {

    // setup to fail foreign key constraint when using cascade save
    MnyC c = new MnyC();
    c.setId(Long.MAX_VALUE);

    MnyB b = new MnyB();
    b.getCs().add(c);

    Transaction transaction = DB.beginTransaction();
    try {
      // turn off cascade ...
      transaction.setPersistCascade(false);
      b.save();

      // commit at this point
      transaction.commitAndContinue();
      try {

        // turn on cascade ... such that the ManyToMany is persisted
        transaction.setPersistCascade(true);

        // save b again which this time cascades to the ManyToMany
        // but this fails due to FK on ManyToMany
        b.save();

        // we actually don't get here due to the FK error
        transaction.commit();

      } catch (PersistenceException e) {
        // so we failed the second save but that is ok'ish
        // we handle this exception knowing b got inserted and committed
        // and that the inserts into the intersection table failed
        logger.info("The ManyToMany intersection error: " + e.getMessage());
      }
    } finally {
      // performs a rollback as the commit at line:80 does not happen
      transaction.end();
    }

    // assert our insert prior to the commitAndContinue succeeded
    MnyB madeIt = DB.find(MnyB.class, b.getId());
    assertNotNull(madeIt);
  }
}
