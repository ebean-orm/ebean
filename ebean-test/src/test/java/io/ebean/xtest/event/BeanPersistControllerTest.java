package io.ebean.xtest.event;


import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.config.DatabaseConfig;
import io.ebean.event.BeanDeleteIdRequest;
import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistRequest;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
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
  public void issued1() {
    Database db = getDatabase(continuePersistingAdapter);

    UTMaster bean0 = new UTMaster("m0");
    bean0.setJournal(new UTMaster.Journal());
    db.save(bean0);

    UTMaster change0 = db.find(UTMaster.class, bean0.getId());
    change0.setName("change0");
    db.save(change0);

    UTMaster change1 = db.find(UTMaster.class, bean0.getId());
    change1.setName("change1");
    db.save(change1);

    UTMaster again = db.find(UTMaster.class, bean0.getId());
    UTMaster.Journal journal = again.getJournal();
    assertThat(journal.getEntries()).hasSize(2);

    db.shutdown();
  }

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

    db.deleteAll(EBasicVer.class, Arrays.asList(22, 23, 24));
    assertThat(stopPersistingAdapter.methodsCalled).hasSize(3);
    assertThat(stopPersistingAdapter.methodsCalled).containsExactly("preDeleteById", "preDeleteById", "preDeleteById");
    stopPersistingAdapter.methodsCalled.clear();

    db.shutdown();
  }

  @Test
  public void testCascade() {
    Database db = getDatabase(new BeanPersistAdapter() {
      @Override
      public boolean isRegisterFor(Class<?> cls) {
        return UTMaster.class == cls;
      }

      @Override
      public boolean preDelete(BeanPersistRequest<?> request) {
        return false;
      }
    });
    Integer id;
    UTMaster master = new UTMaster();
    master.addDetail(new UTDetail());
    db.save(master);
    id = master.getId();

    master = db.find(UTMaster.class, id);
    assertThat(master.getDetails()).hasSize(1);

    try (Transaction txn = db.beginTransaction()) {
      txn.setBatchMode(true);
      db.delete(master);
      txn.commit();
    }

    master = db.find(UTMaster.class, id);
    assertThat(master).isNotNull();
    // CHECKME: Deleting of master was denied by the PersistListener
    // What about detail? Is this intended, that it will be deleted?
    assertThat(master.getDetails()).hasSize(0);

  }

  @Test
  public void testInsertUpdateDelete_with_LazyLoad() {

    Database db = getDatabase(new BeanPersistAdapter() {

      @Override
      public boolean isRegisterFor(Class<?> cls) {
        return EBasicVer.class == cls;
      }

      @Override
      public boolean preInsert(BeanPersistRequest<?> request) {
        assertThat(((EBasicVer) request.bean()).getDescription()).isEqualTo("MyDescription");
        return true;
      }

      @Override
      public boolean preUpdate(BeanPersistRequest<?> request) {
        assertThat(((EBasicVer) request.bean()).getDescription()).isEqualTo("MyDescription");
        return true;
      }

      @Override
      public boolean preDelete(BeanPersistRequest<?> request) {
        assertThat(((EBasicVer) request.bean()).getDescription()).isEqualTo("MyDescription");
        return true;
      }
    });
    Integer id;
    try (Transaction txn = db.beginTransaction()) {
      txn.setBatchMode(true);
      EBasicVer bean = new EBasicVer("testController");
      bean.setDescription("MyDescription");

      db.save(bean);
      txn.commit();
      id = bean.getId();
    }


    try (Transaction txn = db.beginTransaction()) {
      txn.setBatchMode(true);
      EBasicVer bean = db.find(EBasicVer.class).setUseCache(false).select("name").setId(id).findOne();
      bean.setName("otherName");

      db.save(bean);

      txn.commitAndContinue();

      EBasicVer bean2 = db.find(EBasicVer.class).setUseCache(false).select("name").setId(id).findOne();
      assertThat(bean2).isSameAs(bean);
    }

    try (Transaction txn = db.beginTransaction()) {
      txn.setBatchMode(true);
      EBasicVer bean = db.find(EBasicVer.class).setUseCache(false).select("name").setId(id).findOne();

      db.delete(bean);
      txn.commitAndContinue();
      System.out.println(txn);
    }
/*
    db.update(bean);

    db.delete(bean);

    db.delete(EBasicVer.class, 22);

    db.deleteAll(EBasicVer.class, Arrays.asList(22,23,24));
*/
    db.shutdown();
  }

  private Database getDatabase(BeanPersistController persistAdapter) {
    DatabaseBuilder config = new DatabaseConfig();
    config.setName("h2ebasicver");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.addClass(EBasicVer.class);
    config.addClass(UTMaster.class);
    config.addClass(UTDetail.class);

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

      Object bean = request.bean();
      if (bean instanceof UTDetail) {
        UTDetail detail = (UTDetail) bean;
        // invoke lazy loading ... which invoke the flush of the jdbc batch
        detail.setQty(42);
      }
      if (bean instanceof UTMaster) {
        UTMaster master = (UTMaster) bean;
        UTMaster.Journal journal = master.getJournal();
        if (journal == null) {
          journal = new UTMaster.Journal();
          master.setJournal(journal);
        }
        // modify a "mutable scalar type" in preUpdate, should be included in update
        journal.addEntry();
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
