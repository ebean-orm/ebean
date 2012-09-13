package com.avaje.ebeaninternal.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A clause used to describe sorting.
 * <p>
 * Primarily used to support sorting of Lists.
 * </p>
 */
public class SortByClause {

	public static final String NULLSHIGH = "nullshigh";
	
	public static final String NULLSLOW = "nullslow";

	/**
	 * The ascending keyword.
	 */
	public static final String ASC = "asc";
	
	/**
	 * The descending keyword.
	 */
	public static final String DESC = "desc";
	
	
	private List<Property> properties = new ArrayList<Property>();
	
	/**
	 * Return the number of properties in the clause.
	 */
	public int size() {
		return properties.size();
	}

	/**
	 * Return the properties of the sort by clause.
	 */
	public List<Property> getProperties() {
		return properties;
	}

	/**
	 * Add a property to the sort by clause.
	 */
	public void add(Property p){
		properties.add(p);
	}

	/**
	 * A property of the SortByClause.
	 */
	public static class Property implements Serializable {
		
		private static final long serialVersionUID = 7588760362420690963L;

		private final String name;
		
		private final boolean ascending;
		
		private final Boolean nullsHigh;

		public Property(String name, boolean ascending, Boolean nullsHigh) {
			this.name = name;
			this.ascending = ascending;
			this.nullsHigh = nullsHigh;
		}
		
		public String toString() {
			return name+" asc:"+ascending;
		}
		
		/**
		 * Return the property name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Return true if the order should be ascending.
		 */
		public boolean isAscending() {
			return ascending;
		}

		public Boolean getNullsHigh() {
			return nullsHigh;
		}
		
	}
}
