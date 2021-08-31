package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.BeanPropertyJsonMapper;
import io.ebeaninternal.server.persist.dml.DmlMode;

/**
 * Creates the appropriate Bindable for a BeanProperty.
 * <p>
 * Lob properties can be excluded and it creates BindablePropertyInsertGenerated
 * and BindablePropertyUpdateGenerated as required.
 * </p>
 */
final class FactoryProperty {

  private final boolean bindEncryptDataFirst;

  FactoryProperty(boolean bindEncryptDataFirst) {
    this.bindEncryptDataFirst = bindEncryptDataFirst;
  }

  /**
   * Create a Bindable for the property given the mode and withLobs flag.
   */
  public Bindable create(BeanProperty prop, DmlMode mode, boolean withLobs, boolean allowManyToOne) {
    if (DmlMode.INSERT == mode && !prop.isDbInsertable()) {
      return null;
    }
    if (DmlMode.UPDATE == mode && !prop.isDbUpdatable()) {
      return null;
    }
    if (prop.isLob() && !withLobs) {
      // Lob exclusion
      return null;
    }
    if (prop.isDbEncrypted()){
      return new BindableEncryptedProperty(prop, bindEncryptDataFirst);
    }
    if (allowManyToOne && prop instanceof BeanPropertyAssocOne) {
      return  new BindableAssocOne((BeanPropertyAssocOne<?>)prop);
    }
    if (prop instanceof BeanPropertyJsonMapper) {
      if (DmlMode.INSERT == mode) {
        return new BindablePropertyJsonInsert(prop);
      } else if (DmlMode.UPDATE == mode) {
        return new BindablePropertyJsonUpdate(prop);
      }
    }
    return new BindableProperty(prop);
  }
}
