package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlTreeAliasTest {


  @Test
  public void parseRootAlias_when_rootAliasIsNull() {

    SqlTreeAlias treeAlias = new SqlTreeAlias(null, SpiQuery.TemporalMode.CURRENT);

    assertEquals("A B", treeAlias.parseRootAlias("${}A ${}B"));
    assertEquals("ABC", treeAlias.parseRootAlias("A${}B${}C"));
  }

  @Test
  public void parseRootAlias_when_rootAliasHasValue() {

    SqlTreeAlias treeAlias = new SqlTreeAlias("t0", SpiQuery.TemporalMode.CURRENT);

    assertEquals("t0.A t0.B", treeAlias.parseRootAlias("${}A ${}B"));
    assertEquals("At0.Bt0.C", treeAlias.parseRootAlias("A${}B${}C"));
  }

}
