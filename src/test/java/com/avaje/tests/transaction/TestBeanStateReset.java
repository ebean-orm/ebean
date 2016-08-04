package com.avaje.tests.transaction;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.m2m.MnyB;
import com.avaje.tests.model.m2m.MnyC;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

      Ebean.getBeanState(b).resetForInsert();
      b.getCs().clear();

      LoggedSqlCollector.start();
      b.setName("mod");
      b.save();

      List<String> sql = LoggedSqlCollector.stop();
      assertThat(sql.get(0)).contains("insert into mny_b (id, name, version, when_created, when_modified, a_id) values (");
    }

  }
}
