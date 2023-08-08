package io.ebeaninternal.server.expression;

enum SubQueryOp {
  EQ(" = "),
  NE(" <> "),
  GT(" > "),
  GE(" >= "),
  LT(" < "),
  LE(" <= "),
  IN(" in "),
  NOTIN(" not in ");
  final String expression;

  SubQueryOp(String expression) {
    this.expression = expression;
  }
}
