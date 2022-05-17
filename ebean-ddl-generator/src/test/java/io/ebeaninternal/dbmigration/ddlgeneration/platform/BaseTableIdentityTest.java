package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.NamingConvention;
import io.ebean.config.UnderscoreNamingConvention;
import io.ebean.platform.postgres.PostgresPlatform;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.IdentityType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BaseTableIdentityTest {

  static final PlatformDdl postgresDdl = new PostgresDdl(new PostgresPlatform());
  static final NamingConvention namingConvention = new UnderscoreNamingConvention();
  static {
    namingConvention.setDatabasePlatform(new PostgresPlatform());
  }

  @Test
  void identity_noPk() {
    CreateTable createTable = table(column("c0"), column("c1"));
    BaseTableIdentity bti = new BaseTableIdentity(createTable, postgresDdl, namingConvention);
    DdlIdentity identity = bti.identity();

    assertFalse(identity.useIdentity());
    assertFalse(bti.hasPrimaryKey());
    assertThat(bti.pkColumns()).isEmpty();
  }

  @Test
  void identity_singlePk() {
    CreateTable createTable = table(column("c0", true), column("c1"));
    BaseTableIdentity bti = new BaseTableIdentity(createTable, postgresDdl, namingConvention);
    DdlIdentity identity = bti.identity();

    assertTrue(identity.useIdentity());
    assertTrue(bti.hasPrimaryKey());
    assertThat(bti.pkColumns()).hasSize(1);
  }

  @Test
  void identity_singlePk_useSequence_deriveName() {

    BaseTableIdentity bti = new BaseTableIdentity(createTableWithSequence(null), postgresDdl, namingConvention);
    DdlIdentity identity = bti.identity();

    assertThat(identity.getSequenceName()).isEqualTo("tab0_seq");
    assertFalse(identity.useIdentity());
    assertTrue(bti.hasPrimaryKey());
    assertThat(bti.pkColumns()).hasSize(1);
  }

  @Test
  void identity_singlePk_useSequence_explicitName() {
    CreateTable createTable = createTableWithSequence("myseq");
    BaseTableIdentity bti = new BaseTableIdentity(createTable, postgresDdl, namingConvention);
    DdlIdentity identity = bti.identity();

    assertThat(identity.getSequenceName()).isEqualTo("myseq");
    assertFalse(identity.useIdentity());
    assertTrue(bti.hasPrimaryKey());
    assertThat(bti.pkColumns()).hasSize(1);
  }

  private CreateTable createTableWithSequence(String seqName) {
    CreateTable createTable = table(column("c0", true), column("c1"));
    createTable.setIdentityType(IdentityType.SEQUENCE);
    createTable.setName("tab0");
    createTable.setSequenceName(seqName);
    return createTable;
  }

  @Test
  void identity_singlePk_withPartitionColumn() {
    CreateTable createTable = table(column("c0", true), column("c1"), column("pc"));
    createTable.setPartitionMode("DAY");
    createTable.setPartitionColumn("pc");

    BaseTableIdentity bti = new BaseTableIdentity(createTable, postgresDdl, namingConvention);
    DdlIdentity identity = bti.identity();

    assertTrue(identity.useIdentity());
    assertTrue(bti.hasPrimaryKey());
    assertThat(bti.pkColumns()).hasSize(2);
    assertThat(bti.pkColumns().get(0).getName()).isEqualTo("c0");
    assertThat(bti.pkColumns().get(1).getName()).isEqualTo("pc");
  }

  @Test
  void identity_singlePk_withPartitionColumnAlreadyInPk() {
    CreateTable createTable = table(column("c0", true), column("c1"), column("pc", true));
    createTable.setPartitionMode("DAY");
    createTable.setPartitionColumn("pc");

    BaseTableIdentity bti = new BaseTableIdentity(createTable, postgresDdl, namingConvention);
    DdlIdentity identity = bti.identity();

    assertFalse(identity.useIdentity());
    assertTrue(bti.hasPrimaryKey());
    assertThat(bti.pkColumns()).hasSize(2);
    assertThat(bti.pkColumns().get(0).getName()).isEqualTo("c0");
    assertThat(bti.pkColumns().get(1).getName()).isEqualTo("pc");
  }


  CreateTable table(Column... cols) {
    CreateTable createTable = new CreateTable();
    for (Column col : cols) {
      createTable.getColumn().add(col);
    }
    return createTable;
  }

  Column column(String name) {
    return column(name, null);
  }

  Column column(String name, Boolean primaryKey) {
    Column col = new Column();
    col.setName(name);
    col.setPrimaryKey(primaryKey);
    return col;
  }
}
