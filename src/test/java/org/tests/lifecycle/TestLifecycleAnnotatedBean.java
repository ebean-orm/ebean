package org.tests.lifecycle;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.EBasicWithLifecycle;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLifecycleAnnotatedBean extends BaseTestCase {

  @Test
  public void shouldExecutePrePersistMethodsWhenSavingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PrePersist");

    Ebean.save(bean);

    assertThat(bean.getBuffer()).contains("prePersist1");
    assertThat(bean.getBuffer()).contains("prePersist2");
  }

  @Test
  public void shouldExecutePostPersistMethodsWhenSavingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostPersist");

    Ebean.save(bean);

    assertThat(bean.getBuffer()).contains("postPersist1");
    assertThat(bean.getBuffer()).contains("postPersist2");
  }

  @Test
  public void shouldExecutePostLoadMethodsWhenFindingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostLoad");

    Ebean.save(bean);

    EBasicWithLifecycle loaded = Ebean.find(EBasicWithLifecycle.class, bean.getId());
    assertThat(loaded.getBuffer()).contains("postLoad1");
    assertThat(loaded.getBuffer()).contains("postLoad2");
  }

  @Test
  public void shouldExecutePreUpdateMethodsWhenUpdatingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.save(bean);

    bean.setName("PreUpdate");
    Ebean.save(bean);

    assertThat(bean.getBuffer()).contains("preUpdate1");
    assertThat(bean.getBuffer()).contains("preUpdate2");
  }

  @Test
  public void shouldExecutePostUpdateMethodsWhenUpdatingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.save(bean);

    bean.setName("PostUpdate");
    Ebean.save(bean);

    assertThat(bean.getBuffer()).contains("postUpdate1");
    assertThat(bean.getBuffer()).contains("postUpdate2");
  }

  @Test
  public void shouldExecutePreRemoveMethodsWhenRemovingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.save(bean);
    Ebean.delete(bean);

    assertThat(bean.getBuffer()).contains("preSoftDelete");
    assertThat(bean.getBuffer()).contains("postSoftDelete");

    Ebean.deletePermanent(bean);

    assertThat(bean.getBuffer()).contains("preRemove1");
    assertThat(bean.getBuffer()).contains("preRemove2");
    assertThat(bean.getBuffer()).contains("postRemove1");
    assertThat(bean.getBuffer()).contains("postRemove2");
  }

  @Test
  public void shouldExecutePostRemoveMethodsWhenRemovingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.save(bean);
    Ebean.deletePermanent(bean);

    assertThat(bean.getBuffer()).contains("postRemove1");
    assertThat(bean.getBuffer()).contains("postRemove2");
  }

  @Test
  public void shouldExecutePostConstructMethodsWhenFindingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostConstruct");

    Ebean.save(bean);

    EBasicWithLifecycle loaded = Ebean.find(EBasicWithLifecycle.class, bean.getId());
    assertThat(loaded.getBuffer()).contains("postConstruct1");
    assertThat(loaded.getBuffer()).contains("postConstruct2");
    // assert also that postLoad was executed
    assertThat(loaded.getBuffer()).contains("postLoad1");
    assertThat(loaded.getBuffer()).contains("postLoad2");
  }

  @Test
  public void shouldExecutePostConstructMethodsWhenInstantiated() {
    EBasicWithLifecycle bean = Ebean.getDefaultServer().createEntityBean(EBasicWithLifecycle.class);
    bean.setName("PostConstruct");


    assertThat(bean.getBuffer()).contains("postConstruct1");
    assertThat(bean.getBuffer()).contains("postConstruct2");
    // assert also that postLoad is not executed now
    assertThat(bean.getBuffer()).doesNotContain("postLoad1");
    assertThat(bean.getBuffer()).doesNotContain("postLoad2");
  }


  @Test
  public void testLazyLoadBehaviour() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("LazyLoad");

    Ebean.save(bean);

    BeanDescriptor<EBasicWithLifecycle> desc = ((SpiEbeanServer) server()).getBeanDescriptor(EBasicWithLifecycle.class);
    EBasicWithLifecycle loaded = desc.createReference(false, false, bean.getId(), null);

    // Here you see the big difference.
    // @PostLoad is executed always, also on lazy loaded beans
    assertThat(loaded.getBuffer()).contains("postConstruct1");
    assertThat(loaded.getBuffer()).contains("postConstruct2");

    // assert also that postLoad is not yet executed
    assertThat(loaded.getBuffer()).doesNotContain("postLoad1");
    assertThat(loaded.getBuffer()).doesNotContain("postLoad2");

    // now read name -> will load the bean
    assertThat(loaded.getName()).isEqualTo("LazyLoad");
    assertThat(loaded.getBuffer()).contains("postLoad1");
    assertThat(loaded.getBuffer()).contains("postLoad2");
  }
}
