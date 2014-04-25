package com.avaje.ebeaninternal.server.persist.dml;

import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Helper to support the generation of DML statements.
 */
public class GenerateDmlRequest {

    private static final String IS_NULL = " is null";

    private final boolean emptyStringAsNull;

    private final StringBuilder sb = new StringBuilder(100);

    private final EntityBeanIntercept ebi;
    private final boolean changesOnly;

    private StringBuilder insertBindBuffer;

    private String prefix;
    private String prefix2;

    private int insertMode;
    
    private int bindColumnCount;
    
    /**
     * Create from a PersistRequestBean.
     */
    public GenerateDmlRequest(boolean emptyStringAsNull, EntityBeanIntercept ebi, boolean changesOnly) {//, Object oldValues) {
        this.emptyStringAsNull = emptyStringAsNull;
        this.ebi = ebi;
        this.changesOnly = changesOnly;
    }

    /**
     * Create for generating standard all properties DML/SQL.
     */
    public GenerateDmlRequest(boolean emptyStringAsNull) {
        this(emptyStringAsNull, null, false);
    }

    public GenerateDmlRequest append(String s) {
        sb.append(s);
        return this;
    }

    public boolean isDbNull(Object v) {
        return v == null || (emptyStringAsNull && (v instanceof String) && ((String) v).length() == 0);
    }

    /**
     * Return true if this property should be included in the set clause.
     */
    public boolean isIncluded(BeanProperty prop) {
      if (ebi == null) {
        return true;
      }
      if (changesOnly) {
        return ebi.isDirtyProperty(prop.getPropertyIndex());
      } else {
        return ebi.isLoadedProperty(prop.getPropertyIndex());
      }
    }
    
    /**
     * Return true if this property should be included in the where clause.
     */
    public boolean isIncludedWhere(BeanProperty prop) {
      return ebi == null || ebi.isLoadedProperty(prop.getPropertyIndex());
    }

    public void appendColumnIsNull(String column) {
        appendColumn(column, IS_NULL);
    }

    public void appendColumn(String column) {
        //String bind = (insertMode > 0) ? "?" : "=?";
        appendColumn(column, "?");
    }

    public void appendColumn(String column, String bind) {
        appendColumn(column, "", bind);
    }

    public void appendColumn(String column, String expr, String bind) {
        
        ++bindColumnCount;
        
        sb.append(prefix);
        sb.append(column);
        //sb.append(expr);
        if (insertMode > 0) {
            if (insertMode++ > 1) {
                insertBindBuffer.append(",");
            }
            insertBindBuffer.append(bind);
        } else {
            sb.append("=");
            sb.append(bind);
        }

        if (prefix2 != null) {
            prefix = prefix2;
            prefix2 = null;
        }
    }

    public int getBindColumnCount() {
        return bindColumnCount;
    }
    
    public String getInsertBindBuffer() {
        return insertBindBuffer.toString();
    }

    public String toString() {
        return sb.toString();
    }

    public void setWhereMode() {
        this.prefix = " and ";
        this.prefix2 = " and ";
    }

    public void setWhereIdMode() {
        this.prefix = "";
        this.prefix2 = " and ";
    }

    public void setInsertSetMode() {
        this.insertBindBuffer = new StringBuilder(100);
        this.insertMode = 1;
        this.prefix = "";
        this.prefix2 = ", ";
    }

    public void setUpdateSetMode() {
        this.prefix = "";
        this.prefix2 = ", ";
    }

}
