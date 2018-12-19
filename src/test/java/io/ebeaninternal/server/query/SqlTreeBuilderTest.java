package io.ebeaninternal.server.query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlTreeBuilderTest {

  @Test
  public void mergeOnDistinct_equal() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.id"), "t0.id");
  }

  @Test
  public void mergeOnDistinct_add() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_contained() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre, t0.id"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_overlap() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id, t1.id", "t0.cre, t0.id"), "t0.cre, t0.id, t1.id");
  }

  @Test
  public void mergeOnDistinct_overlapBoth() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id, t1.id", "t0.cre, t1.id, t0.id"), "t0.cre, t1.id, t0.id");
  }

  @Test
  public void mergeOnDistinct_inlineAscDesc() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre asc, t1.bb desc, t3.b"), "t0.cre, t1.bb, t3.b, t0.id");
  }

  @Test
  public void mergeOnDistinct_inlineAscDesc2() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre desc, t1.bb asc, t3.b"), "t0.cre, t1.bb, t3.b, t0.id");
  }

  @Test
  public void mergeOnDistinct_trailingAsc() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre asc"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_trailingDesc() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre desc"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_trailingDescNullsFirst() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre desc nulls first"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_trailingDescNullsLast() {
    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre desc nulls last"), "t0.cre, t0.id");
  }

}
