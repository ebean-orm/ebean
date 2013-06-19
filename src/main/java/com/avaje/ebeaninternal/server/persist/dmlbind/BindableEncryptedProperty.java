package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a DB encrypted BeanProperty.
 */
public class BindableEncryptedProperty implements Bindable {

    private final BeanProperty prop;
    
    private final boolean bindEncryptDataFirst;

    public BindableEncryptedProperty(BeanProperty prop, boolean bindEncryptDataFirst) {
        this.prop = prop;
        this.bindEncryptDataFirst = bindEncryptDataFirst;
    }

    public String toString() {
        return prop.toString();
    }

    public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.isAddToUpdate(prop)) {
            list.add(this);
        }
    }

    public void dmlAppend(GenerateDmlRequest request) {

        // columnName = AES_ENCRYPT(?,?)
        request.appendColumn(prop.getDbColumn(), prop.getDbBind());
    }


    /**
     * Bind a value in a Insert SET clause.
     */
    public void dmlBind(BindableRequest request, EntityBean bean)
            throws SQLException {
        
        Object value = null;
        if (bean != null) {
            value = prop.getValue(bean);
        }

        // get Encrypt key
        String encryptKeyValue = prop.getEncryptKey().getStringValue(); 

        if (!bindEncryptDataFirst){
            // H2 encrypt function ... different parameter order
            request.bindNoLog(encryptKeyValue, Types.VARCHAR, prop.getName() + "=****");                        
        }
        request.bindNoLog(value, prop, prop.getName());
        
        if (bindEncryptDataFirst){
            // MySql, Postgres, Oracle
            request.bindNoLog(encryptKeyValue, Types.VARCHAR, prop.getName() + "=****");            
        }
    }
    
}
