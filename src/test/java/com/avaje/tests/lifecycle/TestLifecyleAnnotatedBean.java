package com.avaje.tests.lifecycle;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasicWithLifecycle;

public class TestLifecyleAnnotatedBean extends BaseTestCase {

  @Test
  public void shouldExecutePrePersistMethodsWhenSavingBean() {
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PrePersist");

    Ebean.getServerCacheManager();
    Ebean.save(bean);
    
    Assert.assertThat(bean.getBuffer(), Matchers.containsString("prePersist1"));
    Assert.assertThat(bean.getBuffer(), Matchers.containsString("prePersist2"));
  }

  @Test
  public void shouldExecutePostPersistMethodsWhenSavingBean() {
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostPersist");

    Ebean.getServerCacheManager();
    Ebean.save(bean);

    Assert.assertThat(bean.getBuffer(), Matchers.containsString("postPersist1"));
    Assert.assertThat(bean.getBuffer(), Matchers.containsString("postPersist2"));
  }

  @Test
  public void shouldExecutePostLoadMethodsWhenFindingBean() {
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("PostLoad");

    Ebean.getServerCacheManager();
    Ebean.save(bean);

    EBasicWithLifecycle loaded = Ebean.find(EBasicWithLifecycle.class, bean.getId());
    Assert.assertThat(loaded.getBuffer(), Matchers.containsString("postLoad1"));
    Assert.assertThat(loaded.getBuffer(), Matchers.containsString("postLoad2"));
  }

  @Test
  public void shouldExecutePreUpdateMethodsWhenUpdatingBean() {
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.getServerCacheManager();
    Ebean.save(bean);
    
    bean.setName("PreUpdate");
    Ebean.save(bean);

    Assert.assertThat(bean.getBuffer(), Matchers.containsString("preUpdate1"));
    Assert.assertThat(bean.getBuffer(), Matchers.containsString("preUpdate2"));
  }

  @Test
  public void shouldExecutePostUpdateMethodsWhenUpdatingBean() {
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.getServerCacheManager();
    Ebean.save(bean);

    bean.setName("PostUpdate");
    Ebean.save(bean);

    Assert.assertThat(bean.getBuffer(), Matchers.containsString("postUpdate1"));
    Assert.assertThat(bean.getBuffer(), Matchers.containsString("postUpdate2"));
  }

  @Test
  public void shouldExecutePreRemoveMethodsWhenRemovingBean() {
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.getServerCacheManager();
    Ebean.save(bean);
    Ebean.delete(bean);

    Assert.assertThat(bean.getBuffer(), Matchers.containsString("preRemove1"));
    Assert.assertThat(bean.getBuffer(), Matchers.containsString("preRemove2"));
  }

  @Test
  public void shouldExecutePostRemoveMethodsWhenRemovingBean() {
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("Persisted");

    Ebean.getServerCacheManager();
    Ebean.save(bean);
    Ebean.delete(bean);

    Assert.assertThat(bean.getBuffer(), Matchers.containsString("postRemove1"));
    Assert.assertThat(bean.getBuffer(), Matchers.containsString("postRemove2"));
  }
}
