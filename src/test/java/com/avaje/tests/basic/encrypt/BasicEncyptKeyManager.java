package com.avaje.tests.basic.encrypt;

import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.EncryptKeyManager;

public class BasicEncyptKeyManager implements EncryptKeyManager {

    /**
     * Initialise the key manager.
     */
    public void initialise() {
        
    }

    public EncryptKey getEncryptKey(String tableName, String columnName) {
        // Must be 16 Chars for Oracle function
        return new BasicEncryptKey("simple0123456789");
    }

}
