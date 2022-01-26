package org.tests.model.embedded;

import io.ebean.DB;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class TestEmbeddedDbArray {

  /**
   * Failing test case for #2477
   */
  @Disabled
  @Test
  void testArrayInsert() {
    EmbArrayMaster t = new EmbArrayMaster(singletonList(new EmbArrayMaster.EmbArrayDetail(emptyList())));
    DB.insert(t);
  }
}
