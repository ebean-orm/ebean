package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.DefaultExpressionRequest;
import io.ebeaninternal.server.expression.Op;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MySqlDbExpressionTest {

  private MySqlDbExpression expression = new MySqlDbExpression();

  @Test
  public void testJson() {
    SpiExpressionRequest request = new DefaultExpressionRequest(null);
    expression.json(request, "jsonproperty", "path", Op.EQ, "val");
    assertEquals("(jsonproperty ->> '$.path') = ?", request.getSql());
  }
}
