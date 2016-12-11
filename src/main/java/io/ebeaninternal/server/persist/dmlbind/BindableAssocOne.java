package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.List;

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

  @Override
  public boolean isDraftOnly() {
    return assocOne.isDraftOnly();
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

    EntityBean assocBean = (EntityBean) assocOne.getValue(bean);
    Object boundValue = importedId.bind(request, assocBean);
    if (boundValue == null && assocBean != null) {
      // this is the scenario for a derived foreign key
      // which will require an additional update
      // register for post insert of assocBean
      // update of bean set importedId
      request.getPersistRequest().deferredRelationship(assocBean, importedId, bean);
    }
  }

}
