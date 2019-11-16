package io.ebeaninternal.api;

class NaturalKeyEntrySimple implements NaturalKeyEntry {

  private final Object key;

  NaturalKeyEntrySimple(Object key) {
    this.key = key;
  }

  @Override
  public Object key() {
    return key;
  }

  @Override
  public Object getInValue() {
    return key;
  }
}
