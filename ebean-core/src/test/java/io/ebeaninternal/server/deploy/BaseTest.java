package io.ebeaninternal.server.deploy;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.annotation.Platform;
import io.ebeaninternal.api.SpiEbeanServer;
import org.tests.model.basic.Animal;

import javax.persistence.PersistenceException;

public class BaseTest {

  protected SpiEbeanServer db = (SpiEbeanServer)DB.getDefault();

  protected <T> BeanDescriptor<T> getBeanDescriptor(Class<T> cls) {
    return db.descriptor(cls);
  }

  protected SpiEbeanServer spiEbeanServer() {
    return db;
  }

  protected Database server() {
    return db;
  }

  protected void initTables() {
    try {
      db.find(Animal.class).findCount();
    } catch (PersistenceException e) {
      db.script().run("/h2-init.sql");
    }
  }

  protected boolean isH2() {
    return isPlatform(Platform.H2);
  }
  protected boolean isPostgres() {
    return isPlatform(Platform.POSTGRES);
  }
  protected boolean isSqlServer() {
    return isPlatform(Platform.SQLSERVER);
  }
  protected boolean isMySql() {
    return isPlatform(Platform.MYSQL);
  }
  protected boolean isPlatform(Platform platform) {
    return db.platform().base().equals(platform);
  }
}
