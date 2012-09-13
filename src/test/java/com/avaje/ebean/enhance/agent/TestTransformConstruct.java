package com.avaje.ebean.enhance.agent;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestTransformConstruct extends TestCase {

    
    public void test() {
        
        Transformer t = new Transformer("", "");
        Assert.assertNotNull(t);

        t = new Transformer("d", "");
        Assert.assertNotNull(t);

        t = new Transformer("dd", "");
        Assert.assertNotNull(t);

        t = new Transformer((String)null, null);
        Assert.assertNotNull(t);

    }
    
}
