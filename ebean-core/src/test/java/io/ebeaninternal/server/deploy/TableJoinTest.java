package io.ebeaninternal.server.deploy;


import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TableJoinTest {

  DeployTableJoinColumn col(String localDbColumn, String foreignDbColumn, boolean insertable, boolean updateable, boolean nullable) {
    return new DeployTableJoinColumn(localDbColumn, foreignDbColumn, insertable, updateable, nullable);
  }

  TableJoin table(String tableName, String col1, String col2) {
    DeployTableJoin deploy = new DeployTableJoin();
    deploy.setTable(tableName);
    deploy.addJoinColumn(col(col1, col2, true, true, true));
    return new TableJoin(deploy);
  }

  TableJoin table(String tableName, String col1, String col2, String col3, String col4) {
    DeployTableJoin deploy = new DeployTableJoin();
    deploy.setTable(tableName);
    deploy.addJoinColumn(col(col1, col2, true, true, true));
    deploy.addJoinColumn(col(col3, col4, true, true, true));
    return new TableJoin(deploy);
  }

  @Test
  public void equals_when_same() {
    assertSame(table("myTable", "a", "b"), table("myTable", "a", "b"));
  }

  @Test
  public void equals_when_diffTable() {
    assertDifferent(table("myTable", "a", "b"), table("diffTable", "a", "b"));
  }

  @Test
  public void equals_when_diffColumn() {
    assertDifferent(table("myTable", "a", "b"), table("myTable", "c", "b"));
  }

  @Test
  public void equals_when_moreColumns() {
    assertDifferent(table("myTable", "a", "b"), table("myTable", "a", "b", "c", "d"));
  }

  @Test
  public void equals_when_lessColumns() {
    assertDifferent(table("myTable", "a", "b", "c", "d"), table("myTable", "a", "b"));
  }

  void assertSame(TableJoin join1, TableJoin join2) {
    assertThat(join1).isEqualTo(join1);
    assertThat(join1.hashCode()).isEqualTo(join1.hashCode());
  }


  void assertDifferent(TableJoin join1, TableJoin join2) {
    assertThat(join1).isNotEqualTo(join2);
    assertThat(join1.hashCode()).isNotEqualTo(join2.hashCode());
  }

}
