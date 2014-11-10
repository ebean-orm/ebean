package com.avaje.tests.text.json;

import java.io.IOException;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.BeanWithTimeZone;

public class TestJsonBeanWithTimeZone extends BaseTestCase {

  //private static final Logger logger = LoggerFactory.getLogger(TestJsonBeanWithTimeZone.class);

  @Test
  public void testSimple() throws IOException {

    TimeZone defaultTimeZone = TimeZone.getDefault();

    String[] ids = TimeZone.getAvailableIDs(defaultTimeZone.getRawOffset());
    System.out.println(ids);

    String id = defaultTimeZone.getID();
    TimeZone timeZone = TimeZone.getTimeZone(id);
    Assert.assertEquals(defaultTimeZone, timeZone);

    BeanWithTimeZone bean = new BeanWithTimeZone();
    bean.setName("foo");
    bean.setTimezone(TimeZone.getDefault());

    JsonContext jsonContext = Ebean.createJsonContext();
    String jsonContent = jsonContext.toJsonString(bean);

    BeanWithTimeZone bean2 = jsonContext.toBean(BeanWithTimeZone.class, jsonContent);

    Assert.assertEquals(bean.getTimezone(), bean2.getTimezone());

    Ebean.save(bean);
    BeanWithTimeZone bean3 = Ebean.find(BeanWithTimeZone.class, bean.getId());

    Assert.assertEquals(bean.getTimezone(), bean3.getTimezone());

    // EbeanServer server = Ebean.getServer(null);
    // server.shutdown();
    // logger.info("shudown server manually, JVM shutdown hook fires next");

  }

}
