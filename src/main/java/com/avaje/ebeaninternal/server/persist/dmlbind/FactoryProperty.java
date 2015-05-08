package com.avaje.ebeaninternal.server.persist.dmlbind;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import com.avaje.ebeaninternal.server.persist.dml.DmlMode;

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

		if (DmlMode.INSERT.equals(mode) && !prop.isDbInsertable()){
			return null;
		}
		if (DmlMode.UPDATE.equals(mode) && !prop.isDbUpdatable()){
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

		GeneratedProperty gen = prop.getGeneratedProperty();
		if (gen != null) {
			if (DmlMode.INSERT.equals(mode)) {
				if (gen.includeInInsert()) {
					return new BindablePropertyInsertGenerated(prop, gen);					
				} else {
					return null;
				}

			}
			if (DmlMode.UPDATE.equals(mode)) {
				if (gen.includeInUpdate()) {
					return new BindablePropertyUpdateGenerated(prop, gen);
				} else {
					// An 'Insert Timestamp' is never updated 
					return null;
				}
			}
		}

        return prop.isDbEncrypted() ? new BindableEncryptedProperty(prop, bindEncryptDataFirst) : new BindableProperty(prop);
	}
}
