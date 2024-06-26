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

  final BeanPropertyAssocOne<?> assocOne;

  final ImportedId importedId;

  BindableAssocOne(BeanPropertyAssocOne<?> assocOne) {
    this.assocOne = assocOne;
    this.importedId = assocOne.importedId();
  }

  @Override
  public final boolean isDraftOnly() {
    return assocOne.isDraftOnly();
  }

  @Override
  public final void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (request.isAddToUpdate(assocOne)) {
      list.add(this);
    }
  }

  @Override
  public final void dmlAppend(GenerateDmlRequest request) {
    importedId.dmlAppend(request);
  }

  @Override
  public void dmlType(GenerateDmlRequest request) {
    importedId.dmlType(request);
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    EntityBean assocBean = (EntityBean) assocOne.getValue(bean);
    registerDeferred(request, bean, assocBean);
  }

  /**
   * Bind and register a deferred relationship value.
   */
  private void registerDeferred(BindableRequest request, EntityBean bean, EntityBean assocBean) throws SQLException {
    Object boundValue = importedId.bind(request, assocBean);
    if (boundValue == null && assocBean != null) {
      // this is the scenario for a derived foreign key
      // which will require an additional update
      // register for post insert of assocBean
      // update of bean set importedId
      request.persistRequest().deferredRelationship(assocBean, importedId, bean);
    }
  }

}
