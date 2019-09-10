package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.DefaultExpressionRequest;
import io.ebeaninternal.server.expression.Op;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HanaDbExpressionTest {
  private HanaDbExpression expression = new HanaDbExpression();

  @Test
  public void testArrayContains() {
    SpiExpressionRequest request = new DefaultExpressionRequest(null);
    expression.arrayContains(request, "arrayproperty", true, "v1", "v2", "v3");
    assertEquals("(? member of arrayproperty) and (? member of arrayproperty) and (? member of arrayproperty)",
        request.getSql());
  }

  @Test
  public void testArrayNotContains() {
    SpiExpressionRequest request = new DefaultExpressionRequest(null);
    expression.arrayContains(request, "arrayproperty", false, "v1", "v2", "v3");
    assertEquals(
        "(? not  member of arrayproperty) and (? not  member of arrayproperty) and (? not  member of arrayproperty)",
        request.getSql());
  }

  @Test
  public void testArrayContainsEmpty() {
    SpiExpressionRequest request = new DefaultExpressionRequest(null);
    expression.arrayContains(request, "arrayproperty", true);
    assertEquals("", request.getSql());
  }

  @Test
  public void testArrayIsEmpty() {
    SpiExpressionRequest request = new DefaultExpressionRequest(null);
    expression.arrayIsEmpty(request, "arrayproperty", true);
    assertEquals("cardinality(arrayproperty) = 0", request.getSql());
  }

  @Test
  public void testArrayIsNotEmpty() {
    SpiExpressionRequest request = new DefaultExpressionRequest(null);
    expression.arrayIsEmpty(request, "arrayproperty", false);
    assertEquals("cardinality(arrayproperty) <> 0", request.getSql());
  }

  @Test
  public void testConcat() {
    String concat = expression.concat("property0", "separator", "property1", "suffix");
    assertEquals("concat(property0, 'separator'||property1||'suffix')", concat);
  }

  @Test
  public void testConcatNullSuffix() {
    String concat = expression.concat("property0", "separator", "property1", null);
    assertEquals("concat(property0, 'separator'||property1)", concat);
  }

  @Test
  public void testJson() {
    SpiExpressionRequest request = new DefaultExpressionRequest(null);
    expression.json(request, "jsonproperty", "path", Op.EQ, "val");
    assertEquals("json_value(jsonproperty, '$.path') = ?", request.getSql());
  }
}
