package com.avaje.tests.basic.encrypt;

import com.avaje.ebean.config.EncryptKey;

public class BasicEncryptKey implements EncryptKey {

    private final String key;

    public BasicEncryptKey(String key) {
        this.key = key;
    }
    
    public String getStringValue() {
        return key;
    }
    

    
}
