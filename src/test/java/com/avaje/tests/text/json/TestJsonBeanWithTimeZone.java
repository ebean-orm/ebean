package com.avaje.tests.text.json;

import java.util.TimeZone;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.BeanWithTimeZone;

public class TestJsonBeanWithTimeZone extends TestCase {

  private static final Logger logger = LoggerFactory.getLogger(TestJsonBeanWithTimeZone.class);
  
  public void testSimple() {
    
    TimeZone defaultTimeZone = TimeZone.getDefault();
    
    String[] ids = TimeZone.getAvailableIDs(defaultTimeZone.getRawOffset());
    System.out.println(ids);
    
    String id = defaultTimeZone.getID();
    TimeZone timeZone = TimeZone.getTimeZone(id);
    assertEquals(defaultTimeZone, timeZone);
    
    BeanWithTimeZone bean = new BeanWithTimeZone();
    bean.setName("foo");
    bean.setTimezone(TimeZone.getDefault());
    
    JsonContext jsonContext = Ebean.createJsonContext();
    String jsonContent = jsonContext.toJsonString(bean);
    
    BeanWithTimeZone bean2 = jsonContext.toBean(BeanWithTimeZone.class, jsonContent);
    
    assertEquals(bean.getTimezone(), bean2.getTimezone());
    
    Ebean.save(bean);
    BeanWithTimeZone bean3 = Ebean.find(BeanWithTimeZone.class, bean.getId());

    assertEquals(bean.getTimezone(), bean3.getTimezone());

   // EbeanServer server = Ebean.getServer(null);
   //server.shutdown();
   //logger.info("shudown server manually, JVM shutdown hook fires next");
    
  }
  
}
