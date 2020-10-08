package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Bindable for a DB encrypted BeanProperty.
 */
public class BindableEncryptedProperty implements Bindable {

  private final BeanProperty prop;

  private final boolean bindEncryptDataFirst;

  public BindableEncryptedProperty(BeanProperty prop, boolean bindEncryptDataFirst) {
    this.prop = prop;
    this.bindEncryptDataFirst = bindEncryptDataFirst;
  }

  @Override
  public String toString() {
    return prop.toString();
  }

  @Override
  public boolean isDraftOnly() {
    return prop.isDraftOnly();
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (request.isAddToUpdate(prop)) {
      list.add(this);
    }
  }

  @Override
  public void dmlAppend(GenerateDmlRequest request) {

    // columnName = AES_ENCRYPT(?,?)
    request.appendColumn(prop.getDbColumn(), prop.getDbBind());
  }


  /**
   * Bind a value in a Insert SET clause.
   */
  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

    Object value = null;
    if (bean != null) {
      value = prop.getValue(bean);
    }

    // get Encrypt key
    String encryptKeyValue = prop.getEncryptKey().getStringValue();

    if (!bindEncryptDataFirst) {
      // H2 encrypt function ... different parameter order
      request.bindNoLog(encryptKeyValue, Types.VARCHAR, prop.getName() + "=****");
    }
    request.bindNoLog(value, prop);

    if (bindEncryptDataFirst) {
      // MySql, Postgres, Oracle
      request.bindNoLog(encryptKeyValue, Types.VARCHAR, prop.getName() + "=****");
    }
  }

}
