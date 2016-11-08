package com.avaje.tests.basic.lob;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TBytesOnly;
import org.junit.Test;

public class TestByteOnly extends BaseTestCase {

  @Test
  public void test() {

    byte[] content = new byte[]{1, 1};
    TBytesOnly e = new TBytesOnly();
    e.setContent(content);

    Ebean.save(e);

    byte[] content2 = new byte[]{1, 1};

    TBytesOnly e2 = Ebean.find(TBytesOnly.class, e.getId());
    e2.setContent(content2);

    Ebean.save(e2);
  }

}
