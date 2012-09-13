/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.deploy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.deploy.parse.DeployInheritInfo;
import com.avaje.ebeaninternal.server.query.SqlTreeProperties;
import com.avaje.ebeaninternal.server.subclass.SubClassUtil;

/**
 * Represents a node in the Inheritance tree. Holds information regarding Super
 * Subclass support.
 */
public class InheritInfo {

	private final String discriminatorStringValue;
	private final Object discriminatorValue;

	private final String discriminatorColumn;

	private final int discriminatorType;

	private final int discriminatorLength;

	private final String where;

	private final Class<?> type;

	private final ArrayList<InheritInfo> children = new ArrayList<InheritInfo>();

	/**
	 * Map of discriminator values to InheritInfo.
	 */
	private final HashMap<String, InheritInfo> discMap;
    
    /**
     * Map of class types to InheritInfo (taking into account subclass proxy classes).
     */
    private final HashMap<String, InheritInfo> typeMap;

	private final InheritInfo parent;

	private final InheritInfo root;

	private BeanDescriptor<?> descriptor;

	public InheritInfo(InheritInfo r, InheritInfo parent, DeployInheritInfo deploy) {
		
		this.parent = parent;
		this.type = deploy.getType();
		this.discriminatorColumn = InternString.intern(deploy.getDiscriminatorColumn(parent));
		this.discriminatorValue = deploy.getDiscriminatorObjectValue();
		this.discriminatorStringValue = deploy.getDiscriminatorStringValue();
		
		this.discriminatorType = deploy.getDiscriminatorType(parent);
		this.discriminatorLength = deploy.getDiscriminatorLength(parent);
		this.where = InternString.intern(deploy.getWhere());
		
		if (r == null) {
			// this is a root node
			root = this;
			discMap = new HashMap<String, InheritInfo>();
			typeMap = new HashMap<String, InheritInfo>();
			registerWithRoot(this);

		} else {
			this.root = r;
			// register with the root node...
			discMap = null;
			typeMap = null;
			root.registerWithRoot(this);
		}
	}
	
	/**
	 * Visit all the children in the inheritance tree.
	 */
	public void visitChildren(InheritInfoVisitor visitor) {
		
		for (int i = 0; i < children.size(); i++) {
			InheritInfo child = children.get(i);
			visitor.visit(child);
			child.visitChildren(visitor);
		}
	}

    /**
     * return true if anything in the inheritance hierarchy has a relationship
     * with a save cascade on it.
     */
	public boolean isSaveRecurseSkippable() {
	    return root.isNodeSaveRecurseSkippable();
	}
	
	private boolean isNodeSaveRecurseSkippable() {
	    if (!descriptor.isSaveRecurseSkippable()){
	        return false;
	    }
        for (int i = 0; i < children.size(); i++) {
            InheritInfo child = children.get(i);
            if (!child.isNodeSaveRecurseSkippable()){
                return false;
            }
        }
        return true;
	}
	
    /**
     * return true if anything in the inheritance hierarchy has a relationship
     * with a delete cascade on it.
     */
    public boolean isDeleteRecurseSkippable() {
        return root.isNodeDeleteRecurseSkippable();
    }

    private boolean isNodeDeleteRecurseSkippable() {
        if (!descriptor.isDeleteRecurseSkippable()) {
            return false;
        }
        for (int i = 0; i < children.size(); i++) {
            InheritInfo child = children.get(i);
            if (!child.isNodeDeleteRecurseSkippable()) {
                return false;
            }
        }
        return true;
    }
	
	/**
	 * Set the descriptor for this node.
	 */
	public void setDescriptor(BeanDescriptor<?> descriptor) {
		
		this.descriptor = descriptor;
	}
	
	/**
	 * Return the associated BeanDescriptor for this node.
	 */
	public BeanDescriptor<?> getBeanDescriptor() {
		return descriptor;
	}
	
	/**
	 * Get the bean property additionally looking in the sub types.
	 */
	public BeanProperty findSubTypeProperty(String propertyName) {
		
		BeanProperty prop = null;
		
		for (int i = 0, x=children.size(); i < x; i++) {
			InheritInfo childInfo = children.get(i);
			
			// recursively search this child bean descriptor
			prop = childInfo.getBeanDescriptor().findBeanProperty(propertyName);
			
			if (prop != null){
				return prop;
			}
		}
		
		return null;
	}
	
