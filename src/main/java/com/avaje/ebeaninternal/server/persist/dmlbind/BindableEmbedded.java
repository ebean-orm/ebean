package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a Embedded bean.
 */
public class BindableEmbedded implements Bindable {

    private final Bindable[] items;

    private final BeanPropertyAssocOne<?> embProp;

    public BindableEmbedded(BeanPropertyAssocOne<?> embProp, List<Bindable> list) {
        this.embProp = embProp;
        this.items = list.toArray(new Bindable[list.size()]);
    }

    public String toString() {
        return "BindableEmbedded " + embProp + " items:" + Arrays.toString(items);
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
        dmlAppend(request, checkIncludes);
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
        if (checkIncludes && !request.isIncluded(embProp)) {
            return;
        }

        for (int i = 0; i < items.length; i++) {
            items[i].dmlAppend(request, false);
        }
    }

    /**
     * Used for dynamic where clause generation.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object origBean) {
        if (checkIncludes && !request.isIncludedWhere(embProp)) {
            return;
        }
        Object embBean = embProp.getValue(origBean);
        Object oldValues = getOldValue(embBean);

        for (int i = 0; i < items.length; i++) {
            items[i].dmlWhere(request, false, oldValues);
        }
    }

    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.hasChanged(embProp)) {
            list.add(this);
        }
    }

    public void dmlBind(BindableRequest bindRequest, boolean checkIncludes, Object bean)
            throws SQLException {
        
        if (checkIncludes && !bindRequest.isIncluded(embProp)) {
            return;
        }

        // get the embedded bean
        Object embBean = embProp.getValue(bean);
        
        for (int i = 0; i < items.length; i++) {
            items[i].dmlBind(bindRequest, false, embBean);
        }
    }
    
    public void dmlBindWhere(BindableRequest bindRequest, boolean checkIncludes, Object bean)
            throws SQLException {
        
        if (checkIncludes && !bindRequest.isIncludedWhere(embProp)) {
            return;
        }

        // get the embedded bean
        Object embBean = embProp.getValue(bean);
        Object oldEmbBean = getOldValue(embBean);

        for (int i = 0; i < items.length; i++) {
            items[i].dmlBindWhere(bindRequest, false, oldEmbBean);
        }
    }

    /**
     * Get the old bean which will have the original values.
     * <p>
     * These are bound to the WHERE clause for updates.
     * </p>
     */
    private Object getOldValue(Object embBean) {

        Object oldValues = null;

        if (embBean instanceof EntityBean) {
            // get the old embedded bean (with the original values)
            oldValues = ((EntityBean) embBean)._ebean_getIntercept().getOldValues();
        }

        if (oldValues == null) {
            // this embedded bean was not modified
            // (or not an EntityBean)
            oldValues = embBean;
        }

        return oldValues;
    }

}
