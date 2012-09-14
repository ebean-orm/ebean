package com.avaje.ebeaninternal.server.deploy;

import java.lang.reflect.Method;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.reflect.BeanReflectSetter;

/**
 * A place holder for BeanReflectSetter that should never be called.
 * <p>
 * This is for properties of classes that are abstract and at the root
 * of an inheritance hierarchy.
 * </p>
 * @author rbygrave
 */
public class ReflectSetter {

	/**
	 * Creates place holder objects that should never be called.
	 */
	public static BeanReflectSetter create(DeployBeanProperty prop) {
		
		String fullName = prop.getFullBeanName();
		Method writeMethod = prop.getWriteMethod();
		return new RefCalled(fullName, writeMethod);
	}
	
	static class RefCalled implements BeanReflectSetter {
		
		final String fullName;
		final Method writeMethod;
		
		RefCalled(String fullName, Method writeMethod) {
			this.fullName = fullName;
			this.writeMethod = writeMethod;
		}
		public void set(Object bean, Object value) {
			Object[] a = new Object[1];
			a[0] = value;
			try {
	            writeMethod.invoke(bean, a);
            } catch (Exception e) {
            	String beanType = bean == null ? "null" : bean.getClass().toString();
            	String msg = "Error setting value on "+fullName+" value["+value+"] on type["+beanType+"]";
	            throw new RuntimeException(msg, e);
            } 
		}
		public void setIntercept(Object bean, Object value) {
			String msg = "Not expecting setIntercept to be called. Refer Bug 368";
			throw new RuntimeException(msg);
		}
	}
}
