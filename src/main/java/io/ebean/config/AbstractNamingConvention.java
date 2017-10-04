package io.ebean.config;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.AnnotationUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Inheritance;
import javax.persistence.Table;

/**
 * Provides some base implementation for NamingConventions.
 *
 * @author emcgreal
 */
public abstract class AbstractNamingConvention implements NamingConvention {

  private static final Pattern TABLE_REPLACE = Pattern.compile("{table}", Pattern.LITERAL);

  private static final Pattern COLUMN_REPLACE = Pattern.compile("{column}", Pattern.LITERAL);

  /**
   * The Constant DEFAULT_SEQ_FORMAT.
   */
  public static final String DEFAULT_SEQ_FORMAT = "{table}_seq";

  /**
   * The catalog.
   */
  private String catalog;

  /**
   * The schema.
   */
  private String schema;

  /**
   * The sequence format.
   */
  private String sequenceFormat;

  /**
   * The database platform.
   */
  protected DatabasePlatform databasePlatform;

  /**
   * Used to trim off extra prefix for M2M.
   */
  protected int rhsPrefixLength = 3;

  protected boolean useForeignKeyPrefix;

  /**
   * Construct with a sequence format and useForeignKeyPrefix setting.
   */
  public AbstractNamingConvention(String sequenceFormat, boolean useForeignKeyPrefix) {
    this.sequenceFormat = sequenceFormat;
    this.useForeignKeyPrefix = useForeignKeyPrefix;
  }

  /**
   * Construct with a sequence format.
   *
   * @param sequenceFormat the sequence format
   */
  public AbstractNamingConvention(String sequenceFormat) {
    this.sequenceFormat = sequenceFormat;
    this.useForeignKeyPrefix = true;
  }

  /**
   * Construct with the default sequence format ("{table}_seq") and useForeignKeyPrefix as true.
   */
  public AbstractNamingConvention() {
    this(DEFAULT_SEQ_FORMAT);
  }

  @Override
  public void setDatabasePlatform(DatabasePlatform databasePlatform) {
    this.databasePlatform = databasePlatform;
  }

  @Override
  public String getSequenceName(String tableName, String pkColumn) {
    String s = TABLE_REPLACE.matcher(sequenceFormat).replaceAll(Matcher.quoteReplacement(tableName));
    if (pkColumn == null) {
      pkColumn = "";
    }
    return COLUMN_REPLACE.matcher(s).replaceAll(Matcher.quoteReplacement(pkColumn));
  }

  /**
   * Return the catalog.
   */
  public String getCatalog() {
    return catalog;
  }

  /**
   * Sets the catalog.
   */
  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  /**
   * Return the schema.
   */
  public String getSchema() {
    return schema;
  }

  /**
   * Sets the schema.
   */
  public void setSchema(String schema) {
    this.schema = schema;
  }

  /**
   * Returns the sequence format.
   */
  public String getSequenceFormat() {
    return sequenceFormat;
  }

  /**
   * Set the sequence format used to generate the sequence name.
   * <p>
   * The format should include "{table}". When generating the sequence name
   * {table} is replaced with the actual table name.
   * </p>
   *
   * @param sequenceFormat string containing "{table}" which is replaced with the actual
   *                       table name to generate the sequence name.
   */
  public void setSequenceFormat(String sequenceFormat) {
    this.sequenceFormat = sequenceFormat;
  }

  /**
   * Return true if a prefix should be used building a foreign key name.
   * <p>
   * This by default is true and this works well when the primary key column
   * names are simply "ID". In this case a prefix (such as "ORDER" and
   * "CUSTOMER" etc) is added to the foreign key column producing "ORDER_ID" and
   * "CUSTOMER_ID".
   * </p>
   * <p>
   * This should return false when your primary key columns are the same as the
   * foreign key columns. For example, when the primary key columns are
   * "ORDER_ID", "CUST_ID" etc ... and they are the same as the foreign key
   * column names.
   * </p>
   */
  @Override
  public boolean isUseForeignKeyPrefix() {
    return useForeignKeyPrefix;
  }

