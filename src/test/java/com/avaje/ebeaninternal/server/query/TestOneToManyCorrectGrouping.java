package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.*;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by archie on 29/09/15.
 */
public class TestOneToManyCorrectGrouping extends BaseTestCase{
    public static final int EXPECTED_ITERATIONS = 2;

    @Test
    public void test(){
        ResetBasicData.reset();
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(1);
        ids.add(2);

        Query<Customer> customerQuery =  Ebean.find(Customer.class)
                .fetch("orders")
                .where()
                .idIn(ids)
                .query();
        QueryIterator<Customer> customerQueryIterator = customerQuery.findIterate();

        int count = 0;
        while(customerQueryIterator.hasNext()){
            Customer customer = customerQueryIterator.next();
            System.out.println(String.format("Customer id %s", customer.getId()));
            for (Order order : customer.getOrders()){
                System.out.println(String.format("--> Order id %s", order.getId()));
            }
            count++;
        }

        assertEquals(EXPECTED_ITERATIONS, count);
        customerQueryIterator.close();
    }
}
