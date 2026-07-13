package org.tests.model.embedded;

import io.ebean.DB;
import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class TestEmbeddedDbArray extends BaseTestCase {

  /**
   * Test case for #2477 - inserting an empty collection into a @DbArray column
   * nested inside an @ElementCollection @Embeddable used to throw a NoSuchElementException.
   * <p>
   * Restricted to Postgres as the underlying platform - other platforms (e.g. MariaDB, SQLServer)
   * fall back to JSON storage for @DbArray and MultiValueBind doesn't support binding a single
   * array value in that case, which is a separate, pre-existing limitation.
   */
  @Test
  @ForPlatform(Platform.POSTGRES)
  void testArrayInsert_empty() {
    EmbArrayMaster t = new EmbArrayMaster(singletonList(new EmbArrayMaster.EmbArrayDetail(emptyList())));
    DB.insert(t);
  }

  @Test
  @ForPlatform(Platform.POSTGRES)
  void testArrayInsert_nonEmpty() {
    EmbArrayMaster t = new EmbArrayMaster(singletonList(new EmbArrayMaster.EmbArrayDetail(asList("a", "b"))));
    DB.insert(t);
  }
}
