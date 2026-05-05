package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TableJoinColumnTest {

  TableJoinColumn col(String localDbColumn, String foreignDbColumn, boolean insertable, boolean updateable) {
    DeployTableJoinColumn column = new DeployTableJoinColumn(localDbColumn, foreignDbColumn, insertable, updateable, true);
    return new TableJoinColumn(column);
  }

  @Test
  public void equals_when_same() {
    assertSame(col("a", "b", true, true), col("a", "b", true, true));
  }

  @Test
  public void equals_when_diffFirstCol() {
    assertDifferent(col("a", "b", true, true), col("c", "b", true, true));
  }

  @Test
  public void equals_when_diffSecondCol() {
    assertDifferent(col("a", "b", true, true), col("a", "c", true, true));
  }

  @Test
  public void equals_when_diffInsertable() {
    assertDifferent(col("a", "b", true, true), col("a", "b", false, true));
  }

  @Test
  public void equals_when_diffUpdateable() {
    assertDifferent(col("a", "b", true, true), col("a", "b", true, false));
  }

  private void assertDifferent(TableJoinColumn col, TableJoinColumn col2) {
    assertThat(col).isNotEqualTo(col2);
    assertThat(col.hashCode()).isNotEqualTo(col2.hashCode());
  }

  private void assertSame(TableJoinColumn col, TableJoinColumn col2) {
    assertThat(col).isEqualTo(col2);
    assertThat(col.hashCode()).isEqualTo(col2.hashCode());
  }
}
