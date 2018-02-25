package io.ebean.config;

import io.ebean.annotation.Platform;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertiesWrapperTest {


  @Test
  public void testGetServerName() {

    PropertiesWrapper pw = new PropertiesWrapper(null, "myserver", new Properties());
    assertEquals("myserver", pw.getServerName());
  }

  @Test
  public void testGetEnum() {

    Properties properties = new Properties();
    properties.put("platform", "postgres");

    PropertiesWrapper pw = new PropertiesWrapper("pref", "myserver", properties);
    assertEquals(Platform.POSTGRES, pw.getEnum(Platform.class, "platform", Platform.H2));
    assertEquals(Platform.H2, pw.getEnum(Platform.class, "junk", Platform.H2));
    assertNull(pw.getEnum(Platform.class, "junk", null));
  }

  @Test
  public void testTrimPropertyValues() {

    Properties properties = new Properties();
    properties.put("someBasic", " hello ");
    properties.put("someInt", "42");
    properties.put("noTrimReqr", "jim");
    properties.put("includeSpaces", " jim bob ");

    PropertiesWrapper pw = new PropertiesWrapper("pref", "myserver", properties);
    assertEquals(" hello ", pw.get("someBasic"));
    assertEquals(42, pw.getInt("someInt", 1));
    assertNull(pw.get("doesNotExist", null));
    assertEquals("jim", pw.get("noTrimReqr"));
    assertEquals(" jim bob ", pw.get("includeSpaces"));
  }

  @Test
  public void testGetProperties() {

    Properties properties = new Properties();
    properties.put("someBasic", "hello");
    properties.put("someInt", "42");
    properties.put("someDouble", "5.5");
    properties.put("somePath", "${HOME}/hello");
    properties.put("someSystemProp", "/aaa/${java.io.tmpdir}/bbb");

    PropertiesWrapper pw = new PropertiesWrapper("pref", "myserver", properties);

    assertEquals(42, pw.getInt("someInt", 99));
    assertEquals(Double.valueOf(5.5D), (Double.valueOf(pw.getDouble("someDouble", 99.9D))));


    pw = new PropertiesWrapper(properties);

    assertEquals(42, pw.getInt("someInt", 99));
    assertEquals(Double.valueOf(5.5D), (Double.valueOf(pw.getDouble("someDouble", 99.9D))));
  }

}
