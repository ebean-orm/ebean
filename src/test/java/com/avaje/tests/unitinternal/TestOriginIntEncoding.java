package com.avaje.tests.unitinternal;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.bean.CallStack;

public class TestOriginIntEncoding extends TestCase {

    public void test() {
        
//        for (int i = 0; i < 130; i++) {
//            int x = 0+i;
//            String e = CallStack.enc(x);
//            System.out.println(x+"="+e);
//        }
        
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
