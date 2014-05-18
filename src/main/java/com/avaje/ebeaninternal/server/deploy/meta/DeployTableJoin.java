package com.avaje.ebeaninternal.server.deploy.meta;

import java.util.ArrayList;

import javax.persistence.JoinColumn;

import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.deploy.BeanCascadeInfo;
import com.avaje.ebeaninternal.server.deploy.BeanTable;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

/**
 * Represents a join to another table during deployment phase.
 * <p>
 * This gets converted into a immutable TableJoin when complete.
 * </p>
 */
public class DeployTableJoin {

    /**
     * Flag set when the imported key maps to the primary key.
     * This occurs for intersection tables (ManyToMany).
     */
    private boolean importedPrimaryKey;
    
    /**
     * The joined table.
     */
    private String table;
    
    /**
     * The type of join. LEFT OUTER etc.
     */
    private SqlJoinType type = SqlJoinType.INNER;

    /**
     * The list of properties mapped to this joined table.
     */
    private ArrayList<DeployBeanProperty> properties = new ArrayList<DeployBeanProperty>();

    /**
     * The list of join column pairs. Used to generate the on clause.
     */
    private ArrayList<DeployTableJoinColumn> columns = new ArrayList<DeployTableJoinColumn>();

    /**
     * The persist cascade info.
     */
    private BeanCascadeInfo cascadeInfo = new BeanCascadeInfo();
    
    private InheritInfo inheritInfo;

    /**
     * Create a DeployTableJoin.
     */
    public DeployTableJoin() {
    }
    
    public String toString() {
        return type + " " + table + " " + columns;
    }

    /**
     * Return true if the imported foreign key maps to the primary key.
     */
    public boolean isImportedPrimaryKey() {
		return importedPrimaryKey;
	}

    /**
     * Flag set when the imported key maps to the primary key.
     * This occurs for intersection tables (ManyToMany).
     */
	public void setImportedPrimaryKey(boolean importedPrimaryKey) {
		this.importedPrimaryKey = importedPrimaryKey;
	}

	/**
     * Return true if the JoinOnPair have been set.
     */
    public boolean hasJoinColumns() {
        return columns.size() > 0;
    }

    /**
     * Return the persist info.
     */
    public BeanCascadeInfo getCascadeInfo() {
        return cascadeInfo;
    }
    

    /**
     * Copy all the columns to this join potentially reversing the columns.
     */
	public void setColumns(DeployTableJoinColumn[] cols, boolean reverse) {
		columns = new ArrayList<DeployTableJoinColumn>();
		for (int i = 0; i < cols.length; i++) {
			addJoinColumn(cols[i].copy(reverse));
		}
	}
	
    /**
     * Add a join pair
     */
    public void addJoinColumn(DeployTableJoinColumn pair) {
        columns.add(pair);
    }

    /**
     * Add a JoinColumn
     * <p>
     * The order is generally true for OneToMany and false for ManyToOne relationships.
     * </p>
     */
    public void addJoinColumn(boolean order, JoinColumn jc, BeanTable beanTable) {
		if (!"".equals(jc.table())) {
			setTable(jc.table());
		}
    	addJoinColumn(new DeployTableJoinColumn(order, jc, beanTable));
    }
    
    /**
     * Add a JoinColumn array.
     */
    public void addJoinColumn(boolean order, JoinColumn[] jcArray, BeanTable beanTable) {
    	for (int i = 0; i < jcArray.length; i++) {
    		addJoinColumn(order, jcArray[i], beanTable);
		}
    }
    
    /**
     * Return the join columns.
     */
    public DeployTableJoinColumn[] columns() {
    	return (DeployTableJoinColumn[])columns.toArray(new DeployTableJoinColumn[columns.size()]);
    }

    
    /**
     * For secondary table joins returns the properties mapped to that table.
     */
    public DeployBeanProperty[] properties() {
    	return (DeployBeanProperty[])properties.toArray(new DeployBeanProperty[properties.size()]);
    }

    /**
     * Return the joined table name.
     */
    public String getTable() {
        return table;
    }

    /**
     * set the joined table name.
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Return the type of join. LEFT OUTER JOIN etc.
     */
    public SqlJoinType getType() {
        return type;
    }

    /**
     * Return true if this join is a left outer join.
     */
    public boolean isOuterJoin() {
        return type == SqlJoinType.OUTER;
    }
    
    private void setType(SqlJoinType type) {
      this.type = type;
    }
    
    /**
     * Set the type of join.
     */
    public void setType(String joinType) {
        joinType = joinType.toUpperCase();
        if (joinType.equalsIgnoreCase(TableJoin.JOIN)) {
            type = SqlJoinType.INNER;
        } else if (joinType.indexOf("LEFT") > -1) {
            type = SqlJoinType.OUTER;
        } else if (joinType.indexOf("OUTER") > -1) {
            type = SqlJoinType.OUTER;
        } else if (joinType.indexOf("INNER") > -1) {
            type = SqlJoinType.INNER;
        } else {
            throw new RuntimeException(Message.msg("join.type.unknown", joinType));
        }
    }
    
    public DeployTableJoin createInverse(String tableName) {
    	
    	DeployTableJoin inverse = new DeployTableJoin();

        return copyTo(inverse, true, tableName);

    }
    
    public DeployTableJoin copyTo(DeployTableJoin destJoin, boolean reverse, String tableName) {
    	
    	destJoin.setTable(tableName);
    	destJoin.setType(type);
    	destJoin.setColumns(columns(), reverse);
    	
    	return destJoin;
    }

	public InheritInfo getInheritInfo() {
		return inheritInfo;
	}

	public void setInheritInfo(InheritInfo inheritInfo) {
		this.inheritInfo = inheritInfo;
	}
}
