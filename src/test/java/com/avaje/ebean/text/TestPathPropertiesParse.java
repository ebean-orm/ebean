package com.avaje.ebean.text;

import junit.framework.TestCase;

import com.avaje.ebean.text.PathProperties;

public class TestPathPropertiesParse extends TestCase {

    public void test() {
        
        PathProperties s0 = PathProperties.parse("(id,name)");

        assertEquals(1,s0.getPaths().size());
        assertTrue(s0.get(null).contains("id"));
        assertTrue(s0.get(null).contains("name"));
        assertFalse(s0.get(null).contains("status"));
        
        PathProperties s1 = PathProperties.parse(":(id,name,shipAddr(*))");
        assertEquals(2,s1.getPaths().size());
        assertEquals(3,s1.get(null).size());
        assertTrue(s1.get(null).contains("id"));
        assertTrue(s1.get(null).contains("name"));
        assertTrue(s1.get(null).contains("shipAddr"));
        assertTrue(s1.get("shipAddr").contains("*"));
        assertEquals(1,s1.get("shipAddr").size());


    }
    
}
