package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;

import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for generated ManyToOne - likely 'who modified'.
 */
class BindableAssocOneGeneratedUpdate extends BindableAssocOne {

  private final GeneratedProperty generatedProperty;

  BindableAssocOneGeneratedUpdate(BeanPropertyAssocOne<?> assocOne) {
    super(assocOne);
    this.generatedProperty = assocOne.getGeneratedProperty();
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (generatedProperty.includeInAllUpdates() || request.isAddToUpdate(assocOne)) {
      list.add(this);
    }
  }

  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    Object objectValue = generatedProperty.getUpdateValue(assocOne, bean, request.now());
    EntityBean generatedValue = castToEntityBean(objectValue);
    assocOne.setValueChanged(bean, generatedValue);
    registerDeferred(request, bean, generatedValue);
  }

}
