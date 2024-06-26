package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Bindable for a EmbeddedId.
 */
final class BindableIdEmbedded implements BindableId {

  private final BeanPropertyAssocOne<?> embId;

  private final BeanProperty[] props;

  private final MatchedImportedProperty[] matches;

  BindableIdEmbedded(BeanPropertyAssocOne<?> embId, MatchedImportedProperty[] matches) {
    this.embId = embId;
    this.props = embId.properties();
    this.matches = matches;
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isConcatenated() {
    return true;
  }

  @Override
  public String getIdentityColumn() {
    // return null for concatenated keys
    return null;
  }

  /**
   * Does nothing for BindableId.
   */
  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    // do nothing (id not changing)
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    EntityBean idValue = (EntityBean) embId.getValue(bean);
    for (BeanProperty prop : props) {
      Object value = prop.getValue(idValue);
      request.bind(value, prop);
    }
    request.setIdValue(idValue);
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    if (matches != null) {
      // prefer the match dbColumns over the embedded property ones
      for (MatchedImportedProperty match : matches) {
        request.appendColumn(match.dbColumn());
      }
    } else {
      for (BeanProperty prop : props) {
        request.appendColumn(prop.dbColumn());
      }
    }
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    for (BeanProperty prop : props) {
      request.appendColumn(prop.dbColumnDefn());
    }
  }

  @Override
  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
    if (matches == null) {
      String m = "No matches for " + embId.fullName() + " the concatenated key columns where not found?"
        + " I expect that the concatenated key was null, and this bean does"
        + " not have ManyToOne assoc beans matching the primary key columns?";
      throw new PersistenceException(m);
    }
    EntityBean bean = persist.entityBean();
    // create the new id
    EntityBean newId = (EntityBean) embId.createEmbeddedId();
    // populate it from the assoc one id values...
    for (MatchedImportedProperty match : matches) {
      match.populate(bean, newId);
    }
    embId.setValueIntercept(bean, newId);
    return true;
  }

}
