package com.avaje.tests.model.ivo.converter;

import java.util.Currency;

import com.avaje.ebean.config.CompoundTypeProperty;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.Money;

public class CMoneyCompoundType {//implements CompoundType<CMoney> {

    @SuppressWarnings({ "rawtypes" })
    private CompoundTypeProperty[] props = {new PropertyMoney(), new PropertyCurrency()};
    
    public CMoney create(Object[] v) {
        return new CMoney((Money)v[0], (Currency)v[1]);
    }

    @SuppressWarnings("unchecked")
    public CompoundTypeProperty<CMoney, ?>[] getProperties() {
        return props;
    }
    
    static class PropertyMoney implements CompoundTypeProperty<CMoney,Money> {

        public String getName() {
            return "amount";
        }

        public Money getValue(CMoney valueObject) {
            return valueObject.getAmount();
        }

        public int getDbType() {
            return 0;
        }
    }
    
    static class PropertyCurrency implements CompoundTypeProperty<CMoney,Currency> {

        public String getName() {
            return "currency";
        }

        public Currency getValue(CMoney valueObject) {
            return valueObject.getCurrency();
        }

        public int getDbType() {
            return 0;
        }
    }

}
