package io.ebeaninternal.server.grammer;

import java.util.HashMap;
import java.util.Map;

class OperatorMapping {

  Map<String, EqlOperator> map = new HashMap<>();

  public OperatorMapping() {
    map.put("eq", EqlOperator.EQ);
    map.put("=", EqlOperator.EQ);
    map.put("ieq", EqlOperator.IEQ);

    map.put("ne", EqlOperator.NE);
    map.put("<>", EqlOperator.NE);
    map.put("!=", EqlOperator.NE);

    map.put(">", EqlOperator.GT);
    map.put("gt", EqlOperator.GT);

    map.put(">=", EqlOperator.GTE);
    map.put("gte", EqlOperator.GTE);
    map.put("ge", EqlOperator.GTE);

    map.put("<", EqlOperator.LT);
    map.put("lt", EqlOperator.LT);

    map.put("<=", EqlOperator.LTE);
    map.put("lte", EqlOperator.LTE);
    map.put("le", EqlOperator.LTE);

    map.put("contains", EqlOperator.CONTAINS);
    map.put("startsWith", EqlOperator.STARTS_WITH);
    map.put("endsWith", EqlOperator.ENDS_WITH);
    map.put("like", EqlOperator.LIKE);

    map.put("icontains", EqlOperator.ICONTAINS);
    map.put("istartsWith", EqlOperator.ISTARTS_WITH);
    map.put("iendsWith", EqlOperator.IENDS_WITH);
    map.put("ilike", EqlOperator.ILIKE);

    map.put("between", EqlOperator.BETWEEN);
  }

  public EqlOperator get(String key) {
    return map.get(key);
  }
}
