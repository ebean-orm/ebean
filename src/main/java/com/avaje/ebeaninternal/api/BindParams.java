package com.avaje.ebeaninternal.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.avaje.ebeaninternal.server.querydefn.NaturalKeyBindParam;

/**
 * Parameters used for binding to a statement.
 * <p>
 * Supports ordered or named parameters.
 * </p>
 */
public class BindParams implements Serializable {

	private static final long serialVersionUID = 4541081933302086285L;

	private List<Param> positionedParameters = new ArrayList<Param>();

	private Map<String, Param> namedParameters = new LinkedHashMap<String, Param>();
	
	/**
	 * This is the sql. For named parameters this is the sql after the named
	 * parameters have been replaced with question mark place holders and the
	 * parameters have been ordered by addNamedParamInOrder().
	 */
	private String preparedSql;

  /**
   * Bind hash and count used to detect when the bind values have changed such
   * that the generated SQL (with named parameters) needs to be recalculated.
   */
  private int[] bindHash;

	public BindParams() {  
	}
	
  public int queryBindHash() {
     int hc = namedParameters.hashCode();
     for (int i = 0; i < positionedParameters.size(); i++) {
       hc = hc * 31 + positionedParameters.get(i).hashCode();
     }
     return hc;
   }

  /**
   * Return the hash that should be included with the query plan.
   * <p>
   * This is to handle binding collections to in clauses. The number of values
   * in the collection effects the query (number of bind values) and so must be
   * taken into account when calculating the query hash.
   * </p>
   */
  public void buildQueryPlanHash(HashQueryPlanBuilder builder) {
    int[] vals = calcQueryPlanHash();
    builder.add(vals[0]).bind(vals[1]);
  }

  /**
   * Calculate and return a query plan bind hash with total bind count.
   */
  public int[] calcQueryPlanHash() {
    int tempBindCount;
    int bc = 0;
    int hc = 31;
    for (Param param : positionedParameters) {
      tempBindCount = param.queryBindCount();
      bc += tempBindCount;
      hc = hc * 31 + tempBindCount;
    }

    for (Map.Entry<String, Param> entry : namedParameters.entrySet()) {
      tempBindCount = entry.getValue().queryBindCount();
      bc += tempBindCount;
      hc = hc * 31 + entry.getKey().hashCode();
      hc = hc * 31 + tempBindCount;
    }

    return new int[]{hc, bc};
  }

	/**
	 * Return a deep copy of the BindParams.
	 */
	public BindParams copy() {
		BindParams copy = new BindParams();
		for (Param p : positionedParameters) {
			copy.positionedParameters.add(p.copy());
		}
		for (Entry<String, Param> entry : namedParameters.entrySet()) {
      copy.namedParameters.put(entry.getKey(), entry.getValue().copy());
		}
		return copy;
	}
	
	/**
	 * Return true if there are no bind parameters.
	 */
	public boolean isEmpty() {
		return positionedParameters.isEmpty() && namedParameters.isEmpty();
	}
	
	/**
	 * Return a Natural Key bind param if supported.
	 */
	public NaturalKeyBindParam getNaturalKeyBindParam() {
		if (positionedParameters != null){
			return null;
		}
		if (namedParameters != null && namedParameters.size() == 1){
			Entry<String, Param> e = namedParameters.entrySet().iterator().next();
			return new NaturalKeyBindParam(e.getKey(), e.getValue().getInValue());
		}
		return null;
	}

	public int size() {
		return positionedParameters.size();
	}

	/**
	 * Return true if named parameters are being used and they have not yet been
	 * ordered. The sql needs to be prepared (named replaced with ?) and the
	 * parameters ordered.
	 */
	public boolean requiresNamedParamsPrepare() {
		return !namedParameters.isEmpty();
	}

	/**
	 * Set a null parameter using position.
	 */
	public void setNullParameter(int position, int jdbcType) {
		Param p = getParam(position);
		p.setInNullType(jdbcType);
	}

