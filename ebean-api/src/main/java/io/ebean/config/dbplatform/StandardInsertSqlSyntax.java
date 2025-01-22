package io.ebean.config.dbplatform;

/**
 * Standard Insert SQL syntax.
 */
public final class StandardInsertSqlSyntax implements InsertSqlSyntaxExtension {

  @Override
  public String startColumns() {
    return " (";
  }

  @Override
  public String endColumns() {
    return ") values (";
  }

  @Override
  public boolean useBinding() {
    return true;
  }

  @Override
  public String startTypes() {
    throw new IllegalStateException();
  }

  @Override
  public String endTypes() {
    throw new IllegalStateException();
  }
}
