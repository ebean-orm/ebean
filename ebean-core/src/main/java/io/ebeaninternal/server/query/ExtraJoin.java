package io.ebeaninternal.server.query;

public class ExtraJoin {

  private final STreePropertyAssoc property;
  private final boolean containsMany;

  public ExtraJoin(STreePropertyAssoc property, boolean containsMany) {
    this.property = property;
    this.containsMany = containsMany;
  }

  public STreePropertyAssoc getProperty() {
    return property;
  }

  public boolean isContainsMany() {
    return containsMany;
  }
}
