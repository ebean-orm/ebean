package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebean.annotation.Cache;
import com.avaje.ebean.annotation.DbComment;
import com.avaje.ebean.annotation.DocStore;
import com.avaje.ebean.annotation.Draftable;
import com.avaje.ebean.annotation.DraftableElement;
import com.avaje.ebean.annotation.History;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.ReadAudit;
import com.avaje.ebean.annotation.UpdateMode;
import com.avaje.ebean.annotation.View;
import com.avaje.ebean.config.TableName;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.deploy.IndexDefinition;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Read the class level deployment annotations.
 */
public class AnnotationClass extends AnnotationParser {

  private static final Logger logger = LoggerFactory.getLogger(AnnotationClass.class);

  private final String asOfViewSuffix;

  private final String versionsBetweenSuffix;

  private final boolean disableL2Cache;

  /**
   * Create for normal early parse of class level annotations.
   */
  public AnnotationClass(DeployBeanInfo<?> info, boolean validationAnnotations, String asOfViewSuffix, String versionsBetweenSuffix, boolean disableL2Cache) {
    super(info, validationAnnotations);
    this.asOfViewSuffix = asOfViewSuffix;
    this.versionsBetweenSuffix = versionsBetweenSuffix;
    this.disableL2Cache = disableL2Cache;
  }

  /**
   * Create to parse AttributeOverride annotations which is run last
   * after all the properties/fields have been parsed fully.
   */
  public AnnotationClass(DeployBeanInfo<?> info) {
    super(info, false);
    this.asOfViewSuffix = null;
    this.versionsBetweenSuffix = null;
    this.disableL2Cache = false;
  }

  /**
   * Parse any AttributeOverride set on the class.
   */
  public void parseAttributeOverride() {

    Class<?> cls = descriptor.getBeanType();
    AttributeOverride override = cls.getAnnotation(AttributeOverride.class);
    if (override != null) {
      String propertyName = override.name();
      Column column = override.column();
      DeployBeanProperty beanProperty = descriptor.getBeanProperty(propertyName);
      if (beanProperty == null) {
        logger.error("AttributeOverride property [" + propertyName + "] not found on " + descriptor.getFullName());
      } else {
        readColumn(column, beanProperty);
      }
    }
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

      descriptor.setBaseTable(tableName, asOfViewSuffix, versionsBetweenSuffix);
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

    Index index = cls.getAnnotation(Index.class);
    if (index != null) {
      descriptor.addIndex(new IndexDefinition(index.columnNames(), index.name(), index.unique()));
    }

    UniqueConstraint uc = cls.getAnnotation(UniqueConstraint.class);
    if (uc != null) {
      descriptor.addIndex(new IndexDefinition(uc.columnNames()));
    }

    View view = cls.getAnnotation(View.class);
    if (view != null) {
      descriptor.setView(view.name(), view.dependentTables());
    }
    Table table = cls.getAnnotation(Table.class);
    if (table != null) {
      UniqueConstraint[] uniqueConstraints = table.uniqueConstraints();
      for (UniqueConstraint c : uniqueConstraints) {
        descriptor.addIndex(new IndexDefinition(c.columnNames()));
      }
    }

    Draftable draftable = cls.getAnnotation(Draftable.class);
    if (draftable != null) {
      descriptor.setDraftable();
    }

    DraftableElement draftableElement = cls.getAnnotation(DraftableElement.class);
    if (draftableElement != null) {
      descriptor.setDraftableElement();
    }

    ReadAudit readAudit = cls.getAnnotation(ReadAudit.class);
    if (readAudit != null) {
      descriptor.setReadAuditing();
    }

    History history = cls.getAnnotation(History.class);
    if (history != null) {
      descriptor.setHistorySupport();
    }

    DbComment comment = cls.getAnnotation(DbComment.class);
    if (comment != null) {
      descriptor.setDbComment(comment.value());
    }

    DocStore docStore = cls.getAnnotation(DocStore.class);
    if (docStore != null) {
      descriptor.readDocStore(docStore);
    }

    UpdateMode updateMode = cls.getAnnotation(UpdateMode.class);
    if (updateMode != null) {
      descriptor.setUpdateChangesOnly(updateMode.updateChangesOnly());
    }

    Cache cache = cls.getAnnotation(Cache.class);
    if (cache != null && !disableL2Cache) {
      descriptor.setCache(cache);
    }

    NamedQueries namedQueries = cls.getAnnotation(NamedQueries.class);
    if (namedQueries != null) {
      for (NamedQuery namedQuery : namedQueries.value()) {
        descriptor.addNamedQuery(namedQuery.name(), namedQuery.query());
      }
    }

    NamedQuery namedQuery = cls.getAnnotation(NamedQuery.class);
    if (namedQuery != null) {
      descriptor.addNamedQuery(namedQuery.name(), namedQuery.query());
    }
  }

}
