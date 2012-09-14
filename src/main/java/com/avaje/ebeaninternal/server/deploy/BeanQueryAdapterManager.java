package com.avaje.ebeaninternal.server.deploy;

import java.util.List;
import java.util.logging.Logger;

import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

/**
 * Default implementation for creating BeanControllers.
 */
public class BeanQueryAdapterManager {

	private static final Logger logger = Logger.getLogger(BeanQueryAdapterManager.class.getName());

    private final List<BeanQueryAdapter> list;
    
    public BeanQueryAdapterManager(BootupClasses bootupClasses){
    	
    	list = bootupClasses.getBeanQueryAdapters();
    }
	
    public int getRegisterCount() {
		return list.size();
	}
	
    /**
     * Return the BeanPersistController for a given entity type.
     */
	public void addQueryAdapter(DeployBeanDescriptor<?> deployDesc){
		
		for (int i = 0; i < list.size(); i++) {
			BeanQueryAdapter c = list.get(i);
			if (c.isRegisterFor(deployDesc.getBeanType())){
				logger.fine("BeanQueryAdapter on[" + deployDesc.getFullName() + "] " + c.getClass().getName());
				deployDesc.addQueryAdapter(c);
			}
		}		
    }
    
}
