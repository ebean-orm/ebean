package com.avaje.ebeaninternal.server.deploy.id;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Creates the appropriate IdConvertSet depending on the type of Id property(s).
 */
public class IdBinderFactory {

	private static final IdBinderEmpty EMPTY = new IdBinderEmpty();
	
	private final boolean idInExpandedForm;
	
	public IdBinderFactory(boolean idInExpandedForm) {
	    this.idInExpandedForm = idInExpandedForm;
	}
	
	/**
	 * Create the IdConvertSet for the given type of Id properties.
	 */
	public IdBinder createIdBinder(BeanProperty id) {
		
		if (id == null){
			// for report type beans that don't need an id
			return EMPTY;
			
		} 
		if (id.isEmbedded()){
			return new IdBinderEmbedded(idInExpandedForm, (BeanPropertyAssocOne<?>)id);
		} else {
			return new IdBinderSimple(id);
		}
	}
	
}
