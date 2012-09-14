package com.avaje.ebeaninternal.server.deploy;

import scala.collection.JavaConversions;

/**
 * Converts between Java List and Scala mutable Buffer.
 *
 * @author rbygrave
 */
public class ScalaBufferConverter implements CollectionTypeConverter {

//    @SuppressWarnings({ "rawtypes" })
    public Object toUnderlying(Object wrapped) {
      throw new IllegalArgumentException("Scala types not supported in this build");
//        if (wrapped instanceof JavaConversions.JListWrapper){
//            return ((JavaConversions.JListWrapper)wrapped).underlying();
//        }
//        return null;
    }
    
    public Object toWrapped(Object wrapped) {
      throw new IllegalArgumentException("Scala types not supported in this build");
//        if (wrapped instanceof java.util.List<?>){
//            return  JavaConversions.asScalaBuffer((java.util.List<?>)wrapped);
//        }
//        return wrapped;
    }
    
}
