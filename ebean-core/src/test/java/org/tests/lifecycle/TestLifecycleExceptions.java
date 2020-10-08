package org.tests.lifecycle;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.EBasicWithLifecycleExceptions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import javax.persistence.PersistenceException;

public class TestLifecycleExceptions extends BaseTestCase {

  @Test
  public void prePersist_unchecked() {
    EBasicWithLifecycleExceptions bean = new EBasicWithLifecycleExceptions();
    bean.preException = new IndexOutOfBoundsException();
    assertThatThrownBy(() -> DB.save(bean)).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void prepersist_checked() {
    EBasicWithLifecycleExceptions bean = new EBasicWithLifecycleExceptions();
    bean.preException = new IOException();
    assertThatThrownBy(() -> DB.save(bean))
        .isInstanceOf(PersistenceException.class)
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  public void postPersist_unchecked() {
    EBasicWithLifecycleExceptions bean = new EBasicWithLifecycleExceptions();
    bean.postException = new IndexOutOfBoundsException();
    assertThatThrownBy(() -> DB.save(bean)).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void postpersist_checked() {
    EBasicWithLifecycleExceptions bean = new EBasicWithLifecycleExceptions();
    bean.postException = new IOException();
    assertThatThrownBy(() -> DB.save(bean))
        .isInstanceOf(PersistenceException.class)
        .hasCauseInstanceOf(IOException.class);
  }
}
