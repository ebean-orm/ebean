package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.DataReader;

/**
 * Placeholder to skip reading a column that isn't mapped to a bean property.
 */
class DtoReadSetColumnSkip implements DtoReadSet {

  static final DtoReadSet INSTANCE = new DtoReadSetColumnSkip();

  @Override
  public void readSet(Object bean, DataReader dataReader) {
    dataReader.incrementPos(1);
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }
}
