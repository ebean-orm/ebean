package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.EncryptKeyManager;
import com.avaje.ebean.config.Encryptor;

public class DataEncryptSupport {

    private final EncryptKeyManager encryptKeyManager;
    private final Encryptor encryptor;
    private final String table;
    private final String column;
    
    public DataEncryptSupport(EncryptKeyManager encryptKeyManager, Encryptor encryptor, String table, String column) {
        this.encryptKeyManager = encryptKeyManager;
        this.encryptor = encryptor;
        this.table = table;
        this.column = column;
    }
    
    public byte[] encrypt(byte[] data){
    
        EncryptKey key = encryptKeyManager.getEncryptKey(table, column);
        return encryptor.encrypt(data, key);
    }

    public byte[] decrypt(byte[] data){
        
        EncryptKey key = encryptKeyManager.getEncryptKey(table, column);
        return encryptor.decrypt(data, key);
    }
    
    public String decryptObject(byte[] data) {
        EncryptKey key = encryptKeyManager.getEncryptKey(table, column);
        return encryptor.decryptString(data, key);
    }

    public <T> byte[] encryptObject(String formattedValue) {
        EncryptKey key = encryptKeyManager.getEncryptKey(table, column);
        return encryptor.encryptString(formattedValue, key);
    }

}
