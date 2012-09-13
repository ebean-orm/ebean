package com.avaje.tests.basic;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Update;
import com.avaje.tests.model.basic.EBasic;

public class TestExplicitInsert extends TestCase {

    public void test() {
        
        //GlobalProperties.put("ebean.classes", ""+LDPerson.class.toString()+","+EBasic.class.toString());
    	
        EBasic b = new EBasic();
        b.setName("exp insert");
        b.setDescription("explicit insert");
        b.setStatus(EBasic.Status.ACTIVE);
        
        EbeanServer server = Ebean.getServer(null);
        server.insert(b);
        
        assertNotNull(b.getId());
     
        EBasic b2 = server.find(EBasic.class, b.getId());
        b2.setId(null);
        
        b2.setName("force insert");
        server.insert(b2);
        
        assertNotNull(b2.getId());
        assertTrue(!b.getId().equals(b2.getId()));
        
        
        List<EBasic> list = server.find(EBasic.class)
        	.setMaxRows(10)
        	.findList();
        
        assertTrue(list.size() >= 2);
        
        int firstRow = 1;
        List<EBasic> list2 = server.find(EBasic.class)
        .order().asc("id")
        .setFirstRow(firstRow)
    	.setMaxRows(10)
    	.findList();
    
        int expectedCount = list.size() -firstRow;
        if (expectedCount > 0){
        	assertEquals(expectedCount, list2.size());
        } else {
        	assertTrue(list2.isEmpty());
        }
        
        Update<EBasic> update = Ebean.createUpdate(EBasic.class, "update ebasic set description = 'test'");
        
        int rows = update.execute();
        
        assertTrue(rows > 0);
        Ebean.externalModification("e_basic", true, false, true);
    }
    
}
