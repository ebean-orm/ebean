package com.avaje.ebeaninternal.server.el;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.avaje.ebean.Filter;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Default implementation of the Filter interface.
 */
public final class ElFilter<T> implements Filter<T>  {

	private final BeanDescriptor<T> beanDescriptor;
	
	private ArrayList<ElMatcher<T>> matches = new ArrayList<ElMatcher<T>>();

	private int maxRows;
	
	private String sortByClause;
	
	public ElFilter(BeanDescriptor<T> beanDescriptor) {
		this.beanDescriptor = beanDescriptor;
	}

	private Object convertValue(String propertyName, Object value) {
		// convert type of value to match expected type
		ElPropertyValue elGetValue = beanDescriptor.getElGetValue(propertyName);
		return elGetValue.elConvertType(value);
	}
	
	private ElComparator<T> getElComparator(String propertyName) {
		
		return beanDescriptor.getElComparator(propertyName);
	}
	
	private ElPropertyValue getElGetValue(String propertyName) {
		
		return beanDescriptor.getElGetValue(propertyName);
	}
	
	public Filter<T> sort(String sortByClause) {
		this.sortByClause = sortByClause;
		return this;
	}
	
	protected boolean isMatch(T bean) {
		for (int i = 0; i < matches.size(); i++) {
			ElMatcher<T> matcher = matches.get(i);
			if (!matcher.isMatch(bean)){
				return false;
			}
		}
		return true;
	}
	

	public Filter<T> in(String propertyName, Set<?> matchingValues) {
		
		ElPropertyValue elGetValue = getElGetValue(propertyName);
		
		matches.add(new ElMatchBuilder.InSet<T>(matchingValues, elGetValue));
		return this;
	}

	public Filter<T> eq(String propertyName, Object value) {
		
		value = convertValue(propertyName, value);		
		ElComparator<T> comparator = getElComparator(propertyName);

		matches.add(new ElMatchBuilder.Eq<T>(value, comparator));
		return this;
	}
	

	public Filter<T> ne(String propertyName, Object value) {

		value = convertValue(propertyName, value);		
		ElComparator<T> comparator = getElComparator(propertyName);
		
		matches.add(new ElMatchBuilder.Ne<T>(value, comparator));
		return this;
	}
	
	public Filter<T> between(String propertyName, Object min, Object max) {

		ElPropertyValue elGetValue = getElGetValue(propertyName);
		min = elGetValue.elConvertType(min);
		max = elGetValue.elConvertType(max);
		
		ElComparator<T> elComparator = getElComparator(propertyName);
		
		matches.add(new ElMatchBuilder.Between<T>(min, max, elComparator));
		return this;
	}


	public Filter<T> gt(String propertyName, Object value) {
		
		value = convertValue(propertyName, value);		
		ElComparator<T> comparator = getElComparator(propertyName);
		
		matches.add(new ElMatchBuilder.Gt<T>(value, comparator));
		return this;
	}
	
	public Filter<T> ge(String propertyName, Object value) {
	
		value = convertValue(propertyName, value);		
		ElComparator<T> comparator = getElComparator(propertyName);
		
		matches.add(new ElMatchBuilder.Ge<T>(value, comparator));
		return this;
	}

	public Filter<T> ieq(String propertyName, String value) {

		ElPropertyValue elGetValue = getElGetValue(propertyName);
		
		matches.add(new ElMatchBuilder.Ieq<T>(elGetValue, value));
		return this;
	}


	public Filter<T> isNotNull(String propertyName) {

		ElPropertyValue elGetValue = getElGetValue(propertyName);
		
		matches.add(new ElMatchBuilder.IsNotNull<T>(elGetValue));
		return this;
	}


	public Filter<T> isNull(String propertyName) {
		
		ElPropertyValue elGetValue = getElGetValue(propertyName);
		
		matches.add(new ElMatchBuilder.IsNull<T>(elGetValue));
		return this;
	}

	
	public Filter<T> le(String propertyName, Object value) {

		value = convertValue(propertyName, value);		
		ElComparator<T> comparator = getElComparator(propertyName);
		
		matches.add(new ElMatchBuilder.Le<T>(value, comparator));
		return this;
	}

	
	public Filter<T> lt(String propertyName, Object value) {

		value = convertValue(propertyName, value);		
		ElComparator<T> comparator = getElComparator(propertyName);
		
		matches.add(new ElMatchBuilder.Lt<T>(value, comparator));
		return this;
	}
	
	
	public Filter<T> regex(String propertyName, String regEx) {
		return regex(propertyName, regEx, 0);
	}
	
	public Filter<T> regex(String propertyName, String regEx, int options) {
		
		ElPropertyValue elGetValue = getElGetValue(propertyName);
		
		matches.add(new ElMatchBuilder.RegularExpr<T>(elGetValue, regEx, options));
		return this;
	}

	public Filter<T> contains(String propertyName, String value) {
		
		String quote = ".*"+Pattern.quote(value)+".*";
		
		ElPropertyValue elGetValue = getElGetValue(propertyName);
		matches.add(new ElMatchBuilder.RegularExpr<T>(elGetValue, quote, 0));
		return this;
	}

	public Filter<T> icontains(String propertyName, String value) {
		
		String quote = ".*"+Pattern.quote(value)+".*";
		
		ElPropertyValue elGetValue = getElGetValue(propertyName);
		matches.add(new ElMatchBuilder.RegularExpr<T>(elGetValue, quote, Pattern.CASE_INSENSITIVE));
		return this;
	}

	
	public Filter<T> endsWith(String propertyName, String value) {

		ElPropertyValue elGetValue = getElGetValue(propertyName);
		matches.add(new ElMatchBuilder.EndsWith<T>(elGetValue, value));
		return this;
	}

	public Filter<T> startsWith(String propertyName, String value) {
		
		ElPropertyValue elGetValue = getElGetValue(propertyName);		
		matches.add(new ElMatchBuilder.StartsWith<T>(elGetValue, value));
		return this;
	}
	
	public Filter<T> iendsWith(String propertyName, String value) {

		ElPropertyValue elGetValue = getElGetValue(propertyName);		
		matches.add(new ElMatchBuilder.IEndsWith<T>(elGetValue, value));
		return this;
	}

	public Filter<T> istartsWith(String propertyName, String value) {

		ElPropertyValue elGetValue = getElGetValue(propertyName);		
		matches.add(new ElMatchBuilder.IStartsWith<T>(elGetValue, value));
		return this;
	}

	public Filter<T> maxRows(int maxRows) {
		this.maxRows = maxRows;
		return this;
	}

	public List<T> filter(List<T> list) {

		if (sortByClause != null){
			// create shallow copy and sort 	
			list = new ArrayList<T>(list);
			beanDescriptor.sort(list, sortByClause);
		}
		
		ArrayList<T> filterList = new ArrayList<T>();

		for (int i = 0; i < list.size(); i++) {
			T t = list.get(i);
			if (isMatch(t)) {
				filterList.add(t);
				if (maxRows > 0 && filterList.size() >= maxRows){
					break;
				}
			}
		}

		return filterList;
	}
	
}
