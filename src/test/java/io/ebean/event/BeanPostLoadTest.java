package io.ebean.event;


import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanPostLoadTest extends BaseTestCase {

  PostLoad postLoad = new PostLoad(false);

  @Test
  public void testPostLoad() {

    EbeanServer ebeanServer = getEbeanServer();

    EBasicVer bean = new EBasicVer("testPostLoad");
    bean.setDescription("someDescription");
    bean.setOther("other");

    ebeanServer.save(bean);

    EBasicVer found = ebeanServer.find(EBasicVer.class)
      .select("name, other")
      .setId(bean.getId())
      .findOne();

    assertThat(postLoad.methodsCalled).hasSize(1);
    assertThat(postLoad.methodsCalled).containsExactly("postLoad");
    assertThat(postLoad.beanState.getLoadedProps()).containsExactly("id", "name", "other");
    assertThat(postLoad.bean).isSameAs(found);

    ebeanServer.delete(bean);
  }


  private EbeanServer getEbeanServer() {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig config = new ServerConfig();

    config.setName("h2ebasicver");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.getClasses().add(EBasicVer.class);

    config.add(postLoad);

    return EbeanServerFactory.create(config);
  }

  static class PostLoad implements BeanPostLoad {


    boolean dummy;

    List<String> methodsCalled = new ArrayList<>();

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
