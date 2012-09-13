package com.avaje.tests.basic.lob;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.EBasicClobNoVer;

public class TestBasicClobNoVer extends TestCase {

    public void test() {
        
        EBasicClobNoVer entity = new EBasicClobNoVer();
        entity.setName("test");
        entity.setDescription("This is a test");
        EbeanServer server = Ebean.getServer(null);
        server.save(entity);
        
        Ebean.find(EBasicClobNoVer.class).select("*").findList();
        
        server.refresh(entity);
        System.out.println("description=" + entity.getDescription());

        
    }
    
}
