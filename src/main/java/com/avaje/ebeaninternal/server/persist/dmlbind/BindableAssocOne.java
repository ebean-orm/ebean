package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for an ManyToOne or OneToOne associated bean.
 */
public class BindableAssocOne implements Bindable {

  private final BeanPropertyAssocOne<?> assocOne;

  private final ImportedId importedId;

  public BindableAssocOne(BeanPropertyAssocOne<?> assocOne) {
    this.assocOne = assocOne;
    this.importedId = assocOne.getImportedId();
  }

  public String toString() {
    return "BindableAssocOne " + assocOne;
  }

  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (request.isAddToUpdate(assocOne)) {
      list.add(this);
    }
  }

  public void dmlAppend(GenerateDmlRequest request) {
    importedId.dmlAppend(request);
  }

  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    EntityBean assocBean = (EntityBean)assocOne.getValue(bean);
    Object boundValue = importedId.bind(request, assocBean);
    if (boundValue == null && assocBean != null) {
      // this is the scenario for a derived foreign key
      // which will require an additional update
      // register for post insert of assocBean
      // update of bean set ... importedId.getLogicalName();
      // value of assocBean.getId
      DerivedRelationshipData d = new DerivedRelationshipData(assocBean, assocOne.getName(), bean);
      request.registerDerivedRelationship(d);
    }
  }

}
