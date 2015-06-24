package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebean.event.BeanFinder;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation for BeanFinderFactory.
 */
public class BeanFinderManager {

  final Logger logger = LoggerFactory.getLogger(BeanFinderManager.class);

  /**
   * Register of BeanFinder instances.
   */
  final Map<Class<?>, BeanFinder<?>> registerFor = new HashMap<Class<?>, BeanFinder<?>>();

  private final List<BeanFindController> list;

  public BeanFinderManager(BootupClasses bootupClasses) {
    list = bootupClasses.getBeanFindControllers();

    List<Class<?>> beanFinders = bootupClasses.getBeanFinders();
    for (Class<?> cls : beanFinders) {
      Class<?> entityType = getEntityClass(cls);
      try {
        BeanFinder<?> beanFinder = (BeanFinder<?>) cls.newInstance();
        registerFor.put(entityType, beanFinder);

      } catch (Exception ex) {
        throw new PersistenceException(ex);
      }
    }
  }

  public int getRegisterCount() {
    return registerFor.size() + list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  public void addFindControllers(DeployBeanDescriptor<?> deployDesc) {

    for (int i = 0; i < list.size(); i++) {
      BeanFindController c = list.get(i);
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanFindController on[" + deployDesc.getFullName() + "] " + c.getClass().getName());
        deployDesc.setBeanFinder(c);
      }
    }
    // support the deprecated BeanFinder
    BeanFinder beanFinder = registerFor.get(deployDesc.getBeanType());
    if (beanFinder != null) {
      deployDesc.setBeanFinder(new BeanFinderAdapter(beanFinder));
      logger.debug("BeanFinder on[" + deployDesc.getFullName() + "] " + beanFinder.getClass().getName());
    }
  }

  /**
   * Find the entity class given the controller class.
   * <p>
   * This uses reflection to find the generics parameter type.
   * </p>
   */
  private Class<?> getEntityClass(Class<?> controller) {

    Class<?> cls = ParamTypeUtil.findParamType(controller, BeanFinder.class);

    if (cls == null) {
      String msg = "Could not determine the entity class (generics parameter type) from " + controller + " using reflection.";
      throw new PersistenceException(msg);
    }
    return cls;
  }
}
