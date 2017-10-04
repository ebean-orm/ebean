package io.ebeaninternal.dbmigration.migrationreader;

import io.ebeaninternal.dbmigration.migration.Migration;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrationXmlWriterTest {

  @Test
  public void testReadWrite() throws Exception {

    Migration migration = MigrationXmlReader.read("/container/test-create-table.xml");
    assertThat(migration.getChangeSet()).hasSize(1);
    assertThat(migration.getChangeSet().get(0).getChangeSetChildren()).hasSize(3);

    File temp = File.createTempFile("migrationWrite", ".xml");
    new MigrationXmlWriter("THIS IS A GENERATED FILE - DO NOT MODIFY").write(migration, temp);

    Migration migrationRead = MigrationXmlReader.read(temp);
    assertThat(migrationRead.getChangeSet()).hasSize(1);
    assertThat(migrationRead.getChangeSet().get(0).getChangeSetChildren()).hasSize(3);

    temp = File.createTempFile("migrationWrite", ".xml");
    new MigrationXmlWriter(null).write(migration, temp);

    Migration migrationRead2 = MigrationXmlReader.read(temp);

    assertThat(migrationRead2.getChangeSet()).hasSize(1);
    assertThat(migrationRead.getChangeSet().get(0).getChangeSetChildren()).hasSize(3);
  }
}
