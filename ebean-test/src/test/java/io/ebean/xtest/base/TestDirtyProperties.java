package io.ebean.xtest.base;

import io.ebean.xtest.BaseTestCase;
import io.ebean.ValuePair;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.core.DefaultBeanState;
import org.junit.jupiter.api.Test;
import org.tests.model.embedded.EMain;
import org.tests.model.embedded.Eembeddable;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    Set<String> changedProps = beanState.changedProps();
    assertEquals(1, changedProps.size());
    assertThat(changedProps).contains("name");

    Map<String, ValuePair> dirtyValues = beanState.dirtyValues();
    assertEquals(1, dirtyValues.size());
    assertThat(dirtyValues.keySet()).contains("name");

    ValuePair valuePair = dirtyValues.get("name");
    assertNotNull(valuePair);
    assertEquals("changedFoo", valuePair.getNewValue());
    assertEquals("foo", valuePair.getOldValue());

    Eembeddable embeddableRead = emain.getEmbeddable();
    embeddableRead.setDescription("embChanged");

    Set<String> changedProps2 = beanState.changedProps();
    assertEquals(2, changedProps2.size());
    assertThat(changedProps2).contains("name", "embeddable.description");

    Map<String, ValuePair> dirtyValues2 = beanState.dirtyValues();
    assertEquals(2, dirtyValues2.size());
    assertThat(dirtyValues2.keySet()).contains("name", "embeddable.description");

    ValuePair valuePair2 = dirtyValues2.get("embeddable.description");
    assertEquals("embChanged", valuePair2.getNewValue());
    assertEquals("bar", valuePair2.getOldValue());
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

    assertSame(embeddable, emain.getEmbeddable());

    Eembeddable embeddable2 = setEmbeddedBean(emain, "changeEmbeddedInstance");
    assertSame(embeddable2, emain.getEmbeddable());
    assertNotSame(embeddable, emain.getEmbeddable());


    DefaultBeanState beanState = new DefaultBeanState(eb);

    Set<String> changedProps2 = beanState.changedProps();
    assertEquals(2, changedProps2.size());
    assertThat(changedProps2).contains("name");

    assertThat(changedProps2).contains("embeddable");

    Map<String, ValuePair> dirtyValues2 = beanState.dirtyValues();
    assertEquals(2, dirtyValues2.size());
    assertThat(dirtyValues2.keySet()).contains("name", "embeddable");


    ValuePair valuePair2 = dirtyValues2.get("embeddable");
    assertSame(embeddable2, valuePair2.getNewValue());
    assertSame(embeddable, valuePair2.getOldValue());
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
    assertThat(embeddablePropertyIndex).isGreaterThan(-1);
    ((EntityBean) embeddable)._ebean_getIntercept().setEmbeddedOwner(owner, embeddablePropertyIndex);
    return embeddable;
  }
}
