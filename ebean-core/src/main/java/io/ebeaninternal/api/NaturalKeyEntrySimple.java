package io.ebeaninternal.api;

class NaturalKeyEntrySimple implements NaturalKeyEntry {

  private final String key;
  private final Object val;

  NaturalKeyEntrySimple(Object val) {
    this.key = val.toString();
    this.val = val;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public Object getInValue() {
    return val;
  }
}
