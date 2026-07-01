package org.tests.readonly;

import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.datasource.DataSourceBuilder;
import io.ebean.test.containers.PostgresContainer;
import io.ebeaninternal.api.SpiTransaction;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UTDetail;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostgresReadOnlyDatabaseTest {

  private static Database readDb;

  @BeforeAll
  static void beforeAll() {
    readDb = setupReadOnlyDatabase();
  }

  @Test
  void insertPreventedInReadOnlyTransaction() {
    assertThatThrownBy(() -> {
      try (Transaction transaction = readDb.beginTransaction()) {
        UTDetail u0 = new UTDetail("u0", 45, 3D);
        readDb.save(u0);
      }
    }).isInstanceOf(PersistenceException.class)
      .hasMessageContaining("ERROR: cannot execute INSERT in a read-only transaction");
  }

  @Test
  void queryInExplicitTransaction() {
    try (Transaction transaction = readDb.beginTransaction()) {
      List<UTDetail> list = readDb.find(UTDetail.class).findList();
      assertThat(list).hasSize(2);
      assertThat(transaction.isReadOnly()).isTrue();
      transaction.commit(); // autoCommit=true but this is still allowed
    }
  }

  @Test
  void queryInImplicitTransaction() {
    List<UTDetail> list = readDb.find(UTDetail.class).findList();
    assertThat(list).hasSize(2);
  }

  // Requires .register(true).defaultDatabase(true) ... when running this test
  @Disabled
  @Test
  void queryInReadOnlyTransactional() {
    readOnlyTransactional();
    transactionalOnlyContainingQueries();
  }

  @Transactional(readOnly = true)
  void readOnlyTransactional() {
    List<UTDetail> list = readDb.find(UTDetail.class).findList();
    assertThat(list).hasSize(2);

    SpiTransaction current = (SpiTransaction) Transaction.current();
    assertThat(current.isReadOnly()).isTrue();
  }

  @Transactional
  void transactionalOnlyContainingQueries() {
    List<UTDetail> list = readDb.find(UTDetail.class).findList();
    assertThat(list).hasSize(2);

    SpiTransaction current = (SpiTransaction) Transaction.current();
    assertThat(current.isReadOnly()).isTrue();
  }

  private static Database setupReadOnlyDatabase() {
    PostgresContainer.builder("17")
      .dbName("readonly_test")
      .build()
      .start();

    var dataSourceBuilder = DataSourceBuilder.create()
      .username("readonly_test")
      .password("test")
      .url("jdbc:postgresql://localhost:6432/readonly_test");

    var writeDb = databaseBuilder(dataSourceBuilder)
      .ddlGenerate(true)
      .ddlRun(true)
      .build();

    writeDb.truncate(UTDetail.class);
    writeDb.save(new UTDetail("u0", 45, 3D));
    writeDb.save(new UTDetail("u1", 42, 5D));

    return databaseBuilder(dataSourceBuilder)
      .readOnlyDatabase(true)
      // register + default required for the tests using @Transactional
      // .register(true).defaultDatabase(true)
      .build();
  }

  private static DatabaseBuilder databaseBuilder(DataSourceBuilder dataSourceBuilder) {
    return Database.builder()
      .name("ro_test")
      .dataSourceBuilder(dataSourceBuilder)
      .ddlExtra(false)
      .defaultDatabase(false)
      .register(false)
      .addClass(UTDetail.class);
  }
}
