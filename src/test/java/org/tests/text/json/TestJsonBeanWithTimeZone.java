package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import org.tests.model.basic.BeanWithTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.TimeZone;

public class TestJsonBeanWithTimeZone extends BaseTestCase {

  @Test
  public void testSimple() throws IOException {

    TimeZone defaultTimeZone = TimeZone.getDefault();

    TimeZone.getAvailableIDs(defaultTimeZone.getRawOffset());

    String id = defaultTimeZone.getID();
    TimeZone timeZone = TimeZone.getTimeZone(id);
    Assert.assertEquals(defaultTimeZone, timeZone);

    BeanWithTimeZone bean = new BeanWithTimeZone();
    bean.setName("foo");
    bean.setTimezone(TimeZone.getDefault());

    JsonContext jsonContext = Ebean.json();
    String jsonContent = jsonContext.toJson(bean);

    BeanWithTimeZone bean2 = jsonContext.toBean(BeanWithTimeZone.class, jsonContent);

    Assert.assertEquals(bean.getTimezone(), bean2.getTimezone());

    Ebean.save(bean);
    BeanWithTimeZone bean3 = Ebean.find(BeanWithTimeZone.class, bean.getId());

    Assert.assertEquals(bean.getTimezone(), bean3.getTimezone());

  }

}
