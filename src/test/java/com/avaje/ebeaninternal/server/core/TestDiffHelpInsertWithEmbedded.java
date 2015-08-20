package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.ValuePair;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.embedded.EMain;
import com.avaje.tests.model.embedded.Eembeddable;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestDiffHelpInsertWithEmbedded extends BaseTestCase {

  EbeanServer server;

  BeanDescriptor<EMain> emainDesc;

  public TestDiffHelpInsertWithEmbedded() {
    server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    emainDesc = spiServer.getBeanDescriptor(EMain.class);
  }

  @Test
  public void simple() {

    EMain emain1 = createEMain();

    Map<String, ValuePair> diff = DiffHelpInsert.diff((EntityBean) emain1, emainDesc);
    assertEquals(3, diff.size());
    assertEquals("foo", diff.get("name").getNewValue());
    assertEquals(13L, diff.get("version").getNewValue());

    ValuePair valuePair = diff.get("embeddable.description");
    assertNotNull(valuePair);
    assertEquals("bar", valuePair.getNewValue());
    assertNull(valuePair.getOldValue());
  }

  @Test
  public void scalarPropertyAsNull() {

    EMain emain1 = createEMain();
    emain1.setName(null);

    Map<String, ValuePair> diff = DiffHelpInsert.diff((EntityBean) emain1, emainDesc);
    assertEquals(2, diff.size());
    assertNull(diff.get("name"));
    assertEquals(13L, diff.get("version").getNewValue());

    ValuePair valuePair = diff.get("embeddable.description");
    assertNotNull(valuePair);
    assertEquals("bar", valuePair.getNewValue());
    assertNull(valuePair.getOldValue());
  }

  @Test
  public void embeddedAsNull() {

    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);

    Map<String, ValuePair> diff = DiffHelpInsert.diff((EntityBean) emain1, emainDesc);
    assertEquals(2, diff.size());
    assertEquals("foo", diff.get("name").getNewValue());
    assertEquals(13L, diff.get("version").getNewValue());

    assertNull(diff.get("embeddable.description"));
  }


  @Test
  public void embeddedPropertiesAsNull() {

    EMain emain1 = createEMain();
    emain1.getEmbeddable().setDescription(null);

    Map<String, ValuePair> diff = DiffHelpInsert.diff((EntityBean) emain1, emainDesc);
    assertEquals(2, diff.size());
    assertEquals("foo", diff.get("name").getNewValue());
    assertEquals(13L, diff.get("version").getNewValue());

    assertNull(diff.get("embeddable.description"));
  }

  private EMain createEMain() {

    EMain emain = new EMain();
    emain.setName("foo");
    emain.setVersion(13l);

    Eembeddable embeddable = new Eembeddable();
    embeddable.setDescription("bar");
    emain.setEmbeddable(embeddable);

    return emain;
  }

}
