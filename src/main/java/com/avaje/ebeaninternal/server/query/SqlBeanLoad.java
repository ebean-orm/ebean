package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;

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
	private final EntityBean bean;
	private final EntityBeanIntercept ebi;
  
	private final Class<?> type;
	private final boolean lazyLoading;	
	private final boolean refreshLoading;
  private final boolean rawSql;
	
  public SqlBeanLoad(DbReadContext ctx, Class<?> type, EntityBean bean, Mode queryMode) {

    this.ctx = ctx;
    this.rawSql = ctx.isRawSql();
    this.type = type;
    this.lazyLoading = queryMode.equals(Mode.LAZYLOAD_BEAN);
    this.refreshLoading = queryMode.equals(Mode.REFRESH_BEAN);
    this.bean = bean;
    this.ebi = bean == null ? null : bean._ebean_getIntercept();
  }
	
  /**
   * Return true if this is a lazy loading.
   */
  public boolean isLazyLoad() {
    return lazyLoading;
  }

  /**
   * Increment the resultSet index 1.
   */
  public void loadIgnore(int increment) {
    ctx.getDataReader().incrementPos(increment);
  }

  public Object load(BeanProperty prop) throws SQLException {

    if (!rawSql && !prop.isLoadProperty()) {
      return null;
    }

    if ((bean == null) 
        || (lazyLoading && ebi.isLoadedProperty(prop.getPropertyIndex()))
        || (type != null && !prop.isAssignableFrom(type))) {

      // ignore this property
      // ... null: bean already in persistence context
      // ... lazyLoading: partial bean that is lazy loading
      // ... type: inheritance and not assignable to this instance

      prop.loadIgnore(ctx);
      return null;
    }

    try {
      Object dbVal = prop.read(ctx);
      if (!refreshLoading) {
        prop.setValue(bean, dbVal);
      } else {
        prop.setValueIntercept(bean, dbVal);
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
