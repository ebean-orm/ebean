package com.avaje.ebean.event;


import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanPostLoadTest {

  PostLoad postLoad = new PostLoad(false);

  @Test
  public void testPostLoad() {

    EbeanServer ebeanServer = getEbeanServer();

    EBasicVer bean = new EBasicVer();
    bean.setName("testPostLoad");
    bean.setDescription("someDescription");
    bean.setOther("other");

    ebeanServer.save(bean);

    EBasicVer found = ebeanServer.find(EBasicVer.class)
        .select("name, other")
        .setId(bean.getId())
        .findUnique();

    assertThat(postLoad.methodsCalled).hasSize(1);
    assertThat(postLoad.methodsCalled).containsExactly("postLoad");
    assertThat(postLoad.beanState.getLoadedProps()).containsExactly("id", "name", "other");
    assertThat(postLoad.bean).isSameAs(found);

    ebeanServer.delete(bean);
  }


  private EbeanServer getEbeanServer() {

    ServerConfig config = new ServerConfig();

    config.setName("h2other");
    config.loadFromProperties();

    config.setRegister(false);
    config.setDefaultServer(false);
    config.getClasses().add(EBasicVer.class);

    config.add(postLoad);
    
    return EbeanServerFactory.create(config);
  }

  static class PostLoad implements BeanPostLoad {


    boolean dummy;

    List<String> methodsCalled = new ArrayList<String>();

    Object bean;

    BeanState beanState;

    /**
     * No default constructor so only registered manually.
     */
    PostLoad(boolean dummy) {
      this.dummy = dummy;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return true;
    }

    @Override
    public void postLoad(Object bean) {
      this.methodsCalled.add("postLoad");
      this.bean = bean;
      this.beanState = Ebean.getBeanState(bean);
    }

  }

}