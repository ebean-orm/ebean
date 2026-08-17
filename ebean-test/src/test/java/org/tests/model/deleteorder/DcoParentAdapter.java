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
  private static boolean sqlUpdateOnPreInsert;

  public static void writeOnPreInsert(boolean enabled) {
    writeOnPreInsert = enabled;
  }

  /**
   * Same as {@link #writeOnPreInsert(boolean)} but through SqlUpdate, which takes the
   * BatchControl.executeStatementOrBatch path rather than executeOrQueue.
   */
  public static void sqlUpdateOnPreInsert(boolean enabled) {
    sqlUpdateOnPreInsert = enabled;
  }

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return DcoParent.class.equals(cls);
  }

  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {
    DcoParent parent = (DcoParent) request.bean();
    if (writeOnPreInsert) {
      request.database().save(new DcoAudit("inserting " + parent.getName()), request.transaction());
    }
    if (sqlUpdateOnPreInsert) {
      request.database().sqlUpdate("update dco_audit set message = ? where id = ?")
        .setParameter(1, "inserting " + parent.getName())
        .setParameter(2, -1L)
        .usingTransaction(request.transaction())
        .execute();
    }
    return true;
  }
}
