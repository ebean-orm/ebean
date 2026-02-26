package io.ebeaninternal.server.deploy.id;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanFkeyProperty;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.IntersectionBuilder;
import io.ebeaninternal.server.deploy.IntersectionRow;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import io.ebeaninternal.server.persist.dmlbind.BindableRequest;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;

/**
 * Imported Embedded id.
 */
public final class ImportedIdEmbedded implements ImportedId {

  private final BeanPropertyAssoc<?> owner;
  private final BeanPropertyAssocOne<?> foreignAssocOne;
  private final ImportedIdSimple[] imported;

  public ImportedIdEmbedded(BeanPropertyAssoc<?> owner, BeanPropertyAssocOne<?> foreignAssocOne, ImportedIdSimple[] imported) {
    this.owner = owner;
    this.foreignAssocOne = foreignAssocOne;
    this.imported = imported;
  }

  @Override
  public void addFkeys(String name) {
    BeanProperty[] embeddedProps = foreignAssocOne.properties();
    for (int i = 0; i < imported.length; i++) {
      String n = name + "." + foreignAssocOne.name() + "." + embeddedProps[i].name();
      owner.descriptor().add(new BeanFkeyProperty(n, imported[i].localDbColumn, foreignAssocOne.deployOrder()));
    }
  }

  @Override
  public boolean isScalar() {
    return false;
  }

  @Override
  public String getDbColumn() {
    return null;
  }

  @Override
  public void sqlAppend(DbSqlContext ctx) {
    for (ImportedIdSimple anImported : imported) {
      ctx.appendColumn(anImported.localDbColumn);
    }
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    boolean update = request.isUpdate();
    for (ImportedIdSimple anImported : imported) {
      if (anImported.isInclude(update)) {
        request.appendColumn(anImported.localDbColumn);
      }
    }
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    boolean update = request.isUpdate();
    for (ImportedIdSimple anImported : imported) {
      if (anImported.isInclude(update)) {
        anImported.dmlType(request);
      }
    }
  }

  @Override
  public String importedIdClause() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < imported.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(imported[i].localDbColumn).append(" = ?");
    }
    return sb.toString();
  }

  @Override
  public int bind(int position, SqlUpdate update, EntityBean bean) {
    int pos = position;
    EntityBean embedded = (EntityBean) foreignAssocOne.getValue(bean);
    for (ImportedIdSimple anImported : imported) {
      if (anImported.owner.isUpdateable()) {
        Object scalarValue = anImported.foreignProperty.getValue(embedded);
        if (scalarValue == null) {
          return -1; // could not bind
        }
        update.setParameter(pos++, scalarValue);
      }
    }
    return pos;
  }

  @Override
  public Object bind(BindableRequest request, EntityBean bean) throws SQLException {
    Object embeddedId = (bean == null) ? null : foreignAssocOne.getValue(bean);
    boolean update = request.isUpdate();
    if (embeddedId == null) {
      for (ImportedIdSimple anImported : imported) {
        if (anImported.isInclude(update)) {
          request.bind(null, anImported.foreignProperty);
        }
      }
      // return anything non-null to skip a derived relationship update
      return Object.class;
    } else {
      EntityBean embedded = (EntityBean) embeddedId;
      for (ImportedIdSimple anImported : imported) {
        if (anImported.isInclude(update)) {
          Object scalarValue = anImported.foreignProperty.getValue(embedded);
          request.bind(scalarValue, anImported.foreignProperty);
        }
      }
      return embedded;
    }
  }

  @Override
  public void buildImport(IntersectionRow row, EntityBean other) {
    EntityBean embeddedId = (EntityBean) foreignAssocOne.getValue(other);
    if (embeddedId == null) {
      throw new PersistenceException("Foreign Key value null?");
    }
    for (ImportedIdSimple anImported : imported) {
      Object scalarValue = anImported.foreignProperty.getValue(embeddedId);
      row.put(anImported.localDbColumn, scalarValue);
    }
  }

  @Override
  public void buildImport(IntersectionBuilder row) {
    for (ImportedIdSimple importedScalar : imported) {
      row.addColumn(importedScalar.localDbColumn);
    }
  }

  @Override
  public void bindImport(SqlUpdate sql, EntityBean other) {
    EntityBean embeddedId = (EntityBean) foreignAssocOne.getValue(other);
    if (embeddedId == null) {
      throw new PersistenceException("Foreign Key value null?");
    }
    for (ImportedIdSimple anImported : imported) {
      Object scalarValue = anImported.foreignProperty.getValue(embeddedId);
      sql.setParameter(scalarValue);
    }
  }

  /**
   * Not supported for embedded id.
   */
  @Override
  public BeanProperty findMatchImport(String matchDbColumn) {
    for (ImportedIdSimple anImported : imported) {
      BeanProperty p = anImported.findMatchImport(matchDbColumn);
      if (p != null) {
        return p;
      }
    }
    return null;
  }

}
