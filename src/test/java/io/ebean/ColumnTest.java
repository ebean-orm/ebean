package io.ebean;


import io.ebeaninternal.server.rawsql.SpiRawSql;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ColumnTest {

  SpiRawSql.ColumnMapping.Column col(int indexPos, String dbColumn, String dbAlias) {
    return new SpiRawSql.ColumnMapping.Column(indexPos, dbColumn, dbAlias);
  }

  @Test
  public void equals_when_same() {
    assertSame(col(1, "name", null), col(1, "name", null));
  }

  @Test
  public void equals_when_same_withAlias() {
    assertSame(col(1, "t0.name", "t0"), col(1, "t0.name", "t0"));
  }

  @Test
  public void equals_when_diffIndex() {
    assertDifferent(col(1, "name", null), col(2, "name", null));
  }

  @Test
  public void equals_when_diffProperty() {
    assertDifferent(col(1, "name", null), col(1, "diffName", null));
  }

  @Test
  public void equals_when_diffAlias() {
    assertDifferent(col(1, "t1.name", "t1"), col(1, "t1.name", "t0"));
  }

  @Test
  public void equals_when_diffAliasNullLast() {
    assertDifferent(col(1, "t1.name", "t1"), col(1, "t1.name", null));
  }

  @Test
  public void equals_when_diffAliasNullFirst() {
    assertDifferent(col(1, "t1.name", null), col(1, "t1.name", "t1"));
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
