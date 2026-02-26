package io.ebeaninternal.server.deploy.id;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.deploy.BeanFkeyProperty;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.IntersectionBuilder;
import io.ebeaninternal.server.deploy.IntersectionRow;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import io.ebeaninternal.server.persist.dmlbind.BindableRequest;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Single scalar imported id.
 */
public final class ImportedIdSimple implements ImportedId, Comparable<ImportedIdSimple> {

  /**
   * Helper class to sort ImportedIdSimple.
   */
  private final static class EntryComparator implements Comparator<ImportedIdSimple> {
    @Override
    public int compare(ImportedIdSimple o1, ImportedIdSimple o2) {
      return o1.compareTo(o2);
    }
  }

  private static final EntryComparator COMPARATOR = new EntryComparator();

  final BeanPropertyAssoc<?> owner;
  final String localDbColumn;
  private final String localColumnDefn;
  private final String localSqlFormula;
  final BeanProperty foreignProperty;
  private final int position;
  private final boolean insertable;
  private final boolean updateable;

  public ImportedIdSimple(BeanPropertyAssoc<?> owner, String localDbColumn, String localSqlFormula, BeanProperty foreignProperty, int position,
                          boolean insertable, boolean updateable) {
    this.owner = owner;
    this.localDbColumn = InternString.intern(localDbColumn);
    this.localSqlFormula = InternString.intern(localSqlFormula);
    this.foreignProperty = foreignProperty;
    this.localColumnDefn = foreignProperty.dbColumnDefn();
    this.position = position;
    this.insertable = insertable;
    this.updateable = updateable;
  }

  public ImportedIdSimple(BeanPropertyAssoc<?> owner, String localDbColumn, String localSqlFormula, BeanProperty foreignProperty, int position) {
    this(owner, localDbColumn, localSqlFormula, foreignProperty, position, true, true);
  }

  /**
   * Return the list as an array sorted into the same order as the Bean Properties.
   */
  public static ImportedIdSimple[] sort(List<ImportedIdSimple> list) {
    ImportedIdSimple[] importedIds = list.toArray(new ImportedIdSimple[0]);
    // sort into the same order as the BeanProperties
    Arrays.sort(importedIds, COMPARATOR);
    return importedIds;
  }

  /**
   * Return true if it should be included in the update (or insert).
   */
  boolean isInclude(boolean update) {
    return (update) ? updateable : insertable;
  }

  @Override
  public boolean equals(Object obj) {
    // remove FindBugs warning
    return obj == this;
  }

  @Override
  public int compareTo(ImportedIdSimple other) {
    return Integer.compare(position, other.position);
  }

  @Override
  public void addFkeys(String name) {
    owner.descriptor().add(new BeanFkeyProperty(name + "." + foreignProperty.name(), localDbColumn, owner.deployOrder()));
  }

  @Override
  public boolean isScalar() {
    return true;
  }

  @Override
  public String getDbColumn() {
    return localDbColumn;
  }

  private Object getIdValue(EntityBean bean) {
    return foreignProperty.getValue(bean);
  }

  @Override
  public void buildImport(IntersectionBuilder row) {
    row.addColumn(localDbColumn);
  }

  @Override
  public void bindImport(SqlUpdate sql, EntityBean other) {
    Object value = getIdValue(other);
    if (value == null) {
      throw new PersistenceException("Foreign Key value null?");
    }
    sql.setParameter(value);
  }

  @Override
  public void buildImport(IntersectionRow row, EntityBean other) {
    Object value = getIdValue(other);
    if (value == null) {
      String msg = "Foreign Key value null?";
      throw new PersistenceException(msg);
    }
    row.put(localDbColumn, value);
  }

  @Override
  public void sqlAppend(DbSqlContext ctx) {
    if (localSqlFormula != null) {
      ctx.appendFormulaSelect(localSqlFormula);
    } else {
      ctx.appendColumn(localDbColumn);
    }
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(localDbColumn);
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    request.appendColumnDefn(localDbColumn, localColumnDefn);
  }

  @Override
  public String importedIdClause() {
    return localDbColumn + " = ?";
  }

  @Override
  public int bind(int position, SqlUpdate update, EntityBean bean) {
    Object value = getIdValue(bean);
    if (value == null) {
      return -1; // could not bind
    }
    update.setParameter(position, value);
    return ++position;
  }

  @Override
  public Object bind(BindableRequest request, EntityBean bean) throws SQLException {
    Object value = null;
    if (bean != null) {
      value = getIdValue(bean);
    }
    request.bind(value, foreignProperty);
    return value;
  }

  @Override
  public BeanProperty findMatchImport(String matchDbColumn) {
    if (matchDbColumn.equals(localDbColumn)) {
      return foreignProperty;
    }
    return null;
  }
}