	/**
	 * Set an In Out parameter using position.
	 */
	public void setParameter(int position, Object value, int outType) {

	  Param p = getParam(position);
		p.setInValue(value);
		p.setOutType(outType);
	}

	/**
	 * Using position set the In value of a parameter. Note that for nulls you
	 * must use setNullParameter.
	 */
	public void setParameter(int position, Object value) {

	  Param p = getParam(position);
		p.setInValue(value);
	}

	/**
	 * Register the parameter as an Out parameter using position.
	 */
	public void registerOut(int position, int outType) {
		Param p = getParam(position);
		p.setOutType(outType);
	}

	private Param getParam(String name) {
		Param p = namedParameters.get(name);
		if (p == null) {
			p = new Param();
			namedParameters.put(name, p);
		}
		return p;
	}

	private Param getParam(int position) {
		int more = position - positionedParameters.size();
		if (more > 0) {
			for (int i = 0; i < more; i++) {
				positionedParameters.add(new Param());
			}
		}
		return positionedParameters.get(position - 1);
	}

	/**
	 * Set a named In Out parameter.
	 */
	public void setParameter(String name, Object value, int outType) {

    Param p = getParam(name);
		p.setInValue(value);
		p.setOutType(outType);
	}

	/**
	 * Set a named In parameter that is null.
	 */
	public void setNullParameter(String name, int jdbcType) {
		Param p = getParam(name);
		p.setInNullType(jdbcType);
	}

	/**
	 * Set a named In parameter that is not null.
	 */
	public Param setParameter(String name, Object value) {
	    
	  Param p = getParam(name);
		p.setInValue(value);
		return p;
	}

  /**
   * Set an encryption key as a bind value.
   * <p>
   * Needs special treatment as the value should not be included in a log.
   * </p>
   */
  public Param setEncryptionKey(String name, Object value) {
    Param p = getParam(name);
    p.setEncryptionKey(value);
    return p;
  }

	/**
	 * Register the named parameter as an Out parameter.
	 */
	public void registerOut(String name, int outType) {
		Param p = getParam(name);
		p.setOutType(outType);
	}

	/**
	 * Return the Parameter for a given position.
	 */
	public Param getParameter(int position) {
		// Used to read Out value by CallableSql
		return getParam(position);
	}

	/**
	 * Return the named parameter.
	 */
	public Param getParameter(String name) {
		return getParam(name);
	}

	/**
	 * Return the values of ordered parameters.
	 */
	public List<Param> positionedParameters() {
		return positionedParameters;
	}

	/**
	 * Set the sql with named parameters replaced with place holder ?.
	 */
	public void setPreparedSql(String preparedSql) {
		this.preparedSql = preparedSql;
	}

	/**
	 * Return the sql with ? place holders (named parameters have been processed
	 * and ordered).
	 */
	public String getPreparedSql() {
		return preparedSql;
	}

  /**
   * Return true if the bind hash and count has not changed.
   */
  public boolean isSameBindHash() {

    if (bindHash == null) {
      bindHash = calcQueryPlanHash();
      return false;
    }
    int[] oldPlan = bindHash;
    bindHash = calcQueryPlanHash();
    return bindHash[0] == oldPlan[0] && bindHash[1] == oldPlan[1];
  }

  /**
   * Create a new positioned parameters orderedList.
   */
  public OrderedList createOrderedList() {
    positionedParameters.clear();
    return new OrderedList(positionedParameters);
  }

  /**
	 * The bind parameters in the correct binding order.
	 * <p>
	 * This is the result of converting sql with named parameters
	 * into sql with ? and ordered parameters.
	 * </p>
	 */
	public static final class OrderedList {
		
		private final List<Param> paramList;
		
		private final StringBuilder preparedSql;

		public OrderedList() {
			this(new ArrayList<Param>());
		}
		
