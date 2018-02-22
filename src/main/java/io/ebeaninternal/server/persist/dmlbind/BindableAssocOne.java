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
class BindableAssocOne implements Bindable {

  protected final BeanPropertyAssocOne<?> assocOne;

  protected final ImportedId importedId;

  BindableAssocOne(BeanPropertyAssocOne<?> assocOne) {
    this.assocOne = assocOne;
    this.importedId = assocOne.getImportedId();
  }

  @Override
  public String toString() {
    return "BindableAssocOne " + assocOne;
  }

  @Override
  public boolean isDraftOnly() {
    return assocOne.isDraftOnly();
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (request.isAddToUpdate(assocOne)) {
      list.add(this);
    }
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    importedId.dmlAppend(request);
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    EntityBean assocBean = (EntityBean) assocOne.getValue(bean);
    registerDeferred(request, bean, assocBean);
  }

  /**
   * Bind and register a deferred relationship value.
   */
  void registerDeferred(BindableRequest request, EntityBean bean, EntityBean assocBean) throws SQLException {
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
