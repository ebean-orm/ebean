package com.avaje.ebeaninternal.server.deploy.id;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.server.type.DataBind;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.persistence.PersistenceException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Bind an Id that is an Embedded bean.
 */
public final class IdBinderEmbedded implements IdBinder {

    private final BeanPropertyAssocOne<?> embIdProperty;

    private final boolean idInExpandedForm;
    
    private BeanProperty[] props;

    private BeanDescriptor<?> idDesc;

    private String idInValueSql;
    
    
    public IdBinderEmbedded(boolean idInExpandedForm, BeanPropertyAssocOne<?> embIdProperty) {
        this.idInExpandedForm = idInExpandedForm;
        this.embIdProperty = embIdProperty;
    }

    public void initialise() {
        this.idDesc = embIdProperty.getTargetDescriptor();
        this.props = embIdProperty.getProperties();
        this.idInValueSql = idInExpandedForm ? idInExpanded() : idInCompressed();
    }

    private String idInExpanded() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            sb.append(embIdProperty.getName());
            sb.append(".");
            sb.append(props[i].getName());
            sb.append("=?");
        }
        sb.append(")");

        return sb.toString();        
    }
    
    private String idInCompressed() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");

        return sb.toString();        
    }
    
    public String getOrderBy(String pathPrefix, boolean ascending){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (pathPrefix != null){
                sb.append(pathPrefix).append(".");
            }
            
            sb.append(embIdProperty.getName()).append(".");
            sb.append(props[i].getName());
            if (!ascending){
                sb.append(" desc");
            }
        }
        return sb.toString();
    }

    
    public void createLdapNameById(LdapName name, Object id) throws InvalidNameException {

        for (int i = 0; i < props.length; i++) {
            Object v = props[i].getValue(id);
            Rdn rdn = new Rdn(props[i].getDbColumn(), v);
            name.add(rdn);
        }
    }
    
    

    public void createLdapNameByBean(LdapName name, Object bean) throws InvalidNameException {
        Object id = embIdProperty.getValue(bean);
        createLdapNameById(name, id);
    }

    public BeanDescriptor<?> getIdBeanDescriptor() {
        return idDesc;
    }

    public int getPropertyCount() {
        return props.length;
    }

    public String getIdProperty() {
        return embIdProperty.getName();
    }
    
    public void buildSelectExpressionChain(String prefix, List<String> selectChain) {
        
        prefix = SplitName.add(prefix, embIdProperty.getName());
        
        for (int i = 0; i < props.length; i++) {
            props[i].buildSelectExpressionChain(prefix, selectChain);
        }        
    }

    public BeanProperty findBeanProperty(String dbColumnName) {
        for (int i = 0; i < props.length; i++) {
            if (dbColumnName.equalsIgnoreCase(props[i].getDbColumn())) {
                return props[i];
            }
        }
        return null;
    }

    public boolean isComplexId() {
        return true;
    }

    public String getDefaultOrderBy() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(",");
            }

            sb.append(embIdProperty.getName());
            sb.append(".");
            sb.append(props[i].getName());
        }

        return sb.toString();
    }

    public BeanProperty[] getProperties() {
        return props;
    }

    public void addIdInBindValue(SpiExpressionRequest request, Object value) {
        for (int i = 0; i < props.length; i++) {
            request.addBindValue(props[i].getValue(value));
        }
    }
    
    public String getIdInValueExprDelete(int size) {
        if (!idInExpandedForm){
            return getIdInValueExpr(size);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        
        for (int j = 0; j < size; j++) {
            if (j > 0){
                sb.append(" or ");
            }
            sb.append("(");
            for (int i = 0; i < props.length; i++) {
                if (i > 0) {
                    sb.append(" and ");
                }
                sb.append(props[i].getDbColumn());
                sb.append("=?");
            }
            sb.append(")");
        }
        sb.append(") ");
        return sb.toString();
    }

    public String getIdInValueExpr(int size) {
        
        StringBuilder sb = new StringBuilder();
        
        if (!idInExpandedForm){
            sb.append(" in");
        }
        sb.append(" (");
        for (int i = 0; i < size; i++) {
            if (i > 0){
                if (idInExpandedForm) {
                    sb.append(" or ");
                } else {
                    sb.append(",");                    
                }
            }
            sb.append(idInValueSql);
        }
        sb.append(") ");
        return sb.toString();
    }
    
    public String getIdInValueExpr() {
        return idInValueSql;
    }

    /**
     * Convert the lucene string term value into embedded id.
     */
    public Object readTerm(String idTermValue) {
        
        String[] split = idTermValue.split("|");
        if (split.length != props.length){
            String msg = "Failed to split ["+idTermValue+"] using | for id.";
            throw new PersistenceException(msg);
        }
        Object embId = idDesc.createVanillaBean();
        for (int i = 0; i < props.length; i++) {
            Object v = props[i].getScalarType().parse(split[i]);
            props[i].setValue(embId, v);
        }
        return embId;
    }

    /**
     * Write the embedded id as a Lucene string term value.
     */
    public String writeTerm(Object idValue) {

        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < props.length; i++) {
            Object v = props[i].getValue(idValue);
            String formatValue = props[i].getScalarType().format(v);
            if (i > 0){
                sb.append("|");
            }
            sb.append(formatValue);
        }    
        return sb.toString();
    }

    public Object[] getIdValues(Object bean) {
        bean = embIdProperty.getValue(bean);
        Object[] bindvalues = new Object[props.length];
        for (int i = 0; i < props.length; i++) {
            bindvalues[i] = props[i].getValue(bean);
        }
        return bindvalues;
    }

    public Object[] getBindValues(Object value) {

        Object[] bindvalues = new Object[props.length];
        for (int i = 0; i < props.length; i++) {
            bindvalues[i] = props[i].getValue(value);
        }
        return bindvalues;
    }

    public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {
        for (int i = 0; i < props.length; i++) {
            Object embFieldValue = props[i].getValue(value);
            sqlUpdate.addParameter(embFieldValue);
        }
    }

    public void bindId(DataBind dataBind, Object value) throws SQLException {

        for (int i = 0; i < props.length; i++) {
            Object embFieldValue = props[i].getValue(value);
            props[i].bind(dataBind, embFieldValue);
        }
    }
    
    public Object readData(DataInput dataInput) throws IOException {
        
        Object embId = idDesc.createVanillaBean();
        boolean notNull = true;

        for (int i = 0; i < props.length; i++) {
            Object value = props[i].readData(dataInput);
            props[i].setValue(embId, value);
            if (value == null) {
                notNull = false;
            }
        }

        if (notNull) {
            return embId;
        } else {
            return null;
        }
    }

    public void writeData(DataOutput dataOutput, Object idValue) throws IOException {
        for (int i = 0; i < props.length; i++) {
            Object embFieldValue = props[i].getValue(idValue);
            props[i].writeData(dataOutput, embFieldValue);
        }
    }

    public void loadIgnore(DbReadContext ctx) {
        for (int i = 0; i < props.length; i++) {
            props[i].loadIgnore(ctx);
        }
    }

    public Object read(DbReadContext ctx) throws SQLException {

        Object embId = idDesc.createVanillaBean();
        boolean notNull = true;

        for (int i = 0; i < props.length; i++) {
            Object value = props[i].readSet(ctx, embId, null);
            if (value == null) {
                notNull = false;
            }
        }

        if (notNull) {
            return embId;
        } else {
            return null;
        }
    }

    public Object readSet(DbReadContext ctx, Object bean) throws SQLException {

        Object embId = read(ctx);
        if (embId != null) {
            embIdProperty.setValue(bean, embId);
            return embId;
        } else {
            return null;
        }
    }

    public void appendSelect(DbSqlContext ctx, boolean subQuery) {
        for (int i = 0; i < props.length; i++) {
            props[i].appendSelect(ctx, subQuery);
        }
    }
        
    public String getAssocIdInExpr(String prefix) {

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            if (prefix != null) {
                sb.append(prefix);
                sb.append(".");
            }
            sb.append(props[i].getName());
        }
        sb.append(")");
        return sb.toString();
    }

    public String getAssocOneIdExpr(String prefix, String operator) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            if (prefix != null) {
                sb.append(prefix);
                sb.append(".");
            }

            sb.append(embIdProperty.getName());
            sb.append(".");            
            sb.append(props[i].getName());
            sb.append(operator);
        }
        return sb.toString();
    }

    public String getBindIdSql(String baseTableAlias) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            if (baseTableAlias != null) {
                sb.append(baseTableAlias);
                sb.append(".");
            }
            sb.append(props[i].getDbColumn());
            sb.append(" = ? ");
        }
        return sb.toString();
    }

    public String getBindIdInSql(String baseTableAlias) {

        if (idInExpandedForm){
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < props.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            if (baseTableAlias != null){
                sb.append(baseTableAlias);
                sb.append(".");
            }
            sb.append(props[i].getDbColumn());
        }
        sb.append(")");
        return sb.toString();
    }

    public Object convertSetId(Object idValue, Object bean) {

        // can not cast/convert if it is embedded
        if (bean != null) {
            // support PropertyChangeSupport
            embIdProperty.setValueIntercept(bean, idValue);
        }

        return idValue;
    }
}
