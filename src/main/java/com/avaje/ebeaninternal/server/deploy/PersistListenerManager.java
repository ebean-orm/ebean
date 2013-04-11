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

	private final List<BeanPersistListener<?>> list;

	public PersistListenerManager(BootupClasses bootupClasses) {
		list = bootupClasses.getBeanPersistListeners();
	}

	public int getRegisterCount() {
		return list.size();
	}

	/**
	 * Return the BeanPersistController for a given entity type.
	 */
	@SuppressWarnings("unchecked")
	public <T> void addPersistListeners(DeployBeanDescriptor<T> deployDesc) {

		for (int i = 0; i < list.size(); i++) {
			BeanPersistListener<?> c = list.get(i);
			if (isRegisterFor(deployDesc.getBeanType(), c)) {
				logger.debug("BeanPersistListener on[" + deployDesc.getFullName() + "] " + c.getClass().getName());
				deployDesc.addPersistListener((BeanPersistListener<T>) c);
			}
		}
	}

	public static boolean isRegisterFor(Class<?> beanType, BeanPersistListener<?> c) {
		Class<?> listenerEntity = getEntityClass(c.getClass());
		return beanType.equals(listenerEntity);
	}
	
	/**
	 * Find the entity class given the controller class.
	 * <p>
	 * This uses reflection to find the generics parameter type.
	 * </p>
	 */
	private static Class<?> getEntityClass(Class<?> controller) {

		Class<?> cls = ParamTypeUtil.findParamType(controller, BeanPersistListener.class);
		if (cls == null) {
			String msg = "Could not determine the entity class (generics parameter type) from " + controller
					+ " using reflection.";
			throw new PersistenceException(msg);
		}
		return cls;
	}
}
