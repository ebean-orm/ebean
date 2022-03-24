package org.tests.text.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.BeanWithTimeZone;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonBeanWithTimeZone extends BaseTestCase {

  @Test
  public void testSimple() {
    TimeZone defaultTimeZone = TimeZone.getDefault();

    TimeZone.getAvailableIDs(defaultTimeZone.getRawOffset());

    String id = defaultTimeZone.getID();
    TimeZone timeZone = TimeZone.getTimeZone(id);
    assertEquals(defaultTimeZone, timeZone);

    BeanWithTimeZone bean = new BeanWithTimeZone();
    bean.setName("foo");
    bean.setTimezone(TimeZone.getDefault());

    JsonContext jsonContext = DB.json();
    String jsonContent = jsonContext.toJson(bean);

    BeanWithTimeZone bean2 = jsonContext.toBean(BeanWithTimeZone.class, jsonContent);

    assertEquals(bean.getTimezone(), bean2.getTimezone());

    DB.save(bean);
    BeanWithTimeZone bean3 = DB.find(BeanWithTimeZone.class, bean.getId());

    assertEquals(bean.getTimezone(), bean3.getTimezone());
  }

}
