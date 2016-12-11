package io.ebean.dbmigration;

import io.ebean.BaseTestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class DbMigrationTest extends BaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationTest.class);

  //@Ignore
  @Test
  public void writeCurrent() {

    logger.info("start");

    DbOffline.asH2();
    DbMigration migration = new DbMigration();
    DbOffline.reset();

    //migration.writeCurrent();

    assertThat(DbOffline.isSet()).isFalse();

    logger.info("end");
  }

}
