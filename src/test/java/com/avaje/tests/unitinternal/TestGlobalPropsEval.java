package com.avaje.tests.unitinternal;

import com.avaje.ebean.config.GlobalProperties;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestGlobalPropsEval extends TestCase {

    public void test() {
        
        GlobalProperties.put("unitevaltest.1", "one");
        Assert.assertEquals("one", GlobalProperties.get("unitevaltest.1",""));
        GlobalProperties.put("unitevaltest.2", "a${unitevaltest.1}b");
        Assert.assertEquals("aoneb", GlobalProperties.get("unitevaltest.2",""));
        
        GlobalProperties.put("unitevaltest.3", "a${unitevaltest.4}b");
        Assert.assertEquals("a${unitevaltest.4}b", GlobalProperties.get("unitevaltest.3",""));

        GlobalProperties.put("unitevaltest.4", "four");
        Assert.assertEquals("four", GlobalProperties.get("unitevaltest.4",""));
        Assert.assertEquals("a${unitevaltest.4}b", GlobalProperties.get("unitevaltest.3",""));

        GlobalProperties.evaluateExpressions();
        Assert.assertEquals("afourb", GlobalProperties.get("unitevaltest.3",""));

    }
}
