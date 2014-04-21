package com.avaje.ebeaninternal.server.core;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.ValuePair;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.embedded.EMain;
import com.avaje.tests.model.embedded.Eembeddable;

public class TestDiffHelpWithEmbedded extends BaseTestCase {

  DiffHelp diffHelp = new DiffHelp();

  EbeanServer server;
  BeanDescriptor<EMain> emainDesc;
  
  public TestDiffHelpWithEmbedded() {
    server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer)server;
    emainDesc = spiServer.getBeanDescriptor(EMain.class);
  }
  
  @Test
  public void testChangeExistingEmbedded() {
    
    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    
    emain2.getEmbeddable().setDescription("baz");
   
    Map<String, ValuePair> diff = diffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");
    
    Assert.assertNotNull(valuePair);
    Assert.assertEquals("bar",valuePair.getNewValue());
    Assert.assertEquals("baz",valuePair.getOldValue());
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
    
   
    Map<String, ValuePair> diff = diffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable.description");
    
    Assert.assertNotNull(valuePair);
    Assert.assertEquals("bar",valuePair.getNewValue());
    Assert.assertEquals("baz",valuePair.getOldValue());
  }
  
  @Test
  public void testFirstEmbeddedIsNull() {
    
    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);
    EMain emain2 = createEMain();
    
    Map<String, ValuePair> diff = diffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable");
    
    Assert.assertNotNull(valuePair);
    Assert.assertNull(valuePair.getNewValue());
    Assert.assertTrue(valuePair.getOldValue() instanceof Eembeddable);
    Assert.assertEquals("bar",((Eembeddable)valuePair.getOldValue()).getDescription());
  }
  
  @Test
  public void testSecondEmbeddedIsNull() {
    
    EMain emain1 = createEMain();
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);
    
    Map<String, ValuePair> diff = diffHelp.diff(emain1, emain2, emainDesc);
    Assert.assertEquals(1, diff.size());
    ValuePair valuePair = diff.get("embeddable");
    
    Assert.assertNotNull(valuePair);
    Assert.assertNull(valuePair.getOldValue());
    Assert.assertTrue(valuePair.getNewValue() instanceof Eembeddable);
    Assert.assertEquals("bar",((Eembeddable)valuePair.getNewValue()).getDescription());
  }

  @Test
  public void testBothEmbeddedIsNull() {
    
    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);
    EMain emain2 = createEMain();
    emain2.setEmbeddable(null);
    
    Map<String, ValuePair> diff = diffHelp.diff(emain1, emain2, emainDesc);
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
