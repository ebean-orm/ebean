package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropTable;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MTableTest {

  static MTable base() {
    MTable table = new MTable("tab");
    table.addColumn(new MColumn("id", "bigint"));
    table.addColumn(new MColumn("name", "varchar(20)"));
    table.addColumn(new MColumn("status", "varchar(3)"));

    return table;
  }

  static MTable newTable() {
    MTable table = new MTable("tab");
    table.addColumn(new MColumn("id", "bigint"));
    table.addColumn(new MColumn("name", "varchar(20)"));
    table.addColumn(new MColumn("comment", "varchar(1000)"));

    return table;
  }

  static MTable newTableAdd2Columns() {
    MTable table = new MTable("tab");
    table.addColumn(new MColumn("id", "bigint"));
    table.addColumn(new MColumn("name", "varchar(20)"));
    table.addColumn(new MColumn("status", "varchar(3)"));
    table.addColumn(new MColumn("comment", "varchar(1000)"));
    table.addColumn(new MColumn("note", "varchar(2000)"));
    return table;
  }

  static MTable newTableModifiedColumn() {
    MColumn modCol = new MColumn("name", "varchar(30)");// modified type
    modCol.setNotnull(true);

    MTable table = new MTable("tab");
    table.addColumn(modCol);
    table.addColumn(new MColumn("id", "bigint"));
    table.addColumn(new MColumn("status", "varchar(3)"));
    return table;
  }

  @Test
  public void test_allHistoryColumns() throws Exception {

    MTable base = base();
    base.registerPendingDropColumn("fullName");
    base.registerPendingDropColumn("last");

    assertThat(base.allHistoryColumns(false)).containsExactly("id", "name", "status");
    assertThat(base.allHistoryColumns(true)).containsExactly("id", "name", "status", "fullName", "last");
  }

  @Test
  public void test_dropTable() {

    MTable base = base();
    DropTable dropTable = base.dropTable();
    assertThat(dropTable.getName()).isEqualTo(base.getName());
  }

  @Test
  public void test_compare_addColumnDropColumn() throws Exception {

    ModelDiff diff = new ModelDiff();
    diff.compareTables(base(), newTable());

    List<Object> createChanges = diff.getApplyChanges();
    assertThat(createChanges).hasSize(1);
    AddColumn addColumn = (AddColumn) createChanges.get(0);
    assertThat(addColumn.getColumn()).extracting("name").contains("comment");
    assertThat(addColumn.getColumn()).extracting("type").contains("varchar(1000)");

    List<Object> dropChanges = diff.getDropChanges();
    assertThat(dropChanges).hasSize(1);

    DropColumn dropColumn = (DropColumn) dropChanges.get(0);
    assertThat(dropColumn.getColumnName()).isEqualTo("status");
    assertThat(dropColumn.getTableName()).isEqualTo("tab");
  }

  @Test
  public void test_compare_addTwoColumnsToSameTable() throws Exception {

    ModelDiff diff = new ModelDiff();
    diff.compareTables(base(), newTableAdd2Columns());

    List<Object> createChanges = diff.getApplyChanges();
    assertThat(createChanges).hasSize(1);

    AddColumn addColumn = (AddColumn) createChanges.get(0);
    assertThat(addColumn.getColumn()).extracting("name").contains("comment", "note");
    assertThat(addColumn.getColumn()).extracting("type").contains("varchar(1000)", "varchar(2000)");

    assertThat(diff.getDropChanges()).hasSize(0);

  }

  @Test
  public void test_compare_modifyColumn() throws Exception {

    ModelDiff diff = new ModelDiff();
    diff.compareTables(base(), newTableModifiedColumn());

    List<Object> createChanges = diff.getApplyChanges();
    assertThat(createChanges).hasSize(1);

    AlterColumn alterColumn = (AlterColumn) createChanges.get(0);
    assertThat(alterColumn.getColumnName()).isEqualTo("name");
    assertThat(alterColumn.getType()).isEqualTo("varchar(30)");
    assertThat(alterColumn.isNotnull()).isEqualTo(true);
    assertThat(alterColumn.getUnique()).isNull();
    assertThat(alterColumn.getCheckConstraint()).isNull();
    assertThat(alterColumn.getReferences()).isNull();

    assertThat(diff.getDropChanges()).hasSize(0);

  }

  @Test
  public void test_apply_dropColumn() {

    MTable base = base();

    DropColumn dropColumn = new DropColumn();
    dropColumn.setTableName("tab");
    dropColumn.setColumnName("name");

    base.apply(dropColumn);
    assertThat(base.getColumn("name")).isNull();
  }

  @Test(expected = IllegalStateException.class)
  public void test_apply_dropColumn_doesNotExist() {

    MTable base = base();

    DropColumn dropColumn = new DropColumn();
    dropColumn.setTableName(base.getName());
    dropColumn.setColumnName("DoesNotExist");
    base.apply(dropColumn);
  }

  @Test(expected = IllegalStateException.class)
  public void test_apply_alterColumn_doesNotExist() {

    MTable base = base();

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName(base.getName());
    alterColumn.setColumnName("DoesNotExist");
    alterColumn.setType("integer");
    base.apply(alterColumn);
  }

  @Test
  public void test_apply_alterColumn_type() {

    MTable base = base();

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setTableName(base.getName());
    alterColumn.setColumnName("id");
    alterColumn.setType("uuid");
    base.apply(alterColumn);

    assertThat(base.getColumn("id").getType()).isEqualTo("uuid");
  }

  @Test
  public void test_compare_addAndDropColumn() throws Exception {

    MTable base = base();
    MTable newTable = newTable();

    ModelDiff diff = new ModelDiff();
    base.compare(diff, newTable);

    assertThat(diff.getApplyChanges()).hasSize(1);
    assertThat(diff.getDropChanges()).hasSize(1);
  }

  @Test
  public void test_compare_addHistoryToTable() {

    MTable base = base();
    MTable withHistory = base();
    withHistory.setWithHistory(true);

    ModelDiff diff = new ModelDiff();
    base.compare(diff, withHistory);

    assertThat(diff.getDropChanges()).isEmpty();
    assertThat(diff.getApplyChanges()).hasSize(1);
    assertThat(diff.getApplyChanges().get(0)).isInstanceOf(AddHistoryTable.class);
  }

  @Test
  public void test_compare_removeHistoryFromTable() throws Exception {

    MTable withHistory = base();
    withHistory.setWithHistory(true);

    MTable noHistory = base();

    ModelDiff diff = new ModelDiff();
    withHistory.compare(diff, noHistory);

    assertThat(diff.getApplyChanges()).isEmpty();
    assertThat(diff.getDropChanges()).hasSize(1);
    assertThat(diff.getDropChanges().get(0)).isInstanceOf(DropHistoryTable.class);
  }

}
