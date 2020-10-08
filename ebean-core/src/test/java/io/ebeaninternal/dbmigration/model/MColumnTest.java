package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.AlterColumn;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


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

    assertThat(diff.getApplyChanges()).isEmpty();
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
    assertThat(alterColumn.getUnique()).isNull();
    assertThat(alterColumn.getUniqueOneToOne()).isNull();
    assertThat(alterColumn.getDefaultValue()).isNull();
  }

  @Test
  public void diffNull() throws Exception {

    ModelDiff diff = diff();

    MColumn newCol = basic();
    newCol.setNotnull(false);

    MColumn baseCol = basic();
    baseCol.setNotnull(true);

    baseCol.compare(diff, table, newCol);

    assertChanges(diff);
    AlterColumn alterColumn = getAlterColumn(diff);
    assertThat(alterColumn.isNotnull()).isEqualTo(false);
  }

  @Test
  public void applyNotNull_expect_notNull() throws Exception {

    MColumn newCol = basic();
    newCol.setNotnull(true);

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setNotnull(Boolean.FALSE);
    newCol.apply(alterColumn);

    assertThat(newCol.isNotnull()).isFalse();
  }

  @Test
  public void diffCheckAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setCheckConstraint("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getCheckConstraint()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getDropCheckConstraint()).isNull();
  }

  @Test
  public void diffCheckRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setCheckConstraint("z");
    oldCol.setCheckConstraintName("abc");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getCheckConstraint()).isNull();
    assertThat(getAlterColumn(diff).getDropCheckConstraint()).isEqualTo("abc");
  }

  @Test
  public void diffCheckChange() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setCheckConstraint("abc");
    MColumn oldCol = basic();
    oldCol.setCheckConstraint("z");
    oldCol.setCheckConstraintName("d");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getCheckConstraint()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getDropCheckConstraint()).isNull();
  }

  @Test
  public void diffDefaultValueAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setDefaultValue("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDefaultValue()).isEqualTo("abc");
  }

  @Test
  public void diffDefaultValueRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setDefaultValue("abc");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDefaultValue()).isEqualTo("DROP DEFAULT");
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
    assertThat(getAlterColumn(diff).getDefaultValue()).isEqualTo("abc");
  }

  @Test
  public void diffReferencesAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setReferences("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getReferences()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getDropForeignKey()).isNull();
  }


  @Test
  public void diffReferencesRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setReferences("abc");
    oldCol.setForeignKeyName("fk_ab");
    oldCol.setForeignKeyIndex("ix_ab");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getReferences()).isNull();
    assertThat(getAlterColumn(diff).getForeignKeyName()).isNull();
    assertThat(getAlterColumn(diff).getForeignKeyIndex()).isNull();

    assertThat(getAlterColumn(diff).getDropForeignKey()).isEqualTo("fk_ab");
    assertThat(getAlterColumn(diff).getDropForeignKeyIndex()).isEqualTo("ix_ab");
  }

  @Test
  public void diffReferencesChange() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setReferences("ab");
    newCol.setForeignKeyName("fk_ab");
    newCol.setForeignKeyIndex("ix_ab");

    MColumn oldCol = basic();
    oldCol.setReferences("d");
    oldCol.setForeignKeyName("fk_d");
    oldCol.setForeignKeyIndex("ix_d");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);

    assertThat(getAlterColumn(diff).getReferences()).isEqualTo("ab");
    assertThat(getAlterColumn(diff).getForeignKeyName()).isEqualTo("fk_ab");
    assertThat(getAlterColumn(diff).getForeignKeyIndex()).isEqualTo("ix_ab");

    assertThat(getAlterColumn(diff).getDropForeignKey()).isEqualTo("fk_d");
    assertThat(getAlterColumn(diff).getDropForeignKeyIndex()).isEqualTo("ix_d");
  }

  @Test
  public void diffUniqueAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setUnique("uq_one");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getUnique()).isEqualTo("uq_one");
  }

  @Test
  public void diffUniqueRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setUnique("uq_one");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDropUnique()).isEqualTo("uq_one");
  }

  @Test
  public void diffUniqueOneToOneAdd() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setUniqueOneToOne("uq_new");
    MColumn oldCol = basic();
    oldCol.setUniqueOneToOne("uq_old");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getUniqueOneToOne()).isEqualTo("uq_new");
    assertThat(getAlterColumn(diff).getDropUnique()).isEqualTo("uq_old");
  }

  @Test
  public void diffUniqueOneToOneRemove() throws Exception {

    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setUniqueOneToOne("uq_new");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDropUnique()).isEqualTo("uq_new");
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

  private AlterColumn getAlterColumn(ModelDiff diff) {
    return (AlterColumn) diff.getApplyChanges().get(0);
  }

  private void assertChanges(ModelDiff diff) {
    assertThat(diff.getDropChanges()).isEmpty();
    assertThat(diff.getApplyChanges()).hasSize(1);
  }

}
