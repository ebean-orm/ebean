package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a EmbeddedId.
 */
public final class BindableIdEmbedded implements BindableId {

  private final BeanPropertyAssocOne<?> embId;

  private final BeanProperty[] props;

  private final MatchedImportedProperty[] matches;

  public BindableIdEmbedded(BeanPropertyAssocOne<?> embId, BeanDescriptor<?> desc) {
    this.embId = embId;
    this.props = embId.getTargetDescriptor().propertiesBaseScalar();
    matches = MatchedImportedProperty.build(props, desc);
  }

  public boolean isEmpty() {
    return false;
  }

  public boolean isConcatenated() {
    return true;
  }

  public String getIdentityColumn() {
    // return null for concatenated keys
    return null;
  }

  @Override
  public String toString() {
    return embId + " props:" + Arrays.toString(props);
  }

  /**
   * Does nothing for BindableId.
   */
  public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
    // do nothing (id not changing)
  }

  public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
    dmlBind(request, checkIncludes, bean, true);
  }

  public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
    dmlBind(request, checkIncludes, bean, false);
  }

  private void dmlBind(BindableRequest bindRequest, boolean checkIncludes, Object bean, boolean bindNull) throws SQLException {

    if (checkIncludes && !bindRequest.isIncluded(embId)) {
      return;
    }

    Object idValue = embId.getValue(bean);

    for (int i = 0; i < props.length; i++) {

      Object value = props[i].getValue(idValue);
      bindRequest.bind(value, props[i], props[i].getDbColumn(), bindNull);
    }

    bindRequest.setIdValue(idValue);
  }

  /**
   * Id values are never null in where clause.
   */
  public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
    if (checkIncludes && !request.isIncluded(embId)) {
      return;
    }
    dmlAppend(request, false);
  }

  public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
    dmlAppend(request, checkIncludes);
  }

  public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
    if (checkIncludes && !request.isIncluded(embId)) {
      return;
    }
    for (int i = 0; i < props.length; i++) {
      request.appendColumn(props[i].getDbColumn());
    }
  }

  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {

    if (matches == null) {
      String m = "Matches for the concatinated key columns where not found?"
          + " I expect that the concatinated key was null, and this bean does"
          + " not have ManyToOne assoc beans matching the primary key columns?";
      throw new PersistenceException(m);
    }

    Object bean = persist.getBean();

    // create the new id
    Object newId = embId.createEmbeddedId();

    // populate it from the assoc one id values...
    for (int i = 0; i < matches.length; i++) {
      matches[i].populate(bean, newId);
    }

    // support PropertyChangeSupport
    embId.setValueIntercept(bean, newId);
    return true;
  }

}
