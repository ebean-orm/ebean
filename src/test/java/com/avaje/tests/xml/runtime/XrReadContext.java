package com.avaje.tests.xml.runtime;

public class XrReadContext {

    private Object bean;
    
    public XrReadContext(Object bean) {
        this.bean = bean;
    }
    
    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
}