		public OrderedList(List<Param> paramList) {
			this.paramList = paramList;
			this.preparedSql = new StringBuilder();
		}
		
		/**
		 * Add a parameter in the correct binding order.
		 */
		public void add(Param param) {
			paramList.add(param);
		}
		
		/**
		 * Return the number of bind parameters in this list.
		 */
		public int size() {
			return paramList.size();
		}
		
		/**
		 * Returns the ordered list of bind parameters.
		 */
		public List<Param> list() {
			return paramList;
		}
		
		/**
		 * Append parsedSql that has named parameters converted into ?.
		 */
		public void appendSql(String parsedSql) {
			preparedSql.append(parsedSql);
		}
		
		public String getPreparedSql() {
			return preparedSql.toString();
		}
	}
	
	/**
	 * A In Out capable parameter for the CallableStatement.
	 */
	public static final class Param implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private boolean encryptionKey;
    
		private boolean isInParam;

		private boolean isOutParam;

		private int type;

		private Object inValue;

		private Object outValue;

		/**
		 * Construct a Parameter.
		 */
		public Param() {
		}

		public int queryBindCount() {
		  if (inValue == null) {
		    return 0;
		  }
		  if (inValue instanceof Collection<?>){
		    return ((Collection<?>)inValue).size();
		  }
		  return 1;	  
		}
		
		/**
		 * Create a deep copy of the Param.
		 */
		public Param copy() {
			Param copy = new Param();
			copy.isInParam = isInParam;
			copy.isOutParam = isOutParam;
			copy.type = type;
			copy.inValue = inValue;
			copy.outValue = outValue;
			return copy;
		}
		
		public int hashCode() {
			int hc = getClass().hashCode();
			hc = hc * 31 + (isInParam ? 0 : 1);
			hc = hc * 31 + (isOutParam ? 0 : 1);
			hc = hc * 31 + (type);
			hc = hc * 31 + (inValue == null ? 0 : inValue.hashCode());			
			return hc;
		}

		public boolean equals(Object o) {
      return o != null && (o == this || (o instanceof Param) && hashCode() == o.hashCode());
    }
		
		/**
		 * Return true if this is an In parameter that needs to be bound before
		 * execution.
		 */
		public boolean isInParam() {
			return isInParam;
		}

		/**
		 * Return true if this is an out parameter that needs to be registered
		 * before execution.
		 */
		public boolean isOutParam() {
			return isOutParam;
		}

		/**
		 * Return the jdbc type of this parameter. Used for registering Out
		 * parameters and setting NULL In parameters.
		 */
		public int getType() {
			return type;
		}

		/**
		 * Set the Out parameter type.
		 */
		public void setOutType(int type) {
			this.type = type;
			this.isOutParam = true;
		}

		/**
		 * Set the In value.
		 */
		public void setInValue(Object in) {
			this.inValue = in;
			this.isInParam = true;
		}

    /**
     * Set an encryption key (which can not be logged).
     */
    public void setEncryptionKey(Object in) {
      this.inValue = in;
      this.isInParam = true;
      this.encryptionKey = true;
    }
		
		/**
		 * Specify that the In parameter is NULL and the specific type that it
		 * is.
		 */
		public void setInNullType(int type) {
			this.type = type;
			this.inValue = null;
			this.isInParam = true;
		}

		/**
		 * Return the OUT value that was retrieved. This value is set after
		 * CallableStatement was executed.
		 */
		public Object getOutValue() {
			return outValue;
		}

		/**
		 * Return the In value. If this is null, then the type should be used to
		 * specify the type of the null.
		 */
		public Object getInValue() {
			return inValue;
		}

		/**
		 * Set the OUT value returned by a CallableStatement after it has
		 * executed.
		 */
		public void setOutValue(Object out) {
			this.outValue = out;
		}

    /**
     * If true do not include this value in a transaction log.
     */
    public boolean isEncryptionKey() {
      return encryptionKey;
    }

	}
}
