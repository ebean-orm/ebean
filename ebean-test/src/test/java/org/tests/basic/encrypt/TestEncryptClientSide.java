package org.tests.basic.encrypt;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.dbmigration.DbMigration;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tests.model.basic.EBasicEncryptClient;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEncryptClientSide extends BaseTestCase {

  @Test
  public void insertUpdate() {

    LocalDate today = LocalDate.now();

    EBasicEncryptClient bean = new EBasicEncryptClient();
    bean.setDescription("hello");
    bean.setStatus(EBasicEncryptClient.Status.ONE);
    bean.setDob(today);
    bean.setEmailAddresses(Set.of("first@example.com", "second@example.com"));

    DB.save(bean);

    EBasicEncryptClient found = DB.find(EBasicEncryptClient.class)
      .where()
      .eq("description", "hello")
      .eq("status", EBasicEncryptClient.Status.ONE)
      .eq("dob", today)
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getDescription()).isEqualTo("hello");
    assertThat(found.getStatus()).isEqualTo(EBasicEncryptClient.Status.ONE);
    assertThat(found.getDob()).isEqualTo(today);
    assertThat(found.getEmailAddresses()).containsExactlyInAnyOrder("first@example.com", "second@example.com");


    assertThat(found.getTags()).isNull();
    found.setTags(List.of("first", "second"));
    found.setEmailAddresses(Set.of("updated@example.com"));
    found.setDescription("goodbye");
    found.setStatus(EBasicEncryptClient.Status.TWO);
    DB.save(found);

    found = DB.find(EBasicEncryptClient.class)
      .where()
      .eq("description", "goodbye")
      .eq("status", EBasicEncryptClient.Status.TWO)
      .eq("dob", today)
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getDescription()).isEqualTo("goodbye");

    found = DB.find(EBasicEncryptClient.class)
      .where()
      .in("description", "goodbye")
      .in("status", EBasicEncryptClient.Status.TWO)
      .in("dob", today)
      .findOne();
    assertThat(found).isNotNull();
    assertThat(found.getDescription()).isEqualTo("goodbye");


    assertThat(found.getEmailAddresses()).containsExactly("updated@example.com");
    assertThat(found.getTags()).containsExactly("first", "second");
    found.setTags(Collections.emptyList());
    found.setEmailAddresses(Collections.emptySet());
    DB.save(found);
    found = DB.find(EBasicEncryptClient.class, found.getId());
    assertThat(found.getEmailAddresses()).isEmpty();
    assertThat(found.getTags()).isEmpty();

    DB.delete(found);
  }

  @Test
  public void encryptedArrayMigration(@TempDir Path dir) throws IOException {
    DatabaseBuilder config = Database.builder();
    config.setName("encrypted-array-migration");
    config.setRegister(false);
    config.setDefaultServer(false);
    config.setDbOffline(true);
    config.setDatabasePlatformName("postgres");
    config.classes(Collections.singletonList(EBasicEncryptClient.class));
    config.setEncryptKeyManager(new BasicEncyptKeyManager());
    Database database = config.build();
    try {
      DbMigration migration = DbMigration.create();
      migration.setServer(database);
      migration.setPathToResources(dir.toString());
      migration.setMigrationPath("migration");
      migration.setVersion("1.0");
      migration.setName("encrypted_array");
      String version = migration.generateMigration();
      assertThat(dir.resolve("migration/" + version + ".sql")).content()
        .contains("email_addresses               bytea not null")
        .contains("tags                          bytea,");
    } finally {
      database.shutdown();
    }
  }
}
