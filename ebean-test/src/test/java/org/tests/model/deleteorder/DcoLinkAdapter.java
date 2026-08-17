package org.tests.model.deleteorder;

import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;

/**
 * Persists a bean from inside a delete callback of {@link DcoLink}, the way an audit or an outbox row
 * is written. The BeanPersistController javadoc documents this as a supported use case.
 * <p>
 * Off by default so that the callbacks only fire for the tests that ask for them.
 */
public class DcoLinkAdapter extends BeanPersistAdapter {

  private static boolean writeOnPreDelete;
  private static boolean writeOnPostDelete;

  public static void writeOnPreDelete(boolean enabled) {
    writeOnPreDelete = enabled;
  }

  public static void writeOnPostDelete(boolean enabled) {
    writeOnPostDelete = enabled;
  }

  public static void reset() {
    writeOnPreDelete = false;
    writeOnPostDelete = false;
  }

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return DcoLink.class.equals(cls);
  }

  @Override
  public boolean preDelete(BeanPersistRequest<?> request) {
    if (writeOnPreDelete) {
      audit(request, "pre");
    }
    return true;
  }

  @Override
  public void postDelete(BeanPersistRequest<?> request) {
    if (writeOnPostDelete) {
      audit(request, "post");
    }
  }

  private void audit(BeanPersistRequest<?> request, String phase) {
    DcoLink link = (DcoLink) request.bean();
    request.database().save(new DcoAudit(phase + " delete of link " + link.getId()), request.transaction());
  }
}
