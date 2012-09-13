package com.avaje.tests.text.json;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonReadManyLazyLoad extends TestCase {

    public void test_lazyLoadBoth(){
        
       ResetBasicData.reset();
        
        List<Customer> list = Ebean.find(Customer.class)
            .select("id, status, shippingAddress")
            .order().desc("id")
            .findList();
        
        JsonContext json = Ebean.createJsonContext();
        
        // test that lazy loading on 
        PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
        JsonWriteOptions o = new JsonWriteOptions();
        o.setPathProperties(pp);
        
        System.out.println("Expect lazy loading of Customer beans and customer contacts");
        String s = json.toJsonString(list, true, o);
        System.out.println(s);
        Assert.assertTrue(s.contains("\"contacts\""));
        Assert.assertTrue(s.contains("\"name\""));
        
    }

    public void test_lazyLoadCust(){
        
        ResetBasicData.reset();
         
         List<Customer> list = Ebean.find(Customer.class)
             .select("id, status, shippingAddress")
             .fetch("contacts")
             .order().desc("id")
             .findList();
         
         JsonContext json = Ebean.createJsonContext();
         
         // test that lazy loading on 
         PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
         JsonWriteOptions o = new JsonWriteOptions();
         o.setPathProperties(pp);
         
         
         System.out.println("expecting lazy load of Customer beans to fetch customer name");
         String s = json.toJsonString(list, true, o);
         System.out.println(s);
         Assert.assertTrue(s.contains("\"contacts\""));
         Assert.assertTrue(s.contains("\"name\""));
         
     }
    
    public void test_lazyLoadContacts(){
        
        ResetBasicData.reset();
         
         List<Customer> list = Ebean.find(Customer.class)
             .select("id, name, status, shippingAddress")
             //.fetch("contacts")
             .order().desc("id")
             .findList();
         
         JsonContext json = Ebean.createJsonContext();
         
         // test that lazy loading on 
         PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
         JsonWriteOptions o = new JsonWriteOptions();
         o.setPathProperties(pp);
         
         
         System.out.println("expecting lazy load of Customer contacts ");
         String s = json.toJsonString(list, true, o);
         System.out.println(s);
         Assert.assertTrue(s.contains("\"contacts\""));
         Assert.assertTrue(s.contains("\"name\""));
         
     }

}
