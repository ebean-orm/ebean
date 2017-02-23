package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for a unidirectional relationship.
 * <p>
 * This inserts the foreign key value that is retrieved from the id of the
 * parentBean.
 * </p>
 */
public class BindableUnidirectional implements Bindable {

  private final BeanPropertyAssocOne<?> unidirectional;

  private final ImportedId importedId;

  private final BeanDescriptor<?> desc;

  public BindableUnidirectional(BeanDescriptor<?> desc, BeanPropertyAssocOne<?> unidirectional) {
    this.desc = desc;
    this.unidirectional = unidirectional;
    this.importedId = unidirectional.getImportedId();

  }

  @Override
  public String toString() {
    return "BindableShadowFKey " + unidirectional;
  }

  @Override
  public boolean isDraftOnly() {
    return false;
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    throw new PersistenceException("Never called (for insert only)");
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {
    // always included (in insert)
    importedId.dmlAppend(request);
  }


  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    PersistRequestBean<?> persistRequest = request.getPersistRequest();
    Object parentBean = persistRequest.getParentBean();

    if (parentBean == null) {
      Class<?> localType = desc.getBeanType();
      Class<?> targetType = unidirectional.getTargetType();

      String msg = "Error inserting bean [" + localType + "] with unidirectional relationship. ";
      msg += "For inserts you must use cascade save on the master bean [" + targetType + "].";
      throw new PersistenceException(msg);
    }

    importedId.bind(request, (EntityBean) parentBean);
  }

}
