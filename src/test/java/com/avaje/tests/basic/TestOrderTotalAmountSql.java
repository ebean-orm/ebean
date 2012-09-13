package com.avaje.tests.basic;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderTotalAmountSql extends TestCase {

    public void test() {

        ResetBasicData.reset();

        String sql = "select order_id, sum(order_qty*unit_price) as total_amount from o_order_detail  where order_qty > :minQty  group by order_id";
        List<SqlRow> sqlRows = 
            Ebean.createSqlQuery(sql)
                .setParameter("minQty",1)
                .findList();
        
        for (SqlRow sqlRow : sqlRows) {
            Integer id = sqlRow.getInteger("order_id");
            Double amount = sqlRow.getDouble("total_amount");
            Assert.assertNotNull("sqlRows: "+sqlRows,id);
            Assert.assertNotNull("sqlRows: "+sqlRows,amount);
        }
    }

}
