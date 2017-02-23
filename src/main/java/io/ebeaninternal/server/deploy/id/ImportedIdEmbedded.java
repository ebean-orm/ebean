package io.ebeaninternal.server.deploy.id;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanFkeyProperty;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.IntersectionRow;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import io.ebeaninternal.server.persist.dmlbind.BindableRequest;

import javax.persistence.PersistenceException;
import java.sql.SQLException;

/**
 * Imported Embedded id.
 */
public class ImportedIdEmbedded implements ImportedId {

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

    BeanProperty[] embeddedProps = foreignAssocOne.getProperties();

    for (int i = 0; i < imported.length; i++) {
      String n = name + "." + foreignAssocOne.getName() + "." + embeddedProps[i].getName();
      BeanFkeyProperty fkey = new BeanFkeyProperty(n, imported[i].localDbColumn, foreignAssocOne.getDeployOrder());
      owner.getBeanDescriptor().add(fkey);
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

    for (ImportedIdSimple anImported : imported) {
      request.appendColumn(anImported.localDbColumn);
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
        update.setParameter(pos++, scalarValue);
      }
    }
    return pos;
  }

  @Override
  public Object bind(BindableRequest request, EntityBean bean) throws SQLException {

    Object embeddedId = null;

    if (bean != null) {
      embeddedId = foreignAssocOne.getValue(bean);
    }

    if (embeddedId == null) {
      for (ImportedIdSimple anImported : imported) {
        if (anImported.owner.isUpdateable()) {
          request.bind(null, anImported.foreignProperty);
        }
      }

    } else {
      EntityBean embedded = (EntityBean) embeddedId;
      for (ImportedIdSimple anImported : imported) {
        if (anImported.owner.isUpdateable()) {
          Object scalarValue = anImported.foreignProperty.getValue(embedded);
          request.bind(scalarValue, anImported.foreignProperty);
        }
      }
    }
    // hmmm, not worrying about this just yet
    return null;
  }

  @Override
  public void buildImport(IntersectionRow row, EntityBean other) {

    EntityBean embeddedId = (EntityBean) foreignAssocOne.getValue(other);
    if (embeddedId == null) {
      String msg = "Foreign Key value null?";
      throw new PersistenceException(msg);
    }

    for (ImportedIdSimple anImported : imported) {
      Object scalarValue = anImported.foreignProperty.getValue(embeddedId);
      row.put(anImported.localDbColumn, scalarValue);
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
