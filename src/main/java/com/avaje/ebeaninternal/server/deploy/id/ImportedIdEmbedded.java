package com.avaje.ebeaninternal.server.deploy.id;

import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.*;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableRequest;

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

  public void addFkeys(String name) {

    BeanProperty[] embeddedProps = foreignAssocOne.getProperties();

    for (int i = 0; i < imported.length; i++) {
      String n = name + "." + foreignAssocOne.getName() + "." + embeddedProps[i].getName();
      BeanFkeyProperty fkey = new BeanFkeyProperty(n, imported[i].localDbColumn, foreignAssocOne.getDeployOrder());
      owner.getBeanDescriptor().add(fkey);
    }
  }

  public boolean isScalar() {
    return false;
  }

  public String getDbColumn() {
    return null;
  }

  public void sqlAppend(DbSqlContext ctx) {
    for (int i = 0; i < imported.length; i++) {
      ctx.appendColumn(imported[i].localDbColumn);
    }
  }

  public void dmlAppend(GenerateDmlRequest request) {

    for (int i = 0; i < imported.length; i++) {
      request.appendColumn(imported[i].localDbColumn);
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
    for (int i = 0; i < imported.length; i++) {
      if (imported[i].owner.isUpdateable()) {
        Object scalarValue = imported[i].foreignProperty.getValue(embedded);
        update.setParameter(pos++, scalarValue);
      }
    }
    return pos;
  }

  public Object bind(BindableRequest request, EntityBean bean) throws SQLException {

    Object embeddedId = null;

    if (bean != null) {
      embeddedId = foreignAssocOne.getValue(bean);
    }

    if (embeddedId == null) {
      for (int i = 0; i < imported.length; i++) {
        if (imported[i].owner.isUpdateable()) {
          request.bind(null, imported[i].foreignProperty);
        }
      }

    } else {
      EntityBean embedded = (EntityBean) embeddedId;
      for (int i = 0; i < imported.length; i++) {
        if (imported[i].owner.isUpdateable()) {
          Object scalarValue = imported[i].foreignProperty.getValue(embedded);
          request.bind(scalarValue, imported[i].foreignProperty);
        }
      }
    }
    // hmmm, not worrying about this just yet
    return null;
  }

  public void buildImport(IntersectionRow row, EntityBean other) {

    EntityBean embeddedId = (EntityBean) foreignAssocOne.getValue(other);
    if (embeddedId == null) {
      String msg = "Foreign Key value null?";
      throw new PersistenceException(msg);
    }

    for (int i = 0; i < imported.length; i++) {
      Object scalarValue = imported[i].foreignProperty.getValue(embeddedId);
      row.put(imported[i].localDbColumn, scalarValue);
    }

  }

  /**
   * Not supported for embedded id.
   */
  public BeanProperty findMatchImport(String matchDbColumn) {

    for (int i = 0; i < imported.length; i++) {
      BeanProperty p = imported[i].findMatchImport(matchDbColumn);
      if (p != null) {
        return p;
      }
    }
    return null;
  }

}
