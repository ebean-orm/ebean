package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * Join to Many (or child of a many) to support where clause predicates on many properties.
 */
public class SqlTreeNodeManyWhereJoin implements SqlTreeNode {

    private final String parentPrefix;
    
    private final String prefix;

    private final BeanPropertyAssoc<?> nodeBeanProp;
    
    /**
     * Child joins.
     */
    private final SqlTreeNode[] children;
    
    /**
     * The many where join which is either INNER or OUTER.
     */
    private final SqlJoinType manyJoinType;

    public SqlTreeNodeManyWhereJoin(String prefix, BeanPropertyAssoc<?> prop, SqlJoinType manyJoinType) {
       
        this.nodeBeanProp = prop;
        this.prefix = prefix;
        this.manyJoinType = manyJoinType;

        String[] split = SplitName.split(prefix);
        this.parentPrefix = split[0];
        
        List<SqlTreeNode> childrenList = new ArrayList<SqlTreeNode>(0);
        this.children = childrenList.toArray(new SqlTreeNode[childrenList.size()]);
    }

    /**
     * Append to the FROM clause for this node.
     */
    @Override
    public void appendFrom(DbSqlContext ctx, SqlJoinType currentJoinType) {
        
        // always use the join type as per this many where join 
        // (OUTER for disjunction and otherwise INNER)
        appendFromBaseTable(ctx, manyJoinType);
        
        for (int i = 0; i < children.length; i++) {
            children[i].appendFrom(ctx, manyJoinType);
        }
    }

    /**
     * Join to base table for this node. This includes a join to the
     * intersection table if this is a ManyToMany node.
     */
    public void appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {

        String alias = ctx.getTableAliasManyWhere(prefix);
        String parentAlias = ctx.getTableAliasManyWhere(parentPrefix);

        if (nodeBeanProp instanceof BeanPropertyAssocOne<?>){
            nodeBeanProp.addJoin(joinType, parentAlias, alias, ctx);
            
        } else {
            BeanPropertyAssocMany<?> manyProp = (BeanPropertyAssocMany<?>)nodeBeanProp;
            if (!manyProp.isManyToMany()) {
                manyProp.addJoin(joinType, parentAlias, alias, ctx);
    
            } else {
                String alias2 = alias + "z_";
    
                TableJoin manyToManyJoin = manyProp.getIntersectionTableJoin();
                manyToManyJoin.addJoin(joinType, parentAlias, alias2, ctx);
                manyProp.addJoin(joinType, alias2, alias, ctx);
            }
        }
    }

    public void buildSelectExpressionChain(List<String> selectChain) {
        // nothing to add
    }

    public void appendSelect(DbSqlContext ctx, boolean subQuery) {
        // nothing to do here
    }

    public void appendWhere(DbSqlContext ctx) {
        // nothing to do here
    }

    public void load(DbReadContext ctx, EntityBean parentBean) throws SQLException {
        // nothing to do here
    }

}
