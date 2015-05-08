package com.avaje.ebeaninternal.server.deploy;

import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the assignment/registration of BeanPersistListener with their
 * respective DeployBeanDescriptor's.
 */
public class PersistListenerManager {

	private static final Logger logger = LoggerFactory.getLogger(PersistListenerManager.class);

	private final List<BeanPersistListener> list;

	public PersistListenerManager(BootupClasses bootupClasses) {
		list = bootupClasses.getBeanPersistListeners();
	}

	public int getRegisterCount() {
		return list.size();
	}

	/**
	 * Return the BeanPersistController for a given entity type.
	 */
	public <T> void addPersistListeners(DeployBeanDescriptor<T> deployDesc) {

		for (int i = 0; i < list.size(); i++) {
			BeanPersistListener listener = list.get(i);
      if (listener.isRegisterFor(deployDesc.getBeanType())) {
				logger.debug("BeanPersistListener on[{}] {}", deployDesc.getFullName(), listener.getClass().getName());
				deployDesc.addPersistListener(listener);
			}
		}
	}

}
