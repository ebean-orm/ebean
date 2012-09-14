package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;

/**
 * Controls the loading of property data into a bean.
 * <p>
 * Takes into account the differences of lazy loading and
 * partial objects.
 * </p>
 */
public class SqlBeanLoad {

	private final DbReadContext ctx;
	private final Object bean;
	private final Class<?> type;
	private final Object originalOldValues;
	private final boolean isLazyLoad;
	
	// set of properties to exclude from the refresh because it is
	// not a refresh but rather a lazyLoading event.
	private final Set<String> excludes;
	private final boolean setOriginalOldValues;
	
	private final boolean rawSql;
	
	public SqlBeanLoad(DbReadContext ctx, Class<?> type, Object bean, Mode queryMode) {
	
		this.ctx = ctx;
		this.rawSql = ctx.isRawSql();
		this.type = type;
		this.isLazyLoad = queryMode.equals(Mode.LAZYLOAD_BEAN);
		this.bean = bean;
		
        if (bean instanceof EntityBean) {
            EntityBeanIntercept ebi = ((EntityBean) bean)._ebean_getIntercept();

            this.excludes = isLazyLoad ? ebi.getLoadedProps() : null;
            if (excludes != null) {
                // lazy loading a "Partial Object"... which already
                // contains some properties and perhaps some oldValues
                // and these will need to be maintained...
                originalOldValues = ebi.getOldValues();
            } else {
                originalOldValues = null;
            }
            this.setOriginalOldValues = originalOldValues != null;
        } else {
            this.excludes = null;
            this.originalOldValues = null;
            this.setOriginalOldValues = false;
        }
	}
	
	/**
	 * Return true if this is a lazy loading.
	 */
	public boolean isLazyLoad() {
    	return isLazyLoad;
    }

	/**
	 * Increment the resultSet index 1.
	 */
	public void loadIgnore(int increment) {
	    ctx.getDataReader().incrementPos(increment);
	}
	
	public Object load(BeanProperty prop) throws SQLException {
		
		if (!rawSql && prop.isTransient()){
			return null;
		}
		
		if ((bean == null) 
			|| (excludes != null && excludes.contains(prop.getName()))
			|| (type != null && !prop.isAssignableFrom(type))){

			// ignore this property 
			// ... null: bean already in persistence context
			// ... excludes: partial bean that is lazy loading
			// ... type: inheritance and not assignable to this instance
			
		    prop.loadIgnore(ctx);
			return null;
		}
		
		try {
			Object dbVal = prop.read(ctx);
			if (isLazyLoad){
				prop.setValue(bean, dbVal);					
			} else {
				prop.setValueIntercept(bean, dbVal);
			}
			if (setOriginalOldValues){
				// maintain original oldValues for partially loaded bean
				prop.setValue(originalOldValues, dbVal);
			}	
			return dbVal;
			
		} catch (Exception e) {
			String msg = "Error loading on " + prop.getFullBeanName();
			throw new PersistenceException(msg, e);
		}
	}
	
	public void loadAssocMany(BeanPropertyAssocMany<?> prop) {
		
	    // do nothing, as a lazy loading BeanCollection 'reference'
	    // is created and registered with the loading context
	    // in SqlTreeNodeBean.createListProxies()	    
	}
}
