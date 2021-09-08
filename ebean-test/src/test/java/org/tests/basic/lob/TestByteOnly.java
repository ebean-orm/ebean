package org.tests.basic.lob;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.TBytesOnly;
import org.junit.jupiter.api.Test;

public class TestByteOnly extends BaseTestCase {

  @Test
  public void test() {

    byte[] content = new byte[]{1, 1};
    TBytesOnly e = new TBytesOnly();
    e.setContent(content);

    DB.save(e);

    byte[] content2 = new byte[]{1, 1};

    TBytesOnly e2 = DB.find(TBytesOnly.class, e.getId());
    e2.setContent(content2);

    DB.save(e2);
  }

}
