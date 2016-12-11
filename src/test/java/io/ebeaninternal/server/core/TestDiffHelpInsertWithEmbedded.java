package io.ebeaninternal.server.core;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.ValuePair;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.embedded.EMain;
import org.tests.model.embedded.Eembeddable;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

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

    Map<String, ValuePair> diff = emainDesc.diffForInsert((EntityBean) emain1);
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

    Map<String, ValuePair> diff = emainDesc.diffForInsert((EntityBean) emain1);
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

    Map<String, ValuePair> diff = emainDesc.diffForInsert((EntityBean) emain1);
    assertEquals(2, diff.size());
    assertEquals("foo", diff.get("name").getNewValue());
    assertEquals(13L, diff.get("version").getNewValue());

    assertNull(diff.get("embeddable.description"));
  }


  @Test
  public void embeddedPropertiesAsNull() {

    EMain emain1 = createEMain();
    emain1.getEmbeddable().setDescription(null);

    Map<String, ValuePair> diff = emainDesc.diffForInsert((EntityBean) emain1);
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
