package io.ebean;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.core.DefaultBeanState;
import org.tests.model.embedded.EMain;
import org.tests.model.embedded.Eembeddable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class TestDirtyProperties extends BaseTestCase {

  @Test
  public void testEmbeddedUpdateEmbeddedProperty() {

    EMain emain = new EMain();

    EntityBean eb = (EntityBean) emain;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    emain.setId(1);
    emain.setName("foo");
    Eembeddable embeddable = setEmbeddedBean(emain, "bar");
    setEmbeddedLoaded(embeddable);

    // sets loaded state so follow setters are deemed as changes to the bean
    ebi.setLoaded();

    emain.setName("changedFoo");

    DefaultBeanState beanState = new DefaultBeanState(eb);

    Set<String> changedProps = beanState.getChangedProps();
    Assert.assertEquals(1, changedProps.size());
    Assert.assertTrue(changedProps.contains("name"));

    Map<String, ValuePair> dirtyValues = beanState.getDirtyValues();
    Assert.assertEquals(1, dirtyValues.size());
    Assert.assertTrue(dirtyValues.keySet().contains("name"));

    ValuePair valuePair = dirtyValues.get("name");
    Assert.assertNotNull(valuePair);
    Assert.assertEquals("changedFoo", valuePair.getNewValue());
    Assert.assertEquals("foo", valuePair.getOldValue());

    Eembeddable embeddableRead = emain.getEmbeddable();
    embeddableRead.setDescription("embChanged");

    Set<String> changedProps2 = beanState.getChangedProps();
    Assert.assertEquals(2, changedProps2.size());
    Assert.assertTrue(changedProps2.contains("name"));
    Assert.assertTrue(changedProps2.contains("embeddable.description"));

    Map<String, ValuePair> dirtyValues2 = beanState.getDirtyValues();
    Assert.assertEquals(2, dirtyValues2.size());
    Assert.assertTrue(dirtyValues2.keySet().contains("name"));
    Assert.assertTrue(dirtyValues2.keySet().contains("embeddable.description"));

    ValuePair valuePair2 = dirtyValues2.get("embeddable.description");
    Assert.assertEquals("embChanged", valuePair2.getNewValue());
    Assert.assertEquals("bar", valuePair2.getOldValue());
  }


  @Test
  public void testEmbeddedUpdateSetNewBean() {

    EMain emain = new EMain();

    EntityBean eb = (EntityBean) emain;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    emain.setId(1);
    emain.setName("foo");
    Eembeddable embeddable = setEmbeddedBean(emain, "bar");
    setEmbeddedLoaded(embeddable);

    // sets loaded state so follow setters are deemed as changes to the bean
    ebi.setLoaded();

    emain.setName("changedFoo");

    Assert.assertSame(embeddable, emain.getEmbeddable());

    Eembeddable embeddable2 = setEmbeddedBean(emain, "changeEmbeddedInstance");
    Assert.assertSame(embeddable2, emain.getEmbeddable());
    Assert.assertNotSame(embeddable, emain.getEmbeddable());


    DefaultBeanState beanState = new DefaultBeanState(eb);

    Set<String> changedProps2 = beanState.getChangedProps();
    Assert.assertEquals(2, changedProps2.size());
    Assert.assertTrue(changedProps2.contains("name"));

    Assert.assertTrue("The whole bean instance has changed", changedProps2.contains("embeddable"));

    Map<String, ValuePair> dirtyValues2 = beanState.getDirtyValues();
    Assert.assertEquals(2, dirtyValues2.size());
    Assert.assertTrue(dirtyValues2.keySet().contains("name"));
    Assert.assertTrue(dirtyValues2.keySet().contains("embeddable"));


    ValuePair valuePair2 = dirtyValues2.get("embeddable");
    Assert.assertSame(embeddable2, valuePair2.getNewValue());
    Assert.assertSame(embeddable, valuePair2.getOldValue());

  }


  private void setEmbeddedLoaded(Eembeddable embeddable) {
    ((EntityBean) embeddable)._ebean_getIntercept().setLoaded();
  }


  private Eembeddable setEmbeddedBean(EMain emain, String description) {

    Eembeddable embeddable = new Eembeddable();
    embeddable.setDescription(description);

    emain.setEmbeddable(embeddable);

    EntityBean owner = (EntityBean) emain;
    EntityBeanIntercept ebi = owner._ebean_getIntercept();

    // hooks the embeddable bean back to the owner
    int embeddablePropertyIndex = ebi.findProperty("embeddable");
    Assert.assertTrue(embeddablePropertyIndex > -1);
    ((EntityBean) embeddable)._ebean_getIntercept().setEmbeddedOwner(owner, embeddablePropertyIndex);
    return embeddable;
  }
}
