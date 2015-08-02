package com.avaje.tests.lifecycle;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasicWithLifecycle;
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

    assertThat(bean.getBuffer()).contains("preRemove1");
    assertThat(bean.getBuffer()).contains("preRemove2");
  }

  @Test
  public void shouldExecutePostRemoveMethodsWhenRemovingBean() {

    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.save(bean);
    Ebean.delete(bean);

    assertThat(bean.getBuffer()).contains("postRemove1");
    assertThat(bean.getBuffer()).contains("postRemove2");
  }
}
