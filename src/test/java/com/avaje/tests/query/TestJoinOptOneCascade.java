package com.avaje.tests.query;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.EOptOneA;

public class TestJoinOptOneCascade extends TestCase {

    public void test() {
        
        // the left outer join cascades to the join for c
        Query<EOptOneA> query = Ebean.find(EOptOneA.class)
            .fetch("b")
            .fetch("b.c");
        
        query.findList();
        String sql = query.getGeneratedSql();
        
        Assert.assertTrue(sql.contains("left outer join eopt_one_b "));
        Assert.assertTrue(sql.contains("left outer join eopt_one_c "));
    }
    
}
