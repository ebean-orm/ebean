package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.persist.dml.DmlMode;

/**
 * Creates the appropriate Bindable for a BeanProperty.
 * <p>
 * Lob properties can be excluded and it creates BindablePropertyInsertGenerated
 * and BindablePropertyUpdateGenerated as required.
 * </p>
 */
public class FactoryProperty {

  private final boolean bindEncryptDataFirst;

  public FactoryProperty(boolean bindEncryptDataFirst) {
    this.bindEncryptDataFirst = bindEncryptDataFirst;
  }

  /**
   * Create a Bindable for the property given the mode and withLobs flag.
   */
  public Bindable create(BeanProperty prop, DmlMode mode, boolean withLobs) {

    if (DmlMode.INSERT == mode && !prop.isDbInsertable()) {
      return null;
    }
    if (DmlMode.UPDATE == mode && !prop.isDbUpdatable()) {
      return null;
    }

    if (prop.isLob()) {
      if (!withLobs) {
        // Lob exclusion
        return null;
      } else {
        return prop.isDbEncrypted() ? new BindableEncryptedProperty(prop, bindEncryptDataFirst) : new BindableProperty(prop);
      }
    }

    return prop.isDbEncrypted() ? new BindableEncryptedProperty(prop, bindEncryptDataFirst) : new BindableProperty(prop);
  }
}
