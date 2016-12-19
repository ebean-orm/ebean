package io.ebeaninternal.server.query;

import org.junit.Test;

import static org.junit.Assert.*;

public class SqlTreeBuilderTest {

  @Test
  public void mergeOnDistinct_equal() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.id"), "t0.id");
  }

  @Test
  public void mergeOnDistinct_add() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_contained() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre, t0.id"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_overlap() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id, t1.id", "t0.cre, t0.id"), "t0.cre, t0.id, t1.id");
  }

  @Test
  public void mergeOnDistinct_overlapBoth() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id, t1.id", "t0.cre, t1.id, t0.id"), "t0.cre, t1.id, t0.id");
  }

  @Test
  public void mergeOnDistinct_inlineAscDesc() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre asc, t1.bb desc, t3.b"), "t0.cre, t1.bb, t3.b, t0.id");
  }

  @Test
  public void mergeOnDistinct_inlineAscDesc2() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre desc, t1.bb asc, t3.b"), "t0.cre, t1.bb, t3.b, t0.id");
  }

  @Test
  public void mergeOnDistinct_trailingAsc() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre asc"), "t0.cre, t0.id");
  }

  @Test
  public void mergeOnDistinct_trailingDesc() throws Exception {

    assertEquals(SqlTreeBuilder.mergeOnDistinct("t0.id", "t0.cre desc"), "t0.cre, t0.id");
  }

}
