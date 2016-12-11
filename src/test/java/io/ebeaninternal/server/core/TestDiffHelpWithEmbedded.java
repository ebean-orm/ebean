package io.ebeaninternal.server.core;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.ValuePair;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.embedded.EMain;
import org.tests.model.embedded.Eembeddable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class TestDiffHelpWithEmbedded extends BaseTestCase {

  EbeanServer server;

  BeanDescriptor<EMain> emainDesc;

  public TestDiffHelpWithEmbedded() {
    server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    emainDesc = spiServer.getBeanDescriptor(EMain.class);
  }

  @Test
  public void testChangeExistingEmbedded() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();

    emain2.getEmbeddable().setDescription("baz");

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    Assert.assertNotNull(valuePair);
    Assert.assertEquals("bar", valuePair.getNewValue());
    Assert.assertEquals("baz", valuePair.getOldValue());
  }

  /**
   * Same result as testChangeExistingEmbedded.
   */
  @Test
  public void testSetViaNewEmbedded() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();

    Eembeddable embeddable = new Eembeddable();
    embeddable.setDescription("baz");
    emain2.setEmbeddable(embeddable);


    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    Assert.assertNotNull(valuePair);
    Assert.assertEquals("bar", valuePair.getNewValue());
    Assert.assertEquals("baz", valuePair.getOldValue());
  }

  @Test
  public void testDiffEmbedded_flatMode() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    emain2.getEmbeddable().setDescription("baz");

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    Assert.assertNotNull(valuePair);
    Assert.assertEquals("bar", valuePair.getNewValue());
    Assert.assertEquals("baz", valuePair.getOldValue());
  }

  @Test
  public void testFirstEmbeddedIsNull() {

    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);
    EMain emain2 = createEMain();

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    Assert.assertNotNull(valuePair);
    Assert.assertNull(valuePair.getNewValue());
    Assert.assertEquals("bar", valuePair.getOldValue());
  }

  @Test
  public void testSecondEmbeddedIsNull() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    Assert.assertNotNull(valuePair);
    Assert.assertNull(valuePair.getOldValue());
    Assert.assertEquals("bar", valuePair.getNewValue());
  }

  @Test
  public void testSecondEmbeddedIsNull_given_flatMode() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    Assert.assertNotNull(valuePair);
    Assert.assertNull(valuePair.getOldValue());
    Assert.assertEquals("bar", valuePair.getNewValue());
  }

  @Test
  public void testBothEmbeddedIsNull() {

    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(0, diff.size());
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
