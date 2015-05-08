package com.avaje.ebeaninternal.server.deploy.parse;

import java.lang.annotation.Annotation;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Inheritance;

import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

/**
 * Builds the InheritInfo deployment information.
 */
public class DeployInherit {

	private final Map<Class<?>, DeployInheritInfo> deployMap = new LinkedHashMap<Class<?>, DeployInheritInfo>();

	private final Map<Class<?>, InheritInfo> finalMap = new LinkedHashMap<Class<?>, InheritInfo>();

	private final BootupClasses bootupClasses;
	
	/**
	 * Create the InheritInfoDeploy.
	 */
	public DeployInherit(BootupClasses bootupClasses) {
		this.bootupClasses = bootupClasses;
		initialise();
	}

	public void process(DeployBeanDescriptor<?> desc) {
		InheritInfo inheritInfo = finalMap.get(desc.getBeanType());
		desc.setInheritInfo(inheritInfo);
	}

	private void initialise() {
		List<Class<?>> entityList = bootupClasses.getEntities();

		findInheritClasses(entityList);
		buildDeployTree();
		buildFinalTree();
	}

	private void findInheritClasses(List<Class<?>> entityList) {

		// go through each class and initialise the info object...
		for (Class<?> cls : entityList) {
			if (isInheritanceClass(cls)) {
				DeployInheritInfo info = createInfo(cls);
				deployMap.put(cls, info);
			}
		}
	}

	private void buildDeployTree() {

	  for (DeployInheritInfo info : deployMap.values()) {
			if (!info.isRoot()) {
				DeployInheritInfo parent = getInfo(info.getParent());
				parent.addChild(info);
			}
		}
	}

	private void buildFinalTree() {

		for (DeployInheritInfo deploy : deployMap.values()) {
			if (deploy.isRoot()) {
				// build tree top down...
				createFinalInfo(null, null, deploy);
			}
		}
	}

	private InheritInfo createFinalInfo(InheritInfo root, InheritInfo parent, DeployInheritInfo deploy) {

		InheritInfo node = new InheritInfo(root, parent, deploy);
		if (parent != null) {
			parent.addChild(node);
		}
		finalMap.put(node.getType(), node);

		if (root == null) {
			root = node;
		}

		// buildFinalChildren(root, child, deploy);
		for (DeployInheritInfo childDeploy : deploy.children()) {
			createFinalInfo(root, node, childDeploy);
		}

		return node;
	}

	/**
	 * Build the InheritInfo for a given class.
	 */
	private DeployInheritInfo getInfo(Class<?> cls) {
		return deployMap.get(cls);
	}

	private DeployInheritInfo createInfo(Class<?> cls) {

		DeployInheritInfo info = new DeployInheritInfo(cls);

		Class<?> parent = findParent(cls);
		if (parent != null) {
			info.setParent(parent);
		} else {
			// its the root of inheritance tree...
		}

		Inheritance ia = (Inheritance) cls.getAnnotation(Inheritance.class);
		if (ia != null) {
			ia.strategy();
		}
		DiscriminatorColumn da = (DiscriminatorColumn) cls.getAnnotation(DiscriminatorColumn.class);
		if (da != null) {
		  // lowercase the discriminator column for RawSql and JSON
			info.setDiscriminatorColumn(da.name().toLowerCase());
			DiscriminatorType discriminatorType = da.discriminatorType();
			if (discriminatorType.equals(DiscriminatorType.INTEGER)){
				info.setDiscriminatorType(Types.INTEGER);				
			} else {
				info.setDiscriminatorType(Types.VARCHAR);
			}
			info.setDiscriminatorLength(da.length());
		}

		DiscriminatorValue dv = (DiscriminatorValue) cls.getAnnotation(DiscriminatorValue.class);
		if (dv != null) {
			info.setDiscriminatorValue(dv.value());
		}
		
		return info;
	}

	private Class<?> findParent(Class<?> cls) {
		Class<?> superCls = cls.getSuperclass();
		if (isInheritanceClass(superCls)) {
			return superCls;
		} else {
			return null;
		}
	}

	private boolean isInheritanceClass(Class<?> cls) {
		if (cls.equals(Object.class)) {
			return false;
		}
		Annotation a = cls.getAnnotation(Inheritance.class);
		if (a != null) {
			return true;
		}
		// search up the inheritance heirarchy
		return isInheritanceClass(cls.getSuperclass());
	}

}
