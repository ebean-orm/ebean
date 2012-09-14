package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

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
	 * Always add BindablePropertyUpdateGenerated properties.
	 */
	public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
		
		list.add(this);
	}

    public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !request.isIncluded(prop)){
            return;
        }
        dmlBind(request, bean, true);
    }
    
    public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !request.isIncludedWhere(prop)){
            return;
        }
        dmlBind(request, bean, false);
    }
    
	private void dmlBind(BindableRequest request, Object bean, boolean bindNull) throws SQLException {

		Object value = gen.getUpdateValue(prop, bean);
        
		// generated value should be the correct type
        request.bind(value, prop, prop.getName(), bindNull);
        
        // only register the update value if it was included
        // in the bean in the first place
        if (request.isIncluded(prop)) {
	        // need to set the generated value to the bean later
	        // after the where clause has been generated
	        request.registerUpdateGenValue(prop, bean, value);
        }
    }
	
	/**
	 * Always bind on Insert SET.
	 */
	@Override
	public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes){
		if (checkIncludes && !request.isIncluded(prop)){
			return;
		}
		request.appendColumn(prop.getDbColumn());
	}
	
	
}
