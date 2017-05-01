package org.tests.example;

import io.ebean.config.IdGenerator;
import org.avaje.moduuid.ModUUID;

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
