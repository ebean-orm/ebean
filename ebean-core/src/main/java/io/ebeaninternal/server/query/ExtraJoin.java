package io.ebeaninternal.server.query;

public final class ExtraJoin {

  private final STreePropertyAssoc property;
  private final boolean containsMany;

  public ExtraJoin(STreePropertyAssoc property, boolean containsMany) {
    this.property = property;
    this.containsMany = containsMany;
  }

  public STreePropertyAssoc property() {
    return property;
  }

  public boolean isContainsMany() {
    return containsMany;
  }
}
