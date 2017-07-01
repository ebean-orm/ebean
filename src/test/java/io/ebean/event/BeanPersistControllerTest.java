package io.ebean.event;


import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanPersistControllerTest {

  PersistAdapter continuePersistingAdapter = new PersistAdapter(true);

  PersistAdapter stopPersistingAdapter = new PersistAdapter(false);

  @Test
  public void testInsertUpdateDelete_given_continuePersistingAdapter() {

    EbeanServer ebeanServer = getEbeanServer(continuePersistingAdapter);


    EBasicVer bean = new EBasicVer("testController");

    ebeanServer.save(bean);
    assertThat(continuePersistingAdapter.methodsCalled).hasSize(2);
    assertThat(continuePersistingAdapter.methodsCalled).containsExactly("preInsert", "postInsert");
    continuePersistingAdapter.methodsCalled.clear();

    bean.setName("modified");
    ebeanServer.save(bean);
    assertThat(continuePersistingAdapter.methodsCalled).hasSize(2);
    assertThat(continuePersistingAdapter.methodsCalled).containsExactly("preUpdate", "postUpdate");
    continuePersistingAdapter.methodsCalled.clear();

    ebeanServer.delete(bean);
    assertThat(continuePersistingAdapter.methodsCalled).hasSize(2);
    assertThat(continuePersistingAdapter.methodsCalled).containsExactly("preDelete", "postDelete");

  }

  @Test
  public void testInsertUpdateDelete_given_stopPersistingAdapter() {

    EbeanServer ebeanServer = getEbeanServer(stopPersistingAdapter);

    EBasicVer bean = new EBasicVer("testController");

    ebeanServer.save(bean);
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(1);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preInsert");
    stopPersistingAdapter.methodsCalled.clear();

    bean.setName("modified");
    ebeanServer.update(bean);
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(1);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preUpdate");
    stopPersistingAdapter.methodsCalled.clear();

    ebeanServer.delete(bean);
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(1);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preDelete");

  }

  private EbeanServer getEbeanServer(PersistAdapter persistAdapter) {

    System.setProperty("ebean.ignoreExtraDdl", "true");
    ServerConfig config = new ServerConfig();

    config.setName("h2ebasicver");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.getClasses().add(EBasicVer.class);

    config.add(persistAdapter);

    return EbeanServerFactory.create(config);
  }

  static class PersistAdapter extends BeanPersistAdapter {

    boolean continueDefaultPersisting;

    List<String> methodsCalled = new ArrayList<>();

    /**
     * No default constructor so only registered manually.
     */
    PersistAdapter(boolean continueDefaultPersisting) {
      this.continueDefaultPersisting = continueDefaultPersisting;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return true;
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> request) {
      methodsCalled.add("preDelete");
      return continueDefaultPersisting;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
      methodsCalled.add("preInsert");
      return continueDefaultPersisting;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
      methodsCalled.add("preUpdate");
      return continueDefaultPersisting;
    }

    @Override
    public void postDelete(BeanPersistRequest<?> request) {
      methodsCalled.add("postDelete");
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {
      methodsCalled.add("postInsert");
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
      methodsCalled.add("postUpdate");
    }
  }

}