	/**
	 * Add the local properties for each sub class below this one.
	 */
	public void addChildrenProperties(SqlTreeProperties selectProps) {
		
		for (int i = 0, x=children.size(); i < x; i++) {
			InheritInfo childInfo = children.get(i);
			selectProps.add(childInfo.descriptor.propertiesLocal());
			
			childInfo.addChildrenProperties(selectProps);
		}
	}

    /**
     * Return the associated InheritInfo for this DB row read.
     */
    public InheritInfo readType(DbReadContext ctx) throws SQLException {

        String discValue = ctx.getDataReader().getString();
        return readType(discValue);
    }
	
    /**
     * Return the associated InheritInfo for this discriminator value.
     */
    public InheritInfo readType(String discValue) {

        if (discValue == null) {
            return null;
        }

        InheritInfo typeInfo = root.getType(discValue);
        if (typeInfo == null) {
            String m = "Inheritance type for discriminator value [" + discValue + "] was not found?";
            throw new PersistenceException(m);
        }

        return typeInfo;
    }

    /**
     * Return the associated InheritInfo for this bean type.
     */
    public InheritInfo readType(Class<?> beanType) {

        InheritInfo typeInfo = root.getTypeByClass(beanType);
        if (typeInfo == null) {
            String m = "Inheritance type for bean type [" + beanType.getName() + "] was not found?";
            throw new PersistenceException(m);
        }

        return typeInfo;
    }

	/**
	 * Create an EntityBean for this type.
	 */
	public Object createBean(boolean vanillaMode) {
		return descriptor.createBean(vanillaMode);
	}
	
	/**
	 * Return the IdBinder for this type.
	 */
	public IdBinder getIdBinder() {
		return descriptor.getIdBinder();
	}
	
	/**
	 * return the type.
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Return the root node of the tree.
	 * <p>
	 * The root has a map of discriminator values to types.
	 * </p>
	 */
	public InheritInfo getRoot() {
		return root;
	}

	/**
	 * Return the parent node.
	 */
    public InheritInfo getParent() {
        return parent;
    }
    
	/**
	 * Return true if this is abstract node.
	 */
	public boolean isAbstract() {
		return (discriminatorValue == null);
	}

	/**
	 * Return true if this is the root node.
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * For a discriminator get the inheritance information for this tree.
	 */
	public InheritInfo getType(String discValue) {
		return discMap.get(discValue);
	}
	
	/**
	 * Return the InheritInfo for the given bean type.
	 */
    private InheritInfo getTypeByClass(Class<?> beanType) {
        String clsName = SubClassUtil.getSuperClassName(beanType.getName());
        return typeMap.get(clsName);
    }

	private void registerWithRoot(InheritInfo info) {
		if (info.getDiscriminatorStringValue() != null) {
			String stringDiscValue = info.getDiscriminatorStringValue();
			discMap.put(stringDiscValue, info);
		}
        String clsName = SubClassUtil.getSuperClassName(info.getType().getName());
		typeMap.put(clsName, info);
	}

	/**
	 * Add a child node.
	 */
	public void addChild(InheritInfo childInfo) {
		children.add(childInfo);
	}

	/**
	 * Return the derived where for the discriminator.
	 */
	public String getWhere() {

		return where;
	}

	/**
	 * Return the column name of the discriminator.
	 */
	public String getDiscriminatorColumn() {
		return discriminatorColumn;
	}

	/**
	 * Return the sql type of the discriminator value.
	 */
	public int getDiscriminatorType() {
		return discriminatorType;
	}
	
	
	/**
	 * Return the length of the discriminator column.
	 */
	public int getDiscriminatorLength() {
		return discriminatorLength;
	}

	/**
	 * Return the discriminator value for this node.
	 */
	public String getDiscriminatorStringValue() {
		return discriminatorStringValue;
	}

	public Object getDiscriminatorValue() {
		return discriminatorValue;
	}
	
	public String toString() {
		return "InheritInfo[" + type.getName() + "] disc[" + discriminatorStringValue + "]";
	}

}
