package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for update on a property with a GeneratedProperty.
 * <p>
 * This is typically a 'update timestamp' or 'counter'.
 * </p>
 */
public class BindablePropertyUpdateGenerated extends BindableProperty {
	
	private final GeneratedProperty gen;
	
	public BindablePropertyUpdateGenerated(BeanProperty prop, GeneratedProperty gen) {
		super(prop);
		this.gen = gen;
	}
	
	/**
	 * Add BindablePropertyUpdateGenerated if the property is loaded.
	 */	
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    if (gen.includeInAllUpdates()) {
      list.add(this);
    } else if (request.isLoadedProperty(prop)) {
      list.add(this);
    }
  }

  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

      Object value = gen.getUpdateValue(prop, bean);
        
      // generated value should be the correct type
        request.bind(value, prop, prop.getName());
        
        // only register the update value if it was included
        // in the bean in the first place
        if (request.getPersistRequest().isLoadedProperty(prop)) {
        //if (request.isIncluded(prop)) {
	        // need to set the generated value to the bean later
	        // after the where clause has been generated
	        request.registerUpdateGenValue(prop, bean, value);
        }
    }
	
	/**
	 * Always bind on Insert SET.
	 */
	@Override
	public void dmlAppend(GenerateDmlRequest request){
		request.appendColumn(prop.getDbColumn());
	}
	
	
}
