package io.ebean;

import io.ebean.RawSql;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ColumnMappingTest {

  RawSql.ColumnMapping.Column col(int indexPos, String dbColumn, String dbAlias) {
    return new RawSql.ColumnMapping.Column(indexPos, dbColumn, dbAlias);
  }

  RawSql.ColumnMapping mapping(RawSql.ColumnMapping.Column... cols) {
    return new RawSql.ColumnMapping(Arrays.asList(cols));
  }

  @Test
  public void equals_same() {

    RawSql.ColumnMapping mapping1 = mapping(col(1, "id", null), col(2, "name", null));
    RawSql.ColumnMapping mapping2 = mapping(col(1, "id", null), col(2, "name", null));

    assertSame(mapping1, mapping2);
  }

  @Test
  public void equals_diffPropertyName() {

    RawSql.ColumnMapping mapping1 = mapping(col(1, "id", null), col(2, "name", null));
    RawSql.ColumnMapping mapping2 = mapping(col(1, "id", null), col(2, "diff", null));

    assertDifferent(mapping1, mapping2);
  }

  @Test
  public void equals_moreColumns() {

    RawSql.ColumnMapping mapping1 = mapping(col(1, "id", null), col(2, "name", null));
    RawSql.ColumnMapping mapping2 = mapping(col(1, "id", null), col(2, "name", null), col(2, "diff", null));

    assertDifferent(mapping1, mapping2);
  }


  @Test
  public void equals_lessColumns() {

    RawSql.ColumnMapping mapping1 = mapping(col(1, "id", null), col(2, "name", null));
    RawSql.ColumnMapping mapping2 = mapping(col(1, "id", null));

    assertDifferent(mapping1, mapping2);
  }

  private void assertSame(Object key, Object key1) {
    assertThat(key).isEqualTo(key1);
    assertThat(key.hashCode()).isEqualTo(key1.hashCode());
  }

  private void assertDifferent(Object key, Object key1) {
    assertThat(key).isNotEqualTo(key1);
    assertThat(key.hashCode()).isNotEqualTo(key1.hashCode());
  }
}
