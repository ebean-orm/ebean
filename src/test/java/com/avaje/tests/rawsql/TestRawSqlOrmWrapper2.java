package com.avaje.tests.rawsql;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderAggregate;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestRawSqlOrmWrapper2 extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
                
        String sql 
            = " select order_id, 'ignoreMe', sum(d.order_qty*d.unit_price) as totalAmount "
            + " from o_order_detail d" 
            + " group by order_id ";
        
        RawSql rawSql = 
            RawSqlBuilder
                .parse(sql)
                .columnMapping("order_id",  "order.id")
                .columnMappingIgnore("'ignoreMe'")
                // don't need this when using column alias
                //.columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
                .create();

        
        Query<OrderAggregate> query = Ebean.find(OrderAggregate.class);
        query.setRawSql(rawSql)        
            .fetch("order", "status,orderDate",new FetchConfig().query())
            .fetch("order.customer", "name")
            .where().gt("order.id", 0)
            .having().gt("totalAmount", 20)
            .order().desc("totalAmount")
            .setMaxRows(10);
        
        List<OrderAggregate> list = query.findList();
        assertNotNull(list);

        for (OrderAggregate oa : list) {
            Double totalAmount = oa.getTotalAmount();
            Order order = oa.getOrder();
            Integer id = order.getId();
            System.out.println("Order: "+id+" total:"+totalAmount);            
        }
        
        
    }
}
