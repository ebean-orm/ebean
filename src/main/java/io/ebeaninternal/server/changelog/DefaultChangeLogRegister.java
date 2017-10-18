package io.ebeaninternal.server.changelog;

import io.ebean.annotation.ChangeLog;
import io.ebean.annotation.ChangeLogInsertMode;
import io.ebean.event.BeanPersistRequest;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.util.AnnotationUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of ChangeLogRegister.
 */
public class DefaultChangeLogRegister implements ChangeLogRegister {

  private static final BasicFilter INCLUDE_INSERTS = new BasicFilter(true);

  private static final BasicFilter EXCLUDE_INSERTS = new BasicFilter(false);

  private final boolean defaultInsertsInclude;

  /**
   * Create with the default insertsIncluded from ServerConfig.
   */
  public DefaultChangeLogRegister(boolean defaultInsertsInclude) {
    this.defaultInsertsInclude = defaultInsertsInclude;
  }

  @Override
  public ChangeLogFilter getChangeFilter(Class<?> beanType) {

    ChangeLog changeLog = getChangeLog(beanType);
    if (changeLog == null) {
      return null;
    }

    String[] updatesThatInclude = changeLog.updatesThatInclude();
    if (updatesThatInclude.length == 0) {
      return insertModeInclude(changeLog.inserts()) ? INCLUDE_INSERTS : EXCLUDE_INSERTS;
    }

    Set<String> updateProps = new HashSet<>();
    Collections.addAll(updateProps, updatesThatInclude);

    return new UpdateFilter(insertModeInclude(changeLog.inserts()), updateProps);
  }

  /**
   * Find and return the ChangeLog annotation in the inheritance hierarchy.
   */
  private ChangeLog getChangeLog(Class<?> beanType) {
    return AnnotationUtil.findAnnotationRecursive(beanType, ChangeLog.class);
  }

  /**
   * Return true if inserts should be included in the change log.
   */
  private boolean insertModeInclude(ChangeLogInsertMode inserts) {
    if (inserts == ChangeLogInsertMode.DEFAULT) {
      // return the default as per the ServerConfig
      return defaultInsertsInclude;
    }
    return ChangeLogInsertMode.INCLUDE == inserts;
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
