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
	public IdBinder createIdBinder(BeanProperty[] uids) {
		
		if (uids.length == 0){
			// for report type beans that don't need an id
			return EMPTY;
			
		} else if (uids.length == 1){
			if (uids[0].isEmbedded()){
				return new IdBinderEmbedded(idInExpandedForm, (BeanPropertyAssocOne<?>)uids[0]);
			} else {
				return new IdBinderSimple(uids[0]);
			}
		
		} else {
			return new IdBinderMultiple(uids);
		}
	}
	
}
