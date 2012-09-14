package com.avaje.ebeaninternal.server.deploy;

import scala.collection.JavaConversions;
import scala.collection.convert.DecorateAsScala;

/**
 * Converts between Java Set and Scala mutable Set.
 *
 * @author rbygrave
 */
public class ScalaSetConverter implements CollectionTypeConverter {

//    @SuppressWarnings({ "rawtypes" })
    public Object toUnderlying(Object wrapped) {
        throw new IllegalArgumentException("Scala types not supported in this build");
//        if (wrapped instanceof JavaConversions.JSetWrapper){
//            return ((JavaConversions.JSetWrapper)wrapped).underlying();
//        }
//        return null;
    }
    
    public Object toWrapped(Object wrapped) {
      throw new IllegalArgumentException("Scala types not supported in this build");
//        if (wrapped instanceof java.util.Set<?>){
//            return  JavaConversions.asScalaSet((java.util.Set<?>)wrapped);
//        }
//        return wrapped;
    }
    
}
