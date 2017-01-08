package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.persist.dml.DmlMode;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for an ManyToOne or OneToOne associated bean.
 */
public class BindableAssocOne implements Bindable {

  private final BeanPropertyAssocOne<?> assocOne;

  private final ImportedId importedId;
  private final DmlMode dmlMode;
  private final GeneratedProperty generatedProperty;

  public BindableAssocOne(BeanPropertyAssocOne<?> assocOne, DmlMode mode) {
    this.assocOne = assocOne;
    this.importedId = assocOne.getImportedId();
    this.dmlMode = mode;
    this.generatedProperty = assocOne.getGeneratedProperty();
  }

  public String toString() {
    return "BindableAssocOne " + assocOne;
  }

  @Override
  public boolean isDraftOnly() {
    return assocOne.isDraftOnly();
  }

  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (generatedProperty != null && generatedProperty.includeInAllUpdates()) {
      list.add(this);
    } else if (request.isAddToUpdate(assocOne)) {
      list.add(this);
    }
  }

  public void dmlAppend(GenerateDmlRequest request) {
    importedId.dmlAppend(request);
  }

  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    if (generatedProperty != null) {
      bindGeneratedProperty(request, bean);
    } else {
      bindNotGeneratedProperty(request, bean);
    }
  }

  private void bindGeneratedProperty(BindableRequest request, EntityBean bean) throws SQLException {
    if (DmlMode.INSERT.equals(dmlMode) && generatedProperty.includeInInsert()) {
      bindGeneratedInsert(request, bean);
    } else if (DmlMode.UPDATE.equals(dmlMode) && generatedProperty.includeInAllUpdates()) {
      bindGeneratedUpdate(request, bean);
    }
  }

  private void bindGeneratedUpdate(BindableRequest request, EntityBean bean) throws SQLException {
    Object objectValue = generatedProperty.getUpdateValue(assocOne, bean, request.now());
    EntityBean generatedValue = castToEntityBean(objectValue);
    assocOne.setValueChanged(bean, generatedValue);
    registerDeferred(request, bean, generatedValue);
  }

  private void bindGeneratedInsert(BindableRequest request, EntityBean bean) throws SQLException {
    Object objectValue = generatedProperty.getInsertValue(assocOne, bean, request.now());
    EntityBean generatedValue = castToEntityBean(objectValue);
    assocOne.setValue(bean, generatedValue);
    registerDeferred(request, bean, generatedValue);
  }

  private void bindNotGeneratedProperty(BindableRequest request, EntityBean bean) throws SQLException {
    EntityBean assocBean = (EntityBean) assocOne.getValue(bean);
    registerDeferred(request, bean, assocBean);
  }

  private void registerDeferred(BindableRequest request, EntityBean bean, EntityBean assocBean) throws SQLException {
    Object boundValue = importedId.bind(request, assocBean);
    if (boundValue == null && assocBean != null) {
      // this is the scenario for a derived foreign key
      // which will require an additional update
      // register for post insert of assocBean
      // update of bean set importedId
      request.getPersistRequest().deferredRelationship(assocBean, importedId, bean);
    }
  }

  @Nullable
  private EntityBean castToEntityBean(Object objectValue) {
    EntityBean generatedValue;
    if (objectValue instanceof EntityBean || objectValue == null) {
      generatedValue = (EntityBean) objectValue;
    } else {
      throw new IllegalStateException("Bean " + objectValue.getClass() + " is not enhanced?");
    }
    return generatedValue;
  }

}
