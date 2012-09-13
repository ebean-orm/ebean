package com.avaje.tests.basic.type;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.EBasic;
import com.avaje.tests.model.basic.EBasic.Status;

public class TestEnumValueAnnotation extends TestCase {

    public void test(){
        
        EBasic b = new EBasic();
        b.setName("Banana");
        b.setStatus(Status.NEW);
        
        Ebean.save(b);
        
        SqlQuery q = Ebean.createSqlQuery("select * from e_basic where id = :id");
        q.setParameter("id", b.getId());
        
        SqlRow sqlRow = q.findUnique();
        String strStatus = sqlRow.getString("status");
       
        Assert.assertEquals("N", strStatus);
        
        EBasic b2 = new EBasic();
        b2.setName("Apple");
        b2.setStatus(Status.NEW);
        
        
        Ebean.save(b2);
        
        EBasic b3 = Ebean.find(EBasic.class, b2.getId());
        b3.setName("Orange");
        
        Ebean.save(b3);
        
    }
    
}
