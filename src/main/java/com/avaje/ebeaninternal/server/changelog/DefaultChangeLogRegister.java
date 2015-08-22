package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.annotation.ChangeLog;
import com.avaje.ebean.event.BeanPersistRequest;
import com.avaje.ebean.event.changelog.ChangeLogFilter;
import com.avaje.ebean.event.changelog.ChangeLogRegister;

import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of ChangeLogRegister.
 */
public class DefaultChangeLogRegister implements ChangeLogRegister {

  private static final BasicFilter INCLUDE_INSERTS = new BasicFilter(true);

  private static final BasicFilter EXCLUDE_INSERTS = new BasicFilter(false);


  @Override
  public ChangeLogFilter getChangeFilter(Class<?> beanType) {

    ChangeLog changeLog = beanType.getAnnotation(ChangeLog.class);
    if (changeLog == null) {
      return null;
    }

    String[] updatesThatInclude = changeLog.updatesThatInclude();
    if (updatesThatInclude.length == 0) {
      return changeLog.excludeInserts() ? EXCLUDE_INSERTS : INCLUDE_INSERTS;
    }

    Set<String> updateProps = new HashSet<String>();
    for (int i = 0; i < updatesThatInclude.length; i++) {
      updateProps.add(updatesThatInclude[i]);
    }

    return new UpdateFilter(!changeLog.excludeInserts(), updateProps);
  }

  /**
   * Basic filter that only handles include inserts flag.
   */
  protected static class BasicFilter implements ChangeLogFilter {

    final boolean includeInserts;

    BasicFilter(boolean includeInserts) {
      this.includeInserts = includeInserts;
    }

    @Override
    public boolean includeInsert(BeanPersistRequest<?> insertRequest) {
      return includeInserts;
    }

    @Override
    public boolean includeUpdate(BeanPersistRequest<?> updateRequest) {
      return true;
    }

    @Override
    public boolean includeDelete(BeanPersistRequest<?> deleteRequest) {
      return true;
    }
  }

  /**
   * Filter that takes into account a set of properties to check for updates
   * as well as the include inserts flag.
   */
  protected static class UpdateFilter extends BasicFilter {

    final Set<String> updateProperties;

    UpdateFilter(boolean includeInserts, Set<String> updateProperties) {
      super(includeInserts);
      this.updateProperties = updateProperties;
    }

    @Override
    public boolean includeUpdate(BeanPersistRequest<?> updateRequest) {
      return updateRequest.hasDirtyProperty(updateProperties);
    }

  }
}
