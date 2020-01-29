package io.ebeaninternal.server.deploy;

import org.junit.Test;

import static org.junit.Assert.*;

public class IndexDefinitionTest {

  private static final String[] simpleCol1 = new String[]{"one"};
  private static final String[] simpleCol2 = new String[]{"one","two"};
  private static final String[] formulaCol1 = new String[]{"lower(one)"};
  private static final String[] formulaCol2 = new String[]{"one","lower(two)"};

  @Test
  public void isUniqueConstraint_TRUE_when_simpleMultiColumn() {
    assertTrue(new IndexDefinition(simpleCol1).isUniqueConstraint());
    assertTrue(new IndexDefinition(simpleCol2).isUniqueConstraint());
  }

  @Test
  public void isUniqueConstraint_NOT_when_columnWithFormula() {
    assertFalse(new IndexDefinition(formulaCol1).isUniqueConstraint());
    assertFalse(new IndexDefinition(formulaCol2).isUniqueConstraint());
  }

  @Test
  public void isUniqueConstraint_NOT_when_concurrentTrue() {
    assertFalse(new IndexDefinition(simpleCol1, "name", true, null, true, null).isUniqueConstraint());
  }

  @Test
  public void isUniqueConstraint_NOT_when_definitionNotEmpty() {
    assertFalse(new IndexDefinition(simpleCol1, "name", true, null, false, "create index foo").isUniqueConstraint());
  }

  @Test
  public void isUniqueConstraint_TRUE_otherwise() {
    assertTrue(new IndexDefinition(simpleCol1, "name", true, null, false, "").isUniqueConstraint());
    assertTrue(new IndexDefinition(simpleCol1, "name", true, null, false, null).isUniqueConstraint());
  }
}
