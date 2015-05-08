package com.avaje.ebeaninternal.server.deploy.parse;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.deploy.InheritInfo;

/**
 * Represents a node in the Inheritance tree.
 * Holds information regarding Super Subclass support. 
 */
public class DeployInheritInfo {

	/**
	 * the default discriminator column according to the JPA 1.0 spec.
	 */
	private static final String JPA_DEFAULT_DISCRIM_COLUMN = "dtype";
	
	private int discriminatorLength;
	
	private int discriminatorType;
	
	private String discriminatorStringValue;
	private Object discriminatorObjectValue;
    
	private String discriminatorColumn;

	private String discriminatorWhere;

	private Class<?> type;

	private Class<?> parent;
    
	private ArrayList<DeployInheritInfo> children = new ArrayList<DeployInheritInfo>();
        
    /**
     * Create for a given type.
     */
    public DeployInheritInfo(Class<?> type){
        this.type = type;
    }
    
    /**
     * return the type.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Return the type of the root object.
     */
    public Class<?> getParent() {
        return parent;
    }

    /**
     * Set the type of the root object.
     */
    public void setParent(Class<?> parent) {
        this.parent = parent;
    }
    
    /**
     * Return true if this is abstract node.
     */
    public boolean isAbstract() {
        return (discriminatorObjectValue == null);
    }
    
    /**
     * Return true if this is the root node.
     */
    public boolean isRoot(){
    	return parent == null;
    }

    /**
     * Return the child nodes.
     */
    public List<DeployInheritInfo> children() {
        return children;
    }
    
    /**
     * Add a child node.
     */
    public void addChild(DeployInheritInfo childInfo){
        children.add(childInfo);
    }

    /**
     * Return the derived where for the discriminator.
     */
    public String getDiscriminatorWhere() {
        return discriminatorWhere;
    }

    /**
     * Set the derived where for the discriminator.
     */
    public void setDiscriminatorWhere(String discriminatorWhere) {
        this.discriminatorWhere = discriminatorWhere;
    }

    /**
     * Return the column name of the discriminator.
     */
    public String getDiscriminatorColumn(InheritInfo parent) {
    	if (discriminatorColumn == null){
    		if (parent == null){
    			discriminatorColumn = JPA_DEFAULT_DISCRIM_COLUMN;
    		} else {
    			discriminatorColumn = parent.getDiscriminatorColumn();
    		}
    	} 
		return discriminatorColumn;
    }

    /**
     * Set the column name of the discriminator.
     */
    public void setDiscriminatorColumn(String discriminatorColumn) {
        this.discriminatorColumn = discriminatorColumn;
    }

    public int getDiscriminatorLength(InheritInfo parent) {
    	if (discriminatorLength == 0){
    		if (parent == null){
    			discriminatorLength = 10;
    		} else {
    			discriminatorLength = parent.getDiscriminatorLength();
    		}
    	} 
		return discriminatorLength;    	
    }
    
    /**
     * Return the sql type of the discriminator value.
     */
    public int getDiscriminatorType(InheritInfo parent) {
    	if (discriminatorType == 0){
    		if (parent == null){
    			discriminatorType = Types.VARCHAR;
    		} else {
    			discriminatorType = parent.getDiscriminatorType();
    		}
    	} 
		return discriminatorType;
	}

    /**
     * Set the sql type of the discriminator.
     */
	public void setDiscriminatorType(int discriminatorType) {
		this.discriminatorType = discriminatorType;
	}
	
	/**
	 * Return the length of the discriminator column.
	 */
	public int getDiscriminatorLength() {
		return discriminatorLength;
	}

	/**
	 * Set the length of the discriminator column.
	 */
	public void setDiscriminatorLength(int discriminatorLength) {
		this.discriminatorLength = discriminatorLength;
	}

	/**
     * Return the discriminator value for this node.
     */
    public Object getDiscriminatorObjectValue() {
        return discriminatorObjectValue;
    }

    public String getDiscriminatorStringValue() {
        return discriminatorStringValue;
    }

    /**
     * Set the discriminator value for this node.
     */
    public void setDiscriminatorValue(String value) {
        if (value != null){
        	value = value.trim();
        	if (value.length() == 0){
        		value = null;
        	} else {
            	discriminatorStringValue = value;
        		// convert the value if desired
	        	if (discriminatorType == Types.INTEGER){
	        		this.discriminatorObjectValue = Integer.valueOf(value.toString());
	        	} else {
	        		this.discriminatorObjectValue = value;
	        	}
        	}
        }
    }
    
    public String getWhere() {
    	
    	List<Object> discList = new ArrayList<Object>();
    	
    	appendDiscriminator(discList);

    	return buildWhereLiteral(discList);
    }

    private void appendDiscriminator(List<Object> list) {
    	if (discriminatorObjectValue != null){
    		list.add(discriminatorObjectValue);
    	}
    	for (DeployInheritInfo child : children) {
    		child.appendDiscriminator(list);
		}
    }
    
    private String buildWhereLiteral(List<Object> discList) {
    	int size = discList.size();
    	if (size == 0){
    		return "";
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append(discriminatorColumn);
    	if (size == 1){
    		sb.append(" = ");
    	} else {
    		sb.append(" in (");
    	}
    	for (int i = 0; i < discList.size(); i++) {
    		appendSqlLiteralValue(i, discList.get(i), sb);
		}
    	if (size > 1){
    		sb.append(")");
    	}
		return sb.toString();
    }
    
    private void appendSqlLiteralValue(int count, Object value, StringBuilder sb) {
    	if (count > 0){
    		sb.append(",");
    	}
        if (value instanceof String){
            sb.append("'").append(value).append("'");
        } else {
            sb.append(value);
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InheritInfo[").append(type.getName()).append("]");
        sb.append(" root[").append(parent.getName()).append("]");
        sb.append(" disValue[").append(discriminatorStringValue).append("]");
        return sb.toString();
    }
    
}
