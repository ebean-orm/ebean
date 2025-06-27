package org.tests.model.memleak;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The query cache holds the bean (MemleakChild). If there are "big" beans, then an OOM may occur.
 *
 * @author Jonas Fr&ouml;hler, Foconis Analytics GmbH
 */
public class TestQueryCacheHoldsBean extends BaseTestCase {

  @Test
  @Disabled("Run manually with -Xmx128M")
  void testQueryCacheHoldsBean() {
    for (long id = 0; id <= 1000; id++) {
      DB.find(MemleakParent.class)
        .where().eq("id", id)
        .eq("child", DB.reference(MemleakChild.class, id))
        .setUseQueryCache(true)
        .findOne();
    }
  }
}
