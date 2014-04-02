package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a concatenated id that is not embedded.
 */
public final class BindableIdMap implements BindableId {

  private final BeanProperty[] uids;

  private final MatchedImportedProperty[] matches;

  public BindableIdMap(BeanProperty[] uids, BeanDescriptor<?> desc) {
    this.uids = uids;
    matches = MatchedImportedProperty.build(uids, desc);
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
    return Arrays.toString(uids);
  }

  /**
   * Does nothing for BindableId.
   */
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    // do nothing (id not changing)
  }

  public void dmlAppend(GenerateDmlRequest request) {
    for (int i = 0; i < uids.length; i++) {
      request.appendColumn(uids[i].getDbColumn());
    }
  }

  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    LinkedHashMap<String, Object> mapId = new LinkedHashMap<String, Object>();
    for (int i = 0; i < uids.length; i++) {
      Object value = uids[i].getValue(bean);

      request.bind(value, uids[i], uids[i].getName());

      // putting logicalType into map rather than
      // the dbType (which may have been converted).
      mapId.put(uids[i].getName(), value);
    }
    request.setIdValue(mapId);
  }

  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {

    if (matches == null) {
      String m = "Matches for the concatinated key columns where not found?"
          + " I expect that the concatinated key was null, and this bean does"
          + " not have ManyToOne assoc beans matching the primary key columns?";
      throw new PersistenceException(m);
    }

    EntityBean bean = persist.getEntityBean();

    // populate it from the assoc one id values...
    for (int i = 0; i < matches.length; i++) {
      matches[i].populate(bean, bean);
    }

    return true;
  }

}
