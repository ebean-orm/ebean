package com.avaje.ebeaninternal.server.deploy.parse;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.avaje.ebean.validation.ValidatorMeta;
import com.avaje.ebean.validation.factory.Validator;
import com.avaje.ebean.validation.factory.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorFactoryManager {

	private static final Logger logger = LoggerFactory.getLogger(ValidatorFactoryManager.class);

	Map<Class<?>, ValidatorFactory> factoryMap;

	public ValidatorFactoryManager() {
		factoryMap = new HashMap<Class<?>, ValidatorFactory>();
	}

	public Validator create(Annotation ann, Class<?> type) {
		synchronized (this) {
			ValidatorMeta meta = ann.annotationType().getAnnotation(ValidatorMeta.class);
			if (meta == null) {
				return null;
			}
			Class<?> factoryClass = meta.factory();
			ValidatorFactory factory = getFactory(factoryClass);
			return factory.create(ann, type);
		}
	}

	private ValidatorFactory getFactory(Class<?> factoryClass) {
		try {
			ValidatorFactory factory = factoryMap.get(factoryClass);
			if (factory == null) {
				factory = (ValidatorFactory) factoryClass.newInstance();
				factoryMap.put(factoryClass, factory);
			}
			return factory;

		} catch (Exception e) {
			String msg = "Error creating ValidatorFactory " + factoryClass.getName();
			logger.error(msg, e);
			return null;
		}
	}
}
