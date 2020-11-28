package io.ebean.event;


import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanPostLoadTest extends BaseTestCase {

  PostLoad postLoad = new PostLoad(false);

  @Test
  public void testPostLoad() {

    Database db = createDatabase();

    EBasicVer bean = new EBasicVer("testPostLoad");
    bean.setDescription("someDescription");
    bean.setOther("other");

    db.save(bean);

    EBasicVer found = db.find(EBasicVer.class)
      .select("name, other")
      .setId(bean.getId())
      .findOne();

    assertThat(postLoad.methodsCalled).hasSize(1);
    assertThat(postLoad.methodsCalled).containsExactly("postLoad");
    assertThat(postLoad.beanState.getLoadedProps()).containsExactly("id", "name", "other");
    assertThat(postLoad.bean).isSameAs(found);

    db.delete(bean);
    db.shutdown();
  }


  private Database createDatabase() {

    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2ebasicver");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.getClasses().add(EBasicVer.class);

    config.add(postLoad);

    return DatabaseFactory.create(config);
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
      this.beanState = DB.getBeanState(bean);
    }

  }

}
