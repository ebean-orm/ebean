package com.avaje.ebeaninternal.server.deploy;

import scala.collection.JavaConversions;

/**
 * Converts between Java Map and Scala mutable Map.
 *
 * @author rbygrave
 */
public class ScalaMapConverter implements CollectionTypeConverter {

//    @SuppressWarnings({ "rawtypes" })
    public Object toUnderlying(Object wrapped) {
      throw new IllegalArgumentException("Scala types not supported in this build");
//        if (wrapped instanceof JavaConversions.JMapWrapper){
//            return ((JavaConversions.JMapWrapper)wrapped).underlying();
//        }
//        return null;
    }
    
    public Object toWrapped(Object wrapped) {
      throw new IllegalArgumentException("Scala types not supported in this build");
//        if (wrapped instanceof java.util.Map<?,?>){
//            return  JavaConversions.mapAsScalaMap((java.util.Map<?,?>)wrapped);
//        }
//        return wrapped;
    }
    
}
