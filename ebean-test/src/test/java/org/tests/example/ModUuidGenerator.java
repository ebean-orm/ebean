package org.tests.example;

import io.avaje.moduuid.ModUUID;
import io.ebean.config.IdGenerator;

/**
 * A customer Id Generator that can be assigned by @GeneratedValue(generator="shortUid")
 */
public class ModUuidGenerator implements IdGenerator {

  @Override
  public Object nextValue() {
    return ModUUID.newShortId();
  }

  @Override
  public String getName() {
    return "shortUid";
  }
}
