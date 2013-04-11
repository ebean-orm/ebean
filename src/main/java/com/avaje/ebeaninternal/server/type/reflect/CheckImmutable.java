package com.avaje.ebeaninternal.server.type.reflect;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


public class CheckImmutable {

    private static final Logger logger = LoggerFactory.getLogger(CheckImmutable.class);
    
    private final KnownImmutable knownImmutable; 

    public CheckImmutable(KnownImmutable knownImmutable) {
        this.knownImmutable = knownImmutable;
    }
    
    public CheckImmutableResponse checkImmutable(Class<?> cls) {

        CheckImmutableResponse res = new CheckImmutableResponse();
        
        isImmutable(cls, res);
        
        if (res.isImmutable()){
            res.setCompoundType(isCompoundType(cls));
        }
        
        return res;
    }

    private boolean isCompoundType(Class<?> cls) {
        
        int maxLength = 0;
        Constructor<?> chosen = null;
        
        // find the constructor with the most number of parameters
        Constructor<?>[] constructors = cls.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Class<?>[] parameterTypes = constructors[i].getParameterTypes();
            if (parameterTypes.length > maxLength){
                maxLength = parameterTypes.length;
                chosen = constructors[i];
            }
        }
        
        logger.debug("checkImmutable "+cls+" constructor "+chosen);
        
        return maxLength > 1;
    }
    
    private boolean isImmutable(Class<?> cls, CheckImmutableResponse res) {

        
        if (knownImmutable.isKnownImmutable(cls)) {
            return true;
        }
        
        if (cls.isArray()) {
            return false;
        }
        
        
        if (hasDefaultConstructor(cls)){
            // must not have a default constructor to be considered immutable
            res.setReasonNotImmutable(cls+" has a default constructor");
            return false;
        }

        // check super class
        Class<?> superClass = cls.getSuperclass();

        if (!isImmutable(superClass, res)) {
            res.setReasonNotImmutable("Super not Immutable " + superClass);
            return false;
        }

        if (!hasAllFinalFields(cls, res)){
            return false;
        }

        // Lets hope we didn't forget something
        return true;
    }

    private boolean hasAllFinalFields(Class<?> cls, CheckImmutableResponse res){

        // Check all fields defined in the class for type and if they are final
        Field[] objFields = cls.getDeclaredFields();
        for (int i = 0; i < objFields.length; i++) {
            if (Modifier.isStatic(objFields[i].getModifiers())) {
                // ignore static fields
            } else {
                if (!Modifier.isFinal(objFields[i].getModifiers())) {
                    res.setReasonNotImmutable("Non final field " + cls + "." + objFields[i].getName());
                    return false;
                }
                if (!isImmutable(objFields[i].getType(), res)) {
                    res.setReasonNotImmutable("Non Immutable field type " + objFields[i].getType());
                    return false;
                }
            }
        }

        return true;
    }
    

    private boolean hasDefaultConstructor(Class<?> cls) {

        Class<?>[] noParams = new Class<?>[0];
        try {
            cls.getDeclaredConstructor(noParams);
            return true;

        } catch (SecurityException e) {
            // this is ok
            return false;

        } catch (NoSuchMethodException e) {
            // this is expected for our IVO's
            return false;
        }

    }

}
