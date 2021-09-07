package io.ebeaninternal.server.core;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ValuePair;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.junit.jupiter.api.Test;
import org.tests.model.embedded.EMain;
import org.tests.model.embedded.Eembeddable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestDiffHelpWithEmbedded extends BaseTestCase {

  Database server;

  BeanDescriptor<EMain> emainDesc;

  public TestDiffHelpWithEmbedded() {
    server = DB.getDefault();
    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    emainDesc = spiServer.getBeanDescriptor(EMain.class);
  }

  @Test
  public void testChangeExistingEmbedded() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();

    emain2.getEmbeddable().setDescription("baz");

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    assertNotNull(valuePair);
    assertEquals("bar", valuePair.getNewValue());
    assertEquals("baz", valuePair.getOldValue());
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
    assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    assertNotNull(valuePair);
    assertEquals("bar", valuePair.getNewValue());
    assertEquals("baz", valuePair.getOldValue());
  }

  @Test
  public void testDiffEmbedded_flatMode() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    emain2.getEmbeddable().setDescription("baz");

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    assertNotNull(valuePair);
    assertEquals("bar", valuePair.getNewValue());
    assertEquals("baz", valuePair.getOldValue());
  }

  @Test
  public void testFirstEmbeddedIsNull() {

    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);
    EMain emain2 = createEMain();

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    assertNotNull(valuePair);
    assertNull(valuePair.getNewValue());
    assertEquals("bar", valuePair.getOldValue());
  }

  @Test
  public void testSecondEmbeddedIsNull() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    assertNotNull(valuePair);
    assertNull(valuePair.getOldValue());
    assertEquals("bar", valuePair.getNewValue());
  }

  @Test
  public void testSecondEmbeddedIsNull_given_flatMode() {

    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");

    assertNotNull(valuePair);
    assertNull(valuePair.getOldValue());
    assertEquals("bar", valuePair.getNewValue());
  }

  @Test
  public void testBothEmbeddedIsNull() {

    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);

    Map<String, ValuePair> diff = DiffHelp.diff(emain1, emain2, emainDesc);
    assertEquals(0, diff.size());
  }

  private EMain createEMain() {
    EMain emain = new EMain();
    emain.setName("foo");
    emain.setVersion(13L);

    Eembeddable embeddable = new Eembeddable();
    embeddable.setDescription("bar");
    emain.setEmbeddable(embeddable);
    return emain;
  }

}
