package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

import java.sql.SQLException;

class BindableAssocTenant extends BindableAssocOne {

  BindableAssocTenant(BeanPropertyAssocOne<?> assocOne) {
    super(assocOne);
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    EntityBean assocBean = (EntityBean) assocOne.getValue(bean);
    importedId.bind(request, assocBean);
  }
}
