package com.avaje.ebeaninternal.server.deploy.parse;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.annotation.CacheTuning;
import com.avaje.ebean.annotation.EntityConcurrencyMode;
import com.avaje.ebean.annotation.NamedUpdate;
import com.avaje.ebean.annotation.NamedUpdates;
import com.avaje.ebean.annotation.UpdateMode;
import com.avaje.ebean.config.TableName;
import com.avaje.ebeaninternal.server.core.CacheOptions;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.deploy.CompoundUniqueContraint;
import com.avaje.ebeaninternal.server.deploy.DeployNamedQuery;
import com.avaje.ebeaninternal.server.deploy.DeployNamedUpdate;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Read the class level deployment annotations.
 */
public class AnnotationClass extends AnnotationParser {

  public AnnotationClass(DeployBeanInfo<?> info) {
    super(info);
  }

  /**
   * Read the class level deployment annotations.
   */
  public void parse() {
    read(descriptor.getBeanType());
    setTableName();
  }

  /**
   * Set the table name if it has not already been set.
   */
  private void setTableName() {

    if (descriptor.isBaseTableType()) {

      // default the TableName using NamingConvention.
      TableName tableName = namingConvention.getTableName(descriptor.getBeanType());

      descriptor.setBaseTable(tableName);
    }
  }

  private void read(Class<?> cls) {

    Entity entity = cls.getAnnotation(Entity.class);
    if (entity != null) {
      if (entity.name().equals("")) {
        descriptor.setName(cls.getSimpleName());
      } else {
        descriptor.setName(entity.name());
      }
    }

    Embeddable embeddable = cls.getAnnotation(Embeddable.class);
    if (embeddable != null) {
      descriptor.setEntityType(EntityType.EMBEDDED);
      descriptor.setName("Embeddable:" + cls.getSimpleName());
    }

    UniqueConstraint uc = cls.getAnnotation(UniqueConstraint.class);
    if (uc != null) {
      descriptor.addCompoundUniqueConstraint(new CompoundUniqueContraint(uc.columnNames()));
    }

    Table table = cls.getAnnotation(Table.class);
    if (table != null) {
      UniqueConstraint[] uniqueConstraints = table.uniqueConstraints();
      if (uniqueConstraints != null) {
        for (UniqueConstraint c : uniqueConstraints) {
          descriptor.addCompoundUniqueConstraint(new CompoundUniqueContraint(c.columnNames()));
        }
      }
    }

    UpdateMode updateMode = cls.getAnnotation(UpdateMode.class);
    if (updateMode != null) {
      descriptor.setUpdateChangesOnly(updateMode.updateChangesOnly());
    }

    NamedQueries namedQueries = cls.getAnnotation(NamedQueries.class);
    if (namedQueries != null) {
      readNamedQueries(namedQueries);
    }
    NamedQuery namedQuery = cls.getAnnotation(NamedQuery.class);
    if (namedQuery != null) {
      readNamedQuery(namedQuery);
    }

    NamedUpdates namedUpdates = cls.getAnnotation(NamedUpdates.class);
    if (namedUpdates != null) {
      readNamedUpdates(namedUpdates);
    }

    NamedUpdate namedUpdate = cls.getAnnotation(NamedUpdate.class);
    if (namedUpdate != null) {
      readNamedUpdate(namedUpdate);
    }

    CacheStrategy cacheStrategy = cls.getAnnotation(CacheStrategy.class);
    CacheTuning cacheTuning = cls.getAnnotation(CacheTuning.class);
    if (cacheStrategy != null || cacheTuning != null) {
      readCacheStrategy(cacheStrategy, cacheTuning);
    }

    EntityConcurrencyMode entityConcurrencyMode = cls.getAnnotation(EntityConcurrencyMode.class);
    if (entityConcurrencyMode != null) {
      descriptor.setConcurrencyMode(entityConcurrencyMode.value());
    }
  }

  private void readCacheStrategy(CacheStrategy cacheStrategy, CacheTuning cacheTuning) {

    CacheOptions cacheOptions = descriptor.getCacheOptions();
    if (cacheTuning != null) {
      cacheOptions.setMaxSecsToLive(cacheTuning.maxSecsToLive());
      cacheOptions.setMaxIdleSecs(cacheTuning.maxIdleSecs()); 
    }
    if (cacheStrategy != null) {
      cacheOptions.setUseCache(cacheStrategy.useBeanCache());
      cacheOptions.setReadOnly(cacheStrategy.readOnly());
      cacheOptions.setWarmingQuery(cacheStrategy.warmingQuery());
      if (cacheStrategy.naturalKey().length() > 0) {
        String propName = cacheStrategy.naturalKey().trim();
        DeployBeanProperty beanProperty = descriptor.getBeanProperty(propName);
        if (beanProperty != null) {
          beanProperty.setNaturalKey(true);
          cacheOptions.setNaturalKey(propName);
        }
      }
    }
  }

  private void readNamedQueries(NamedQueries namedQueries) {
    NamedQuery[] queries = namedQueries.value();
    for (int i = 0; i < queries.length; i++) {
      readNamedQuery(queries[i]);
    }
  }

  private void readNamedQuery(NamedQuery namedQuery) {
    DeployNamedQuery q = new DeployNamedQuery(namedQuery);
    descriptor.add(q);
  }

  private void readNamedUpdates(NamedUpdates updates) {
    NamedUpdate[] updateArray = updates.value();
    for (int i = 0; i < updateArray.length; i++) {
      readNamedUpdate(updateArray[i]);
    }
  }

  private void readNamedUpdate(NamedUpdate update) {
    DeployNamedUpdate upd = new DeployNamedUpdate(update);
    descriptor.add(upd);
  }

}
