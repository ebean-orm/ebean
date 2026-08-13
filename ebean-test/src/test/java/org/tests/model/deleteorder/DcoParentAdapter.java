package org.tests.model.deleteorder;

import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;

/**
 * Persists a bean from inside the insert callback of {@link DcoParent}, the way an audit or an outbox
 * row is written. The BeanPersistController javadoc documents this as a supported use case.
 * <p>
 * Off by default so that the callback only fires for the tests that ask for it.
 */
public class DcoParentAdapter extends BeanPersistAdapter {

  private static boolean writeOnPreInsert;

  public static void writeOnPreInsert(boolean enabled) {
    writeOnPreInsert = enabled;
  }

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return DcoParent.class.equals(cls);
  }

  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {
    if (writeOnPreInsert) {
      DcoParent parent = (DcoParent) request.bean();
      request.database().save(new DcoAudit("inserting " + parent.getName()), request.transaction());
    }
    return true;
  }
}
