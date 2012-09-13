package com.avaje.tests.text.json;

import java.sql.Date;
import java.sql.Timestamp;

import org.junit.Assert;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebeaninternal.server.text.json.DefaultJsonValueAdapter;
import com.avaje.tests.model.basic.Car;
import com.avaje.tests.model.basic.Vehicle;

public class TestTextJsonUtilDateFormat extends TestCase {

    public void test() {
        
        GlobalProperties.put("ebean.ddl.generate", "false");
        GlobalProperties.put("ebean.ddl.run", "false");
        
        Vehicle v = new Car();
        v.setId(100);
        v.setRegistrationDate(new java.util.Date());
        v.setUpdtime(new Timestamp(System.currentTimeMillis()));
        
        JsonContext context = Ebean.createJsonContext();
        JsonWriteOptions o = new JsonWriteOptions();
        o.setValueAdapter(new CustomDateFormatAdapter());
        
        String jsonString = context.toJsonString(v, true, o);
        System.out.println(jsonString);
        
        Assert.assertTrue(jsonString.contains("\"registrationDate\":'"));
    }
    
    class CustomDateFormatAdapter implements JsonValueAdapter {

        DefaultJsonValueAdapter defaultImplementation = new DefaultJsonValueAdapter();
        
        public String jsonFromDate(Date date) {
            // TODO
            return null;
        }

        public String jsonFromTimestamp(Timestamp date) {
            // add some single quotes around the timestamp value
            return "'"+defaultImplementation.jsonFromTimestamp(date)+"'";
        }

        public Date jsonToDate(String jsonDate) {
            // TODO 
            return null;
        }

        public Timestamp jsonToTimestamp(String jsonDateTime) {
            // TODO 
            return null;
        }
        
    }
}
