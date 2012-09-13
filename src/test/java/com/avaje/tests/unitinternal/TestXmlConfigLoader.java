package com.avaje.tests.unitinternal;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebeaninternal.server.core.XmlConfigLoader;
import com.avaje.ebeaninternal.server.lib.util.Dnode;

public class TestXmlConfigLoader extends TestCase {

    public void test() {
        
        XmlConfigLoader xmlConfigLoader = new XmlConfigLoader(null);
        
        List<Dnode> ebeanOrmXml = xmlConfigLoader.search("META-INF/ebean-orm.xml");
        
        assertNotNull(ebeanOrmXml);
        assertTrue("Found ebean-orm.xml",ebeanOrmXml.size() > 0);
        
    }
}
