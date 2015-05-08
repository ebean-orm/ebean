package com.avaje.ebeaninternal.server.deploy;

import java.util.List;

import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for creating BeanControllers.
 */
public class PersistControllerManager {

	private static final Logger logger = LoggerFactory.getLogger(PersistControllerManager.class);

    private final List<BeanPersistController> list;
    
    public PersistControllerManager(BootupClasses bootupClasses){
    	
    	list = bootupClasses.getBeanPersistControllers();
    }
	
    public int getRegisterCount() {
		return list.size();
	}
	
    /**
     * Return the BeanPersistController for a given entity type.
     */
	public void addPersistControllers(DeployBeanDescriptor<?> deployDesc){
		
		for (int i = 0; i < list.size(); i++) {
			BeanPersistController c = list.get(i);
			if (c.isRegisterFor(deployDesc.getBeanType())){
				logger.debug("BeanPersistController on[" + deployDesc.getFullName() + "] " + c.getClass().getName());
				deployDesc.addPersistController(c);
			}
		}		
    }
    
}
