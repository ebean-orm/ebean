package com.avaje.ebean.dbmigration.migrationreader;

import com.avaje.ebean.dbmigration.migration.Migration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MigrationXmlWriterTest {

  Logger logger = LoggerFactory.getLogger(MigrationXmlWriterTest.class);

  @Test
  public void testWrite() throws Exception {

    Migration migration = MigrationXmlReader.read("/test/container/test-create-table.xml");

    File temp = File.createTempFile("migrationWrite",".xml");
    MigrationXmlWriter writer = new MigrationXmlWriter();
    writer.write(migration, temp);

    logger.info("wrote migration file: "+temp.getAbsolutePath());

  }
}