package com.avaje.tests.compositekeys;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.composite.ROrder;
import com.avaje.tests.model.composite.ROrderPK;

public class TestCKeyIdInExpression extends TestCase {

    public void testDummy() {

    }
    //public void testRunManually() {
    public void notRanAutomatically() {
        
        GlobalProperties.put("datasource.default", "");
        EbeanServer server = CreateIdExpandedFormServer.create();
        
        ROrderPK k0 = new ROrderPK("compa", 100);
        ROrderPK k1 = new ROrderPK("compa", 101);
        ROrderPK k2 = new ROrderPK("b", 105);
        ROrderPK k3 = new ROrderPK("c", 106);

        List<ROrderPK> keys = new ArrayList<ROrderPK>();
        keys.add(k0);
        keys.add(k1);
        keys.add(k2);
        keys.add(k3);
        
         Query<ROrder> query = server.find(ROrder.class)
            .where().idIn(keys)
            .query();
         
         query.findList();
         String sql = query.getGeneratedSql();
         
         assertTrue(sql.contains("(r.company=? and r.order_number=?) or"));
         
         Query<ROrder> query2 = server.find(ROrder.class)
             .setId(k0);
         
         query2.findUnique();
         sql = query2.getGeneratedSql();
         assertTrue(sql.contains("r.company = ? "));
         assertTrue(sql.contains(" and r.order_number = ?"));
         
         
         server.delete(ROrder.class, k0);
         
         server.delete(ROrder.class, keys);
         
    }
    
}
