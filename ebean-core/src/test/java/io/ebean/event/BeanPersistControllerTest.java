package io.ebean.event;


import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.config.DatabaseConfig;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanPersistControllerTest {

  private final PersistAdapter continuePersistingAdapter = new PersistAdapter(true);

  private final PersistAdapter stopPersistingAdapter = new PersistAdapter(false);

  @Test
  public void issue_1341() {

    Database db = getDatabase(continuePersistingAdapter);

    UTMaster bean0 = new UTMaster("one0");
    UTDetail detail0 = new UTDetail("detail0", 12, 23D);
    bean0.getDetails().add(detail0);

    db.save(bean0);

    UTMaster master = db.find(UTMaster.class)
      .setId(bean0.getId())
      .fetch("details", "name, version")
      .findOne();

    UTDetail utDetail = master.getDetails().get(0);
    utDetail.setName("detail0 mod");

    Transaction txn = db.beginTransaction();
    try {
      txn.setBatchMode(true);
      db.save(master);
      txn.commit();
    } finally {
      txn.end();
    }

    db.shutdown();
  }

  @Test
  public void testInsertUpdateDelete_given_continuePersistingAdapter() {

    Database db = getDatabase(continuePersistingAdapter);

    EBasicVer bean = new EBasicVer("testController");

    db.save(bean);
    assertThat(continuePersistingAdapter.methodsCalled).hasSize(2);
    assertThat(continuePersistingAdapter.methodsCalled).containsExactly("preInsert", "postInsert");
    continuePersistingAdapter.methodsCalled.clear();

    bean.setName("modified");
    db.save(bean);
    assertThat(continuePersistingAdapter.methodsCalled).hasSize(2);
    assertThat(continuePersistingAdapter.methodsCalled).containsExactly("preUpdate", "postUpdate");
    continuePersistingAdapter.methodsCalled.clear();

    db.delete(bean);
    assertThat(continuePersistingAdapter.methodsCalled).hasSize(2);
    assertThat(continuePersistingAdapter.methodsCalled).containsExactly("preDelete", "postDelete");

    db.shutdown();
  }

  @Test
  public void testInsertUpdateDelete_given_stopPersistingAdapter() {

    Database db = getDatabase(stopPersistingAdapter);

    EBasicVer bean = new EBasicVer("testController");

    db.save(bean);
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(1);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preInsert");
    stopPersistingAdapter.methodsCalled.clear();

    bean.setName("modified");
    db.update(bean);
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(1);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preUpdate");
    stopPersistingAdapter.methodsCalled.clear();

    db.delete(bean);
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(1);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preDelete");
    stopPersistingAdapter.methodsCalled.clear();

    db.delete(EBasicVer.class, 22);
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(1);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preDeleteById");
    stopPersistingAdapter.methodsCalled.clear();

    db.deleteAll(EBasicVer.class, Arrays.asList(22,23,24));
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(3);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preDeleteById", "preDeleteById", "preDeleteById");
    stopPersistingAdapter.methodsCalled.clear();

    db.shutdown();
  }

  private Database getDatabase(PersistAdapter persistAdapter) {

    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2ebasicver");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.getClasses().add(EBasicVer.class);
    config.getClasses().add(UTMaster.class);
    config.getClasses().add(UTDetail.class);

    config.add(persistAdapter);

    return DatabaseFactory.create(config);
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

      Object bean = request.getBean();
      if (bean instanceof UTDetail) {
        UTDetail detail = (UTDetail)bean;
        // invoke lazy loading ... which invoke the flush of the jdbc batch
        detail.setQty(42);
      }

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

    @Override
    public void preDelete(BeanDeleteIdRequest request) {
      methodsCalled.add("preDeleteById");
    }
  }

}
