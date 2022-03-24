package org.tests.lifecycle;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicWithLifecycle;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLifecycleAnnotatedBean extends BaseTestCase {

  @Test
  public void shouldExecutePrePersistMethodsWhenSavingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PrePersist");

    DB.save(bean);

    assertThat(bean.getBuffer()).contains("prePersist1");
    assertThat(bean.getBuffer()).contains("prePersist2");
  }

  @Test
  public void shouldExecutePostPersistMethodsWhenSavingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostPersist");

    DB.save(bean);

    assertThat(bean.getBuffer()).contains("postPersist1");
    assertThat(bean.getBuffer()).contains("postPersist2");
  }

  @Test
  public void shouldExecutePostLoadMethodsWhenFindingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostLoad");

    DB.save(bean);

    EBasicWithLifecycle loaded = DB.find(EBasicWithLifecycle.class, bean.getId());
    assertThat(loaded.getBuffer()).contains("postLoad1");
    assertThat(loaded.getBuffer()).contains("postLoad2");
  }

  @Test
  public void shouldExecutePreUpdateMethodsWhenUpdatingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    DB.save(bean);

    bean.setName("PreUpdate");
    DB.save(bean);

    assertThat(bean.getBuffer()).contains("preUpdate1");
    assertThat(bean.getBuffer()).contains("preUpdate2");
  }

  @Test
  public void shouldExecutePreUpdateMethod_via_modelUpdate() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("before");
    bean.save();
    assertThat(bean.getBuffer()).contains("prePersist1", "prePersist2", "postPersist1", "postPersist2");

    final EBasicWithLifecycle found = DB.find(EBasicWithLifecycle.class, bean.getId());
    found.setName("after");
    found.update();
    assertThat(found.getBuffer()).contains("preUpdate1", "preUpdate2", "postUpdate1", "postUpdate2");

    final EBasicWithLifecycle check = DB.find(EBasicWithLifecycle.class, bean.getId());
    assertThat(check.getOther()).isEqualTo("nullpreUpdate1");
  }

  @Test
  public void shouldExecutePostUpdateMethodsWhenUpdatingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    DB.save(bean);

    bean.setName("PostUpdate");
    DB.save(bean);

    assertThat(bean.getBuffer()).contains("postUpdate1");
    assertThat(bean.getBuffer()).contains("postUpdate2");
  }

  @Test
  public void shouldExecutePreRemoveMethodsWhenRemovingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    DB.save(bean);
    DB.delete(bean);

    assertThat(bean.getBuffer()).contains("preSoftDelete");
    assertThat(bean.getBuffer()).contains("postSoftDelete");

    DB.deletePermanent(bean);

    assertThat(bean.getBuffer()).contains("preRemove1");
    assertThat(bean.getBuffer()).contains("preRemove2");
    assertThat(bean.getBuffer()).contains("postRemove1");
    assertThat(bean.getBuffer()).contains("postRemove2");
  }

  @Test
  public void shouldExecutePostRemoveMethodsWhenRemovingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    DB.save(bean);
    DB.deletePermanent(bean);

    assertThat(bean.getBuffer()).contains("postRemove1");
    assertThat(bean.getBuffer()).contains("postRemove2");
  }

  @Test
  public void shouldExecutePostConstructMethodsWhenFindingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostConstruct");

    DB.save(bean);

    EBasicWithLifecycle loaded = DB.find(EBasicWithLifecycle.class, bean.getId());
    assertThat(loaded.getBuffer()).contains("postConstruct1");
    assertThat(loaded.getBuffer()).contains("postConstruct2");
    // assert also that postLoad was executed
    assertThat(loaded.getBuffer()).contains("postLoad1");
    assertThat(loaded.getBuffer()).contains("postLoad2");
  }

  @Test
  public void shouldExecutePostConstructMethodsWhenInstantiated() {
    EBasicWithLifecycle bean = DB.getDefault().createEntityBean(EBasicWithLifecycle.class);
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

    DB.save(bean);

    BeanDescriptor<EBasicWithLifecycle> desc = ((SpiEbeanServer) server()).descriptor(EBasicWithLifecycle.class);
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
