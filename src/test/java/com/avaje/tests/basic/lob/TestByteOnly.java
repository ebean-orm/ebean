package com.avaje.tests.basic.lob;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TBytesOnly;

public class TestByteOnly extends BaseTestCase {

  @Test
  public void test() {

    byte[] content = new byte[] { 1, 1 };
    TBytesOnly e = new TBytesOnly();
    e.setContent(content);

    Ebean.save(e);

    byte[] content2 = new byte[] { 1, 1 };

    TBytesOnly e2 = Ebean.find(TBytesOnly.class, e.getId());
    e2.setContent(content2);

    Ebean.save(e2);

    // Ebean.getServer(null).getAdminAutofetch().collectUsageViaGC();
    // Ebean.getServer(null).getAdminAutofetch().updateTunedQueryInfo();
    System.out.println("done");
  }

}
