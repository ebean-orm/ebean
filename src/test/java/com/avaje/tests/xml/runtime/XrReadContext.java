package com.avaje.tests.xml.runtime;

public class XrReadContext {

    private boolean vanillaMode;
    
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

    public boolean isVanillaMode() {
        return vanillaMode;
    }

    public void setVanillaMode(boolean vanillaMode) {
        this.vanillaMode = vanillaMode;
    }

}
