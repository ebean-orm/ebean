package com.avaje.tests.ldap;

import junit.framework.TestCase;

import org.joda.time.LocalDateTime;
import org.junit.Assert;

import com.avaje.ebeaninternal.server.type.ScalarTypeJodaLocalDateTime;
import com.avaje.ebeaninternal.server.type.ScalarTypeLdapTimestamp;

public class TestLdapTimestamp extends TestCase {

    
    public void test() {
        
        ScalarTypeJodaLocalDateTime bt = new ScalarTypeJodaLocalDateTime();
        
        ScalarTypeLdapTimestamp<LocalDateTime> s = new ScalarTypeLdapTimestamp<LocalDateTime>(bt);
        
        LocalDateTime now = new LocalDateTime();
        Object v = s.toJdbcType(now);

        Assert.assertNotNull(v);
        Assert.assertTrue(v instanceof String);

        Object convertBack = s.toBeanType(v);
        Assert.assertTrue(convertBack instanceof LocalDateTime);
        // Not equals due to loss of precision in LDAP format
        //Assert.assertEquals(now, convertBack);
        
    }
}
