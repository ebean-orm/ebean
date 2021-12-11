package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.AlterColumn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MColumnTest {

  private final MTable table = new MTable("tab");

  private MColumn basic() {
    return new MColumn("col", "integer");
  }

  private ModelDiff diff() {
    return new ModelDiff();
  }

  @Test
  void localDateTime() {
    assertTrue(basic().localDateTime("timestamp", "localdatetime"));
  }

  @Test
  void localDateTime_when_not() {
    assertFalse(basic().localDateTime("other", "localdatetime"));
    assertFalse(basic().localDateTime("timestamp", "other"));
    assertFalse(basic().localDateTime("localdatetime", "timestamp"));
    assertFalse(basic().localDateTime("timestamp2", "localdatetime"));
  }

  @Test
  void noDiff(){
    ModelDiff diff = diff();
    basic().compare(diff, table, basic());

    assertThat(diff.getApplyChanges()).isEmpty();
    assertThat(diff.getDropChanges()).isEmpty();
  }

  @Test
  void diffType() {
    ModelDiff diff = diff();
    basic().compare(diff, table, new MColumn("col", "integer(8)"));

    assertChanges(diff);
    AlterColumn alterColumn = getAlterColumn(diff);
    assertThat(alterColumn.getType()).isEqualTo("integer(8)");
  }

  @Test
  void diffNotNull() {
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
  void diffNull() {
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
  void applyNotNull_expect_notNull() {
    MColumn newCol = basic();
    newCol.setNotnull(true);

    AlterColumn alterColumn = new AlterColumn();
    alterColumn.setNotnull(Boolean.FALSE);
    newCol.apply(alterColumn);

    assertThat(newCol.isNotnull()).isFalse();
  }

  @Test
  void diffCheckAdd() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setCheckConstraint("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getCheckConstraint()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getDropCheckConstraint()).isNull();
  }

  @Test
  void diffCheckRemove() {
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
  void diffCheckChange() {
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
  void diffDefaultValueAdd() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setDefaultValue("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDefaultValue()).isEqualTo("abc");
  }

  @Test
  void diffDefaultValueRemove() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setDefaultValue("abc");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDefaultValue()).isEqualTo("DROP DEFAULT");
  }

  @Test
  void diffDefaultValueChange() {
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
  void diffReferencesAdd() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setReferences("abc");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getReferences()).isEqualTo("abc");
    assertThat(getAlterColumn(diff).getDropForeignKey()).isNull();
  }

  @Test
  void diffReferencesRemove() {
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
  void diffReferencesChange() {
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
  void diffUniqueAdd() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setUnique("uq_one");
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getUnique()).isEqualTo("uq_one");
  }

  @Test
  void diffUniqueRemove() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setUnique("uq_one");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDropUnique()).isEqualTo("uq_one");
  }

  @Test
  void diffUniqueOneToOneAdd() {
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
  void diffUniqueOneToOneRemove() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    MColumn oldCol = basic();
    oldCol.setUniqueOneToOne("uq_new");
    oldCol.compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).getDropUnique()).isEqualTo("uq_new");
  }

  @Test
  void diffHistoryExcludeAdd() {
    ModelDiff diff = diff();
    MColumn newCol = basic();
    newCol.setHistoryExclude(true);
    basic().compare(diff, table, newCol);

    assertChanges(diff);
    assertThat(getAlterColumn(diff).isHistoryExclude()).isEqualTo(true);
  }

  @Test
  void diffHistoryExcludeRemove() {
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
