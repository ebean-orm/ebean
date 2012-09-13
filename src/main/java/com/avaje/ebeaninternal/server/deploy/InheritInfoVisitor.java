package com.avaje.ebeaninternal.server.deploy;

/**
 * Used to visit all the InheritInfo in a single inheritance hierarchy.
 */
public interface InheritInfoVisitor {

	/**
 	 * visit the InheritInfo for this node.
	 */
	public void visit(InheritInfo inheritInfo);
	
}
