package com.avaje.tests.rawsql;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.CustomerAggregate;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestRawSqlCustomerAggregate extends TestCase {

    public void test() {

        ResetBasicData.reset();
        
        RawSql rawSql = 
            RawSqlBuilder
                .parse("select c.customer_id, count(*) as totalContacts from contact c  group by c.customer_id")
                .columnMapping("c.customer_id", "customer.id")
                .create();
                    
        Query<CustomerAggregate> query = Ebean.find(CustomerAggregate.class);
        query.setRawSql(rawSql);
        query.where().ge("customer.id", 1);
        
        List<CustomerAggregate> list = query.findList();
        assertNotNull(list);
    }
}
