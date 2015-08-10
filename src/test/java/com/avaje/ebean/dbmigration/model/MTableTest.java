package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class MTableTest {

  MTable base() {
    MTable table = new MTable("tab");
    table.addColumn(new MColumn("id","bigint"));
    table.addColumn(new MColumn("name","varchar(20)"));
    table.addColumn(new MColumn("status","varchar(3)"));

    return table;
  }

  MTable newTable() {
    MTable table = new MTable("tab");
    table.addColumn(new MColumn("id","bigint"));
    table.addColumn(new MColumn("name","varchar(20)"));
    table.addColumn(new MColumn("comment","varchar(1000)"));

    return table;
  }

  MTable newTableAdd2Columns() {
    MTable table = new MTable("tab");
    table.addColumn(new MColumn("id","bigint"));
    table.addColumn(new MColumn("name","varchar(20)"));
    table.addColumn(new MColumn("status","varchar(3)"));
    table.addColumn(new MColumn("comment","varchar(1000)"));
    table.addColumn(new MColumn("note","varchar(2000)"));
    return table;
  }

  MTable newTableModifiedColumn() {
    MColumn modCol = new MColumn("name", "varchar(30)");// modified type
    modCol.setNotnull(true);

    MTable table = new MTable("tab");
    table.addColumn(modCol);
    table.addColumn(new MColumn("id","bigint"));
    table.addColumn(new MColumn("status","varchar(3)"));
    return table;
  }

  @Test
  public void testCompare_addColumnDropColumn() throws Exception {

    ModelDiff diff = new ModelDiff();
    diff.compareTables(base(), newTable());

    List<Object> createChanges = diff.getCreateChanges();
    assertThat(createChanges).hasSize(1);
    AddColumn addColumn = (AddColumn)createChanges.get(0);
    assertThat(addColumn.getColumn()).extracting("name").contains("comment");
    assertThat(addColumn.getColumn()).extracting("type").contains("varchar(1000)");

    List<Object> dropChanges = diff.getDropChanges();
    assertThat(dropChanges).hasSize(1);

    DropColumn dropColumn = (DropColumn)dropChanges.get(0);
    assertThat(dropColumn.getColumnName()).isEqualTo("status");
    assertThat(dropColumn.getTableName()).isEqualTo("tab");
  }

  @Test
  public void testCompare_addTwoColumnsToSameTable() throws Exception {

    ModelDiff diff = new ModelDiff();
    diff.compareTables(base(), newTableAdd2Columns());

    List<Object> createChanges = diff.getCreateChanges();
    assertThat(createChanges).hasSize(1);

    AddColumn addColumn = (AddColumn)createChanges.get(0);
    assertThat(addColumn.getColumn()).extracting("name").contains("comment","note");
    assertThat(addColumn.getColumn()).extracting("type").contains("varchar(1000)","varchar(2000)");

    assertThat(diff.getDropChanges()).hasSize(0);

  }

  @Test
  public void testCompare_modifyColumn() throws Exception {

    ModelDiff diff = new ModelDiff();
    diff.compareTables(base(), newTableModifiedColumn());

    List<Object> createChanges = diff.getCreateChanges();
    assertThat(createChanges).hasSize(1);

    AlterColumn alterColumn = (AlterColumn)createChanges.get(0);
    assertThat(alterColumn.getColumnName()).isEqualTo("name");
    assertThat(alterColumn.getType()).isEqualTo("varchar(30)");
    assertThat(alterColumn.isNotnull()).isEqualTo(true);
    assertThat(alterColumn.getUnique()).isNull();
    assertThat(alterColumn.getCheckConstraint()).isNull();
    assertThat(alterColumn.getReferences()).isNull();

    assertThat(diff.getDropChanges()).hasSize(0);

  }
}