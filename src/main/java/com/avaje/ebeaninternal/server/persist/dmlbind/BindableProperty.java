package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
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

    public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.isAddToUpdate(prop)) {
            list.add(this);
        }
    }

    public void dmlAppend(GenerateDmlRequest request) {
        request.appendColumn(prop.getDbColumn());
    }

    public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {

        Object value = null;
        if (bean != null) {
            value = prop.getValue(bean);
        }
        // value = prop.getDefaultValue();
        request.bind(value, prop, prop.getName());
    }
}
