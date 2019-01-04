package io.ebeaninternal.dbmigration.model;


import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.DropTable;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.migrationreader.MigrationXmlReader;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelContainerTest {

  @Test
  public void apply_when_noPendingDrops_then_emptyPending() {

    ModelContainer container = new ModelContainer();
    container.apply(mig("1.0.model.xml"), ver("1.1"));
    assertThat(container.getPendingDrops()).isEmpty();
  }


  @Test
  public void apply_when_pendingDrops_then_registeredHistoryTable() {

    ModelContainer base = container_1_1();

    MTable table = base.getTable("document");
    assertThat(table.allHistoryColumns(true)).doesNotContain("zing");

    container_1_1().registerPendingHistoryDropColumns(base);
    assertThat(table.allHistoryColumns(true)).contains("zing");
    assertThat(table.allHistoryColumns(false)).doesNotContain("zing");
  }


  @Test
  public void apply_when_pendingDropsApplied_then_droppedTableNotInHistory() {

    ModelContainer container = container_1_1();
    container.apply(mig("1.1_2__drops.model.xml"), ver("1.1_2"));

    ModelContainer base = container_1_1();

    container.registerPendingHistoryDropColumns(base);

    assertThat(base.getTable("document").allHistoryColumns(false)).doesNotContain("zing");
    assertThat(base.getTable("document").allHistoryColumns(true)).doesNotContain("zing");
  }

  @Test
  public void apply_when_apply_partial_pendingDrops_then_some_remainder() {

    ModelContainer container = container_2_1();
    container.apply(mig("2.2__drops.model.xml"), ver("2.2"));

    ModelContainer base = container_2_1();

    container.registerPendingHistoryDropColumns(base);

    List<String> normalColumns = base.getTable("document").allHistoryColumns(false);
    List<String> historyColumns = base.getTable("document").allHistoryColumns(true);

    assertThat(historyColumns).contains("zong", "boom", "baz", "bar");
    assertThat(normalColumns).doesNotContain("zing", "zong", "boom", "baz", "bar");
  }

  @Test
  public void apply_sql() {
    ModelContainer container = new ModelContainer();
    container.apply(mig("3.0__rawSql.model.xml"), ver("3.0"));
    assertThat(container.getPendingDrops()).isEmpty();
  }

  @Test
  public void apply_alterForeignKey() {
    ModelContainer container = new ModelContainer();
    container.apply(mig("4.0__alterForeignKey.model.xml"), ver("4.0"));
    assertThat(container.getPendingDrops()).isEmpty();
  }

  @Test
  public void apply_dropTable_when_notInModel_then_ok() {

    ModelContainer container = new ModelContainer();
    container.apply(mig("5.0__dropTable.model.xml"), ver("5.0"));
    assertThat(container.getTables()).isEmpty();
  }

  @Test
  public void apply_drop_when_notInModel_then_ok() {

    ModelContainer container = new ModelContainer();
    DropTable dropTable = new DropTable();
    dropTable.setName("DoesNotExist");
    container.applyChange(dropTable);

    DropIndex dropIndex = new DropIndex();
    dropIndex.setIndexName("DoesNotExist");
    container.applyChange(dropIndex);

    DropHistoryTable dropHistoryTable = new DropHistoryTable();
    dropHistoryTable.setBaseTable("DoesNotExist");
    container.applyChange(dropHistoryTable);
  }

  private ModelContainer container_2_1() {
    ModelContainer container = new ModelContainer();
    container.apply(mig("2.0.model.xml"), ver("2.0"));
    container.apply(mig("2.1.model.xml"), ver("2.1"));
    return container;
  }

  private ModelContainer container_1_1() {
    ModelContainer container = new ModelContainer();
    container.apply(mig("1.0.model.xml"), ver("1.0"));
    container.apply(mig("1.1.model.xml"), ver("1.1"));
    return container;
  }

  private MigrationVersion ver(String version) {
    return MigrationVersion.parse(version);
  }

  private Migration mig(String path) {
    return MigrationXmlReader.read(ModelContainerTest.class.getResourceAsStream(path));
  }
}
