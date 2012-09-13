/**
 * 
 */
package com.avaje.ebeaninternal.server.query;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * The select properties for a node in the SqlTree.
 */
public class SqlTreeProperties {

	/**
	 * The included Properties that will be used by EntityBeanIntercept
	 * to determine lazy loading on partial objects.
	 */
	Set<String> includedProps;

	/**
	 * True if this node of the tree should have read only entity beans.
	 */
	boolean readOnly;

	/**
	 * set to false if the id field is not included.
	 */
	boolean includeId = true;

	TableJoin[] tableJoins = new TableJoin[0];

	/**
	 * The bean properties in order.
	 */
	List<BeanProperty> propsList = new ArrayList<BeanProperty>();

	/**
	 * Maintain a list of property names to detect embedded bean additions.
	 */
	LinkedHashSet<String> propNames = new LinkedHashSet<String>();
	
	public SqlTreeProperties() {

	}
	
	public boolean containsProperty(String propName){
		return propNames.contains(propName);
	}

	public void add(BeanProperty[] props) {
		for (BeanProperty beanProperty : props) {
			propsList.add(beanProperty);
		}
	}

	public void add(BeanProperty prop) {
		propsList.add(prop);
		propNames.add(prop.getName());

	}
	
	public BeanProperty[] getProps() {
		return propsList.toArray(new BeanProperty[propsList.size()]);
	}

	public boolean isIncludeId() {
		return includeId;
	}

	public void setIncludeId(boolean includeId) {
		this.includeId = includeId;
	}

	public boolean isPartialObject() {
		return includedProps != null;
	}
	
	public Set<String> getIncludedProperties() {
		return includedProps;
	}

	public void setIncludedProperties(Set<String> includedProps) {
		this.includedProps = includedProps;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public TableJoin[] getTableJoins() {
		return tableJoins;
	}

	public void setTableJoins(TableJoin[] tableJoins) {
		this.tableJoins = tableJoins;
	}

}