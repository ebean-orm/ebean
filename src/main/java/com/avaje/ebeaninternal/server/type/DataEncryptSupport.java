/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.Encryptor;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.EncryptKeyManager;

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
