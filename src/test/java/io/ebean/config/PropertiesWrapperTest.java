package io.ebean.config;

import io.ebean.annotation.Platform;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertiesWrapperTest {


  @Test
  public void testGetServerName() throws Exception {

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
    properties.put("someInt", " 42 ");
    properties.put("noTrimReqr", "jim");
    properties.put("includeSpaces", " jim bob ");

    PropertiesWrapper pw = new PropertiesWrapper("pref", "myserver", properties);
    assertEquals("hello", pw.get("someBasic"));
    assertEquals(42, pw.getInt("someInt", 1));
    assertNull(pw.get("doesNotExist", null));
    assertEquals("jim", pw.get("noTrimReqr"));
    assertEquals("jim bob", pw.get("includeSpaces"));
  }

  @Test
  public void testGetProperties() throws Exception {

    String home = System.getenv("HOME");
    String tmpDir = System.getProperty("java.io.tmpdir");

    Properties properties = new Properties();
    properties.put("someBasic", "hello");
    properties.put("someInt", "42");
    properties.put("someDouble", "5.5");
    properties.put("somePath", "${HOME}/hello");
    properties.put("someSystemProp", "/aaa/${java.io.tmpdir}/bbb");

    PropertiesWrapper pw = new PropertiesWrapper("pref", "myserver", properties);

    assertEquals(42, pw.getInt("someInt", 99));
    assertEquals(Double.valueOf(5.5D), (Double.valueOf(pw.getDouble("someDouble", 99.9D))));
    assertEquals(home + "/hello", pw.get("somePath", null));
    assertEquals(tmpDir, "/aaa/" + tmpDir + "/bbb", pw.get("someSystemProp"));

    Properties properties1 = pw.asPropertiesLowerCase();
    assertEquals("hello", properties1.getProperty("somebasic"));
    assertEquals("42", properties1.getProperty("someint"));
    assertEquals(home + "/hello", properties1.get("somepath"));
    assertEquals(tmpDir, "/aaa/" + tmpDir + "/bbb", properties1.get("somesystemprop"));


    pw = new PropertiesWrapper(properties);

    assertEquals(42, pw.getInt("someInt", 99));
    assertEquals(Double.valueOf(5.5D), (Double.valueOf(pw.getDouble("someDouble", 99.9D))));
    assertEquals(home + "/hello", pw.get("somePath", null));
    assertEquals(tmpDir, "/aaa/" + tmpDir + "/bbb", pw.get("someSystemProp"));

    properties1 = pw.asPropertiesLowerCase();
    assertEquals("hello", properties1.getProperty("somebasic"));
    assertEquals("42", properties1.getProperty("someint"));
    assertEquals(home + "/hello", properties1.get("somepath"));
    assertEquals(tmpDir, "/aaa/" + tmpDir + "/bbb", properties1.get("somesystemprop"));
  }

}
