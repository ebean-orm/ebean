package com.avaje.tests.model.ivo.converter;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
import com.avaje.tests.model.ivo.Rate;

public class ExhangeCompoundType implements CompoundType<ExhangeCMoneyRate> {

    public ExhangeCMoneyRate create(Object[] propertyValues) {
        return new ExhangeCMoneyRate((Rate)propertyValues[0], (CMoney)propertyValues[1]);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CompoundTypeProperty<ExhangeCMoneyRate, ?>[] getProperties() {

        CompoundTypeProperty[] props = {new RateProp(), new CMoneyProp()};
        return props;
    }

    static class RateProp implements CompoundTypeProperty<ExhangeCMoneyRate, Rate> {

        public String getName() {
            return "rate";
        }

        public Rate getValue(ExhangeCMoneyRate valueObject) {
            return valueObject.getRate();
        } 
        
        public int getDbType() {
            return 0;
        }

    }
    
    static class CMoneyProp implements CompoundTypeProperty<ExhangeCMoneyRate, CMoney> {

        public String getName() {
            return "cmoney";
        }

        public CMoney getValue(ExhangeCMoneyRate valueObject) {
            return valueObject.getCmoney();
        }

        public int getDbType() {
            return 0;
        }
        
    }
    
}
