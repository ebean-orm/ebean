package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AlterColumn;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


public class MColumnTest {

  MTable table = new MTable("tab");

  MColumn basic() {
    return new MColumn("col", "integer");
  }

  ModelDiff diff() {
    return new ModelDiff();
  }

  @Test
  public void noDiff() throws Exception {

    ModelDiff diff = diff();
    basic().compare(diff, table, basic());

    assertThat(diff.getCreateChanges()).isEmpty();
    assertThat(diff.getDropChanges()).isEmpty();
  }

  @Test
  public void diffType() throws Exception {

    ModelDiff diff = diff();
    basic().compare(diff, table, new MColumn("col", "integer(8)"));

    assertChanges(diff);
    AlterColumn alterColumn = getAlterColumn(diff);
    assertThat(alterColumn.getType()).isEqualTo("integer(8)");
  }

  @Test
  public void diffNotNull() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setNotnull(true);
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    AlterColumn alterColumn = getAlterColumn(diff);
    assertThat(alterColumn.isNotnull()).isEqualTo(true);

    assertThat(alterColumn.getType()).isNull();
    assertThat(alterColumn.isUnique()).isNull();
    assertThat(alterColumn.isUniqueOneToOne()).isNull();
    assertThat(alterColumn.getNewDefaultValue()).isNull();
  }

  @Test
  public void diffCheckAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setCheckConstraint("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewCheckConstraint()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getOldCheckConstraint()).isNull();
  }

  @Test
  public void diffCheckRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setCheckConstraint("abc");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewCheckConstraint()).isNull();
    assertThat(getAlterColumn(diff).getOldCheckConstraint()).isEqualTo("abc");
  }

  @Test
  public void diffCheckChange() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setCheckConstraint("abc");
    MColumn oldCol = basic();
    oldCol.setCheckConstraint("d");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewCheckConstraint()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getOldCheckConstraint()).isEqualTo("d");
  }

  @Test
  public void diffDefaultValueAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setDefaultValue("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewDefaultValue()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getOldDefaultValue()).isNull();
  }

  @Test
  public void diffDefaultValueRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setDefaultValue("abc");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewDefaultValue()).isNull();
    assertThat(getAlterColumn(diff).getOldDefaultValue()).isEqualTo("abc");
  }

  @Test
  public void diffDefaultValueChange() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setDefaultValue("abc");
    MColumn oldCol = basic();
    oldCol.setDefaultValue("d");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewDefaultValue()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getOldDefaultValue()).isEqualTo("d");
  }

  @Test
  public void diffReferencesAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setReferences("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewReferences()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getOldReferences()).isNull();
  }


  @Test
  public void diffReferencesRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setReferences("abc");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewReferences()).isNull();
    assertThat(getAlterColumn(diff).getOldReferences()).isEqualTo("abc");
  }

  @Test
  public void diffReferencesChange() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setReferences("abc");
    MColumn oldCol = basic();
    oldCol.setReferences("d");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getNewReferences()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getOldReferences()).isEqualTo("d");
  }

  @Test
  public void diffUniqueAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setUnique(true);
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).isUnique()).isEqualTo(true);
  }

  @Test
  public void diffUniqueRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setUnique(true);
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).isUnique()).isEqualTo(false);
  }

  @Test
  public void diffUniqueOneToOneAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setUniqueOneToOne(true);
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).isUniqueOneToOne()).isEqualTo(true);
  }

  @Test
  public void diffUniqueOneToOneRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setUniqueOneToOne(true);
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).isUniqueOneToOne()).isEqualTo(false);
  }

  @Test
  public void diffHistoryExcludeAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setHistoryExclude(true);
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).isHistoryExclude()).isEqualTo(true);
  }

  @Test
  public void diffHistoryExcludeRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setHistoryExclude(true);
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).isHistoryExclude()).isEqualTo(false);
  }

  @NotNull
  private AlterColumn getAlterColumn(ModelDiff diff) {
    return (AlterColumn) diff.getCreateChanges().get(0);
  }

  private void assertChanges(ModelDiff diff) {
    assertThat(diff.getDropChanges()).isEmpty();
    assertThat(diff.getCreateChanges()).hasSize(1);
  }

}