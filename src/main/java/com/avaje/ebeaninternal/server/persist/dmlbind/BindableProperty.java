package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a single BeanProperty.
 */
public class BindableProperty implements Bindable {

    protected final BeanProperty prop;

    public BindableProperty(BeanProperty prop) {
        this.prop = prop;
    }

    public String toString() {
        return prop.toString();
    }

    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.hasChanged(prop)) {
            list.add(this);
        }
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
        dmlAppend(request, checkIncludes);
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        request.appendColumn(prop.getDbColumn());
    }

    /**
     * Used for dynamic where clause generation.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
        if (checkIncludes && !request.isIncludedWhere(prop)) {
            return;
        }

        if (bean == null || request.isDbNull(prop.getValue(bean))) {
            request.appendColumnIsNull(prop.getDbColumn());

        } else {
            request.appendColumn(prop.getDbColumn());
        }
    }

    public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {

        if (checkIncludes && !request.isIncluded(prop)) {
            return;
        }
        dmlBind(request, bean, true);
    }
    
    public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !request.isIncludedWhere(prop)) {
            return;
        }
        dmlBind(request, bean, false);
    }
    
    private void dmlBind(BindableRequest request, Object bean, boolean bindNull)
            throws SQLException {
        
        Object value = null;
        if (bean != null) {
            value = prop.getValue(bean);
        }
        // value = prop.getDefaultValue();
        request.bind(value, prop, prop.getName(), bindNull);
    }
}
