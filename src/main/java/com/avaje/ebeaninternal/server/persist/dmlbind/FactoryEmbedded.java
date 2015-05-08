package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.persist.dml.DmlMode;

/**
 * A factory that builds Bindable for embedded bean properties.
 */
public class FactoryEmbedded {

	private final FactoryProperty factoryProperty;

	public FactoryEmbedded(boolean bindEncryptDataFirst) {
		factoryProperty = new FactoryProperty(bindEncryptDataFirst);
	}
	
	/**
	 * Add bindable for the embedded properties to the list.
	 */
	public void create(List<Bindable> list, BeanDescriptor<?> desc, DmlMode mode, boolean withLobs) {
		
		BeanPropertyAssocOne<?>[] embedded = desc.propertiesEmbedded();
				
		for (int j = 0; j < embedded.length; j++) {
		
		  BeanProperty[] props = embedded[j].getProperties();
		  
			List<Bindable> bindList = new ArrayList<Bindable>(props.length);
			
			for (int i = 0; i < props.length; i++) {
				Bindable item = factoryProperty.create(props[i], mode, withLobs);
				if (item != null){
					bindList.add(item);
				}
			}
			
			list.add(new BindableEmbedded(embedded[j], bindList));
		}
	}
	

}
