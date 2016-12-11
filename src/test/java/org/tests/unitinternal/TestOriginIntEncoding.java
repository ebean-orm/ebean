package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.bean.CallStack;
import org.junit.Assert;
import org.junit.Test;

public class TestOriginIntEncoding extends BaseTestCase {

  @Test
  public void test() {

    Assert.assertEquals("A", CallStack.enc(0));
    Assert.assertEquals("B", CallStack.enc(1));
    Assert.assertEquals("Z", CallStack.enc(25));
    Assert.assertEquals("a", CallStack.enc(26));
    Assert.assertEquals("z", CallStack.enc(51));
    Assert.assertEquals("0", CallStack.enc(52));
    Assert.assertEquals("9", CallStack.enc(61));
    Assert.assertEquals("-", CallStack.enc(62));
    Assert.assertEquals("_", CallStack.enc(63));
    Assert.assertEquals("BA", CallStack.enc(64));
    Assert.assertEquals("Bk", CallStack.enc(100));
    Assert.assertEquals("B9", CallStack.enc(125));
    Assert.assertEquals("B-", CallStack.enc(126));
    Assert.assertEquals("B_", CallStack.enc(127));
    Assert.assertEquals("CA", CallStack.enc(128));

  }
}
