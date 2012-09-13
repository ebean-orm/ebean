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

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.Encryptor;

/**
 * Simple AES based encryption and decryption.
 * 
 * @author rbygrave
 */
public class SimpleAesEncryptor implements Encryptor {

    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";

    private static final String padding = "asldkalsdkadsdfkjsldfjl";

    public SimpleAesEncryptor() {
    }

    private String paddKey(EncryptKey encryptKey) {

        String key = encryptKey.getStringValue();
        int addChars = 16 - key.length();
        if (addChars < 0) {
            return key.substring(0, 16);
        } else if (addChars > 0) {
            return key + padding.substring(0, addChars);
        }
        return key;
    }

    private byte[] getKeyBytes(String skey) {

        try {
            return skey.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private IvParameterSpec getIvParameterSpec(String initialVector) {
        return new IvParameterSpec(initialVector.getBytes());
    }

    public byte[] decrypt(byte[] data, EncryptKey encryptKey) {

        if (data == null) {
            return null;
        }

        String key = paddKey(encryptKey);

        try {

            byte[] keyBytes = getKeyBytes(key);
            IvParameterSpec iv = getIvParameterSpec(key);

            SecretKeySpec sks = new SecretKeySpec(keyBytes, "AES");
            Cipher c = Cipher.getInstance(AES_CIPHER);

            c.init(Cipher.DECRYPT_MODE, sks, iv);

            return c.doFinal(data);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(byte[] data, EncryptKey encryptKey) {

        if (data == null) {
            return null;
        }

        String key = paddKey(encryptKey);

        try {
            byte[] keyBytes = getKeyBytes(key);
            IvParameterSpec iv = getIvParameterSpec(key);

            SecretKeySpec sks = new SecretKeySpec(keyBytes, "AES");
            Cipher c = Cipher.getInstance(AES_CIPHER);

            c.init(Cipher.ENCRYPT_MODE, sks, iv);

            return c.doFinal(data);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptString(byte[] data, EncryptKey key) {
        if (data == null) {
            return null;
        }
        
        byte[] bytes = decrypt(data, key);
        try {
            return new String(bytes, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encryptString(String valueFormatValue, EncryptKey key) {

        if (valueFormatValue == null) {
            return null;
        }
        try {
            byte[] d = valueFormatValue.getBytes("UTF-8");
            return encrypt(d, key);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
