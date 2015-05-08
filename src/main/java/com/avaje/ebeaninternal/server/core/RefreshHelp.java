package com.avaje.ebeaninternal.server.core;


/**
 * Helper for performing a 'refresh' on an Entity bean.
 * <p>
 * Note that this does not 'refresh' any OnetoMany or ManyToMany properties. It
 * refreshes all the other properties though.
 * </p>
 */
public class RefreshHelp {
//
//	/**
//	 * Helper for debug of lazy loading.
//	 */
//	private final DebugLazyLoad debugLazyLoad;
//	
//	private final MAdminLoggingMBean logControl;
//	
//	public RefreshHelp(MAdminLoggingMBean logControl, boolean debugLazyLoad){
//		this.logControl = logControl;
//		this.debugLazyLoad = new DebugLazyLoad(debugLazyLoad);
//	}
//
//	/**
//	 * Refresh the bean from property values in dbBean.
//	 */
//	public void refresh(Object o, Object dbBean, BeanDescriptor<?> desc, EntityBeanIntercept ebi, Object id, boolean isLazyLoad) {
//
//		Object originalOldValues = null;
//		boolean setOriginalOldValues = false;
//
//		// set of properties to exclude from the refresh because it is
//		// not a refresh but rather a lazyLoading event.
//		Set<String> excludes = null;
//					
//		// turn off intercepting so lazy loading is
//		// not invoked when populating the bean
//		// with PropertyChangeSupport
//		ebi.setIntercepting(false);
//		
//		boolean readOnly = ebi.isReadOnly();
//		boolean sharedInstance = ebi.isSharedInstance();
//		
//		if (isLazyLoad){
//			excludes = ebi.getLoadedProps();
//			if (excludes != null){
//				// lazy loading a "Partial Object"... which already
//				// contains some properties and perhaps some oldValues
//				// and these will need to be maintained...
//				originalOldValues = ebi.getOldValues();
//				setOriginalOldValues = originalOldValues != null;					
//			}
//			
//			if (logControl.isDebugLazyLoad()){
//				debug(desc, ebi, id, excludes);
//			}				
//		}
//	
//				
//		BeanProperty[] props = desc.propertiesBaseScalar();
//		for (int i = 0; i < props.length; i++) {
//			BeanProperty prop = props[i];
//			if (excludes != null && excludes.contains(prop.getName())){
//				// ignore this property (partial bean lazy loading)
//				
//			} else {
//				Object dbVal = prop.getValue(dbBean);
//				if (isLazyLoad) {
//					prop.setValue(o, dbVal);
//				} else {			
//					prop.setValueIntercept(o, dbVal);
//				}
//				if (setOriginalOldValues){
//					// maintain original oldValues for partially loaded bean
//					prop.setValue(originalOldValues, dbVal);
//				}
//			}
//		}
//
//		BeanPropertyAssocOne<?>[] ones = desc.propertiesOne();
//		for (int i = 0; i < ones.length; i++) {
//			BeanProperty prop = ones[i];
//			if (excludes != null && excludes.contains(prop.getName())){
//				 // ignore this property (partial bean lazy loading)
//				
//			} else {
//				Object dbVal = prop.getValue(dbBean);
//				if (isLazyLoad){
//					prop.setValue(o, dbVal);					
//				} else {
//					prop.setValueIntercept(o, dbVal);
//				}
//				if (setOriginalOldValues){
//					// maintain original oldValues for partially loaded bean
//					prop.setValue(originalOldValues, dbVal);
//				}
//				if (dbVal != null){
//					if (sharedInstance){
//						// propagate sharedInstance status to associated beans
//						((EntityBean)dbVal)._ebean_getIntercept().setSharedInstance();
//					} else if (readOnly) {
//						// propagate readOnly status to associated beans
//						((EntityBean)dbVal)._ebean_getIntercept().setReadOnly(true);						
//					}
//				}
//				
//			}
//		}
//
//		refreshEmbedded(o, dbBean, desc, excludes, readOnly);
//
//		// set a lazy loading many proxy if required
//		BeanPropertyAssocMany<?>[] manys = desc.propertiesMany();
//		for (int i = 0; i < manys.length; i++) {
//			BeanPropertyAssocMany<?> prop = manys[i];
//			if (excludes != null && excludes.contains(prop.getName())){
//				// the many already existed on the bean
//				
//			} else {
//				// set a lazy loading proxy
//				prop.createReference(o, null, readOnly, sharedInstance);				
//			}
//		}
//		
//		// the refreshed/lazy loaded bean is always fully
//		// populated so set loadedProps to null
//		ebi.setLoadedProps(null);
//		
//		
//		// reset the loaded status
//		ebi.setLoaded();		
//	}
//
//	/**
//	 * Refresh the Embedded beans.
//	 */
//	private void refreshEmbedded(Object o, Object dbBean, BeanDescriptor<?> desc, Set<String> excludes, boolean propagateReadOnly) {
//
//		BeanPropertyAssocOne<?>[] embeds = desc.propertiesEmbedded();
//		for (int i = 0; i < embeds.length; i++) {
//			BeanPropertyAssocOne<?> prop = embeds[i];
//			if (excludes != null && excludes.contains(prop.getName())){
//				// ignore this property
//			} else {
//				// the original embedded bean
//				Object oEmb = prop.getValue(o);
//				
//				// the new one from the database
//				Object dbEmb = prop.getValue(dbBean);
//	
//				if (oEmb == null){
//					// original embedded bean was null
//					// so just replace the entire embedded bean
//					prop.setValueIntercept(o, dbEmb);
//					if (propagateReadOnly && dbEmb != null){
//						// propagate readOnly status to embedded beans
//						((EntityBean)dbEmb)._ebean_getIntercept().setReadOnly(true);
//					}
//					
//				} else {
//					// refresh each property of the original
//					// embedded bean
//					if (oEmb instanceof EntityBean){
//						// turn off interception to stop invoking lazy loading
//						// but allow PropertyChangeSupport
//						((EntityBean) oEmb)._ebean_getIntercept().setIntercepting(false);
//					}
//					
//					BeanProperty[] props = prop.getProperties();
//					for (int j = 0; j < props.length; j++) {
//						Object v = props[j].getValue(dbEmb);
//						props[j].setValueIntercept(oEmb, v);
//					}
//		
//					// No longer calling setLoaded() on embedded bean
//					// as the EntityBean itself
//					// .. calls setEmbeddedLoaded() on each of
//					// .. its embedded beans itself.					
//				}
//			}
//		}
//	}
//	
//
//	/**
//	 * Output some debug to describe the lazy loading event.
//	 */
//	private void debug(BeanDescriptor<?> desc, EntityBeanIntercept ebi, Object id, Set<String> excludes) {
//		
//				
//		Class<?> beanType = desc.getBeanType();
//		
//		StackTraceElement cause = debugLazyLoad.getStackTraceElement(beanType);
//		
//		String lazyLoadProperty = ebi.getLazyLoadProperty();
//		String msg = "debug.lazyLoad ["+desc+"] id["+id+"] lazyLoadProperty["+lazyLoadProperty+"]";
//		if (excludes != null){
//			msg += " partialProps"+excludes;
//		} 
//		if (cause != null){
//			String causeLine = cause.toString();
//			if (causeLine.indexOf(".groovy:") > -1){
//				// eclipse console does not like finding groovy source at the moment
//				causeLine = StringHelper.replaceString(causeLine, ".groovy:", ".groovy :");
//			}
//			msg += " at: "+causeLine;
//		}
//		System.err.println(msg);		
//	}
//	
//
//	

}
