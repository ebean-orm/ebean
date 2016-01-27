package com.avaje.ebean.dbmigration.migrationreader;

import com.avaje.ebean.dbmigration.migration.Migration;
import org.junit.Test;

import java.io.File;

public class MigrationXmlWriterTest {

  @Test
  public void testReadWrite() throws Exception {

    Migration migration = MigrationXmlReader.read("/container/test-create-table.xml");

    File temp = File.createTempFile("migrationWrite",".xml");
    MigrationXmlWriter writer = new MigrationXmlWriter();
    writer.write(migration, temp);

  }
}