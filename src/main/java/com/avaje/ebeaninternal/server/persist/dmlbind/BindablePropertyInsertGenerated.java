package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for insert on a property with a GeneratedProperty.
 * <p>
 * This is typically a 'insert timestamp', 'update timestamp' or 'counter'.
 * </p>
 */
public class BindablePropertyInsertGenerated extends BindableProperty {
	
	private final GeneratedProperty gen;

	public BindablePropertyInsertGenerated(BeanProperty prop, GeneratedProperty gen) {
		super(prop);
		this.gen = gen;
	}
	
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
        
		Object value = gen.getInsertValue(prop, bean);
		
		// generated value should be the correct type
		if (bean != null){
			// support PropertyChangeSupport
			//prop.setValueIntercept(bean, value);
			prop.setValue(bean, value);
		}
    request.bind(value, prop, prop.getName());
  }

	/**
	 * Always bind on Insert SET.
	 */
	@Override
	public void dmlAppend(GenerateDmlRequest request){
		request.appendColumn(prop.getDbColumn());
	}
	
}
