package com.avaje.tests.unitinternal;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebeaninternal.server.core.XmlConfigLoader;
import com.avaje.ebeaninternal.server.lib.util.Dnode;

public class TestXmlConfigLoader extends BaseTestCase {

  @Test
  public void test() {

    XmlConfigLoader xmlConfigLoader = new XmlConfigLoader(null);

    List<Dnode> ebeanOrmXml = xmlConfigLoader.search("META-INF/ebean-orm.xml");

    Assert.assertNotNull(ebeanOrmXml);
    Assert.assertTrue("Found ebean-orm.xml", ebeanOrmXml.size() > 0);

  }
}
