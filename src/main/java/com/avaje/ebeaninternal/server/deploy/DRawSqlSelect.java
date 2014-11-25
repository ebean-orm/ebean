package com.avaje.ebeaninternal.server.deploy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.query.CQueryPredicates;
import com.avaje.ebeaninternal.server.query.SqlTree;
import com.avaje.ebeaninternal.server.query.SqlTreeNode;
import com.avaje.ebeaninternal.server.query.SqlTreeNodeRoot;
import com.avaje.ebeaninternal.server.query.SqlTreeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a SqlSelect raw sql query.
 */
public class DRawSqlSelect {

	private static final Logger logger = LoggerFactory.getLogger(DRawSqlSelect.class);

	private final BeanDescriptor<?> desc;
	
	private final DRawSqlColumnInfo[] selectColumns;

	private final Map<String,DRawSqlColumnInfo> columnMap;
	
	private final String preWhereExprSql;

	private final boolean andWhereExpr;

	private final String preHavingExprSql;

	private final boolean andHavingExpr;

	private final String orderBySql;

	private final String whereClause;

	private final String havingClause;

	private final String query;

	private final String columnMapping;

	private final String name;
	
	private final SqlTree sqlTree;
	
	private boolean withId;

	private final String tableAlias;
	
	public DRawSqlSelect(BeanDescriptor<?> desc, List<DRawSqlColumnInfo> selectColumns,
			String tableAlias, String preWhereExprSql, boolean andWhereExpr, String preHavingExprSql,
			boolean andHavingExpr, String orderBySql, DRawSqlMeta meta) {

		this.desc = desc;
		this.tableAlias = tableAlias;
		this.selectColumns = selectColumns.toArray(new DRawSqlColumnInfo[selectColumns.size()]);
		this.preHavingExprSql = preHavingExprSql;
		this.preWhereExprSql = preWhereExprSql;
		this.andHavingExpr = andHavingExpr;
		this.andWhereExpr = andWhereExpr;
		this.orderBySql = orderBySql;
		this.name = meta.getName();
		this.whereClause = meta.getWhere();
		this.havingClause = meta.getHaving();
		this.query = meta.getQuery();
		this.columnMapping = meta.getColumnMapping();
		
		this.sqlTree = initialise(desc);
		this.columnMap = createColumnMap(this.selectColumns);
	}

	private Map<String,DRawSqlColumnInfo> createColumnMap(DRawSqlColumnInfo[] selectColumns) {
	    
	    HashMap<String,DRawSqlColumnInfo> m = new HashMap<String,DRawSqlColumnInfo>();
	    for (int i = 0; i < selectColumns.length; i++) {
            m.put(selectColumns[i].getPropertyName(), selectColumns[i]);
        }
	    
	    return m;
	}
	
	/**
	 * Find foreign keys for assoc one types and build SqlTree.
	 */
	private SqlTree initialise(BeanDescriptor<?> owner){
		
		try {
			return buildSqlTree(owner);
			
		} catch (Exception e){
			String m = "Bug? initialising query "+name+" on "+owner;
			throw new RuntimeException(m, e);
		}
	}

	/**
	 * Return the RawSqlColumnInfo given it's logical property name.
	 */
	public DRawSqlColumnInfo getRawSqlColumnInfo(String propertyName){
	    return columnMap.get(propertyName);
	}
	
	public String getTableAlias() {
		return tableAlias;
	}

	/**
	 * Build the SqlTree for this query.
	 * <p>
	 * Most commonly this is just a simple list of properties - aka flat, but it
	 * could be a real object graph tree for more complex scenarios.
	 * </p>
	 */
	private SqlTree buildSqlTree(BeanDescriptor<?> desc){

		LinkedHashSet<String> includedProps = new LinkedHashSet<String>();
		SqlTreeProperties selectProps = new SqlTreeProperties();

		for (int i = 0; i < selectColumns.length; i++) {

			DRawSqlColumnInfo columnInfo = selectColumns[i];
			String propName = columnInfo.getPropertyName();
			BeanProperty beanProperty = desc.getBeanProperty(propName);
			if (beanProperty != null) {
				if (beanProperty.isId()){
					if (i > 0){
						String m = "With "+desc+" query:"+name+" the ID is not the first column in the select. It must be...";
						throw new PersistenceException(m);
					} else {
						withId = true;
					}
				} else {
					includedProps.add(beanProperty.getName());
					selectProps.add(beanProperty);
				}
				
				
			} else {
				String m = "Mapping for " + desc.getFullName();
				m += " query["+name+"] column[" + columnInfo + "] index[" + i;
				m += "] not matched to bean property?";
				logger.error(m);
			}
		}

		SqlTreeNode sqlRoot = new SqlTreeNodeRoot(desc, selectProps, null, withId);

    return new SqlTree(desc.getName(), sqlRoot);
	}

	/**
	 * Build the full SQL Select statement for the request.
	 */
	public String buildSql(String orderBy, CQueryPredicates predicates, OrmQueryRequest<?> request) {


		StringBuilder sb = new StringBuilder();
		sb.append(preWhereExprSql);
		sb.append(" ");

		String dynamicWhere = null;
		if (request.getQuery().getId() != null) {
			// need to convert this as well. This avoids the
			// assumption that id has its proper dbColumn assigned
			// which may change if using multiple raw sql statements
			// against the same bean.
			BeanDescriptor<?> descriptor = request.getBeanDescriptor();
			//FIXME: I think this is broken... needs to be logical 
			// and then parsed for RawSqlSelect...
			dynamicWhere = descriptor.getIdBinderIdSql();
		}

		String dbWhere = predicates.getDbWhere();
		if (dbWhere != null && dbWhere.length() > 0) {
			if (dynamicWhere == null) {
				dynamicWhere = dbWhere;
			} else {
				dynamicWhere += " and " + dbWhere;
			}
		}

		if (dynamicWhere != null) {
			if (andWhereExpr) {
				sb.append(" and ");
			} else {
				sb.append(" where ");
			}
			sb.append(dynamicWhere);
			sb.append(" ");
		}

		if (preHavingExprSql != null) {
			sb.append(preHavingExprSql);
			sb.append(" ");
		}

		String dbHaving = predicates.getDbHaving();
		
		if (dbHaving != null && dbHaving.length() > 0) {
			if (andHavingExpr) {
				sb.append(" and ");
			} else {
				sb.append(" having ");
			}
			sb.append(dbHaving);
			sb.append(" ");
		}

		if (orderBy != null) {
			sb.append(" order by ").append(orderBy);
		}

		return sb.toString();
	}

	public String getOrderBy(CQueryPredicates predicates) {
		String orderBy = predicates.getDbOrderBy();
		if (orderBy != null) {
			return orderBy;
		} else {
			return orderBySql;			
		}
	}
	
	
	public String getName() {
		return name;
	}

	public SqlTree getSqlTree() {
		return sqlTree;
	}

	public boolean isWithId() {
		return withId;
	}

	public String getQuery() {
		return query;
	}

	public String getColumnMapping() {
		return columnMapping;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public String getHavingClause() {
		return havingClause;
	}

	public String toString() {
		return Arrays.toString(selectColumns);
	}
	
	public BeanDescriptor<?> getBeanDescriptor() {
        return desc;
    }

    public DeployParser createDeployPropertyParser() {
		return new DeployPropertyParserRawSql(this);
	}
	
}
