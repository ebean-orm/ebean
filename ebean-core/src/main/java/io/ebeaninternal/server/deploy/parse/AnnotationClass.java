package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Index;
import io.ebean.annotation.*;
import io.ebean.config.TableName;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.IndexDefinition;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.PartitionMeta;
import io.ebeaninternal.server.deploy.TablespaceMeta;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.*;

import static io.ebean.util.AnnotationUtil.typeGet;
import static java.lang.System.Logger.Level.ERROR;

/**
 * Read the class level deployment annotations.
 */
final class AnnotationClass extends AnnotationParser {

  private final String asOfViewSuffix;
  private final String versionsBetweenSuffix;
  private final boolean disableL2Cache;

  /**
   * Create to parse AttributeOverride annotations which is run last
   * after all the properties/fields have been parsed fully.
   */
  AnnotationClass(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig) {
    super(info, readConfig);
    this.asOfViewSuffix = readConfig.getAsOfViewSuffix();
    this.versionsBetweenSuffix = readConfig.getVersionsBetweenSuffix();
    this.disableL2Cache = readConfig.isDisableL2Cache();
  }

  /**
   * Parse any AttributeOverride set on the class.
   */
  void parseAttributeOverride() {
    Class<?> cls = descriptor.getBeanType();
    AttributeOverride override = typeGet(cls, AttributeOverride.class);
    if (override != null) {
      String propertyName = override.name();
      Column column = override.column();
      DeployBeanProperty beanProperty = descriptor.getBeanProperty(propertyName);
      if (beanProperty == null) {
        CoreLog.log.log(ERROR, "AttributeOverride property [" + propertyName + "] not found on " + descriptor.getFullName());
      } else {
        readColumn(column, beanProperty);
      }
    }
  }

  /**
   * Read the class level deployment annotations.
   */
  @Override
  public void parse() {
    read(descriptor.getBeanType());
    setTableName();
  }

  /**
   * Set the table name if it has not already been set.
   */
  private void setTableName() {
    if (descriptor.isBaseTableType()) {
      Class<?> beanType = descriptor.getBeanType();
      InheritInfo inheritInfo = descriptor.getInheritInfo();
      if (inheritInfo != null) {
        beanType = inheritInfo.getRoot().getType();
      }
      // default the TableName using NamingConvention.
      TableName tableName = namingConvention.getTableName(beanType);
      descriptor.setBaseTable(tableName, asOfViewSuffix, versionsBetweenSuffix);
    }
  }

  private void read(Class<?> cls) {
    // maybe doc store only so check for this before @Entity
    DocStore docStore = typeGet(cls, DocStore.class);
    if (docStore != null) {
      descriptor.readDocStore(docStore);
      descriptor.setEntityType(EntityType.DOC);
      descriptor.setName(cls.getSimpleName());
    }
    Entity entity = typeGet(cls, Entity.class);
    if (entity != null) {
      descriptor.setEntityType(EntityType.ORM);
      if (entity.name().isEmpty()) {
        descriptor.setName(cls.getSimpleName());
      } else {
        descriptor.setName(entity.name());
      }
    }
    Identity identity = typeGet(cls, Identity.class);
    if (identity != null) {
      descriptor.setIdentityMode(identity);
    }
    IdClass idClass = typeGet(cls, IdClass.class);
    if (idClass != null) {
      descriptor.setIdClass(idClass.value());
    }

    Embeddable embeddable = typeGet(cls, Embeddable.class);
    if (embeddable != null) {
      descriptor.setEntityType(EntityType.EMBEDDED);
      descriptor.setName("Embeddable:" + cls.getSimpleName());
    }
    for (Index index : annotationClassIndexes(cls)) {
      descriptor.addIndex(new IndexDefinition(convertColumnNames(index.columnNames()), index.name(),
        index.unique(), index.platforms(), index.concurrent(), index.definition()));
    }

    UniqueConstraint uc = typeGet(cls, UniqueConstraint.class);
    if (uc != null) {
      descriptor.addIndex(new IndexDefinition(uc.name(), convertColumnNames(uc.columnNames())));
    }
    View view = typeGet(cls, View.class);
    if (view != null) {
      descriptor.setView(view.name(), view.dependentTables());
    }
    Table table = typeGet(cls, Table.class);
    if (table != null) {
      UniqueConstraint[] uniqueConstraints = table.uniqueConstraints();
      for (UniqueConstraint c : uniqueConstraints) {
        descriptor.addIndex(new IndexDefinition(c.name(), convertColumnNames(c.columnNames())));
      }
      for (javax.persistence.Index index : table.indexes()) {
        final String[] cols = index.columnList().split(",");
        descriptor.addIndex(new IndexDefinition(index.name(), convertColumnNames(cols), index.unique()));
      }
    }
    StorageEngine storage = typeGet(cls, StorageEngine.class);
    if (storage != null) {
      descriptor.setStorageEngine(storage.value());
    }
    DbPartition partition = typeGet(cls, DbPartition.class);
    if (partition != null) {
      descriptor.setPartitionMeta(new PartitionMeta(partition.mode(), partition.property()));
    }
    Tablespace tablespace = typeGet(cls, Tablespace.class);
    if (tablespace != null) {
      String indexTs = tablespace.index();
      if("".equals(indexTs)) {
        indexTs = tablespace.value();
      }
      String lobTs = tablespace.lob();
      if("".equals(lobTs)) {
        lobTs = tablespace.value();
      }
      descriptor.setTablespaceMeta(new TablespaceMeta(tablespace.value(), indexTs, lobTs));
    }
    Draftable draftable = typeGet(cls, Draftable.class);
    if (draftable != null) {
      descriptor.setDraftable();
    }
    DraftableElement draftableElement = typeGet(cls, DraftableElement.class);
    if (draftableElement != null) {
      descriptor.setDraftableElement();
    }
    ReadAudit readAudit = typeGet(cls, ReadAudit.class);
    if (readAudit != null) {
      descriptor.setReadAuditing();
    }
    History history = typeGet(cls, History.class);
    if (history != null) {
      descriptor.setHistorySupport();
    }
    DbComment comment = typeGet(cls, DbComment.class);
    if (comment != null) {
      descriptor.setDbComment(comment.value());
    }
    if (!disableL2Cache) {
      Cache cache = typeGet(cls, Cache.class);
      if (cache != null) {
        descriptor.setCache(cache);
      } else {
        InvalidateQueryCache invalidateQueryCache = typeGet(cls, InvalidateQueryCache.class);
        if (invalidateQueryCache != null) {
          descriptor.setInvalidateQueryCache(invalidateQueryCache.region());
        }
      }
    }
    for (NamedQuery namedQuery : annotationClassNamedQuery(cls)) {
      descriptor.addNamedQuery(namedQuery.name(), namedQuery.query());
    }
  }

}
