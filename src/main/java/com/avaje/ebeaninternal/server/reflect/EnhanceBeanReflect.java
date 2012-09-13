package com.avaje.ebeaninternal.server.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.EntityBean;

/**
 * A BeanReflect implementation based on the enhancement that creates EntityBean
 * implementations.
 * <p>
 * That is, based on the fact that instances of the class passed in implement
 * the EntityBean interface.
 * </p>
 */
public final class EnhanceBeanReflect implements BeanReflect {

	private static final Object[] constuctorArgs = new Object[0];

	private final Class<?> clazz;
	private final EntityBean entityBean;
	private final Constructor<?> constructor;
	private final Constructor<?> vanillaConstructor;
	private final boolean hasNewInstanceMethod;
	private final boolean vanillaOnly;
	
	public EnhanceBeanReflect(Class<?> vanillaType, Class<?> clazz) {
		try {
			this.clazz = clazz;
			if (Modifier.isAbstract(clazz.getModifiers())) {
				this.entityBean = null;
				this.constructor = null;
				this.vanillaConstructor = null;
				this.hasNewInstanceMethod = false;
				this.vanillaOnly = false;
			} else {
                this.vanillaConstructor = defaultConstructor(vanillaType);
                this.constructor = defaultConstructor(clazz);
                
                Object newInstance = clazz.newInstance();
                if (newInstance instanceof EntityBean){
                    this.entityBean = (EntityBean)newInstance;
                    this.vanillaOnly = false;
                    this.hasNewInstanceMethod = hasNewInstanceMethod(clazz);
                } else {
                    // probably an XmlElement
                    this.entityBean = null;
                    this.vanillaOnly = true;
                    this.hasNewInstanceMethod = false;
                }
			}
		} catch (InstantiationException e) {
			throw new PersistenceException(e);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		}
	}

	private Constructor<?> defaultConstructor(Class<?> cls) {
		try {
			Class<?>[] params = new Class[0];
			return cls.getDeclaredConstructor(params);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
    private boolean hasNewInstanceMethod(Class<?> clazz) {
        Class<?>[] params = new Class[0];
        try {
            Method method = clazz.getMethod("_ebean_newInstance", params);
            if (method == null){
                return false;
            }
            try {
                Object o = constructor.newInstance(constuctorArgs);
                method.invoke(o, new Object[0]);
                return true;

            } catch (AbstractMethodError e){
                return false;

            } catch (InvocationTargetException e){
                return false;
               
            } catch (Exception e) {
                throw new RuntimeException("Unexpected? ", e);
            }
        } catch (SecurityException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    
    
	public boolean isVanillaOnly() {
        return vanillaOnly;
    }

    public Object createEntityBean() {
	    if (hasNewInstanceMethod){
	        return entityBean._ebean_newInstance();
	    } else {
    		try {
    			return constructor.newInstance(constuctorArgs);
    		} catch (Exception ex) {
    			throw new RuntimeException(ex);
    		}
	    }
	}

	public Object createVanillaBean() {
        try {
            return vanillaConstructor.newInstance(constuctorArgs);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
	}

	private int getFieldIndex(String fieldName) {
		if (entityBean == null){
			throw new RuntimeException("Trying to get fieldName on abstract class "+clazz);
		}
		String[] fields = entityBean._ebean_getFieldNames();
		for (int i = 0; i < fields.length; i++) {
			if (fieldName.equals(fields[i])) {
				return i;
			}
		}
		String fieldList = Arrays.toString(fields);
		String msg = "field [" + fieldName + "] not found in [" + clazz.getName() + "]" + fieldList;
		throw new IllegalArgumentException(msg);
	}

	public BeanReflectGetter getGetter(String name) {
		int i = getFieldIndex(name);
		return new Getter(i, entityBean);
	}

	public BeanReflectSetter getSetter(String name) {
		int i = getFieldIndex(name);
		return new Setter(i, entityBean);
	}

	static final class Getter implements BeanReflectGetter {
	    private final int fieldIndex;
	    private final EntityBean entityBean;

		Getter(int fieldIndex, EntityBean entityBean) {
			this.fieldIndex = fieldIndex;
			this.entityBean = entityBean;
		}

		public Object get(Object bean) {
			return entityBean._ebean_getField(fieldIndex, bean);
		}

		public Object getIntercept(Object bean) {
			return entityBean._ebean_getFieldIntercept(fieldIndex, bean);
		}
	}

	static final class Setter implements BeanReflectSetter {
		private final int fieldIndex;
		private final EntityBean entityBean;

		Setter(int fieldIndex, EntityBean entityBean) {
			this.fieldIndex = fieldIndex;
			this.entityBean = entityBean;
		}

		public void set(Object bean, Object value) {
			entityBean._ebean_setField(fieldIndex, bean, value);
		}

		public void setIntercept(Object bean, Object value) {
			entityBean._ebean_setFieldIntercept(fieldIndex, bean, value);
		}

	}
}