  /**
   * Set this to false when the primary key columns matching your foreign key
   * columns.
   */
  public void setUseForeignKeyPrefix(boolean useForeignKeyPrefix) {
    this.useForeignKeyPrefix = useForeignKeyPrefix;
  }

  /**
   * Return the tableName using the naming convention (rather than deployed
   * Table annotation).
   */
  protected abstract TableName getTableNameByConvention(Class<?> beanClass);

  /**
   * Returns the table name for a given entity bean.
   * <p>
   * This first checks for the @Table annotation and if not present uses the
   * naming convention to define the table name.
   * </p>
   *
   * @see #getTableNameFromAnnotation(Class)
   * @see #getTableNameByConvention(Class)
   */
  @Override
  public TableName getTableName(Class<?> beanClass) {
    while (true) {

      TableName tableName = getTableNameFromAnnotation(beanClass);
      if (tableName == null) {
        Class<?> supCls = beanClass.getSuperclass();
        if (hasInheritance(supCls)) {
          // get the table as per inherited class in case there
          // is not a table annotation in the inheritance hierarchy
          beanClass = supCls;
          continue;
        }

        tableName = getTableNameByConvention(beanClass);
      }

      // Use naming convention for catalog or schema,
      // if not set in the annotation.
      String catalog = tableName.getCatalog();
      if (isEmpty(catalog)) {
        catalog = getCatalog();
      }
      String schema = tableName.getSchema();
      if (isEmpty(schema)) {
        schema = getSchema();
      }
      return new TableName(catalog, schema, tableName.getName());
    }
  }

  /**
   * Return true if this class is part of entity inheritance.
   */
  protected boolean hasInheritance(Class<?> supCls) {
    return AnnotationUtil.findAnnotationRecursive(supCls, Inheritance.class) != null
        || AnnotationUtil.findAnnotation(supCls, DiscriminatorValue.class) != null;
  }


  @Override
  public TableName getM2MJoinTableName(TableName lhsTable, TableName rhsTable) {

    StringBuilder buffer = new StringBuilder();
    buffer.append(lhsTable.getName());
    buffer.append("_");

    String rhsTableName = rhsTable.getName();
    if (rhsTableName.indexOf('_') < rhsPrefixLength) {
      // trim off a xx_ prefix if there is one
      rhsTableName = rhsTableName.substring(rhsTableName.indexOf('_') + 1);
    }
    buffer.append(rhsTableName);

    int maxTableNameLength = databasePlatform.getMaxTableNameLength();

    // maxConstraintNameLength is used as the max table name length.
    if (buffer.length() > maxTableNameLength) {
      buffer.setLength(maxTableNameLength);
    }

    return new TableName(lhsTable.getCatalog(), lhsTable.getSchema(), buffer.toString());
  }

  /**
   * Gets the table name from annotation.
   */
  protected TableName getTableNameFromAnnotation(Class<?> beanClass) {

    final Table t = AnnotationUtil.findAnnotationRecursive(beanClass, Table.class);

    // Take the annotation if defined
    if (t != null && !isEmpty(t.name())) {
      // Note: empty catalog and schema are converted to null
      // Only need to convert quoted identifiers from annotations
      return new TableName(quoteIdentifiers(t.catalog()), quoteIdentifiers(t.schema()), quoteIdentifiers(t.name()));
    }

    // No annotation
    return null;
  }

  /**
   * Replace back ticks (if they are used) with database platform specific
   * quoted identifiers.
   */
  protected String quoteIdentifiers(String s) {
    return databasePlatform.convertQuotedIdentifiers(s);
  }

  /**
   * Checks string is null or empty .
   */
  protected boolean isEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

  /**
   * Load settings from properties.
   */
  @Override
  public void loadFromProperties(PropertiesWrapper properties) {

    useForeignKeyPrefix = properties.getBoolean("namingConvention.useForeignKeyPrefix", useForeignKeyPrefix);
    sequenceFormat = properties.get("namingConvention.sequenceFormat", sequenceFormat);
    schema = properties.get("namingConvention.schema", schema);
  }

}
