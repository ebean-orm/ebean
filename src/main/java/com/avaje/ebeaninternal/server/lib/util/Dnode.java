package com.avaje.ebeaninternal.server.lib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A lightweight tree structure for simple XML handling.
 * <p>
 * It removes support for nodes being mixed with content. That is, a node can
 * only contain content or a list of one or more child nodes. It does not
 * support mixing bits of content between the child nodes.
 * </p>
 * <p>
 * Although designed to simplify XML in supported cases it can be used as a
 * general tree structure with attributes of java Objects.
 * </p>
 */
public class Dnode {

	int level;

	String nodeName;

	String nodeContent;

	ArrayList<Dnode> children;

	LinkedHashMap<String, String> attrList = new LinkedHashMap<String, String>();

	/**
	 * Create a node.
	 */
	public Dnode() {
	}

	/**
	 * Parse the raw XML string.
	 */
	public static Dnode parse(String s){
		DnodeReader r = new DnodeReader();
		return r.parseXml(s);
	}
	
	/**
	 * Return the node as XML.
	 */
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		generate(sb);
		return sb.toString();
	}

	/**
	 * Generate this node as xml to the buffer.
	 */
	public StringBuilder generate(StringBuilder sb) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		sb.append("<").append(nodeName);
		for (String attr : attrList.keySet()) {
			Object attrValue = getAttribute(attr);
			sb.append(" ").append(attr).append("=\"");
			if (attrValue != null) {
				sb.append(attrValue);
			}
			sb.append("\"");
		}

		if (nodeContent == null && !hasChildren()) {
			sb.append(" />");

		} else {
			sb.append(">");
			if (children != null && children.size() > 0) {
				for (int i = 0; i < children.size(); i++) {
					Dnode child = children.get(i);
					child.generate(sb);
				}
			}
			if (nodeContent != null) {
				sb.append(nodeContent);
			}
			sb.append("</").append(nodeName).append(">");
		}
		return sb;
	}

	/**
	 * Return the node name.
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * Set the node name.
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * Return the node content.
	 */
	public String getNodeContent() {
		return nodeContent;
	}

	/**
	 * Set the node content.
	 */
	public void setNodeContent(String nodeContent) {
		this.nodeContent = nodeContent;
	}

	/**
	 * Return true if this node has children.
	 */
	public boolean hasChildren() {
		return getChildrenCount() > 0;
	}

	/**
	 * Return the number of children this node has.
	 */
	public int getChildrenCount() {
		if (children == null) {
			return 0;
		}
		return children.size();
	}

	/**
	 * Remove a ancestor node.
	 */
	public boolean remove(Dnode node) {
		if (children == null) {
			return false;
		}
		if (children.remove(node)) {
			return true;
		}
		for (Dnode child : children) {
			if (child.remove(node)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * List of children nodes.
	 */
	public List<Dnode> children() {
		if (children == null) {
			return null;
		}
		return children;
	}

	/**
	 * Add a child.
	 */
	public void addChild(Dnode child) {
		if (children == null) {
			children = new ArrayList<Dnode>();
		}
		children.add(child);
		child.setLevel(level + 1);
	}

	/**
	 * Return the level or depth of the node from the root.
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Set the level or depth of this node from the root.
	 */
	public void setLevel(int level) {
		this.level = level;
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Dnode child = children.get(i);
				child.setLevel(level + 1);
			}
		}
	}

	/**
	 * Find the first matching node using nodeName. This is a depth first tree
	 * search.
	 */
	public Dnode find(String nodeName) {
		return find(nodeName, null, null);
	}

	/**
	 * Find the first node matching nodeName and attribute value. This is a
	 * depth first tree search.
	 */
	public Dnode find(String nodeName, String attrName, Object value) {

		return find(nodeName, attrName, value, -1);

	}

	/**
	 * Search for a single node with control over maxLevel. Find the first node
	 * matching nodeName and attribute value. If attrName and value are null
	 * then this will just search using the nodeName. This is a depth first tree
	 * search. Once a matching node is found the search will stop.
	 */
	public Dnode find(String nodeName, String attrName, Object value, int maxLevel) {

		ArrayList<Dnode> list = new ArrayList<Dnode>();
		findByNode(list, nodeName, true, attrName, value, maxLevel);
		if (list.size() >= 1) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * Find all the nodes that match the nodeName.
	 * 
	 */
	public List<Dnode> findAll(String nodeName, int maxLevel) {
		int level = -1;
		if (maxLevel > 0) {
			level = this.level + maxLevel;
		}
		return findAll(nodeName, null, null, level);
	}

	/**
	 * Find all the nodes that match the nodeName and attribute value.
	 */
	public List<Dnode> findAll(String nodeName, String attrName, Object value, int maxLevel) {
	    
		if (nodeName == null && attrName == null) {
			throw new RuntimeException("You can not have both nodeName and attrName null");
		}
		ArrayList<Dnode> list = new ArrayList<Dnode>();
		findByNode(list, nodeName, false, attrName, value, maxLevel);
		return list;
	}

	/**
	 * Used for recursive calling.
	 */
	private void findByNode(List<Dnode> list, String node, boolean findOne,String attrName, Object value, int maxLevel) {
		
	    if (findOne && list.size() == 1) {
			return;
		}
		if (node == null || node.equals(nodeName)) {
			if (attrName == null || value.equals(getAttribute(attrName))) {
				list.add(this);
				if (findOne) {
					return;
				}
			}
		}
		if (maxLevel > 0 && level >= maxLevel) {
			// hit max level

		} else if (children != null) {
			// recursively search the children
			for (int i = 0; i < children.size(); i++) {
				Dnode child = children.get(i);
				child.findByNode(list, node, findOne, attrName, value,maxLevel);
			}
		}
	}

	/**
	 * The attribute names as strings.
	 */
	public Collection<String> attributeNames() {
		return attrList.keySet();
	}

	/**
	 * Return the attribute for a given name.
	 */
	public String getAttribute(String name) {
		return attrList.get(name);
	}

	/**
	 * Returns an Attribute as a String.
	 * <p>
	 * Will throw a ClassCastException if the attribute is not a String.
	 * </p>
	 */
	public String getStringAttr(String name, String defaultValue) {
		Object o  = attrList.get(name);
		if (o == null){
			return defaultValue;
		} else {
			return o.toString();
		}
	}
	
	/**
	 * Set an attribute.
	 */
	public void setAttribute(String name, String value) {
		attrList.put(name, value);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(getNodeName()).append(" ").append(attrList).append("]");
		return sb.toString();
	}

}
